<#
.SYNOPSIS
    Unlocks the Logic.Monitor SecretStore via a macOS dialog, connects, and downloads LogicModules.
#>
[CmdletBinding()]
param(
    [string]$OutputPath = $PSScriptRoot,
    [string]$CachedAccountName,
    [switch]$ListOnly
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$logPath = Join-Path $OutputPath 'download.log'
function Write-Log {
    param([string]$Message, [string]$Color = 'White')
    $line = '[{0:HH:mm:ss}] {1}' -f (Get-Date), $Message
    Add-Content -Path $logPath -Value $line
    Write-Host $line -ForegroundColor $Color
}

Write-Log 'Prompting for SecretStore vault password (macOS dialog)...' 'Cyan'

$promptScript = @'
display dialog "Enter your Logic.Monitor SecretStore vault password." default answer "" with hidden answer with title "LogicMonitor" buttons {"Cancel", "Unlock"} default button "Unlock"
return text returned of result
'@

try {
    $vaultPasswordText = & osascript -e $promptScript 2>&1
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($vaultPasswordText)) {
        throw 'Vault password prompt was cancelled or returned empty.'
    }
}
catch {
    throw "Failed to read vault password from the macOS dialog: $_"
}

try {
    $vaultPassword = ConvertTo-SecureString -String $vaultPasswordText -AsPlainText -Force
}
finally {
    $vaultPasswordText = $null
}

Write-Log 'Unlocking SecretStore...' 'Cyan'
Unlock-SecretStore -Password $vaultPassword -PasswordTimeout 14400
$vaultPassword = $null
Write-Log 'SecretStore unlocked for this process (4 hour timeout).' 'Green'

Import-Module Logic.Monitor -ErrorAction Stop

$accounts = @(Get-SecretInfo -Vault Logic.Monitor | Where-Object {
        $_.Metadata['Type'] -ne 'EAI' -and $_.Name -notlike '*LMSessionSync*'
    })

if ($accounts.Count -eq 0) {
    throw 'No LogicMonitor cached accounts found in vault Logic.Monitor.'
}

Write-Log ("Found {0} cached account(s): {1}" -f $accounts.Count, (($accounts.Name) -join ', ')) 'Green'

if (-not $CachedAccountName) {
    if ($accounts.Count -eq 1) {
        $CachedAccountName = $accounts[0].Name
    }
    else {
        $quoted = ($accounts.Name | ForEach-Object { '"{0}"' -f ($_ -replace '"', '\\"') }) -join ', '
        $chooser = @"
set theChoice to choose from list {$quoted} with prompt "Select the LogicMonitor portal to download LogicModules from." with title "LogicMonitor" default items {"$($accounts[0].Name)"}
if theChoice is false then
    error "cancelled"
end if
return item 1 of theChoice
"@
        $CachedAccountName = (& osascript -e $chooser).Trim()
        if ([string]::IsNullOrWhiteSpace($CachedAccountName) -or $CachedAccountName -eq 'false') {
            throw 'Portal selection was cancelled.'
        }
    }
}

Write-Log "Connecting with cached account: $CachedAccountName" 'Cyan'
Connect-LMAccount -CachedAccountName $CachedAccountName -SkipVersionCheck | Out-Null

$status = Get-LMAccountStatus
if (-not $status.Valid) {
    throw 'Connect-LMAccount did not produce a valid session.'
}
Write-Log "Connected to portal: $($status.Portal)" 'Green'

$downloadScript = Join-Path $PSScriptRoot 'Download-LMLogicModules.ps1'
$downloadParams = @{
    OutputPath = $OutputPath
}
if ($ListOnly) {
    $downloadParams.ListOnly = $true
}

& $downloadScript @downloadParams
Write-Log 'Download script finished.' 'Green'
