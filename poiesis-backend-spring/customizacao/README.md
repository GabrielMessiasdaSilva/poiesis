# Microsserviço de Customização

O serviço persiste customizações em H2 e consulta produtos no catálogo por Feign. As leituras e criações de customização exigem `USER` ou `ADMIN`. Ele não usa RabbitMQ porque ainda não existe outro serviço consumindo eventos de customização.

Execute `SpringframeworkApplication` pelo IntelliJ. A porta padrão é `8086`; o serviço de catálogo deve estar disponível em `http://localhost:8082`.

O serviço valida JWT HMAC emitido pelo login usando o segredo compartilhado nos `application.properties`. O domínio permanece independente do Spring.

O `ADMIN` também pode inativar uma opção de customização com `DELETE /v1/customizacoes/{id}`. A resposta é `204 No Content`; opções inativas deixam de aparecer em `GET /v1/customizacoes/produto/{produtoId}`. A exclusão é lógica para preservar o registro.
