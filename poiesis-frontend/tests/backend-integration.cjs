// Exercises the real TypeScript service clients against a running gateway.
// Replaces platform/storage for Node; --web also supplies a cookie jar for Axios.
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const { createRequire } = require('node:module');
const ts = require('typescript');
const web = process.argv.includes('--web');
const cookies = new Map();
const origin = 'http://localhost:8090';

// Node does not have a browser cookie jar. Share one across both Axios clients
// to exercise the application's cookie/CSRF interceptors against the real gateway.
function browserAxios(axios) {
    return { ...axios, create(options) {
        const client = axios.create(options);
        client.interceptors.request.use(config => {
            config.headers.Origin = origin;
            if (cookies.size) config.headers.Cookie = [...cookies].map(([key, value]) => `${key}=${value}`).join('; ');
            return config;
        });
        function receive(response) {
            for (const header of response?.headers['set-cookie'] ?? []) {
                const [pair] = header.split(';');
                const separator = pair.indexOf('=');
                const key = pair.slice(0, separator), value = pair.slice(separator + 1);
                if (!value || /max-age=0(?:;|$)/i.test(header)) cookies.delete(key);
                else cookies.set(key, value);
                if (key === 'poiesis_session' && value) {
                    assert.match(header, /HttpOnly/i);
                    assert.match(header, /SameSite=Lax/i);
                }
            }
            return response;
        }
        client.interceptors.response.use(receive, error => { receive(error.response); return Promise.reject(error); });
        return client;
    } };
}

if (!process.env.EXPO_PUBLIC_API_URL) throw new Error('Defina EXPO_PUBLIC_API_URL para o gateway usado no teste.');
const root = path.resolve(__dirname, '..');
const cache = new Map();
let token;
function load(relative) {
    const file = path.resolve(root, relative);
    if (cache.has(file)) return cache.get(file).exports;
    const module = { exports: {} };
    cache.set(file, module);
    const nativeRequire = createRequire(file);
    const source = ts.transpileModule(fs.readFileSync(file, 'utf8'), {
        compilerOptions: { module: ts.ModuleKind.CommonJS, esModuleInterop: true },
    }).outputText;
    vm.runInNewContext(source, {
        module, exports: module.exports, process,
        window: { location: { protocol: 'http:', hostname: 'localhost' } },
        require(name) {
            if (name === 'axios' && web) return browserAxios(nativeRequire(name));
            if (name === 'react-native') return { Platform: { OS: web ? 'web' : 'android' } };
            if (name === '@react-native-async-storage/async-storage') return { async getItem() {
                assert.ok(!web, 'A web não deve ler credenciais locais.');
                return token ?? null;
            } };
            if (name.startsWith('.')) return load(path.relative(root, path.resolve(path.dirname(file), name + '.ts')));
            return nativeRequire(name);
        },
    }, { filename: file });
    return module.exports;
}
const api = load('src/services/api.ts').default;
const { produtosService, customizacoesService } = load('src/services/produtos.ts');
const { pedidosService } = load('src/services/pedidos.ts');
const { producaoService, statusProducao } = load('src/services/producao.ts');
const { relatoriosService } = load('src/services/relatorios.ts');
const { mensagemApi } = load('src/services/erros.ts');

async function login(email, senha) {
    const response = await api.post('/v1/auth/login', { email, senha });
    if (web) {
        assert.equal(response.data.token, undefined);
        assert.equal(response.data.tipo, 'Cookie');
        assert.ok(cookies.has('poiesis_session'));
    } else {
        token = response.data.token;
        assert.ok(token);
    }
}

async function main() {
    const marker = Date.now();
    const email = process.env.ADMIN_EMAIL || 'admin@poiesis.com';
    const senha = process.env.ADMIN_PASSWORD || 'admin123';
    if (web) {
        const preflight = await api.options('/v1/auth/login', { headers: {
            'Access-Control-Request-Method': 'POST',
            'Access-Control-Request-Headers': 'content-type,x-auth-mode,x-csrf-token',
        } });
        assert.equal(preflight.headers['access-control-allow-origin'], origin);
        assert.equal(preflight.headers['access-control-allow-credentials'], 'true');
    }
    await login(email, senha);
    const session = (await api.get('/v1/auth/session')).data;
    assert.ok(session.roles.includes('ADMIN'));
    assert.ok(!session.roles.includes('FACTOR_PASSWORD'));
    console.log('OK: cliente HTTP, autenticação e sessão reais.');
    let produto, opcao;
    try {
        const dados = { nome: `Frontend ${marker}`, descricao: 'Validação de integração', precoBase: 30, categoria: 'CAMISETA' };
        produto = await produtosService.cadastrar(dados);
        assert.equal(produto.precoBase, 30);
        assert.equal(produto.ativo, true);
        produto = await produtosService.atualizar(produto.id, { ...dados, nome: `Frontend editado ${marker}` });
        assert.ok((await produtosService.listar()).some(p => p.id === produto.id && p.nome === produto.nome));
        const personalizacao = { produtoId: produto.id, tipo: 'COR', nome: 'Azul', precoAdicional: 5 };
        opcao = await customizacoesService.cadastrar(personalizacao);
        assert.ok((await customizacoesService.listarPorProduto(produto.id)).some(o => o.id === opcao.id));
        assert.ok((await customizacoesService.listarComProdutos()).some(p => p.produto.id === produto.id && p.opcoes.some(o => o.id === opcao.id)));
        await assert.rejects(() => customizacoesService.cadastrar({ ...personalizacao, produtoId: Number.MAX_SAFE_INTEGER }),
            error => error.response?.status === 400 && mensagemApi(error).includes('Produto'));
        console.log('OK: APIs de produtos e customizações, incluindo erro no campo "erro".');

        const pedido = await pedidosService.criar({ itens: [{ produtoId: produto.id, quantidade: 2, customizacaoIds: [opcao.id] }] });
        assert.equal(pedido.valorTotal, 70);
        assert.equal(pedido.itens[0].quantidade, 2);
        assert.equal(pedido.itens[0].customizacoes[0].precoAdicional, 5);
        await customizacoesService.atualizar(opcao.id, { ...personalizacao, nome: 'Verde', precoAdicional: 10 });
        const salvo = (await pedidosService.listar()).find(p => p.id === pedido.id);
        assert.equal(salvo.itens[0].customizacoes[0].nome, 'Azul');
        assert.equal(salvo.valorTotal, 70);
        await assert.rejects(() => pedidosService.criar({ itens: [{ produtoId: Number.MAX_SAFE_INTEGER, quantidade: 1 }] }),
            error => error.response?.status === 400);
        console.log('OK: pedido personalizado, valor calculado pelo servidor e histórico preservado.');

        const now = new Date();
        const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`;
        let ordem, vendas;
        const deadline = Date.now() + 45000;
        do {
            ordem = (await producaoService.listar()).find(o => o.pedidoId === pedido.id);
            vendas = await relatoriosService.consultarVendas(today, today);
            if (ordem && vendas.totalPedidos > 0) break;
            await new Promise(resolve => setTimeout(resolve, 1000));
        } while (Date.now() < deadline);
        assert.ok(ordem, 'A mensagem deve criar uma ordem de produção.');
        assert.ok(vendas.totalPedidos > 0);
        assert.ok(Number.isFinite(vendas.ticketMedio));
        assert.ok(statusProducao.includes('CANCELADO'));
        assert.equal((await producaoService.atualizarStatus(ordem.id, 'CANCELADO')).status, 'CANCELADO');
        const resumo = await relatoriosService.consultarProducao();
        for (const field of ['emPendente', 'emCorte', 'emCostura', 'emAcabamento', 'concluidos']) assert.ok(Number.isFinite(resumo[field]));
        console.log('OK: evento real, alteração de produção para CANCELADO e relatórios.');

        const cliente = { nome: 'Frontend USER', email: `frontend-${marker}@example.com`, senha: 'Frontend123!' };
        await api.post('/v1/auth/register', cliente);
        await api.post('/v1/auth/logout');
        await login(cliente.email, cliente.senha);
        assert.ok(!(await pedidosService.listar()).some(p => p.id === pedido.id));
        await assert.rejects(() => producaoService.listar(), error => error.response?.status === 403);
        await assert.rejects(() => produtosService.cadastrar(dados), error => error.response?.status === 403);
        await assert.rejects(() => customizacoesService.cadastrar(personalizacao), error => error.response?.status === 403);
        console.log('OK: permissões ADMIN/USER e acesso aos próprios pedidos.');
    } finally {
        await api.post('/v1/auth/logout');
        await login(email, senha);
        const cleanup = await Promise.allSettled([
            opcao ? customizacoesService.inativar(opcao.id) : Promise.resolve(),
            produto ? produtosService.inativar(produto.id) : Promise.resolve(),
        ]);
        for (const result of cleanup) if (result.status === 'rejected') throw result.reason;
    }
    await api.post('/v1/auth/logout');
    if (web) assert.ok(!cookies.has('poiesis_session'), 'Logout deve expirar o cookie.');
    await assert.rejects(() => api.get('/v1/auth/session'), error => error.response?.status === 401);
    console.log('OK: logout e revogação. Usuário e pedido de teste ficam registrados no backend usado.');
}
main().catch(error => {
    // Never print Axios config/headers: they may contain the test access token.
    console.error(`Falha na integração: ${error.message}; status HTTP: ${error.response?.status ?? 'sem resposta'}`);
    process.exitCode = 1;
});
