# Login Service

O login autentica as credenciais persistidas em H2 e emite JWT próprio assinado com HMAC. Somente o gateway valida o JWT, usando a chave compartilhada com o emissor. O login não possui filtro de segurança HTTP; mantém apenas a autenticação de credenciais e o gerenciamento das sessões.

As roles são armazenadas na tabela `tb_usuario_roles` como `USER` ou `ADMIN`. O cadastro público sempre cria `USER`. O token contém `roles`, com valores simples, e expira após 15 minutos. O administrador acadêmico inicial é `admin@poiesis.com` / `admin123`.

Use `Authorization: Bearer <token>` nas rotas protegidas pelo gateway (`http://localhost:8080`). Execute a aplicação `LoginApplication` pelo IntelliJ. A porta padrão do login é `8081`.

O segredo HMAC atual é apenas para execução acadêmica local; substitua-o se o serviço for usado em outro ambiente.

`GET /v1/auth/session` retorna a identidade validada pelo gateway. `POST /v1/auth/logout` revoga o token no banco. Somente o gateway consulta `POST /internal/sessions/validate` para verificar revogação, usuário ativo e perfis atuais; esse endpoint não está exposto pelas rotas públicas. Usuários e revogações são persistidos em H2 em arquivo (`POIESIS_AUTH_DB`), inclusive após reinício. Veja [a configuração da segurança centralizada](../README-POSTMAN.md#segurança-centralizada).
