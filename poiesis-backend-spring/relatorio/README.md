# Microsserviço de Relatório

O serviço mantém métricas próprias em H2 e consome eventos de pedido criado do CloudAMQP. Ele valida JWT HMAC emitido pelo login e exige role `ADMIN` para consultar relatórios.

Execute `SpringframeworkApplication` pelo IntelliJ; a porta padrão é `8084`. A conexão CloudAMQP está configurada diretamente em `springframework/src/main/resources/application.properties`.

Rotas: `GET /v1/relatorios/vendas`, `POST /v1/relatorios/vendas/filtrar` e `GET /v1/relatorios/producao`.

Vendas somam pedidos criados por período, usando a data original recebida no evento. Produção consulta `/v1/producao/resumo` com o JWT do administrador e retorna contagens reais do banco de produção. Consulte [origem dos dados e limitações](../ANALISE-ENDPOINTS-E-AUTENTICACAO.md).
