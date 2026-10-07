#!/usr/bin/env python3
"""Smoke test against isolated jars on ports 18080..18086; does not restart the app.
Build each service with `mvn -f SERVICE/pom.xml test package` before running.
RabbitMQ consumers are disabled; database aggregation is covered by Java tests.
"""
import json
from pathlib import Path
import subprocess
import tempfile
import time
import urllib.error
import urllib.request

ROOT = Path(__file__).resolve().parents[1]
PORTS = dict(gateway=18080, login=18081, catalogo=18082, pedido=18083, relatorio=18084, producao=18085, customizacao=18086)
OPENER = urllib.request.build_opener(urllib.request.ProxyHandler({}))


def request(service, path, token=None, data=None):
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    req = urllib.request.Request(f'http://127.0.0.1:{PORTS[service]}{path}', headers=headers,
                                 data=json.dumps(data).encode() if data is not None else None)
    try:
        with OPENER.open(req, timeout=20) as response:
            body = response.read()
            return response.status, json.loads(body) if body else None
    except urllib.error.HTTPError as error:
        return error.code, None


def main():
    processes, logs = {}, {}
    with tempfile.TemporaryDirectory(prefix='poiesis-smoke-') as temp:
        def start(service):
            jar = ROOT / service / ('target' if service == 'gateway' else 'springframework/target') / (
                'gateway-0.0.1-SNAPSHOT.jar' if service == 'gateway' else 'springframework-0.0.1-SNAPSHOT.jar')
            args = ['java', '-Xmx256m', '-jar', str(jar), f'--server.port={PORTS[service]}',
                    '--spring.rabbitmq.listener.simple.auto-startup=false',
                    '--application.login.url=http://127.0.0.1:18081',
                    '--application.producao.url=http://127.0.0.1:18085',
                    '--spring.jpa.show-sql=false',
                    '--spring.datasource.url=' + (f'jdbc:h2:file:{temp}/auth' if service == 'login' else f'jdbc:h2:mem:smoke_{service}')]
            if service == 'gateway':
                for name, port in PORTS.items():
                    if name != 'gateway':
                        args.append(f'--spring.cloud.discovery.client.simple.instances.{name}-service[0].uri=http://127.0.0.1:{port}')
            log = open(Path(temp) / f'{service}.log', 'ab')
            logs[service] = log
            processes[service] = subprocess.Popen(args, cwd=ROOT, stdout=log, stderr=log)
            deadline = time.monotonic() + 180
            while time.monotonic() < deadline:
                if processes[service].poll() is not None:
                    raise RuntimeError(f'{service} failed to start; inspect the test logs before cleanup.')
                try:
                    request(service, '/v1/auth/session')
                    print(f'{service}: ready', flush=True)
                    return
                except (urllib.error.URLError, TimeoutError):
                    time.sleep(.5)
            raise TimeoutError(service)

        def expect(service, path, status, token=None, data=None):
            actual, body = request(service, path, token, data)
            assert actual == status, f'{service} {path}: expected {status}, received {actual}'
            return body

        try:
            for service in ['login', 'pedido', 'producao', 'relatorio', 'catalogo', 'customizacao', 'gateway']:
                start(service)
            expect('gateway', '/v1/pedidos', 401)
            admin = expect('gateway', '/v1/auth/login', 200, data={'email': 'admin@poiesis.com', 'senha': 'admin123'})['token']
            registered = expect('gateway', '/v1/auth/register', 201, data={
                'nome': 'Smoke', 'email': 'smoke@test.com', 'senha': 'test-password', 'roles': ['ADMIN']})
            assert registered['roles'] == ['USER'], 'Public registration must not allow ADMIN'
            user = expect('gateway', '/v1/auth/login', 200, data={'email': 'smoke@test.com', 'senha': 'test-password'})['token']
            expect('gateway', '/v1/pedidos', 200, user)
            sales = '/v1/relatorios/vendas?dataInicio=2026-10-01&dataFim=2026-10-31'
            for path in [sales, '/v1/relatorios/producao', '/v1/producao']:
                expect('gateway', path, 403, user)
            expect('relatorio', sales, 403, user)
            expect('producao', '/v1/producao/resumo', 403, user)
            expect('catalogo', '/v1/produtos', 403, user, data={})
            summary = expect('gateway', '/v1/relatorios/producao', 200, admin)
            assert summary == {'emPendente': 0, 'emCorte': 0, 'emCostura': 0, 'emAcabamento': 0, 'concluidos': 0}
            assert expect('gateway', sales, 200, admin)['totalPedidos'] == 0
            expect('gateway', '/v1/relatorios/vendas?dataInicio=2026-10-31&dataFim=2026-10-01', 400, admin)
            expect('gateway', '/v1/auth/logout', 204, user, data={})
            for service, path in [('gateway', '/v1/pedidos'), ('pedido', '/v1/pedidos'), ('login', '/v1/auth/session'),
                                  ('catalogo', '/v1/produtos'), ('customizacao', '/v1/customizacoes/produto/1'),
                                  ('relatorio', sales), ('producao', '/v1/producao')]:
                expect(service, path, 401, user)
            print('Roles, HTTP status codes, report forwarding and revocation on all services: PASS', flush=True)
            processes['login'].terminate()
            processes['login'].wait(timeout=30)
            logs['login'].close()
            start('login')
            expect('pedido', '/v1/pedidos', 401, user)
            expect('login', '/v1/auth/session', 401, user)
            fresh = expect('gateway', '/v1/auth/login', 200, data={'email': 'smoke@test.com', 'senha': 'test-password'})['token']
            expect('gateway', '/v1/pedidos', 200, fresh)
            print('Revocation survives login restart; fresh login works: PASS', flush=True)
        finally:
            for process in processes.values():
                if process.poll() is None:
                    process.terminate()
            for process in processes.values():
                try:
                    process.wait(timeout=30)
                except subprocess.TimeoutExpired:
                    process.kill()
                    process.wait()
            for log in logs.values():
                log.close()


if __name__ == '__main__':
    main()
