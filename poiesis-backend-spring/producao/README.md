# Microsserviço de Produção

Produção consome eventos `pedido.criado` do CloudAMQP e cria ordens no seu banco H2. O gateway exige `ADMIN` para consultar ordens, consultar o resumo e alterar status. O serviço não valida JWT e escuta apenas em `127.0.0.1`. `GET /v1/producao/resumo` calcula as contagens reais por status na tabela `tb_ordem_producao`.

Execute `ProducaoApplication` pelo IntelliJ; a porta padrão é `8085`. A conexão CloudAMQP está configurada diretamente em `spring/src/main/resources/application.properties`.

O evento tem o contrato JSON `{ "pedidoId": 123, "clienteEmail": "...", "valorTotal": 12.50, "dataCriacao": "2026-10-07T10:00:00" }`. O consumidor mantém um DTO local e não depende de classes Java do serviço de pedidos.
