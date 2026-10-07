import http from 'k6/http';
import { check, fail } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
export const options = {
  scenarios: { customizacoes: { executor: 'shared-iterations', vus: Number(__ENV.VUS || 10), iterations: Number(__ENV.ITERATIONS || 10), maxDuration: '2m' } },
  thresholds: { checks: ['rate==1'], http_req_failed: ['rate==0'] },
};
const params = (token) => ({ headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } });
export function setup() {
  const login = http.post(`${BASE_URL}/v1/auth/login`, JSON.stringify({ email: __ENV.ADMIN_EMAIL || 'admin@poiesis.com', senha: __ENV.ADMIN_PASSWORD || 'admin123' }), { headers: { 'Content-Type': 'application/json' } });
  if (!check(login, { 'login retorna 200': (r) => r.status === 200 })) fail(`Login: ${login.status}`);
  const token = login.json('token');
  const produto = http.post(`${BASE_URL}/v1/produtos`, JSON.stringify({ nome: `Customização k6 ${Date.now()}`, descricao: 'Teste CRUD concorrente', categoria: 'CAMISETA', precoBase: 49.9 }), params(token));
  if (!check(produto, { 'produto criado': (r) => r.status === 201 })) fail(`Produto: ${produto.status} ${produto.body}`);
  return { token, produtoId: produto.json('id') };
}
export default function (data) {
  const p = params(data.token);
  const body = { produtoId: data.produtoId, tipo: 'COR', nome: `Azul ${__VU}-${__ITER}`, precoAdicional: 5 };
  const criada = http.post(`${BASE_URL}/v1/customizacoes`, JSON.stringify(body), p);
  if (!check(criada, { 'customização criada (201)': (r) => r.status === 201 })) { console.error(`${criada.status}: ${criada.body}`); return; }
  const id = criada.json('id');
  const atualizada = http.put(`${BASE_URL}/v1/customizacoes/${id}`, JSON.stringify({ ...body, nome: 'Verde', precoAdicional: 10 }), p);
  check(atualizada, { 'edição preserva ID e preço': (r) => r.status === 200 && r.json('id') === id && r.json('nome') === 'Verde' && Number(r.json('precoAdicional')) === 10 });
  const lista = http.get(`${BASE_URL}/v1/customizacoes/produto/${data.produtoId}`, p);
  check(lista, { 'opção editada persistida': (r) => r.status === 200 && r.json().some((o) => o.id === id && o.nome === 'Verde') });
  const excluida = http.del(`${BASE_URL}/v1/customizacoes/${id}`, null, p);
  check(excluida, { 'exclusão retorna 204': (r) => r.status === 204 });
  const restantes = http.get(`${BASE_URL}/v1/customizacoes/produto/${data.produtoId}`, p);
  check(restantes, { 'opção inativa não aparece': (r) => r.status === 200 && !r.json().some((o) => o.id === id) });
}
export function teardown(data) {
  const response = http.del(`${BASE_URL}/v1/produtos/${data.produtoId}`, null, params(data.token));
  check(response, { 'produto temporário inativado': (r) => r.status === 204 });
}
