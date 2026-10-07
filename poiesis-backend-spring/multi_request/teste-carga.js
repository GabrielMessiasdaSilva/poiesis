import http from 'k6/http';
import { check } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || 'admin@poiesis.com';
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'admin123';
const VUS = Number(__ENV.VUS || 10);
const ITERATIONS = Number(__ENV.ITERATIONS || VUS);
const QUANTIDADE_POR_PEDIDO = Number(__ENV.QUANTIDADE_POR_PEDIDO || 1);

const pedidosCriados = new Counter('pedidos_criados');
const pedidosFalhos = new Counter('pedidos_falhos');
const pedidosPersistidos = new Counter('pedidos_persistidos');

export const options = {
  thresholds: {
    checks: ['rate==1'],
  },
  scenarios: {
    criacao_concorrente_de_pedidos: {
      executor: 'shared-iterations',
      vus: VUS,
      iterations: ITERATIONS,
      maxDuration: '2m',
    },
  },
};

const jsonParams = (token) => ({
  headers: {
    'Content-Type': 'application/json',
    Authorization: `Bearer ${token}`,
  },
});

export function setup() {
  const login = http.post(
    `${BASE_URL}/v1/auth/login`,
    JSON.stringify({ email: ADMIN_EMAIL, senha: ADMIN_PASSWORD }),
    { headers: { 'Content-Type': 'application/json' } },
  );

  if (!check(login, { 'login de administrador retorna 200': (r) => r.status === 200 })) {
    throw new Error(`Falha no login (${login.status}). Confira BASE_URL e as credenciais do administrador.`);
  }

  const token = login.json('token');
  if (!token) throw new Error('A resposta do login não contém o campo token.');

  // O catálogo usa IDs Long gerados pelo banco. Criamos um produto próprio para
  // cada execução, evitando depender de um ID fixo ou de dados previamente cadastrados.
  const produto = http.post(
    `${BASE_URL}/v1/produtos`,
    JSON.stringify({
      nome: `Produto teste carga ${Date.now()}`,
      descricao: 'Produto criado automaticamente pelo teste k6',
      precoBase: 49.9,
      categoria: 'CAMISETA',
    }),
    jsonParams(token),
  );

  if (!check(produto, { 'produto de teste criado (201)': (r) => r.status === 201 })) {
    throw new Error(`Não foi possível preparar o produto para o teste (${produto.status}): ${produto.body}`);
  }

  const produtoId = produto.json('id');
  if (!produtoId) throw new Error('A resposta de criação do produto não contém o campo id.');

  return { token, produtoId, totalEsperado: ITERATIONS };
}

export default function (data) {
  const response = http.post(
    `${BASE_URL}/v1/pedidos`,
    JSON.stringify({
      itens: [{ produtoId: data.produtoId, quantidade: QUANTIDADE_POR_PEDIDO }],
    }),
    jsonParams(data.token),
  );

  const created = check(response, {
    'pedido criado (201)': (r) => r.status === 201,
    'resposta contém ID do pedido': (r) => r.status === 201 && Boolean(r.json('id')),
  });

  if (created) pedidosCriados.add(1);
  else pedidosFalhos.add(1);
}

export function teardown(data) {
  if (!data?.token || !data?.produtoId) return;

  // A consulta acontece após todas as iterações e verifica o estado persistido,
  // independentemente do status HTTP recebido em cada POST.
  const consulta = http.get(`${BASE_URL}/v1/pedidos`, jsonParams(data.token));
  const consultaOk = check(consulta, {
    'consulta administrativa de pedidos retorna 200': (r) => r.status === 200,
  });

  if (consultaOk) {
    const pedidos = consulta.json();
    const pedidosDoTeste = Array.isArray(pedidos)
      ? pedidos.filter((pedido) => Array.isArray(pedido.itens)
        && pedido.itens.some((item) => String(item.produtoId) === String(data.produtoId)))
      : [];
    const idsUnicos = new Set(pedidosDoTeste.map((pedido) => pedido.id));
    const estruturaIntegra = pedidosDoTeste.every((pedido) =>
      pedido.itens.length === 1
      && String(pedido.itens[0].produtoId) === String(data.produtoId)
      && pedido.itens[0].quantidade === QUANTIDADE_POR_PEDIDO);

    pedidosPersistidos.add(pedidosDoTeste.length);
    check(pedidosDoTeste, {
      [`${data.totalEsperado} pedidos do produto de teste persistidos`]: (orders) =>
        orders.length === data.totalEsperado,
      'cada pedido persistido tem ID único': (orders) => new Set(orders.map((pedido) => pedido.id)).size === orders.length,
      'itens dos pedidos persistidos estão íntegros': () => estruturaIntegra,
    });
    console.log(`Pedidos persistidos: ${pedidosDoTeste.length}/${data.totalEsperado}; IDs únicos: ${idsUnicos.size}.`);
  }

  // A exclusão do catálogo é lógica; os pedidos criados continuam registrados.
  const cleanup = http.del(`${BASE_URL}/v1/produtos/${data.produtoId}`, null, jsonParams(data.token));
  check(cleanup, { 'produto de teste inativado (204)': (r) => r.status === 204 });

  console.log(`Produto temporário ${data.produtoId} inativado. Consulte as métricas pedidos_criados e pedidos_falhos.`);
}
