# Endpoints, relatórios e autenticação

## Diagnóstico em 07/10/2026

| Endpoint | Resultado observado antes das alterações | Origem dos dados |
| --- | --- | --- |
| `GET /v1/pedidos` sem Bearer | `401` | Rota protegida no gateway e no serviço de pedidos |
| `GET /v1/pedidos` com administrador autenticado | `200`, lista vazia na execução inspecionada | `pedido-service` → `SpringDataPedidoRepository` → H2 `pedidos`, tabelas `tb_pedidos` e `tb_item_pedido` |
| `GET /v1/relatorios/vendas?dataInicio=2026-10-01&dataFim=2026-10-31` com administrador | `200`, totais zerados na execução inspecionada | H2 `relatorio`, tabela `tb_relatorio_consolidado`, alimentada por eventos RabbitMQ |
| `GET /v1/relatorios/producao` com administrador | `200`, números `10/5/8/3/42` | Constantes em `RelatorioPersistenceAdapter`, sem consulta ao banco |

A rota de pedidos existe, está registrada no gateway e funcionou na chamada autenticada. Uma navegação direta pela barra de endereço não envia o Bearer salvo pelo aplicativo. O frontend também podia reutilizar uma sessão expirada sem perceber; isso foi corrigido. A listagem já filtrava clientes pelo e-mail do JWT e permitia ao administrador listar todos. Os testes adicionados confirmam essa regra com registros e itens persistidos. Pedidos de outro cliente retornam `404` na consulta individual.

Os logs anteriores também registravam falhas de criação por produto inválido. Argumentos inválidos agora retornam `400`; produto inexistente no catálogo retorna `400`; indisponibilidade do catálogo ou RabbitMQ retorna `503`. O processamento de erros mantém o status original e não o transforma em um `401`, evitando logout por uma falha de negócio.

## Vendas

Fluxo: pedido salvo → `PedidoProducer` → exchange `pedido.eventos` / routing key `pedido.criado` → `RelatorioConsumer` → `tb_relatorio_consolidado` → duas consultas `SUM`, com `BETWEEN` inclusivo → total de pedidos, valor total e ticket médio.

O relatório é uma projeção assíncrona do banco de pedidos; não consulta diretamente `tb_pedidos`. Cada evento válido gera uma linha com `pedidoId`, data do pedido, quantidade de pedidos `1` e valor total. A chave única de pedido e a verificação de existência evitam contar novamente uma entrega duplicada.

Antes, o consumidor usava `LocalDate.now()`: um pedido antigo recebido hoje entrava no período de hoje. O evento agora transporta `dataCriacao`, e a consolidação usa essa data. Eventos sem identificador, data ou valor válido são rejeitados sem reenvio infinito. Não são convertidos em vendas com data/valor inventados. Publique produtores e consumidores atualizados juntos; eventos antigos sem data precisam de migração/reprocessamento a partir dos pedidos originais. A destinação de mensagens rejeitadas depende da configuração de dead-letter do broker.

A métrica chamada `faturamentoTotal` representa **valor de pedidos criados**, sem confirmação de pagamento nem desconto posterior de cancelamentos: não existem esses eventos na projeção atual. Pode haver atraso até o consumo RabbitMQ; falha de publicação impede a entrada na projeção. Pedidos e relatórios continuam usando H2 em memória: reiniciar esses serviços apaga seus dados. Para histórico durável e entrega garantida, serão necessários banco persistente e publicação transacional/outbox; essas capacidades não estão implementadas neste ajuste.

## Produção

O mock foi removido. `GET /v1/relatorios/producao` encaminha o Bearer do administrador a `GET /v1/producao/resumo`. O serviço de produção consulta sua própria tabela `tb_ordem_producao` por status, dentro de uma transação de leitura consistente:

| Campo | Status no banco |
| --- | --- |
| `emPendente` | `PENDENTE` |
| `emCorte` | `EM_CORTE` |
| `emCostura` | `EM_COSTURA` |
| `emAcabamento` | `ACABAMENTO` |
| `concluidos` | `CONCLUIDO` |

São contagens de ordens, não de unidades dos itens. `CANCELADO` fica fora desses cinco campos. Ordens são criadas pelo consumidor de pedidos e atualizadas pelo endpoint de status. Sem registros, os valores são zero; produção indisponível retorna `503`, sem substituir os dados por números fictícios. O H2 de produção também permanece em memória.

## Sessão e logout

- `GET /v1/auth/session`: requer JWT válido e retorna `email`, `roles` e `expiresAt` em milissegundos.
- `POST /v1/auth/logout`: requer JWT válido, grava seu hash SHA-256 em `tb_revoked_tokens` e retorna `204`.
- Tokens possuem identificador único (`jti`), assinatura e expiração. Expiração é validada sem tolerância adicional; token revogado, usuário removido/inativo ou perfil alterado retorna `401`.
- Gateway e todos os serviços validam assinatura/expiração e consultam o login para verificar revogação. O acesso direto às portas dos serviços também rejeita tokens encerrados. O login precisa estar disponível para chamadas autenticadas; não há cache que permita reutilizar token revogado.
- O login passou a usar H2 em arquivo, preservando usuários e revogações após reinício. O script define `POIESIS_AUTH_DB` para `.run-local/auth` dentro do backend; a variável pode apontar para outro caminho. Ao executar o jar diretamente, o padrão é `.run-local/auth` relativo ao diretório atual. A troca do antigo banco em memória não migra automaticamente usuários daquela execução.
- O frontend verifica a sessão ao iniciar e ao retomar o aplicativo/janela. O perfil vem da resposta validada pelo backend, sem confiar no usuário salvo localmente.
- O interceptor trata `401` autenticado limpando token/usuário e o estado de sessão. `403` mantém a sessão e representa falta de permissão. Respostas atrasadas de um token anterior não encerram uma sessão nova.
- Um temporizador limpa a sessão na expiração. Logout limpa imediatamente o armazenamento e o estado local, depois solicita revogação; eventual falha de conexão é informada ao usuário.
- `Stack.Protected` e `Tabs.Protected` removem as rotas proibidas da navegação; URLs diretas e o botão voltar não dão acesso a telas protegidas após a sessão ser encerrada. Eventos de foco/retorno da página também revalidam a sessão. Referência: [rotas protegidas do Expo Router](https://docs.expo.dev/router/advanced/protected/).

## Perfis

| Funcionalidade | USER | ADMIN |
| --- | --- | --- |
| Catálogo, opções de customização e criação de pedidos | Sim | Sim |
| Consulta de pedidos | Apenas próprios | Todos |
| Conta/logout | Sim | Sim |
| Tela Administração e relatórios | Não | Sim |
| Lista/resumo/atualização da produção | Não | Sim |
| Escrita no catálogo e nas opções de customização | Não | Sim |

O cadastro público continua atribuindo `USER` no servidor: enviar `roles: ["ADMIN"]` não eleva privilégios. Os controllers administrativos usam `@PreAuthorize` com segurança de métodos habilitada. A interface possui aba Administração para relatórios e aba Conta para perfil e logout; não foi criada uma interface de CRUD administrativo.

## Validação e aplicação

Resultado executado: builds dos sete serviços concluídos; 12 testes Java passaram; lint e TypeScript passaram; smoke HTTP passou, incluindo revogação após reinício do login e novo login válido. A navegação não foi validada visualmente em navegador/dispositivo.

Testes Java cobrem sessão inválida/expirada/revogada, novo login após logout, mudança de perfil, pedidos por cliente, datas inclusivas, consumo duplicado e contagem de produção após mudança de status. Surefire foi configurado para executar JUnit 5; a configuração de descoberta de dois testes antigos também foi corrigida.

```bash
# Em poiesis-frontend:
npm run lint
./node_modules/.bin/tsc --noEmit

# Em poiesis-backend-spring (repetir para cada serviço):
mvn -f login/pom.xml test package

# Depois dos builds de todos os serviços:
python3 tests/auth_endpoints_smoke.py
```

O smoke test cria bancos isolados e usa as portas `18080` a `18086`. Verifica autorização via gateway e acesso direto, relatórios reais vazios, revogação em todos os serviços e persistência da revogação após reiniciar somente seu login de teste. Ele não reinicia os serviços em uso nem publica eventos RabbitMQ.

As alterações precisam de reinicialização dos processos para serem aplicadas à execução nas portas `8080` a `8086`. Os serviços existentes foram preservados durante a validação porque seus bancos de negócio estão em memória. O roteiro manual de frontend deve conferir login, expiração, logout, recarregamento, acesso por URL direta, voltar do navegador e dois perfis; lint/typecheck não substituem essa validação visual em navegador/dispositivo.
