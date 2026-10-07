# Contexto atual do front-end Poiesis

Atualizado em 2026-10-07.

## Integração implementada

- Administração: seções Produtos, Customizações, Produção e Relatórios, exclusivas do perfil `ADMIN`. Produtos e opções por produto têm cadastro, edição e exclusão com confirmação (inativação). Edição usa `PUT /v1/produtos/{id}` e `PUT /v1/customizacoes/{id}`, adicionados ao backend. Produção lista `GET /v1/producao` e altera status via `PATCH /v1/producao/{id}/status`. Catálogo e customizações recarregam ao ganhar foco para refletir alterações administrativas. Reinicie/recompile catálogo e customização para disponibilizar as novas rotas.

- Cadastro: tela `/(auth)/cadastro`, acessível pelo login, envia `{ nome, email, senha }` para `POST /v1/auth/register`. Valida campos, e-mail e confirmação de senha, exibe conflito de e-mail e sucesso. Após criar a conta, o usuário retorna ao login; não é iniciada sessão automaticamente.

- Login: `POST /v1/auth/login` com `{ email, senha }`. O backend retorna `{ token, tipo }`; o app salva o JWT e o usuário localmente e envia Bearer Token nas chamadas protegidas.
- Catálogo: `GET /v1/produtos`, exibindo produtos ativos, descrição, categoria e preço retornados pela API.
- Customizações: para cada produto, `GET /v1/customizacoes/produto/{produtoId}`; a tela apresenta somente opções ativas, com tipo, nome e preço adicional.
- Pedidos: `POST /v1/pedidos` com um item `{ produtoId, quantidade: 1, customizacaoIds: [...] }`; a listagem usa `GET /v1/pedidos` e mostra os itens, valores, data e status persistidos.
- Corrigido o mapeamento de preço na integração pedido → catálogo: a resposta usa `precoBase`. O serviço de pedidos precisa ser recompilado/reiniciado para deixar de rejeitar produtos válidos por preço nulo.
- A URL base padrão é `http://10.0.2.2:8080` no emulador Android e `http://localhost:8080` nas demais plataformas. `EXPO_PUBLIC_API_URL` deve apontar para a raiz do gateway, sem `/api` ou `/v1` no final.
- Os scripts do Expo usam a porta `8090` para não conflitar com o login (`8081`) nem com os demais serviços (`8080` a `8086`). O gateway aceita origens HTTP/HTTPS na porta `8090` e também `http://localhost:8087` / `http://127.0.0.1:8087` durante o desenvolvimento. O favicon está configurado em `app.json` e disponível em `public/favicon.ico`.

## Regras e limites do backend

- Controle de estoque está fora do escopo, conforme orientação do usuário em 2026-10-07. Quantidade no pedido representa a quantidade solicitada; não há saldo, reserva, baixa de peças ou bloqueio por estoque. Cadastro e inativação de produtos controlam o catálogo, e status de produção acompanha a execução dos pedidos.
- O JWT é necessário para pedidos e leitura de opções de customização. A sessão é validada em `/v1/auth/session`, expira automaticamente e respostas `401` autenticadas encerram o acesso; `403` indica falta de permissão. O e-mail do cliente é determinado pelo token no backend.
- Produtos têm nome, descrição, preço base, categoria e estado ativo. A API não fornece tamanhos ou cores; por isso o app não oferece seletores locais que poderiam sugerir uma disponibilidade inexistente.
- O endpoint de customizações cadastra/lista opções por produto. No catálogo, o cliente pode selecionar uma opção por tipo; o pedido envia somente os IDs. Pedido consulta as opções ativas do produto com o token do cliente, calcula os adicionais por unidade e persiste uma cópia de ID, tipo, nome e preço adicional. A aba Pedidos exibe as escolhas; editar ou inativar a opção não altera o histórico. Texto/arte/posição livres continuam fora do contrato.
- O pedido atual aceita produto, quantidade e IDs opcionais de customização, calcula preço com base no catálogo e nos adicionais e retorna `CRIADO`, `EM_PROCESSAMENTO`, `FINALIZADO` ou `CANCELADO`. Não existe endpoint para cliente editar ou cancelar pedidos.
- A API lista pedidos próprios para `USER` e todos os pedidos para `ADMIN`. A produção é assíncrona via RabbitMQ e não é necessária para a lista de pedidos.

## Execução para validação

Validação final em 2026-10-07: Pedido passou em 7 testes Maven e Gateway em 3 (incluindo preflight sem token para cadastro em 8087 e rejeição de origem não autorizada). O teste `../poiesis-backend-spring/tests/fluxo_sem_estoque.py` passou com os sete serviços reais, cobrindo CORS, cadastro/login/sessão, catálogo/opções, permissões ADMIN, pedido personalizado com quantidade 2 e total R$ 129,80, rejeição de IDs duplicados/inativos/inexistentes, preservação das escolhas após edição/inativação, evento para produção, alteração para `EM_CORTE` e relatórios. Frontend passou em lint e `tsc --noEmit`; `/cadastro` e `/favicon.ico` responderam 200 na porta 8087. Não houve teste visual em aparelho/emulador. Os sete serviços foram mantidos ativos na sessão de execução ao terminar; a parada documentada usa `iniciar-projeto.sh parar`.

1. Inicie os serviços do backend seguindo `../poiesis-backend-spring/README-POSTMAN.md`.
2. Inicie o app com `npx expo start` (ou `npx expo start --web`). Em aparelho físico, configure `EXPO_PUBLIC_API_URL` para o IP do computador acessível pela rede.
3. Use um usuário existente. O administrador local documentado pelo backend é `admin@poiesis.com` / `admin123`; também é possível cadastrar usuário `USER` pelo endpoint `POST /v1/auth/register` documentado no backend.
4. Valide catálogo, lista de opções, criação do pedido e atualização da aba Pedidos.

Os serviços de negócio usam H2 em memória; reiniciar esses serviços limpa os registros locais. O login usa H2 em arquivo para preservar usuários e revogação de tokens. Consulte o README do backend para limitações de RabbitMQ e permissões.

## Sessão, logout e perfis

- A aba Conta exibe o e-mail/perfil e permite sair. Logout limpa o estado e o armazenamento, solicita revogação no servidor e retorna ao login pelas rotas protegidas.
- O perfil vem da resposta autenticada de `/v1/auth/session`. A aba Administração existe somente para `ADMIN` e consulta vendas por período e o resumo atual de produção. As APIs também validam o perfil.
- Rotas protegidas do Expo Router bloqueiam URL direta e histórico após logout. Foco, retorno à página e retomada do app revalidam a sessão; o temporizador trata expiração sem precisar aguardar uma chamada à API.
- Vendas representam pedidos criados recebidos por RabbitMQ; produção agora usa contagens reais do banco de produção. Consulte [a análise do backend](../poiesis-backend-spring/ANALISE-ENDPOINTS-E-AUTENTICACAO.md) para origem, persistência e limitações.
