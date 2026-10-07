# Login Service

O login autentica as credenciais persistidas em H2 e emite JWT próprio assinado com HMAC. Gateway e Resource Servers validam o token com a mesma chave secreta configurada nos `application.properties`.

As roles são armazenadas na tabela `tb_usuario_roles` como `USER` ou `ADMIN`. O cadastro público sempre cria `USER`. O token contém `roles`, com valores simples, e expira após 15 minutos. O administrador acadêmico inicial é `admin@poiesis.com` / `admin123`.

Use `Authorization: Bearer <token>` nas rotas protegidas. Execute a aplicação `SpringframeworkApplication` pelo IntelliJ. A porta padrão do login é `8081`.

O segredo HMAC atual é apenas para execução acadêmica local; substitua-o se o serviço for usado em outro ambiente.
