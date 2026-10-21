# Carga e concorrência do Poiesis

Execute com os seis serviços e RabbitMQ disponíveis, preferencialmente em um ambiente isolado. Os testes cadastram dados reais: produtos e opções são inativados ao terminar, mas pedidos, ordens e projeções de vendas permanecem no ambiente. `BASE_URL` aponta para a raiz do gateway. `ADMIN_EMAIL` e `ADMIN_PASSWORD` substituem o administrador local padrão.

```bash
k6 run -e BASE_URL=http://localhost:8080 -e VUS=20 -e ITERATIONS=100 -e QUANTIDADE_POR_PEDIDO=3 multi_request/teste-carga.js
k6 run -e BASE_URL=http://localhost:8080 -e VUS=10 -e ITERATIONS=50 multi_request/teste-customizacao.js
k6 run -e BASE_URL=http://localhost:8080 -e VUS=10 -e ITERATIONS=50 multi_request/teste-concorrencia.js
```

Execute os comandos na pasta `poiesis-backend-spring`. Use `--summary-export=resultado.json` para guardar as métricas. Todos exigem 100% dos checks e nenhuma resposta HTTP inesperada; o terceiro reconhece `409` como conflito esperado.

| Cenário | O que verifica |
| --- | --- |
| `teste-carga.js` | Criação concorrente de pedidos, quantidade e valores, contagem persistida, IDs únicos, integridade dos itens e exatamente uma ordem de produção por pedido |
| `teste-customizacao.js` | Cadastro, edição, leitura e inativação simultâneos de opções diferentes |
| `teste-concorrencia.js` | Edições simultâneas do mesmo produto, opção e ordem; sucesso `200` ou conflito `409`, sem `500`; campos persistidos pertencentes à mesma escrita |

O fluxo é produção sob demanda: quantidade é o número de peças solicitado, sem saldo, reserva ou baixa de estoque. Os testes não simulam disputa de unidades em estoque.

`@Version` protege atualizações que disputam a mesma versão no banco. Os conflitos são traduzidos para `409`, para que a interface peça uma nova consulta. Isso não equivale a validar uma versão enviada pelo formulário: o contrato atual não tem `ETag`/`If-Match` nem versão de edição, portanto uma edição antiga enviada após outra já concluída pode sobrescrever os campos. Os testes não afirmam impedir esse caso.

A interface bloqueia envios repetidos enquanto uma operação está em andamento e não repete automaticamente escritas após falha. O backend ainda não oferece uma chave de idempotência para a criação do pedido. Duas requisições independentes representam dois pedidos; em caso de falha de rede, consulte a lista antes de reenviar.

A etapa de produção e o status do pedido são campos de serviços distintos. Alterar a etapa atualiza a ordem e o resumo de produção, mas atualmente não publica uma atualização do status no serviço Pedido. O relatório de vendas soma pedidos criados recebidos por evento, não pagamentos ou receita líquida. Salvamento de pedido e publicação RabbitMQ continuam sem transação atômica/outbox.

Os resultados locais avaliam correção sob a carga indicada. Não são uma estimativa de capacidade de produção nem um SLA de latência.
