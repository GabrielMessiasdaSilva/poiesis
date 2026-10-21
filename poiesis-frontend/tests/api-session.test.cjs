const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const vm = require('node:vm');
const ts = require('typescript');

function loadApi(platform = 'web') {
    const clients = [];
    let storageReads = 0;
    let csrfRequests = 0;
    const axios = {
        create(options) {
            const client = {
                options,
                interceptors: {
                    request: { use(handler) { client.request = handler; } },
                    response: { use(_success, failure) { client.failure = failure; } },
                },
                async get() { csrfRequests++; return { data: { token: `csrf-${csrfRequests}` } }; },
            };
            clients.push(client);
            return client;
        },
    };
    const source = fs.readFileSync(path.join(__dirname, '../src/services/api.ts'), 'utf8');
    const { outputText } = ts.transpileModule(source, {
        compilerOptions: { module: ts.ModuleKind.CommonJS, esModuleInterop: true },
    });
    const module = { exports: {} };
    vm.runInNewContext(outputText, {
        module, exports: module.exports,
        process: { env: {} },
        window: { location: { protocol: 'http:', hostname: '127.0.0.1' } },
        require(name) {
            if (name === 'axios') return axios;
            if (name === 'react-native') return { Platform: { OS: platform } };
            if (name === '@react-native-async-storage/async-storage') return {
                async getItem() { storageReads++; return 'native-token'; },
            };
            throw new Error(`Unexpected dependency: ${name}`);
        },
    });
    return { api: clients[0], session: module.exports,
        storageReads: () => storageReads, csrfRequests: () => csrfRequests };
}

test('web sends cookies and CSRF without reading local credentials', async () => {
    const context = loadApi();
    assert.equal(context.api.options.withCredentials, true);
    assert.equal(context.api.options.baseURL, 'http://127.0.0.1:8080');
    const login = await context.api.request({ url: '/v1/auth/login', method: 'post', headers: {} });
    assert.equal(login.headers['X-Auth-Mode'], 'cookie');
    assert.equal(login.headers['X-CSRF-TOKEN'], 'csrf-1');
    assert.equal(login.headers.Authorization, undefined);
    const read = await context.api.request({ url: '/v1/auth/session', method: 'get', headers: {} });
    assert.equal(read.headers.Authorization, undefined);
    const order = await context.api.request({ url: '/v1/pedidos', method: 'post', headers: {} });
    assert.equal(order.headers['X-CSRF-TOKEN'], 'csrf-2');
    assert.equal(context.csrfRequests(), 2);
    assert.equal(context.storageReads(), 0);
});

test('concurrent writes share only the CSRF request in flight', async () => {
    const context = loadApi();
    const [first, second] = await Promise.all([
        context.api.request({ url: '/v1/produtos', method: 'post', headers: {} }),
        context.api.request({ url: '/v1/customizacoes', method: 'post', headers: {} }),
    ]);
    assert.equal(first.headers['X-CSRF-TOKEN'], second.headers['X-CSRF-TOKEN']);
    assert.equal(context.csrfRequests(), 1);
    const next = await context.api.request({ url: '/v1/pedidos', method: 'post', headers: {} });
    assert.equal(next.headers['X-CSRF-TOKEN'], 'csrf-2');
});

test('old 401 responses preserve a newer login and 403 preserves the session', async () => {
    const context = loadApi();
    let ended = 0;
    context.session.setUnauthorizedHandler(async () => { ended++; });
    const oldRequest = await context.api.request({ url: '/v1/auth/session', method: 'get', headers: {} });
    context.session.advanceSessionRevision();
    await assert.rejects(context.api.failure({ response: { status: 401 }, config: oldRequest }));
    assert.equal(ended, 0);
    const current = await context.api.request({ url: '/v1/pedidos', method: 'post', headers: {} });
    await assert.rejects(context.api.failure({ response: { status: 403 }, config: current }));
    assert.equal(ended, 0);
    await context.api.request({ url: '/v1/pedidos', method: 'post', headers: {} });
    assert.equal(context.csrfRequests(), 2);
    await assert.rejects(context.api.failure({ response: { status: 401 }, config: current }));
    assert.equal(ended, 1);
});

test('native clients retain Bearer transport without browser CSRF', async () => {
    const context = loadApi('android');
    assert.equal(context.api.options.withCredentials, false);
    const request = await context.api.request({ url: '/v1/pedidos', method: 'post', headers: {} });
    assert.equal(request.headers.Authorization, 'Bearer native-token');
    assert.equal(request.headers['X-CSRF-TOKEN'], undefined);
    assert.equal(context.csrfRequests(), 0);
});
