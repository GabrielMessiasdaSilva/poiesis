# Microsserviço Produto

Reúne o antigo Catálogo e a antiga Customização em uma aplicação Spring Boot, na porta 8082, e um banco H2 em memória (`produto`).

- `/v1/produtos`: cadastro, consulta, atualização e inativação de produtos.
- `/v1/customizacoes`: cadastro, consulta por produto, atualização e inativação de opções.

Os contratos HTTP e as permissões do gateway são preservados. A validação do produto ao criar ou editar uma opção usa `CatalogoService` internamente, sem Feign ou HTTP.

`domain` contém entidades, repositories e serviços em Java puro. `spring` contém controllers, DTOs, adapters, configuração e persistência ORM/JPA. Catálogo e customizações mantêm classes próprias, com responsabilidades distintas.

Pedido continua em outra aplicação e consulta as duas APIs de Produto por `application.config.produto-url`.

```bash
mvn -f poiesis-backend-spring/produto/pom.xml clean test
```

Para iniciar o sistema completo, consulte [README-POSTMAN.md](../README-POSTMAN.md). Encerre os processos antigos de Catálogo e Customização antes de iniciar a versão consolidada. Como os bancos anteriores eram em memória, os dados devem ser cadastrados novamente.
