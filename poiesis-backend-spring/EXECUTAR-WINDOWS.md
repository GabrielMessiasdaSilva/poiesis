# Apresentação no Windows

O backend possui um iniciador para Windows PowerShell 5.1 ou superior, chamado pelo arquivo `iniciar-projeto.cmd`. Não precisa de Bash, WSL ou Git Bash. O script Linux continua disponível.

## Preparação

Copie o projeto completo para o Windows. Instale/configure **JDK 21** e **Maven** no PATH e confira no terminal:

```powershell
java -version
mvn -version
```

A primeira compilação precisa de internet para baixar as dependências Maven. O fluxo de pedidos, produção e relatórios também precisa de acesso ao CloudAMQP configurado no projeto, inclusive quando você inicia sem recompilar. Prepare e teste essa conexão antes da apresentação.

Abra o PowerShell ou o Prompt de Comando dentro da pasta `poiesis-backend-spring`:

```powershell
.\iniciar-projeto.cmd
```

O comando compila as seis aplicações e as inicia em segundo plano, usando as portas 8080 a 8085. Aguarde a mensagem **Projeto iniciado**. O gateway fica em **http://localhost:8080**. As regras de negócio e o frontend são os mesmos utilizados no Linux.

O wrapper usa `ExecutionPolicy Bypass` somente no processo PowerShell que executa o script; ele não altera a política permanente do computador. Também é possível chamar o PowerShell diretamente:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\iniciar-projeto.ps1
```

## Durante a apresentação

Depois de compilar a versão que será apresentada, você pode iniciar usando os JARs existentes:

```powershell
.\iniciar-projeto.cmd -SemCompilar
```

Essa opção exige que os JARs atuais estejam em `spring\target` de cada serviço e em `gateway\target`. Se alterar o código, execute novamente sem `-SemCompilar`. Não copie apenas o código-fonte e espere que essa opção compile o projeto.

Para encerrar:

```powershell
.\iniciar-projeto.cmd parar
```

A parada identifica processos Java pelo caminho absoluto dos JARs deste projeto, inclusive os JARs antigos de Catálogo e Customização. Não encerra todos os processos Java da máquina. No Windows, usa `Stop-Process` para encerrar as JVMs; não promete desligamento gracioso. Se um serviço iniciado pela IDE usar outro comando de execução, encerre-o pela própria IDE.

Se a inicialização falhar, o script encerra somente as JVMs criadas naquela tentativa. Ele verifica portas ocupadas, código de saída do Maven e existência dos JARs. Uma trava impede duas inicializações/paradas simultâneas. Execute sempre usando o mesmo usuário do Windows que iniciou os serviços.

## Logs e dados

Os logs ficam em `.run-local\logs\<servico>.log` e `<servico>.err.log`. Por exemplo:

```powershell
Get-Content .\.run-local\logs\pedido.log -Tail 40 -Wait
Get-Content .\.run-local\logs\pedido.err.log -Tail 40
```

Use `Ctrl+C` para sair da leitura do log. Os serviços continuam ativos até executar o comando de parada.

Os bancos dos serviços de negócio ficam em memória: cadastre os produtos necessários após reiniciar. Login guarda usuários e revogações em `.run-local\auth`, salvo quando `POIESIS_AUTH_DB` estiver configurado. Os JARs executados são cópias em `.run-local\artifacts`, para que recompilar não substitua arquivos usados pela JVM.

## Frontend

Em outro terminal, na pasta `poiesis-frontend`:

```powershell
npm ci
npm run web
```

Use **http://localhost:8090** no navegador e mantenha a API em **http://localhost:8080**. Instale as dependências no Windows; não reutilize a pasta `node_modules` do Linux. A refatoração das telas continua sendo uma etapa separada.

## Verificação do iniciador

Os testes de sintaxe, identificação dos JARs, proteção contra parada de processos alheios/PIDs antigos e detecção de portas podem ser executados sem iniciar o backend:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\tests\iniciar-windows.Tests.ps1
```

Os testes passaram em um contêiner Linux com PowerShell 7.4, incluindo o parser oficial do PowerShell. A execução completa com Windows PowerShell 5.1, `Win32_Process` e JVMs precisa ser conferida no Windows antes da apresentação. Os testes não iniciam nem encerram processos reais.

Referências técnicas: [Start-Process](https://learn.microsoft.com/en-us/powershell/module/microsoft.powershell.management/start-process?view=powershell-5.1) e [Get-CimInstance](https://learn.microsoft.com/powershell/module/cimcmdlets/get-ciminstance?view=powershell-5.1).
