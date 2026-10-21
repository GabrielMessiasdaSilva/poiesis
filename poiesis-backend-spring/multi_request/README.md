# Carga e concorrência do Poiesis

Execute com os seis serviços e RabbitMQ disponíveis, preferencialmente em um ambiente isolado. Os testes cadastram dados reais: produtos e opções são inativados ao terminar, mas usuários de teste, pedidos, ordens e projeções de vendas permanecem no ambiente. `BASE_URL` aponta para a raiz do gateway. `ADMIN_EMAIL` e `ADMIN_PASSWORD` identificam o administrador; `CLIENT_PASSWORD` define a senha das contas temporárias de cliente (mínimo de oito caracteres). Cada execução cria uma conta de cliente com e-mail exclusivo, usada para efetuar a compra.

```bash
k6 run -e BASE_URL=http://localhost:8080 -e VUS=20 -e ITERATIONS=100 -e QUANTIDADE_POR_PEDIDO=3 multi_request/teste-carga.js
k6 run -e BASE_URL=http://localhost:8080 -e VUS=10 -e ITERATIONS=50 multi_request/teste-customizacao.js
k6 run -e BASE_URL=http://localhost:8080 -e VUS=10 multi_request/teste-concorrencia.js
```

Execute os comandos na pasta `poiesis-backend-spring`. Use `--summary-export=resultado.json` para guardar as métricas. Todos exigem 100% dos checks e nenhuma resposta HTTP inesperada; o terceiro reconhece `409` como conflito esperado.

| Cenário | O que verifica |
| --- | --- |
| `teste-carga.js` | Criação concorrente de pedidos, quantidade e valores, contagem persistida, IDs únicos, integridade dos itens e exatamente uma ordem de produção por pedido |
| `teste-customizacao.js` | Cadastro, edição, leitura e inativação simultâneos de opções diferentes |
| `teste-concorrencia.js` | Uma disputa simultânea por VU no mesmo produto, opção e ordem; sucesso `200` ou conflito `409`, sem `500`; campos persistidos pertencentes à mesma escrita e status propagado ao pedido |

O fluxo é produção sob demanda: quantidade é o número de peças solicitado, sem saldo, reserva ou baixa de estoque. Os testes não simulam disputa de unidades em estoque.

`@Version` protege atualizações que disputam a mesma versão no banco. Os conflitos são traduzidos para `409`, para que a interface peça uma nova consulta. Isso não equivale a validar uma versão enviada pelo formulário: o contrato atual não tem `ETag`/`If-Match` nem versão de edição, portanto uma edição antiga enviada após outra já concluída pode sobrescrever os campos. Os testes não afirmam impedir esse caso.

A interface bloqueia envios repetidos enquanto uma operação está em andamento e não repete automaticamente escritas após falha. O backend ainda não oferece uma chave de idempotência para a criação do pedido. Duas requisições independentes representam dois pedidos; em caso de falha de rede, consulte a lista antes de reenviar.

O fluxo de status é: cliente cria o pedido → RabbitMQ avisa produção → administrador atualiza a etapa → RabbitMQ publica `producao.atualizada` → Pedido atualiza o status resumido (`EM_PROCESSAMENTO`, `FINALIZADO` ou `CANCELADO`). O consumidor é idempotente para eventos repetidos e o cliente consulta o acompanhamento detalhado da produção; a tela atualiza os dados automaticamente a cada 15 segundos enquanto está aberta. A publicação e a gravação no banco ainda não usam outbox transacional, portanto uma indisponibilidade do broker no momento da publicação pode atrasar a atualização resumida; o acompanhamento detalhado consultado pelo cliente continua refletindo a ordem gravada. O relatório de vendas soma pedidos recebidos por evento, não pagamentos ou receita líquida.

Os resultados locais avaliam correção sob a carga indicada. Não são uma estimativa de capacidade de produção nem um SLA de latência.
