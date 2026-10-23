# Microsserviço de Relatório

O serviço mantém métricas próprias em H2 e consome eventos de pedido criado do CloudAMQP. O gateway exige role `ADMIN` para consultar relatórios; o serviço não valida JWT e escuta apenas em `127.0.0.1`.

Execute `RelatorioApplication` pelo IntelliJ; a porta padrão é `8084`. A conexão CloudAMQP está configurada diretamente em `spring/src/main/resources/application.properties`.

Rotas: `GET /v1/relatorios/vendas`, `POST /v1/relatorios/vendas/filtrar` e `GET /v1/relatorios/producao`.

Vendas somam pedidos criados por período, usando a data original recebida no evento. Produção consulta `/v1/producao/resumo` pela rede interna e retorna contagens reais do banco de produção. Consulte [a documentação da API](../README-POSTMAN.md).
