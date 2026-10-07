# Poiesis Backend — execução e testes da API

Este repositório contém sete aplicações Spring Boot independentes. O gateway centraliza as requisições HTTP em `http://localhost:8080` e encaminha cada rota para o microsserviço correspondente. Ele não inicia nem substitui os outros serviços: todos precisam estar ativos.

## 1. Requisitos e inicialização

- Java 21 e Maven instalados no terminal.
- Acesso de rede à instância CloudAMQP configurada nos serviços de Pedido, Produção e Relatório.
- Portas `8080` a `8086` livres.

Na raiz do projeto, execute uma vez para compilar e iniciar todos os serviços:

```bash
./iniciar-projeto.sh
```

O script compila os sete módulos e inicia os serviços em segundo plano. Acompanhe a inicialização em `.run-local/logs/`. Não execute o comando de inicialização novamente enquanto os serviços estiverem ativos: as portas já estarão ocupadas. Para encerrar os processos iniciados pelo script:

```bash
./iniciar-projeto.sh parar
```

O comando de parada localiza os processos pelos JARs deste projeto, mesmo quando os arquivos `.pid` estão ausentes. Ele aguarda até 30 segundos pelo encerramento normal, força a parada dos processos restantes e verifica se as portas foram liberadas. Processos de outras aplicações não são encerrados. Se uma porta continuar ocupada, o comando informa a falha. O script guarda PID e logs em `.run-local/` e usa `flock` (pacote `util-linux`) para impedir execuções simultâneas.

A inicialização executa `clean install` para recompilar os módulos com os nomes dos parâmetros Java preservados, necessários para os controllers Spring. Se a inicialização falhar, confira se algum processo iniciado pela IDE ainda está usando as portas e se a conexão CloudAMQP configurada está acessível.

| Aplicação | Porta |
|---|---:|
| Gateway | 8080 |
| Login | 8081 |
| Catálogo | 8082 |
| Pedido | 8083 |
| Relatório | 8084 |
| Produção | 8085 |
| Customização | 8086 |

Faça as requisições pelo gateway: `http://localhost:8080`. No Postman, crie uma variável `baseUrl` com esse valor. Os bancos H2 dos serviços de negócio estão em memória e seus dados são reiniciados junto com os serviços. O login usa H2 em arquivo para preservar usuários e revogações de tokens. Pedido publica eventos no CloudAMQP, consumidos de forma assíncrona por Produção e Relatório.

## 2. Autenticar como administrador

O serviço de Login cria um administrador inicial. Credenciais locais documentadas no projeto:

```http
POST {{baseUrl}}/v1/auth/login
Content-Type: application/json
```

```json
{
  "email": "admin@poiesis.com",
  "senha": "admin123"
}
```

A resposta contém `token`. Copie-o para uma variável `tokenAdmin`. Nas chamadas protegidas, selecione **Authorization → Bearer Token** e informe `{{tokenAdmin}}`.

Para criar um usuário comum, envie `POST {{baseUrl}}/v1/auth/register` com:

```json
{
  "nome": "Pessoa Teste",
  "email": "pessoa@exemplo.com",
  "senha": "senha123"
}
```

Depois faça login com esse usuário. O cadastro público recebe a role `USER`; não use esse token nas operações exclusivas de administrador.

## 3. Criar um produto

Com o token de administrador:

```http
POST {{baseUrl}}/v1/produtos
Content-Type: application/json
Authorization: Bearer {{tokenAdmin}}
```

```json
{
  "nome": "Camiseta Poiesis",
  "descricao": "Camiseta de teste",
  "precoBase": 49.90,
  "categoria": "CAMISETA"
}
```

Guarde o `id` da resposta na variável `produtoId`. Categorias aceitas: `CAMISA`, `CAMISETA`, `MOLETOM`, `CALCA`, `ACESSORIO`.

Consulta pública do catálogo:

```http
GET {{baseUrl}}/v1/produtos
GET {{baseUrl}}/v1/produtos/{{produtoId}}
```

## 4. Criar e remover uma customização

Criação, com token de usuário ou administrador:

```http
POST {{baseUrl}}/v1/customizacoes
Content-Type: application/json
Authorization: Bearer {{tokenAdmin}}
```

```json
{
  "produtoId": {{produtoId}},
  "tipo": "COR",
  "nome": "Azul-marinho",
  "precoAdicional": 5.00
}
```

Guarde o `id` retornado como `customizacaoId`. Liste as opções ativas do produto:

```http
GET {{baseUrl}}/v1/customizacoes/produto/{{produtoId}}
Authorization: Bearer {{tokenAdmin}}
```

Somente administrador pode inativar uma customização. É uma exclusão lógica: o registro permanece no banco, mas deixa de aparecer na listagem.

```http
DELETE {{baseUrl}}/v1/customizacoes/{{customizacaoId}}
Authorization: Bearer {{tokenAdmin}}
```

Resposta esperada: `204 No Content`. ID inexistente: `404 Not Found`.

## 5. Criar um pedido e observar os eventos

Use token de usuário ou administrador. O Catálogo precisa estar ativo porque Pedido consulta o produto para obter nome e preço.

```http
POST {{baseUrl}}/v1/pedidos
Content-Type: application/json
Authorization: Bearer {{tokenAdmin}}
```

```json
{
  "itens": [
    {
      "produtoId": {{produtoId}},
      "quantidade": 2
    }
  ]
}
```

Cada item aceita `customizacaoIds` opcional, por exemplo `{ "produtoId": 1, "quantidade": 2, "customizacaoIds": [3] }`. Os IDs devem corresponder a opções ativas desse produto, sem duplicatas e com no máximo uma opção por tipo (até 20 por item). O preço unitário é o preço base mais os adicionais; nomes e preços das opções são copiados para o pedido e retornados em `itens[].customizacoes`. Não envie nomes ou preços de customização: o servidor consulta esses valores no serviço de Customização. O serviço Pedido usa `application.config.customizacao-url` (padrão `http://localhost:8086`).

Guarde o campo `id` da resposta (ele será o `pedidoId` dentro do evento). Após a criação, Pedido publica o evento `pedido.criado` no RabbitMQ. Produção e Relatório o processam de forma assíncrona; aguarde alguns segundos antes de consultar esses serviços.

Para consultar pedidos, usuário comum vê somente os próprios pedidos; administrador vê todos. A consulta por ID também respeita esse acesso: pedido inexistente ou pertencente a outro usuário retorna `404`.

```http
GET {{baseUrl}}/v1/pedidos
GET {{baseUrl}}/v1/pedidos/{{pedidoId}}
Authorization: Bearer {{tokenAdmin}}
```

Consulte as ordens de produção (somente administrador):

```http
GET {{baseUrl}}/v1/producao
Authorization: Bearer {{tokenAdmin}}
```

A resposta contém as ordens criadas. Para atualizar uma ordem, use o ID da **ordem de produção** retornado pela consulta (não necessariamente o ID do pedido):

```http
PATCH {{baseUrl}}/v1/producao/{{ordemId}}/status
Content-Type: application/json
Authorization: Bearer {{tokenAdmin}}
```

```json
{
  "status": "EM_CORTE"
}
```

Status aceitos: `PENDENTE`, `EM_CORTE`, `EM_COSTURA`, `ACABAMENTO`, `CONCLUIDO`, `CANCELADO`.

## 6. Consultar relatórios

As rotas de relatório exigem administrador. Use datas no formato ISO (`AAAA-MM-DD`):

```http
GET {{baseUrl}}/v1/relatorios/vendas?dataInicio=2026-10-01&dataFim=2026-10-31
GET {{baseUrl}}/v1/relatorios/producao
Authorization: Bearer {{tokenAdmin}}
```

Também é possível enviar o filtro de vendas por POST:

```http
POST {{baseUrl}}/v1/relatorios/vendas/filtrar
Content-Type: application/json
Authorization: Bearer {{tokenAdmin}}
```

```json
{
  "dataInicio": "2026-10-01",
  "dataFim": "2026-10-31"
}
```

## CORS no frontend web

O gateway autoriza o frontend em `http://localhost:8087`, `http://127.0.0.1:8087` e origens HTTP/HTTPS na porta `8090`. `OPTIONS /v1/auth/register` é processado antes da autenticação; cadastro e login não exigem token. Alterações exigem recompilar e reiniciar o gateway. A URL da API permanece `http://localhost:8080`, independentemente da porta do frontend.

## 7. O que esperar de erros comuns

- `401 Unauthorized`: token ausente, inválido ou expirado; faça login novamente.
- `403 Forbidden`: token válido, mas sem a role exigida. Cadastro público cria usuário `USER`; crie/inative produtos, atualize produção e consulte relatórios com o token `ADMIN`.
- `404` ou `502` ao criar pedido/customização: confira se o Catálogo está ativo e se o ID existe.
- Pedido criado, mas sem ordem/relatório: confira a conexão CloudAMQP nos logs e aguarde o consumidor processar a mensagem.
- Porta ocupada: não inicie uma segunda cópia. Use `./iniciar-projeto.sh parar`; se a porta continuar ocupada, encerre o processo correspondente na IDE ou identifique-o com `sudo ss -ltnp 'sport = :8082'` (troque a porta conforme a mensagem).

## 8. Teste de carga com k6

O teste em [`multi_request/teste-carga.js`](multi_request/teste-carga.js) simula criações concorrentes de pedidos através do gateway. Ele autentica com o administrador, cria um produto de teste no Catálogo, envia pedidos e inativa esse produto ao terminar. Os pedidos criados permanecem no banco até os serviços serem reiniciados.

O teste não verifica estoque: o modelo atual de Produto não possui campo de estoque nem o fluxo de Pedido faz baixa de estoque. Cada iteração valida `201 Created` com ID; ao final, consulta os pedidos e confere se a quantidade persistida para o produto temporário coincide com as iterações, se os IDs são únicos e se os itens estão íntegros. A publicação de eventos também exige que a conexão CloudAMQP esteja disponível.

Controle de estoque está fora do escopo do projeto. A quantidade enviada representa a solicitação do cliente; o sistema acompanha catálogo, pedidos e produção, sem saldo, reserva ou baixa de peças.

Para validar o fluxo funcional com os sete serviços ativos, execute:

```bash
python3 tests/fluxo_sem_estoque.py
```

Esse teste verifica cadastro, login, sessão, criação e edição de produto e opções, permissões, pedido com cálculo de preço, consumo do evento pela produção, atualização de status e relatórios. Ao terminar, inativa o produto e a opção usados. O usuário e o pedido de teste permanecem registrados. Também verifica preflight de cadastro e cabeçalhos CORS para `http://localhost:8087`, persistência das customizações escolhidas, cálculo dos adicionais, histórico após edição/inativação e rejeição de opções indisponíveis ou IDs duplicados.

Com os sete serviços ativos e o k6 instalado, rode na raiz:

```bash
k6 run multi_request/teste-carga.js
```

Por padrão, são executadas 10 iterações com até 10 usuários virtuais. Para ajustar o volume:

```bash
VUS=20 ITERATIONS=100 k6 run multi_request/teste-carga.js
```

Variáveis disponíveis:

| Variável | Padrão | Uso |
|---|---:|---|
| `BASE_URL` | `http://localhost:8080` | URL do gateway |
| `ADMIN_EMAIL` | `admin@poiesis.com` | E-mail do administrador bootstrap |
| `ADMIN_PASSWORD` | `admin123` | Senha do administrador bootstrap |
| `VUS` | `10` | Número de usuários virtuais |
| `ITERATIONS` | valor de `VUS` | Total de pedidos a tentar criar |
| `QUANTIDADE_POR_PEDIDO` | `1` | Quantidade do produto em cada pedido |

Exemplo usando um endereço ou credencial diferente:

```bash
BASE_URL=http://localhost:8080 ADMIN_EMAIL=admin@poiesis.com ADMIN_PASSWORD=admin123 VUS=5 ITERATIONS=25 k6 run multi_request/teste-carga.js
```

Confira as métricas `pedidos_criados`, `pedidos_falhos`, `pedidos_persistidos` e a taxa de respostas HTTP no resumo do k6. Se o total persistido for diferente do esperado, investigue os logs do Pedido e a conexão com o RabbitMQ: atualmente, o salvamento do pedido é transacional no banco, mas a publicação da mensagem ocorre depois do commit e não participa da mesma transação. O teste não impõe um limite de latência; use `http_req_duration` do relatório para avaliar o desempenho observado.

O teste define `checks: rate==1`; portanto, ele termina com falha se qualquer requisição, conferência de persistência ou limpeza não passar.

## Sessão e relatórios atualizados

Consulte [ANALISE-ENDPOINTS-E-AUTENTICACAO.md](ANALISE-ENDPOINTS-E-AUTENTICACAO.md) para origem dos relatórios, novos endpoints de sessão/logout, revogação persistente e regras de perfil.

## Build Maven de todos os serviços

O POM da raiz agrega os sete serviços. Cada serviço de negócio herda `spring-boot-starter-parent:4.1.1` e possui Java 21, metadados próprios e os módulos `domain` / `springframework`. O diretório `springframework` contém o módulo Spring indicado no modelo de arquitetura. O gateway é uma aplicação Spring única.

Os artefatos são exclusivos por serviço: `login-domain` / `login-spring`, `catalogo-domain` / `catalogo-spring` etc. Isso evita sobrescrever módulos de outro serviço no repositório Maven local. Os módulos de domínio não declaram dependências do Spring; somente os módulos de API ativam o plugin de empacotamento executável. O parent gerencia versões e configurações de plugins, conforme a [documentação do Spring Boot](https://docs.spring.io/spring-boot/maven-plugin/using.html).

```bash
# Dentro de poiesis-backend-spring:
mvn clean install
python3 tests/auth_endpoints_smoke.py

# Ou apenas um microsserviço:
mvn -f login/pom.xml clean install
```

O script `iniciar-projeto.sh` compila o agregador e usa os novos jars `<servico>-spring-0.0.1-SNAPSHOT.jar`. O smoke inicia os sete serviços em portas isoladas e preserva os processos existentes.

Cadastro público: `POST /v1/auth/register` exige nome, e-mail válido e senha com 8 a 72 caracteres. Retorna `201` no sucesso, `400` para dados inválidos e `409` para e-mail já cadastrado. O frontend oferece a tela de cadastro e o retorno ao login.

### Verificar customizações com k6

```bash
k6 run multi_request/teste-customizacao.js
```

O teste cria um produto temporário e valida criação, edição com o mesmo ID,
consulta e inativação de opções com 10 usuários concorrentes. Aceita `BASE_URL`,
`ADMIN_EMAIL`, `ADMIN_PASSWORD`, `VUS` e `ITERATIONS`, como o teste de pedidos.
Exige 100% dos checks e nenhuma falha HTTP.

O script de inicialização executa cópias dos JARs em `.run-local/artifacts`,
para evitar erros de classes ausentes quando Maven substitui os arquivos em
`target` durante uma recompilação. Para aplicar novas versões, pare os serviços
antes de iniciar novamente; recompilar não atualiza uma JVM que já está rodando.
