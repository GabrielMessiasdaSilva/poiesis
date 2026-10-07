"""Valida o fluxo pelo gateway ativo; cria dados identificados de teste."""
import datetime
import json
import os
import time
import urllib.error
import urllib.request
from decimal import Decimal

BASE = os.getenv('BASE_URL', 'http://localhost:8080').rstrip('/')


def request(method, path, body=None, token=None, expected=200):
    headers = {'Content-Type': 'application/json', 'Origin': 'http://localhost:8087'}
    if token:
        headers['Authorization'] = f'Bearer {token}'
    req = urllib.request.Request(BASE + path, data=json.dumps(body).encode() if body is not None else None,
                                 headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=20) as response:
            status, raw = response.status, response.read()
            assert response.headers.get('Access-Control-Allow-Origin') == 'http://localhost:8087', f'CORS ausente em {path}' 
    except urllib.error.HTTPError as error:
        status, raw = error.code, error.read()
    assert status == expected, f'{method} {path}: esperado {expected}, recebido {status}'
    return json.loads(raw) if raw else None


def main():
    preflight = urllib.request.Request(BASE + '/v1/auth/register', method='OPTIONS', headers={
        'Origin': 'http://localhost:8087', 'Access-Control-Request-Method': 'POST',
        'Access-Control-Request-Headers': 'content-type,authorization'})
    with urllib.request.urlopen(preflight, timeout=20) as response:
        assert response.status == 200
        assert response.headers.get('Access-Control-Allow-Origin') == 'http://localhost:8087'
    print('OK: preflight CORS do cadastro na porta 8087', flush=True)
    marker = str(time.time_ns())
    admin = request('POST', '/v1/auth/login', {
        'email': os.getenv('ADMIN_EMAIL', 'admin@poiesis.com'),
        'senha': os.getenv('ADMIN_PASSWORD', 'admin123')})['token']
    email = f'validacao-{marker}@example.com'
    password = 'Validacao123!'
    request('POST', '/v1/auth/register', {'nome': 'Validação integrada', 'email': email, 'senha': password}, expected=201)
    user = request('POST', '/v1/auth/login', {'email': email, 'senha': password})['token']
    request('GET', '/v1/auth/session', token=user)
    print('OK: cadastro, login e sessão', flush=True)
    product = option = None
    try:
        data = {'nome': f'Validação {marker}', 'descricao': 'Teste do fluxo sem estoque', 'precoBase': 49.9, 'categoria': 'CAMISETA'}
        product = request('POST', '/v1/produtos', data, admin, 201)['id']
        data['precoBase'] = 59.9
        edited = request('PUT', f'/v1/produtos/{product}', data, admin)
        assert edited['id'] == product and Decimal(str(edited['precoBase'])) == Decimal('59.9')
        assert any(p['id'] == product for p in request('GET', '/v1/produtos'))
        custom = {'produtoId': product, 'tipo': 'COR', 'nome': 'Azul de teste', 'precoAdicional': 5}
        option = request('POST', '/v1/customizacoes', custom, admin, 201)['id']
        custom['nome'] = 'Verde de teste'
        assert request('PUT', f'/v1/customizacoes/{option}', custom, admin)['id'] == option
        assert any(o['id'] == option and o['nome'] == custom['nome'] for o in request('GET', f'/v1/customizacoes/produto/{product}', token=user))
        request('PUT', f'/v1/produtos/{product}', data, user, 403)
        print('OK: catálogo, edição e opções de customização; permissão ADMIN', flush=True)
        order = request('POST', '/v1/pedidos', {'itens': [{'produtoId': product, 'quantidade': 2, 'customizacaoIds': [option]}]}, user, 201)
        assert Decimal(str(order['valorTotal'])) == Decimal('129.8')
        saved = request('GET', f'/v1/pedidos/{order["id"]}', token=user)
        assert saved['itens'][0]['quantidade'] == 2 and saved['clienteEmail'] == email
        snapshot = saved['itens'][0]['customizacoes'][0]
        assert snapshot['id'] == option and snapshot['nome'] == 'Verde de teste'
        assert Decimal(str(snapshot['precoAdicional'])) == Decimal('5')
        custom.update(nome='Opção alterada depois da compra', precoAdicional=30)
        request('PUT', f'/v1/customizacoes/{option}', custom, admin)
        assert request('GET', f'/v1/pedidos/{order["id"]}', token=user)['itens'][0]['customizacoes'][0] == snapshot
        request('POST', '/v1/pedidos', {'itens': [{'produtoId': product, 'quantidade': 1, 'customizacaoIds': [option, option]}]}, user, 400)
        request('POST', '/v1/pedidos', {'itens': [{'produtoId': product, 'quantidade': 1, 'customizacaoIds': [999999999]}]}, user, 400)
        assert any(p['id'] == order['id'] for p in request('GET', '/v1/pedidos', token=user))
        request('POST', '/v1/pedidos', {'itens': [{'produtoId': product, 'quantidade': 0}]}, user, 400)
        print('OK: pedido personalizado persistido, adicionais e histórico preservado após edição', flush=True)
        deadline = time.monotonic() + 45
        production = None
        today = datetime.date.today().isoformat()
        while time.monotonic() < deadline:
            production = next((p for p in request('GET', '/v1/producao', token=admin) if p['pedidoId'] == order['id']), None)
            sales = request('GET', f'/v1/relatorios/vendas?dataInicio={today}&dataFim={today}', token=admin)
            if production and sales['totalPedidos'] >= 1:
                break
            time.sleep(2)
        assert production, 'Evento do pedido não chegou à produção em 45s'
        assert sales['totalPedidos'] >= 1, 'Relatório de vendas não recebeu o evento'
        changed = request('PATCH', f'/v1/producao/{production["id"]}/status', {'status': 'EM_CORTE'}, admin)
        assert changed['status'] == 'EM_CORTE'
        request('GET', '/v1/relatorios/producao', token=admin)
        request('GET', '/v1/producao', token=user, expected=403)
        request('GET', '/v1/relatorios/producao', token=user, expected=403)
        print('OK: evento, produção, atualização de status e relatórios', flush=True)
    finally:
        if option:
            request('DELETE', f'/v1/customizacoes/{option}', token=admin, expected=204)
            if 'order' in locals():
                assert request('GET', f'/v1/pedidos/{order["id"]}', token=user)['itens'][0]['customizacoes'][0] == snapshot
                request('POST', '/v1/pedidos', {'itens': [{'produtoId': product, 'quantidade': 1, 'customizacaoIds': [option]}]}, user, 400)
            assert all(o['id'] != option for o in request('GET', f'/v1/customizacoes/produto/{product}', token=admin))
        if product:
            request('DELETE', f'/v1/produtos/{product}', token=admin, expected=204)
            assert all(p['id'] != product for p in request('GET', '/v1/produtos'))
        print('OK: inativação dos registros de catálogo usados no teste', flush=True)
    print('Fluxo validado. Usuário e pedido de teste permanecem registrados.', flush=True)


if __name__ == '__main__':
    main()
