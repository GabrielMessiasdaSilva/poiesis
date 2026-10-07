# Cola para apresentação do Poiesis

## Visão geral

O projeto separa as responsabilidades em microsserviços Spring Boot. Cada serviço possui sua própria API e banco H2 em memória. O Gateway concentra a entrada HTTP na porta `8080` e encaminha cada caminho para o serviço correspondente.

| Serviço | Responsabilidade | Porta |
|---|---|---:|
| Login | Cadastro, autenticação e emissão de JWT | 8081 |
| Catálogo | Cadastro e consulta de produtos | 8082 |
| Pedido | Validação e criação de pedidos | 8083 |
| Relatório | Consolidação e consulta de métricas | 8084 |
| Produção | Criação e acompanhamento de ordens | 8085 |
| Customização | Opções de personalização dos produtos | 8086 |
| Gateway | Roteamento e validação inicial do JWT | 8080 |

## Como apresentar o fluxo

1. O cliente chama o Gateway, que encaminha a rota ao serviço configurado para aquele caminho.
2. Login verifica a senha armazenada com hash e devolve um JWT com as roles do usuário.
3. O cliente envia o JWT como Bearer Token nas rotas protegidas. Gateway e serviços validam a assinatura com a chave compartilhada; as roles viram autoridades `ROLE_USER` e `ROLE_ADMIN`.
4. Para criar um pedido, Pedido consulta Catálogo via HTTP para validar cada produto e obter seu preço. Em seguida persiste o pedido e publica o evento de criação.
5. Produção e Relatório recebem o evento separadamente pelo RabbitMQ. Produção cria uma ordem; Relatório atualiza sua consolidação. Essa comunicação é assíncrona.

## Organização do código

Os projetos seguem a separação `domain` e `springframework`:

- **Domain** contém entidades, regras de negócio, serviços/use cases e portas/interfaces de repositório e integração.
- **Springframework** contém controllers/DTOs, segurança, entidades JPA, repositórios, adaptadores e configurações de integração.
- Adaptadores implementam as portas do domínio. Assim, a regra de negócio depende de abstrações e não diretamente de JPA, HTTP ou RabbitMQ.

Exemplos para apontar no código durante a apresentação:

- Criação e validação do pedido: `pedido/domain/.../CriarPedidoUseCase.java`.
- Consulta HTTP ao Catálogo: `pedido/springframework/.../CatalogoIntegration.java`.
- Publicação do evento: `pedido/springframework/.../PedidoProducer.java`.
- Consumidor de Produção: `producao/springframework/.../PedidoConsumer.java`.
- Consumidor do Relatório: `relatorio/springframework/.../RelatorioConsumer.java`.
- Roteamento: `gateway/src/main/resources/application.properties`.

## RabbitMQ: o que é e como está sendo usado

RabbitMQ é o broker de mensagens que desacopla quem publica um evento de quem o processa. O serviço de Pedido publica um JSON com `pedidoId`, `clienteEmail` e `valorTotal` no exchange direto `pedido.eventos`, usando a routing key `pedido.criado`.

Produção e Relatório declaram filas próprias e vinculam cada fila ao mesmo exchange/routing key. O broker entrega uma cópia da mensagem a cada fila vinculada; cada consumidor recebe a sua cópia sem uma chamada síncrona do serviço de Pedido. As filas são duráveis.

Consequências para explicar:

- Pedido não precisa esperar Relatório terminar para responder à chamada HTTP.
- A ordem de produção e a consolidação podem aparecer alguns instantes depois do pedido: é consistência eventual.
- A falha de um consumidor não transforma o fluxo inteiro em uma única chamada HTTP; o processamento e os logs do consumidor precisam ser observados separadamente.
- A conexão atual usa CloudAMQP configurado nos `application.properties`; para executar o fluxo completo, a instância e as credenciais configuradas precisam estar acessíveis.

## Concorrência e consistência

Os serviços rodam em processos separados e podem atender requisições enquanto os consumidores RabbitMQ processam mensagens em segundo plano. A aplicação não depende de uma thread HTTP manter os dois consumidores sincronizados: a resposta do pedido e a atualização dos outros serviços acontecem em momentos diferentes.

Várias entidades JPA usam `@Version` (por exemplo Produto, Pedido, Ordem de Produção e Customização). Isso implementa controle otimista de concorrência: se duas operações tentarem salvar versões conflitantes da mesma linha, o JPA detecta a versão desatualizada em vez de sobrescrever silenciosamente a alteração mais nova. Isso não é um bloqueio global nem garante que qualquer operação distribuída seja atômica entre serviços.

O consumidor de Relatório também verifica se já existe consolidação para o `pedidoId` antes de salvar, como proteção contra repetição. Cada serviço tem banco próprio; não existe uma transação única envolvendo os bancos de Pedido, Produção e Relatório.

## Segurança e regras que vale destacar

- Cadastro público cria role `USER`; o administrador inicial local está documentado no README do Login.
- Catálogo permite consulta pública; criação e inativação de produto exigem `ADMIN`.
- Pedido exige autenticação `USER` ou `ADMIN`.
- Produção permite consulta a usuários autenticados; atualizar status exige `ADMIN`.
- Relatórios exigem `ADMIN`.
- Customização pode ser criada/listada por `USER` ou `ADMIN`; remoção administrativa é lógica (marca `ativo=false`) e não apaga o registro físico.
- O JWT expira em 15 minutos na configuração atual.

## Limitações conhecidas para contextualizar

- H2 em memória serve para desenvolvimento/demonstração; os dados se perdem ao reiniciar os serviços.
- Não há endpoint GET para consultar pedidos diretamente; a criação retorna o pedido e as consultas seguintes são de Produção/Relatório.
- O processamento por RabbitMQ é assíncrono; durante a demo, aguarde alguns segundos antes de consultar Produção e Relatório.
- O script `./iniciar-projeto.sh` compila os módulos e inicia os sete processos sem abrir a IDE. Use `./iniciar-projeto.sh parar` para encerrá-los.

## Roteiro curto de demonstração

1. Iniciar tudo com `./iniciar-projeto.sh`.
2. Fazer login de administrador pelo gateway e copiar o JWT.
3. Cadastrar produto no Catálogo.
4. Criar pedido com o ID desse produto.
5. Mostrar nos logs a publicação/consumo e, após alguns segundos, consultar `/v1/producao` e `/v1/relatorios/vendas`.
6. Criar uma customização e demonstrar `DELETE` administrativo seguido da listagem sem a opção inativada.
