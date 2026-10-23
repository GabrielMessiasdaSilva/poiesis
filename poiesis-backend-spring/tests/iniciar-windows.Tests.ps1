#requires -Version 5.1
# Run with powershell.exe -NoProfile -File .\tests\iniciar-windows.Tests.ps1
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$scriptPath = Join-Path (Split-Path $PSScriptRoot -Parent) 'iniciar-projeto.ps1'
$tokens = $null
$parseErrors = $null
$ast = [System.Management.Automation.Language.Parser]::ParseFile($scriptPath, [ref]$tokens, [ref]$parseErrors)
if ($parseErrors.Count -gt 0) { throw ($parseErrors | Out-String) }
# Load function definitions only. Never execute the launcher's startup/stop orchestration.
$definitions = $ast.FindAll({ param($node) $node -is [System.Management.Automation.Language.FunctionDefinitionAst] }, $false)
foreach ($definition in $definitions) { . ([scriptblock]::Create($definition.Extent.Text)) }

$Root = Join-Path ([System.IO.Path]::GetTempPath()) 'Poiesis teste com espacos'
$RunDir = Join-Path $Root '.run-local'
$ManagedServices = @('produto', 'gateway')
$script:FakeProcesses = @()
$script:Stopped = @()
function Get-CimInstance { param($ClassName, $Filter) return $script:FakeProcesses }
function Stop-Process { param($Id, [switch]$Force, $ErrorAction) $script:Stopped += $Id }
function Get-Process { param($Id, $ErrorAction) return $null }

function Assert-True($Condition, [string]$Message) {
    if (-not $Condition) { throw $Message }
}

$ownedJar = Join-Path $RunDir 'artifacts\produto.jar'
$otherRoot = Join-Path ([System.IO.Path]::GetTempPath()) 'Outro projeto'
$otherJar = Join-Path $otherRoot 'produto.jar'
$script:FakeProcesses = @(
    [PSCustomObject]@{ ProcessId = 101; CommandLine = ('java.exe -jar "' + $ownedJar + '"') },
    [PSCustomObject]@{ ProcessId = 202; CommandLine = ('java.exe -jar "' + $otherJar + '"') },
    [PSCustomObject]@{ ProcessId = 303; CommandLine = 'java.exe -jar produto.jar' },
    [PSCustomObject]@{ ProcessId = 404; CommandLine = $null },
    [PSCustomObject]@{ ProcessId = 505; CommandLine = ('java.exe -jar "' + $ownedJar + '.backup"') }
)
$owned = @(Get-OwnedProcesses)
Assert-True ($owned.Count -eq 1 -and $owned[0].ProcessId -eq 101) 'Ownership must match only the exact JAR, including paths with spaces.'
Stop-OwnedProcess 202
Assert-True ($script:Stopped.Count -eq 0) 'An unrelated Java process must not be stopped.'
Stop-OwnedProcess 101
Assert-True ($script:Stopped.Count -eq 1 -and $script:Stopped[0] -eq 101) 'The owned process should be stopped.'
$script:FakeProcesses = @([PSCustomObject]@{ ProcessId = 101; CommandLine = ('java.exe -jar "' + $otherJar + '"') })
Stop-OwnedProcess 101
Assert-True ($script:Stopped.Count -eq 1) 'A stale PID must not stop a replacement process.'

$listener = New-Object System.Net.Sockets.TcpListener -ArgumentList ([System.Net.IPAddress]::Loopback), 0
$listener.Start()
$port = $listener.LocalEndpoint.Port
try { Assert-True (Test-Port $port) 'An open port should be detected.' }
finally { $listener.Stop() }
Assert-True (-not (Test-Port $port)) 'A closed port should be detected.'
Write-Host 'OK: syntax, exact JAR ownership, paths with spaces, unrelated/stale PIDs, and TCP readiness.'
