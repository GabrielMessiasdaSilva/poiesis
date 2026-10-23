# Poiesis Front-end

Aplicativo mobile-first para personalização de camisetas, desenvolvido com Expo, React Native, TypeScript e Expo Router. O aplicativo consome as APIs do backend Spring Boot da Poiesis pelo gateway.

## Requisitos

- Node.js 22.13 ou superior.
- npm.
- Para executar em dispositivo ou simulador, Expo Go ou um development build compatível.

## Instalar e executar

```bash
npm install
npm start
```

Para abrir diretamente no navegador:

```bash
npm run web
```

O terminal do Expo também permite abrir o aplicativo em um emulador Android, simulador iOS ou dispositivo com Expo Go.

Os scripts iniciam o Expo na porta `8090`, pois as portas `8080` a `8085` são usadas pelo gateway e pelos microsserviços. No navegador, acesse `http://localhost:8090`. Para usar a porta 8086, execute `npx expo start --web --port 8086`; o gateway também aceita `http://localhost:8086` e `http://127.0.0.1:8086`. Depois de alterar o CORS, recompile e reinicie o gateway. Se `POIESIS_CORS_ALLOWED_ORIGINS` estiver definida, ela substitui os padrões: inclua as origens de 8086 nessa variável.

## Estrutura

```text
app/
   _layout.tsx              # Sessao e navegacao raiz
   (auth)/login.tsx          # Entrada
   (tabs)/_layout.tsx        # Navegacao por abas
   (tabs)/index.tsx          # Catálogo e criação de pedidos
   (tabs)/customizacoes.tsx  # Opções ativas de customização
   (tabs)/pedidos.tsx        # Pedidos do usuário autenticado
src/
   contexts/AuthContext.tsx  # Estado de autenticacao
   services/api.ts           # Cliente HTTP Axios
   aoi.ts                    # Reexport legado do cliente HTTP
```

As telas ficam em `app/`; código compartilhado fica em `src/`.

## Integração com o backend

No navegador, o app autentica em `POST /v1/auth/login` usando `X-Auth-Mode: cookie`. O servidor cria `poiesis_session` com `HttpOnly`, `SameSite=Lax`, caminho `/v1` e validade igual à sessão. Axios envia os cookies com `withCredentials`; o JWT e o usuário não são persistidos no armazenamento local da web. O perfil é obtido em `/v1/auth/session` e mantido em memória. Nas versões nativas, permanece o transporte Bearer. O catálogo lê `GET /v1/produtos`; a aba Customizações lista as opções ativas de `GET /v1/customizacoes/produto/{produtoId}`. A criação de pedido permite escolher a quantidade, revisar os detalhes e confirmar o envio do produto selecionado a `POST /v1/pedidos`, e a aba Pedidos consulta `GET /v1/pedidos`.

No catálogo, o cliente seleciona uma opção por tipo de customização. O pedido envia os IDs selecionados, e o backend calcula o total e preserva as escolhas no histórico. Campos livres de arte e tamanho não fazem parte do contrato atual. Consulte [FRONTEND_HANDOFF.md](FRONTEND_HANDOFF.md) para os limites conhecidos.

## URL do backend

Sem configuração, o cliente usa `http://10.0.2.2:8080` no emulador Android e `http://localhost:8080` nas demais plataformas. Configure a URL base do gateway sem acrescentar `/api`. No PowerShell:

```powershell
$env:EXPO_PUBLIC_API_URL = "http://localhost:8080"
npm start
```

Em um dispositivo físico, use o endereço IP do computador na rede local, por exemplo `http://192.168.0.10:8080`. Inicie os seis serviços do backend com `bash poiesis-backend-spring/iniciar-projeto.sh` a partir da raiz do repositório. No Windows, siga [EXECUTAR-WINDOWS.md](../poiesis-backend-spring/EXECUTAR-WINDOWS.md). Produto reúne catálogo e customizações na porta 8082; todas as chamadas do app passam pelo gateway na porta 8080. Variáveis com prefixo `EXPO_PUBLIC_` são incorporadas ao aplicativo; nunca coloque senhas, tokens ou outras credenciais nelas.

## Validacoes

```bash
npx expo lint
npx tsc --noEmit
```

Na última validação, `npx expo lint` e `npx tsc --noEmit` passaram. Execute-os novamente após alterações, pois novos problemas podem surgir.

## Expo e codigo nativo

Antes de alterar APIs do Expo ou React Native, consulte `AGENTS.md` e a documentação correspondente à versão instalada do Expo. As pastas `ios/` e `android/` são geradas pelo Continuous Native Generation e não devem ser editadas manualmente.


## Verificar o cookie no navegador

1. Recompile/reinicie o backend e reinicie o frontend após esta atualização.
2. Abra a aplicação usando o mesmo hostname da API: `localhost` com `localhost`, ou `127.0.0.1` com `127.0.0.1`. A URL web padrão da API acompanha o hostname da página na porta 8080.
3. Faça um novo login e abra **F12 → Application (Aplicativo) → Storage → Cookies**. Confira `poiesis_session` no host usado pela API. Em Network, a resposta do login contém `Set-Cookie`; nas requisições seguintes, o navegador envia `Cookie`.
4. Recarregue a página: a sessão será recuperada pelo servidor, sem ler JWT do Local Storage.
5. Ao sair, a sessão é revogada e o servidor expira `poiesis_session`. Credenciais antigas em `@Poiesis:token` e `@Poiesis:user` são removidas automaticamente na primeira carga web.

O cookie de sessão é `HttpOnly`: aparece no painel Cookies, mas não é lido por `document.cookie`. Há também `poiesis_csrf`, usado na proteção das operações que alteram dados. Antes de cada escrita, o cliente obtém o token atual em `/v1/auth/csrf` e o envia em `X-CSRF-TOKEN`. O valor fica somente em memória e não é reutilizado em escritas posteriores, pois a autenticação pode renovar o cookie no servidor.

Para HTTPS, defina `POIESIS_COOKIE_SECURE=true` no backend. As origens web permitidas são configuradas em `POIESIS_CORS_ALLOWED_ORIGINS`, como uma lista de URLs exatas separadas por vírgula. Em HTTP local, `Secure` permanece desativado. Se usar `EXPO_PUBLIC_API_URL`, mantenha o mesmo site da página para que `SameSite=Lax` permita o envio dos cookies.

Para executar os testes do transporte de sessão: `npm run test:session`. As verificações do frontend continuam sendo `npm run lint` e `npx tsc --noEmit`.

## Testar os clientes contra o backend

Com os seis serviços e RabbitMQ disponíveis, execute na pasta do frontend:

```bash
EXPO_PUBLIC_API_URL=http://localhost:8080 npm run test:backend
EXPO_PUBLIC_API_URL=http://localhost:8080 npm run test:backend:web
```

No PowerShell, defina `$env:EXPO_PUBLIC_API_URL = "http://localhost:8080"` antes de executar os mesmos scripts npm.

Os testes usam os serviços TypeScript reais para autenticação, produtos, customizações, pedidos, produção, relatórios e logout. O segundo verifica cookies HttpOnly, CSRF e preflight CORS usando um armazenamento de cookies em Node; a conferência visual no navegador continua sendo uma etapa separada. Por padrão, usam o administrador local; `ADMIN_EMAIL` e `ADMIN_PASSWORD` permitem outra conta. Criam usuário e pedido de teste, e inativam os produtos e opções criados. Execute em ambiente de desenvolvimento.

## Interface e validação no navegador

A interface usa creme, marrom e caramelo, com componentes compartilhados em `src/ui`. No desktop, a navegação fica na lateral e o catálogo ocupa duas colunas; no celular, a navegação fica na base. O catálogo permite buscar, filtrar, selecionar detalhes, ajustar a quantidade e revisar antes de confirmar. Os valores definitivos vêm do backend. O fluxo continua sendo produção sob demanda, sem saldo ou reserva de estoque.

`tests/browser-flows.cjs` valida as telas com Playwright contra serviços reais. Requer o pacote `playwright` disponível (ou `PLAYWRIGHT_MODULE` apontando para sua instalação), um navegador Chromium (`BROWSER_PATH`) e `FRONTEND_URL` (padrão `http://127.0.0.1:8090`). Os dados usados são de teste; execute em ambiente isolado. A variável `UI_ARTIFACTS` escolhe a pasta das capturas. O teste cobre cadastro, administração do catálogo, personalização, quantidade, duplo clique, pedidos, sessão, produção, relatórios, logout e larguras 360, 390, 768 e 1440.

Os cenários de carga e disputa de edições estão documentados em [multi_request/README.md](../poiesis-backend-spring/multi_request/README.md).
