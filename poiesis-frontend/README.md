# Poiesis Front-end

Aplicativo mobile-first para personalização de camisetas, desenvolvido com Expo, React Native, TypeScript e Expo Router. O projeto está sendo preparado para integrar com o backend Spring Boot da Poiesis.

## Requisitos

- Node.js 22.13 ou superior.
- npm.
- Para executar em dispositivo ou simulador, Expo Go ou um development build compatível.

## Instalar e executar

```bash
npm install
npx expo start
```

Para abrir diretamente no navegador:

```bash
npx expo start --web
```

O terminal do Expo também permite abrir o aplicativo em um emulador Android, simulador iOS ou dispositivo com Expo Go.

Os scripts iniciam o Expo na porta `8090`, pois as portas `8080` a `8086` são usadas pelo gateway e pelos microsserviços. No navegador, acesse `http://localhost:8090`.

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

O app autentica em `POST /v1/auth/login`, persiste o JWT retornado e envia `Authorization: Bearer ...` nas rotas protegidas. O catálogo lê `GET /v1/produtos`; a aba Customizações lista as opções ativas de `GET /v1/customizacoes/produto/{produtoId}`. A criação de pedido envia uma unidade do produto selecionado a `POST /v1/pedidos`, e a aba Pedidos consulta `GET /v1/pedidos`.

O backend atual não associa cor/tamanho nem arte a um pedido. Por isso, o app não apresenta esses campos como se fossem persistidos. A rota de customizações gerencia opções disponíveis por produto, não peças criadas pelo cliente. Consulte [FRONTEND_HANDOFF.md](FRONTEND_HANDOFF.md) para os limites conhecidos.

## URL do backend

Sem configuração, o cliente usa `http://10.0.2.2:8080` no emulador Android e `http://localhost:8080` nas demais plataformas. Configure a URL base do gateway sem acrescentar `/api`. No PowerShell:

```powershell
$env:EXPO_PUBLIC_API_URL = "http://localhost:8080"
npx expo start
```

Em um dispositivo físico, use o endereço IP do computador na rede local, por exemplo `http://192.168.0.10:8080`. Inicie os serviços do backend seguindo `../poiesis-backend-spring/README-POSTMAN.md`. Variáveis com prefixo `EXPO_PUBLIC_` são incorporadas ao aplicativo; nunca coloque senhas, tokens ou outras credenciais nelas.

## Validacoes

```bash
npx expo lint
npx tsc --noEmit
```

Na última validação, `npx expo lint` e `npx tsc --noEmit` passaram. Execute-os novamente após alterações, pois novos problemas podem surgir.

## Expo e codigo nativo

Antes de alterar APIs do Expo ou React Native, consulte `AGENTS.md` e a documentação correspondente à versão instalada do Expo. As pastas `ios/` e `android/` são geradas pelo Continuous Native Generation e não devem ser editadas manualmente.
