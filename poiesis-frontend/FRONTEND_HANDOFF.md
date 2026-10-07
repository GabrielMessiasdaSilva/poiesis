# Contexto atual do front-end Poiesis

Atualizado em 2026-10-07.

## Integração implementada

- Login: `POST /v1/auth/login` com `{ email, senha }`. O backend retorna `{ token, tipo }`; o app salva o JWT e o usuário localmente e envia Bearer Token nas chamadas protegidas.
- Catálogo: `GET /v1/produtos`, exibindo produtos ativos, descrição, categoria e preço retornados pela API.
- Customizações: para cada produto, `GET /v1/customizacoes/produto/{produtoId}`; a tela apresenta somente opções ativas, com tipo, nome e preço adicional.
- Pedidos: `POST /v1/pedidos` com um item `{ produtoId, quantidade: 1 }`; a listagem usa `GET /v1/pedidos` e mostra os itens, valores, data e status persistidos.
- A URL base padrão é `http://10.0.2.2:8080` no emulador Android e `http://localhost:8080` nas demais plataformas. `EXPO_PUBLIC_API_URL` deve apontar para a raiz do gateway, sem `/api` ou `/v1` no final.
- Os scripts do Expo usam a porta `8090` para não conflitar com o login (`8081`) nem com os demais serviços (`8080` a `8086`). O gateway aceita origens HTTP/HTTPS na porta `8090` durante o desenvolvimento.

## Regras e limites do backend

- O JWT é necessário para pedidos e leitura de opções de customização. O e-mail do cliente é determinado pelo token no backend.
- Produtos têm nome, descrição, preço base, categoria e estado ativo. A API não fornece tamanhos ou cores; por isso o app não oferece seletores locais que poderiam sugerir uma disponibilidade inexistente.
- O endpoint de customizações cadastra/lista opções por produto (por exemplo, uma opção de cor e seu adicional). Ele não salva a peça escolhida pelo cliente e não aceita texto/arte/posição.
- O pedido atual aceita produto e quantidade, calcula preço com base no catálogo e retorna `CRIADO`, `EM_PROCESSAMENTO`, `FINALIZADO` ou `CANCELADO`. Não existe endpoint para cliente editar ou cancelar pedidos.
- A API expõe consulta de pedidos do usuário autenticado, então a tela mostra os registros desse usuário. A produção é assíncrona via RabbitMQ e não é necessária para a lista de pedidos.

## Execução para validação

1. Inicie os serviços do backend seguindo `../poiesis-backend-spring/README-POSTMAN.md`.
2. Inicie o app com `npx expo start` (ou `npx expo start --web`). Em aparelho físico, configure `EXPO_PUBLIC_API_URL` para o IP do computador acessível pela rede.
3. Use um usuário existente. O administrador local documentado pelo backend é `admin@poiesis.com` / `admin123`; também é possível cadastrar usuário `USER` pelo endpoint `POST /v1/auth/register` documentado no backend.
4. Valide catálogo, lista de opções, criação do pedido e atualização da aba Pedidos.

O backend usa H2 em memória; reiniciar seus serviços limpa os registros locais. Consulte o README do backend para limitações de RabbitMQ e permissões.
