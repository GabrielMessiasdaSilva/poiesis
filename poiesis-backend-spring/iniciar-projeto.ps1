#requires -Version 5.1
[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [ValidateSet('iniciar', 'parar', 'stop')]
    [string]$Acao = 'iniciar',
    [switch]$SemCompilar
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$Root = $PSScriptRoot
$RunDir = Join-Path $Root '.run-local'
$LogDir = Join-Path $RunDir 'logs'
$Services = @('login', 'produto', 'pedido', 'producao', 'relatorio', 'gateway')
$ManagedServices = $Services + @('catalogo', 'customizacao')
$Ports = @{ login = 8081; produto = 8082; pedido = 8083; producao = 8085; relatorio = 8084; gateway = 8080 }
$StartedIds = New-Object 'System.Collections.Generic.List[int]'
$ControlLock = $null
$ResultCode = 0
$StartupComplete = $false

function Get-ServiceJar([string]$Service) {
    if ($Service -eq 'gateway') {
        return Join-Path $Root 'gateway\target\gateway-0.0.1-SNAPSHOT.jar'
    }
    return Join-Path $Root "$Service\spring\target\$Service-spring-0.0.1-SNAPSHOT.jar"
}

function Test-Port([int]$Port) {
    $client = New-Object System.Net.Sockets.TcpClient
    try {
        $connection = $client.ConnectAsync('127.0.0.1', $Port)
        if ($connection.Wait(300)) { return $client.Connected }
        return $false
    } catch {
        return $false
    } finally {
        $client.Dispose()
    }
}

function Get-OwnedProcesses {
    # Check the full JAR path, never just a PID or the java.exe process name.
    $processes = Get-CimInstance Win32_Process -Filter "Name='java.exe' OR Name='javaw.exe'"
    foreach ($entry in $processes) {
        if (-not $entry.CommandLine) { continue }
        $jarMatch = [regex]::Match($entry.CommandLine, '(?i)(?:^|\s)-jar\s+(?:"([^"]+)"|(\S+))')
        if (-not $jarMatch.Success) { continue }
        $jarPath = $jarMatch.Groups[1].Value
        if (-not $jarPath) { $jarPath = $jarMatch.Groups[2].Value }
        if (-not [System.IO.Path]::IsPathRooted($jarPath)) { continue }
        $jarPath = [System.IO.Path]::GetFullPath($jarPath)
        foreach ($service in $ManagedServices) {
            $allowed = @(
                (Get-ServiceJar $service),
                (Join-Path $RunDir "artifacts\$service.jar"),
                (Join-Path $Root "$service\spring\target\springframework-0.0.1-SNAPSHOT.jar"),
                (Join-Path $Root "$service\springframework\target\$service-spring-0.0.1-SNAPSHOT.jar"),
                (Join-Path $Root "$service\springframework\target\springframework-0.0.1-SNAPSHOT.jar")
            )
            if ($allowed -contains $jarPath) {
                [PSCustomObject]@{ Service = $service; ProcessId = [int]$entry.ProcessId }
                break
            }
        }
    }
}

function Stop-OwnedProcess([int]$ProcessId) {
    # Recheck ownership before stopping; stale .pid files are not trusted.
    $owned = @(Get-OwnedProcesses | Where-Object { $_.ProcessId -eq $ProcessId })
    if ($owned.Count -gt 0) {
        Write-Host "Encerrando $($owned[0].Service) (PID $ProcessId)"
        Stop-Process -Id $ProcessId -Force -ErrorAction Stop
        $remaining = Get-Process -Id $ProcessId -ErrorAction SilentlyContinue
        if ($remaining) { Wait-Process -Id $ProcessId -Timeout 15 -ErrorAction SilentlyContinue }
    }
}

function Stop-Services {
    foreach ($entry in @(Get-OwnedProcesses)) { Stop-OwnedProcess $entry.ProcessId }
    foreach ($service in $ManagedServices) {
        Remove-Item -LiteralPath (Join-Path $RunDir "$service.pid") -Force -ErrorAction SilentlyContinue
    }
    $remaining = @(Get-OwnedProcesses)
    if ($remaining.Count -gt 0) { throw 'Ainda existem processos do projeto ativos.' }
    foreach ($service in $Services) {
        if (Test-Port $Ports[$service]) {
            throw "A porta $($Ports[$service]) ($service) continua ocupada. Confira o processo na IDE."
        }
    }
    Write-Host 'Servicos encerrados.'
}

function Start-ServiceApp([string]$Service, [string]$JavaExe) {
    $port = $Ports[$Service]
    if (Test-Port $port) { throw "Porta $port ($Service) ocupada." }
    $runtimeJar = Join-Path $RunDir "artifacts\$Service.jar"
    Copy-Item -LiteralPath (Get-ServiceJar $Service) -Destination $runtimeJar -Force
    $stdout = Join-Path $LogDir "$Service.log"
    $stderr = Join-Path $LogDir "$Service.err.log"
    # Start-Process joins ArgumentList: explicit quotes preserve paths with spaces.
    $javaArguments = '-jar "' + $runtimeJar + '"'
    $process = Start-Process -FilePath $JavaExe -ArgumentList $javaArguments `
        -WorkingDirectory $Root -WindowStyle Hidden -PassThru `
        -RedirectStandardOutput $stdout -RedirectStandardError $stderr
    $StartedIds.Add($process.Id)
    Set-Content -LiteralPath (Join-Path $RunDir "$Service.pid") -Value $process.Id -Encoding ASCII
    $deadline = [DateTime]::UtcNow.AddSeconds(180)
    while ([DateTime]::UtcNow -lt $deadline) {
        $process.Refresh()
        if ($process.HasExited) {
            foreach ($log in @($stdout, $stderr)) {
                if (Test-Path -LiteralPath $log) { Get-Content -LiteralPath $log -Tail 30 | Out-Host }
            }
            throw "$Service encerrou durante a inicializacao. Consulte $stdout e $stderr."
        }
        if (Test-Port $port) {
            Write-Host "$Service iniciado na porta $port (PID $($process.Id))"
            return
        }
        Start-Sleep -Milliseconds 500
    }
    throw "Tempo esgotado aguardando $Service na porta $port. Consulte $stdout e $stderr."
}

try {
    if ($env:OS -ne 'Windows_NT') { throw 'Use este script no Windows. No Linux, use iniciar-projeto.sh.' }
    New-Item -ItemType Directory -Path $LogDir -Force | Out-Null
    New-Item -ItemType Directory -Path (Join-Path $RunDir 'artifacts') -Force | Out-Null
    try {
        $ControlLock = [System.IO.File]::Open((Join-Path $RunDir 'control.lock'),
            [System.IO.FileMode]::OpenOrCreate, [System.IO.FileAccess]::ReadWrite, [System.IO.FileShare]::None)
    } catch {
        throw 'Outra inicializacao/parada esta em andamento ou a pasta .run-local nao permite escrita.'
    }

    if ($Acao -in @('parar', 'stop')) {
        Stop-Services
    } else {
        $java = Get-Command java.exe -CommandType Application -ErrorAction Stop | Select-Object -First 1
        # java -version writes to stderr; read it directly to support Windows PowerShell 5.1.
        $versionInfo = New-Object System.Diagnostics.ProcessStartInfo
        $versionInfo.FileName = $java.Source
        $versionInfo.Arguments = '-version'
        $versionInfo.UseShellExecute = $false
        $versionInfo.CreateNoWindow = $true
        $versionInfo.RedirectStandardError = $true
        $versionInfo.RedirectStandardOutput = $true
        $versionProcess = [System.Diagnostics.Process]::Start($versionInfo)
        $javaVersion = $versionProcess.StandardError.ReadToEnd() + $versionProcess.StandardOutput.ReadToEnd()
        $versionProcess.WaitForExit()
        $versionExit = $versionProcess.ExitCode
        $versionProcess.Dispose()
        if ($versionExit -ne 0 -or $javaVersion -notmatch '\bversion\s+"21(?:\.|\")') {
            throw "Este projeto requer Java 21 no PATH. Encontrado: $javaVersion"
        }
        if (@(Get-OwnedProcesses).Count -gt 0) { throw 'O projeto ja esta ativo. Execute iniciar-projeto.cmd parar.' }
        foreach ($service in $Services) {
            if (Test-Port $Ports[$service]) { throw "Porta $($Ports[$service]) ($service) ocupada. Confira a IDE." }
            if (-not (Test-Path -LiteralPath (Join-Path $Root "$service\pom.xml"))) {
                throw "pom.xml nao encontrado para $service."
            }
        }
        if (-not $SemCompilar) {
            $maven = Get-Command mvn.cmd -CommandType Application -ErrorAction Stop | Select-Object -First 1
            foreach ($service in $Services) {
                Write-Host "Compilando $service..."
                & $maven.Source -f (Join-Path $Root "$service\pom.xml") -DskipTests clean install
                if ($LASTEXITCODE -ne 0) { throw "Falha na compilacao de $service. Nenhum servico foi iniciado." }
            }
        }
        foreach ($service in $Services) {
            if (-not (Test-Path -LiteralPath (Get-ServiceJar $service))) {
                throw "JAR de $service nao encontrado. Execute sem -SemCompilar para compilar."
            }
        }
        if (-not $env:POIESIS_AUTH_DB) {
            $env:POIESIS_AUTH_DB = (Join-Path $RunDir 'auth').Replace('\', '/')
        }
        # Consumers start before Pedido, so both queues exist before publishing.
        foreach ($service in @('produto', 'login', 'producao', 'relatorio', 'pedido', 'gateway')) {
            Start-ServiceApp $service $java.Source
        }
        $StartupComplete = $true
        Write-Host "Projeto iniciado. Gateway: http://localhost:8080"
        Write-Host "Logs: $LogDir"
        Write-Host 'Para encerrar: .\iniciar-projeto.cmd parar'
    }
} catch {
    [Console]::Error.WriteLine($_.Exception.Message)
    $ResultCode = 1
} finally {
    # Also clean up an interrupted startup, without touching pre-existing JVMs.
    if (-not $StartupComplete) {
        foreach ($startedId in $StartedIds) {
            try { Stop-OwnedProcess $startedId } catch { [Console]::Error.WriteLine($_.Exception.Message) }
        }
    }
    if ($null -ne $ControlLock) { $ControlLock.Dispose() }
}
exit $ResultCode
