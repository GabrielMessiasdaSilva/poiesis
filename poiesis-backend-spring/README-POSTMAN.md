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

Depois de parar os serviços, aguarde alguns segundos antes de iniciá-los novamente. Se a inicialização falhar, confira se algum processo iniciado pela IDE ou por outra execução ainda está usando as portas e se a conexão CloudAMQP configurada está acessível. O script guarda PID e logs em `.run-local/`.

| Aplicação | Porta |
|---|---:|
| Gateway | 8080 |
| Login | 8081 |
| Catálogo | 8082 |
| Pedido | 8083 |
| Relatório | 8084 |
| Produção | 8085 |
| Customização | 8086 |

Faça as requisições pelo gateway: `http://localhost:8080`. No Postman, crie uma variável `baseUrl` com esse valor. Os bancos H2 estão em memória: os dados são reiniciados quando os serviços são reiniciados. Pedido publica eventos no CloudAMQP, consumidos de forma assíncrona por Produção e Relatório.

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

Guarde o campo `id` da resposta (ele será o `pedidoId` dentro do evento). Após a criação, Pedido publica o evento `pedido.criado` no RabbitMQ. Produção e Relatório o processam de forma assíncrona; aguarde alguns segundos antes de consultar esses serviços.

Para consultar pedidos, usuário comum vê somente os próprios pedidos; administrador vê todos. A consulta por ID também respeita esse acesso: pedido inexistente ou pertencente a outro usuário retorna `404`.

```http
GET {{baseUrl}}/v1/pedidos
GET {{baseUrl}}/v1/pedidos/{{pedidoId}}
Authorization: Bearer {{tokenAdmin}}
```

Consulte as ordens de produção:

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

## 7. O que esperar de erros comuns

- `401 Unauthorized`: token ausente, inválido ou expirado; faça login novamente.
- `403 Forbidden`: token válido, mas sem a role exigida. Cadastro público cria usuário `USER`; crie/inative produtos, atualize produção e consulte relatórios com o token `ADMIN`.
- `404` ou `502` ao criar pedido/customização: confira se o Catálogo está ativo e se o ID existe.
- Pedido criado, mas sem ordem/relatório: confira a conexão CloudAMQP nos logs e aguarde o consumidor processar a mensagem.
- Porta ocupada: não inicie uma segunda cópia. Use `./iniciar-projeto.sh parar`; se a porta continuar ocupada, encerre o processo correspondente na IDE ou identifique-o com `sudo ss -ltnp 'sport = :8082'` (troque a porta conforme a mensagem).

## 8. Teste de carga com k6

O teste em [`multi_request/teste-carga.js`](multi_request/teste-carga.js) simula criações concorrentes de pedidos através do gateway. Ele autentica com o administrador, cria um produto de teste no Catálogo, envia pedidos e inativa esse produto ao terminar. Os pedidos criados permanecem no banco até os serviços serem reiniciados.

O teste não verifica estoque: o modelo atual de Produto não possui campo de estoque nem o fluxo de Pedido faz baixa de estoque. Cada iteração valida `201 Created` com ID; ao final, consulta os pedidos e confere se a quantidade persistida para o produto temporário coincide com as iterações, se os IDs são únicos e se os itens estão íntegros. A publicação de eventos também exige que a conexão CloudAMQP esteja disponível.

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
