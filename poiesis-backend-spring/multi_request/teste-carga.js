import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const ADMIN_EMAIL = __ENV.ADMIN_EMAIL || 'admin@poiesis.com';
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'admin123';
const CLIENT_PASSWORD = __ENV.CLIENT_PASSWORD || 'teste-k6-123';
const VUS = Number(__ENV.VUS || 10);
const ITERATIONS = Number(__ENV.ITERATIONS || VUS);
const QUANTIDADE_POR_PEDIDO = Number(__ENV.QUANTIDADE_POR_PEDIDO || 1);

const pedidosCriados = new Counter('pedidos_criados');
const pedidosFalhos = new Counter('pedidos_falhos');
const pedidosPersistidos = new Counter('pedidos_persistidos');

export const options = {
  thresholds: {
    checks: ['rate==1'],
    http_req_failed: ['rate==0'],
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

  const adminToken = login.json('token');
  if (!adminToken) throw new Error('A resposta do login não contém o campo token.');

  const clienteEmail = `k6-${Date.now()}@poiesis.test`;
  const cadastro = http.post(`${BASE_URL}/v1/auth/register`, JSON.stringify({
    nome: 'Cliente de teste k6', email: clienteEmail, senha: CLIENT_PASSWORD,
  }), { headers: { 'Content-Type': 'application/json' } });
  if (cadastro.status !== 201) throw new Error(`Não foi possível criar o cliente de teste (${cadastro.status}).`);
  const loginCliente = http.post(`${BASE_URL}/v1/auth/login`, JSON.stringify({ email: clienteEmail, senha: CLIENT_PASSWORD }), { headers: { 'Content-Type': 'application/json' } });
  if (loginCliente.status !== 200 || !loginCliente.json('token')) throw new Error('Não foi possível autenticar o cliente de teste.');
  const token = loginCliente.json('token');

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
    jsonParams(adminToken),
  );

  if (!check(produto, { 'produto de teste criado (201)': (r) => r.status === 201 })) {
    throw new Error(`Não foi possível preparar o produto para o teste (${produto.status}): ${produto.body}`);
  }

  const produtoId = produto.json('id');
  if (!produtoId) throw new Error('A resposta de criação do produto não contém o campo id.');

  return { token, adminToken, produtoId, totalEsperado: ITERATIONS };
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
    'valor calculado para a quantidade solicitada': (r) => r.status === 201 && Math.round(Number(r.json('valorTotal')) * 100) === 4990 * QUANTIDADE_POR_PEDIDO,
  });

  if (created) pedidosCriados.add(1);
  else {
    pedidosFalhos.add(1);
    console.error(`Falha ao criar pedido (${response.status}): ${response.body}`);
  }
}

export function teardown(data) {
  if (!data?.token || !data?.produtoId) return;

  // A consulta acontece após todas as iterações e verifica o estado persistido,
  // independentemente do status HTTP recebido em cada POST.
  const consulta = http.get(`${BASE_URL}/v1/pedidos`, jsonParams(data.token));
  const consultaOk = check(consulta, {
      'consulta de pedidos do cliente retorna 200': (r) => r.status === 200,
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
      && pedido.itens[0].quantidade === QUANTIDADE_POR_PEDIDO
      && Math.round(Number(pedido.valorTotal) * 100) === 4990 * QUANTIDADE_POR_PEDIDO);

    pedidosPersistidos.add(pedidosDoTeste.length);
    check(pedidosDoTeste, {
      [`${data.totalEsperado} pedidos do produto de teste persistidos`]: (orders) =>
        orders.length === data.totalEsperado,
      'cada pedido persistido tem ID único': (orders) => new Set(orders.map((pedido) => pedido.id)).size === orders.length,
      'itens dos pedidos persistidos estão íntegros': () => estruturaIntegra,
    });
    let ordens = [];
    for (let attempt = 0; attempt < 30; attempt++) {
      const response = http.get(`${BASE_URL}/v1/producao`, jsonParams(data.adminToken));
      if (response.status === 200) ordens = response.json().filter(o => idsUnicos.has(o.pedidoId));
      if (ordens.length >= pedidosDoTeste.length) break;
      sleep(1);
    }
    check(ordens, {
      'cada pedido gera exatamente uma ordem de produção': rows => rows.length === pedidosDoTeste.length && new Set(rows.map(o => o.pedidoId)).size === pedidosDoTeste.length,
    });
    console.log(`Pedidos persistidos: ${pedidosDoTeste.length}/${data.totalEsperado}; IDs únicos: ${idsUnicos.size}.`);
  }

  // A exclusão do catálogo é lógica; os pedidos criados continuam registrados.
  const cleanup = http.del(`${BASE_URL}/v1/produtos/${data.produtoId}`, null, jsonParams(data.adminToken));
  check(cleanup, { 'produto de teste inativado (204)': (r) => r.status === 204 });

  console.log(`Produto temporário ${data.produtoId} inativado. Consulte as métricas pedidos_criados e pedidos_falhos.`);
}
