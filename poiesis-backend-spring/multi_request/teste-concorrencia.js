import http from 'k6/http';
import { check, fail, sleep } from 'k6';
import { Counter } from 'k6/metrics';
const BASE = __ENV.BASE_URL || 'http://localhost:8080';
const conflicts = new Counter('conflitos_tratados');
export const options = {
    scenarios: { mesmo_registro: { executor: 'per-vu-iterations', vus: Number(__ENV.VUS || 10), iterations: 1, maxDuration: '2m' } },
    thresholds: { checks: ['rate==1'], http_req_failed: ['rate==0'] },
};
const params = token => ({ headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` } });
export function setup() {
    const login = http.post(`${BASE}/v1/auth/login`, JSON.stringify({ email: __ENV.ADMIN_EMAIL || 'admin@poiesis.com', senha: __ENV.ADMIN_PASSWORD || 'admin123' }), { headers: { 'Content-Type': 'application/json' } });
    if (login.status !== 200) fail(`Login: ${login.status}`);
    const adminToken = login.json('token'), p = params(adminToken);
    const clienteEmail = `k6-concorrencia-${Date.now()}@poiesis.test`;
    const senhaCliente = __ENV.CLIENT_PASSWORD || 'teste-k6-123';
    const cadastro = http.post(`${BASE}/v1/auth/register`, JSON.stringify({ nome: 'Cliente de teste k6', email: clienteEmail, senha: senhaCliente }), { headers: { 'Content-Type': 'application/json' } });
    if (cadastro.status !== 201) fail(`Cadastro cliente: ${cadastro.status}`);
    const loginCliente = http.post(`${BASE}/v1/auth/login`, JSON.stringify({ email: clienteEmail, senha: senhaCliente }), { headers: { 'Content-Type': 'application/json' } });
    if (loginCliente.status !== 200) fail(`Login cliente: ${loginCliente.status}`);
    const clientToken = loginCliente.json('token');
    const produto = http.post(`${BASE}/v1/produtos`, JSON.stringify({ nome: 'Concorrência 10', descricao: '10', precoBase: 10, categoria: 'CAMISETA' }), p);
    if (produto.status !== 201) fail(`Produto: ${produto.status}`);
    const produtoId = produto.json('id');
    const opcao = http.post(`${BASE}/v1/customizacoes`, JSON.stringify({ produtoId, tipo: 'COR', nome: '10', precoAdicional: 10 }), p);
    if (opcao.status !== 201) fail(`Customização: ${opcao.status}`);
    const pedido = http.post(`${BASE}/v1/pedidos`, JSON.stringify({ itens: [{ produtoId, quantidade: 1 }] }), params(clientToken));
    if (pedido.status !== 201) fail(`Pedido: ${pedido.status}`);
    let ordem;
    for (let i = 0; i < 30; i++) {
        const lista = http.get(`${BASE}/v1/producao`, p);
        if (lista.status === 200) ordem = lista.json().find(o => o.pedidoId === pedido.json('id'));
        if (ordem) break;
        sleep(1);
    }
    if (!ordem) fail('Evento não chegou à produção.');
    return { token: adminToken, clientToken, pedidoId: pedido.json('id'), produtoId, opcaoId: opcao.json('id'), ordemId: ordem.id };
}
export default function (data) {
    const value = __VU * 1000 + __ITER + 1;
    const p = { ...params(data.token), responseCallback: http.expectedStatuses(200, 409) };
    const responses = http.batch([
        ['PUT', `${BASE}/v1/produtos/${data.produtoId}`, JSON.stringify({ nome: `Concorrência ${value}`, descricao: String(value), precoBase: value, categoria: 'CAMISETA' }), p],
        ['PUT', `${BASE}/v1/customizacoes/${data.opcaoId}`, JSON.stringify({ produtoId: data.produtoId, tipo: 'COR', nome: String(value), precoAdicional: value }), p],
        ['PATCH', `${BASE}/v1/producao/${data.ordemId}/status`, JSON.stringify({ status: 'APROVADO' }), p],
    ]);
    responses.forEach((response, index) => {
        check(response, { [`escrita ${index}: sucesso ou conflito explícito, sem 500`]: r => r.status === 200 || r.status === 409 });
        conflicts.add(response.status === 409 ? 1 : 0);
    });
}
export function teardown(data) {
    if (!data) return;
    const p = params(data.token);
    const produto = http.get(`${BASE}/v1/produtos/${data.produtoId}`, p);
    check(produto, { 'produto mantém campos da mesma escrita': r => r.status === 200 && r.json('nome') === `Concorrência ${r.json('precoBase')}` && Number(r.json('descricao')) === r.json('precoBase') });
    const opcoes = http.get(`${BASE}/v1/customizacoes/produto/${data.produtoId}`, p);
    check(opcoes, { 'customização mantém campos da mesma escrita': r => r.status === 200 && r.json().some(o => o.id === data.opcaoId && Number(o.nome) === o.precoAdicional) });
    check(http.del(`${BASE}/v1/customizacoes/${data.opcaoId}`, null, p), { 'opção inativada': r => r.status === 204 });
    check(http.del(`${BASE}/v1/produtos/${data.produtoId}`, null, p), { 'produto inativado': r => r.status === 204 });
    let pedido;
    for (let i = 0; i < 30; i++) {
        const response = http.get(`${BASE}/v1/pedidos/${data.pedidoId}`, params(data.clientToken));
        if (response.status === 200) pedido = response.json();
        if (pedido?.status === 'EM_PROCESSAMENTO') break;
        sleep(1);
    }
    check(pedido, { 'status de produção foi propagado ao pedido do cliente': value => value?.status === 'EM_PROCESSAMENTO' });
}
