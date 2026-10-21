// Run against the real local frontend/backend. Requires Playwright and a Chromium browser.
const { chromium } = require(process.env.PLAYWRIGHT_MODULE || 'playwright');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const base = process.env.FRONTEND_URL || 'http://127.0.0.1:8090';
const output = process.env.UI_ARTIFACTS || '/tmp/poiesis-ui-results';
fs.mkdirSync(output, { recursive: true });
const marker = Date.now();
async function visible(locator) { await locator.waitFor({ state: 'visible', timeout: 25000 }); }
async function noOverflow(page) {
    const result = await page.evaluate(() => ({ width: window.innerWidth, scroll: document.documentElement.scrollWidth }));
    assert.ok(result.scroll <= result.width + 1, `Horizontal overflow: ${JSON.stringify(result)}`);
}
async function main() {
    const browser = await chromium.launch({ executablePath: process.env.BROWSER_PATH || (fs.existsSync('/opt/brave.com/brave/brave') ? '/opt/brave.com/brave/brave' : undefined), headless: true, args: ['--no-sandbox', '--disable-dev-shm-usage'] });
    const errors = [];
    try {
        const admin = await browser.newContext({ viewport: { width: 1440, height: 1050 } });
        const page = await admin.newPage();
        console.log('Abrindo login e cadastro administrativo.');
        page.on('pageerror', e => errors.push(e.message));
        await page.goto(base + '/login');
        await visible(page.getByRole('button', { name: 'Entrar na minha conta', exact: true }));
        await page.screenshot({ path: output + '/login-desktop.png', fullPage: true });
        await page.getByRole('textbox', { name: 'E-mail', exact: true }).fill(process.env.ADMIN_EMAIL || 'admin@poiesis.com');
        await page.getByLabel('Senha', { exact: true }).fill(process.env.ADMIN_PASSWORD || 'admin123');
        await page.getByRole('button', { name: 'Entrar na minha conta', exact: true }).click();
        await visible(page.getByText('Vista a sua essência.', { exact: true }));
        await page.getByRole('tab', { name: 'Administração' }).click();
        await page.getByRole('button', { name: 'Cadastrar produto', exact: true }).click();
        await page.getByRole('textbox', { name: 'Nome', exact: true }).fill(`Camiseta Essência ${marker}`);
        await page.getByRole('textbox', { name: 'Descrição', exact: true }).fill('Uma base leve para os detalhes que contam a sua história.');
        await page.getByRole('textbox', { name: 'Preço base (R$)', exact: true }).fill('79,90');
        await page.getByRole('button', { name: 'Salvar', exact: true }).click();
        await visible(page.getByText('Registro salvo com sucesso.', { exact: true }));
        await page.getByRole('button', { name: 'Customizações', exact: true }).click();
        await page.getByRole('button', { name: `Camiseta Essência ${marker}`, exact: true }).click();
        await page.getByRole('button', { name: 'Cadastrar customização', exact: true }).click();
        await page.getByRole('textbox', { name: 'Nome', exact: true }).fill('Bordado terracota');
        await page.getByRole('textbox', { name: 'Tipo (ex.: COR, ESTAMPA)', exact: true }).fill('BORDADO');
        await page.getByRole('textbox', { name: 'Preço adicional (R$)', exact: true }).fill('12,50');
        await page.getByRole('button', { name: 'Salvar', exact: true }).click();
        await visible(page.getByText('Registro salvo com sucesso.', { exact: true }));
        await page.getByRole('tab', { name: /Catálogo/ }).click();
        await visible(page.getByRole('heading', { name: `Camiseta Essência ${marker}`, exact: true }));
        await noOverflow(page);
        await page.screenshot({ path: output + '/catalogo-desktop.png', fullPage: true });

        console.log('Catálogo administrativo conferido. Iniciando cliente.');
        const client = await browser.newContext({ viewport: { width: 390, height: 844 } });
        const mobile = await client.newPage();
        mobile.on('pageerror', e => errors.push(e.message));
        await mobile.goto(base + '/cadastro');
        await visible(mobile.getByRole('button', { name: 'Criar minha conta', exact: true }));
        await noOverflow(mobile);
        await mobile.screenshot({ path: output + '/cadastro-mobile.png', fullPage: true });
        await mobile.getByRole('textbox', { name: 'Nome completo', exact: true }).fill('Cliente de validação');
        await mobile.getByRole('textbox', { name: 'E-mail', exact: true }).fill(`ui-${marker}@example.com`);
        await mobile.getByLabel('Senha', { exact: true }).fill('Validacao123!');
        await mobile.getByLabel('Confirmar senha', { exact: true }).fill('Validacao123!');
        await mobile.getByRole('button', { name: 'Criar minha conta', exact: true }).click();
        await mobile.getByRole('link', { name: 'Entrar na minha conta', exact: true }).click();
        await mobile.getByRole('textbox', { name: 'E-mail', exact: true }).fill(`ui-${marker}@example.com`);
        await mobile.getByLabel('Senha', { exact: true }).fill('Validacao123!');
        await mobile.getByRole('button', { name: 'Entrar na minha conta', exact: true }).click();
        await visible(mobile.getByRole('heading', { name: `Camiseta Essência ${marker}`, exact: true }));
        assert.equal(await mobile.getByRole('tab', { name: /Gestão|Administração/ }).count(), 0);
        await mobile.getByRole('textbox', { name: 'Encontre sua próxima peça', exact: true }).fill(`Camiseta Essência ${marker}`);
        await mobile.getByRole('button', { name: 'Escolher detalhes', exact: true }).click();
        await mobile.getByRole('checkbox', { name: /Bordado terracota/ }).click();
        await mobile.getByRole('button', { name: `Aumentar quantidade de Camiseta Essência ${marker}`, exact: true }).click();
        await mobile.getByRole('button', { name: /Revisar pedido/ }).click();
        await visible(mobile.getByText('Total estimado: R$ 184,80', { exact: true }));
        await noOverflow(mobile);
        await mobile.screenshot({ path: output + '/revisao-mobile.png', fullPage: true });
        let posts = 0;
        mobile.on('request', req => { if (req.method() === 'POST' && req.url().endsWith('/v1/pedidos')) posts++; });
        await mobile.getByRole('button', { name: 'Confirmar pedido', exact: true }).dblclick();
        await visible(mobile.getByRole('button', { name: 'Acompanhar pedido', exact: true }));
        assert.equal(posts, 1, 'Duplo clique deve criar apenas um pedido.');
        await mobile.getByRole('button', { name: 'Acompanhar pedido', exact: true }).click();
        await visible(mobile.getByText('BORDADO: Bordado terracota · + R$ 12,50 por unidade', { exact: true }));
        await mobile.reload();
        await visible(mobile.getByText('Seus pedidos.', { exact: true }));
        await noOverflow(mobile);
        await mobile.screenshot({ path: output + '/pedidos-mobile.png', fullPage: true });
        for (const width of [360, 768, 1440]) {
            await mobile.setViewportSize({ width, height: 900 });
            await mobile.getByRole('tab', { name: /Catálogo/ }).click();
            await visible(mobile.getByRole('heading', { name: `Camiseta Essência ${marker}`, exact: true }));
            await noOverflow(mobile);
            await mobile.screenshot({ path: `${output}/catalogo-${width}.png`, fullPage: true });
        }
        await page.getByRole('tab', { name: 'Administração' }).click();
        await page.getByRole('button', { name: 'Produção', exact: true }).click();
        await visible(page.getByText(`ui-${marker}@example.com`, { exact: true }));
        const ordem = page.getByText(`ui-${marker}@example.com`, { exact: true }).locator('..');
        await ordem.getByRole('button', { name: 'EM CORTE', exact: true }).click();
        await visible(page.getByText('Status atualizado com sucesso.', { exact: true }));
        await page.getByRole('button', { name: 'Relatórios', exact: true }).click();
        await page.getByRole('button', { name: 'Consultar relatórios', exact: true }).click();
        await visible(page.getByText('Produção agora', { exact: true }));
        await noOverflow(page);
        await page.screenshot({ path: output + '/relatorios-desktop.png', fullPage: true });
        await page.setViewportSize({ width: 360, height: 900 });
        await noOverflow(page);
        await page.screenshot({ path: output + '/relatorios-mobile.png', fullPage: true });
        await page.getByRole('button', { name: 'Produtos', exact: true }).click();
        await page.getByRole('button', { name: 'Cadastrar produto', exact: true }).click();
        await noOverflow(page);
        await page.screenshot({ path: output + '/gestao-mobile.png', fullPage: true });
        await mobile.getByRole('tab', { name: /Minha conta/ }).click();
        await mobile.getByRole('button', { name: 'Sair da conta', exact: true }).click();
        await visible(mobile.getByRole('button', { name: 'Entrar na minha conta', exact: true }));
        await mobile.goto(base + '/administracao');
        await visible(mobile.getByRole('button', { name: 'Entrar na minha conta', exact: true }));
        assert.deepEqual(errors, [], 'Não deve haver erros JavaScript no navegador.');
        console.log('OK: navegador real, cadastro, login, catálogo, customizações, pedido único no duplo clique, histórico, restauração da sessão, produção, relatórios, logout e responsividade 360/390/768/1440.');
    } catch (error) {
        for (const [index, context] of browser.contexts().entries()) {
            const p = context.pages()[0];
            if (p) { await p.screenshot({ path: `${output}/falha-${index}.png` }); console.log((await p.getByRole('tab').allTextContents()).join(' | ')); }
        }
        throw error;
    } finally { await browser.close(); }
}
main().catch(error => { console.error(error.message); process.exitCode = 1; });
