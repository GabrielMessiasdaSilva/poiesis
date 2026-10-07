# Microsserviço de Produção

Produção consome eventos `pedido.criado` do CloudAMQP e cria ordens no seu banco H2. Usuários autenticados (`USER` ou `ADMIN`) podem consultar ordens; somente `ADMIN` pode alterar status.

Execute `SpringframeworkApplication` pelo IntelliJ; a porta padrão é `8085`. A conexão CloudAMQP está configurada diretamente em `springframework/src/main/resources/application.properties`.

O evento tem o contrato JSON `{ "pedidoId": 123, "clienteEmail": "...", "valorTotal": 12.50 }`. O consumidor mantém um DTO local e não depende de classes Java do serviço de pedidos.
