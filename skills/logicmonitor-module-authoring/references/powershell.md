# PowerShell Scripting

See also: [script-structure.md](script-structure.md), [docs/concepts/collection-modes.md](../../../docs/concepts/collection-modes.md)

## Device properties

Access credentials and connection info via LogicMonitor device property tokens. Never hardcode credentials.

```powershell
$hostname = '##system.hostname##'
$wmiUser = '##wmi.user##'
$wmiPass = @'
##wmi.pass##
'@
```

Use multiline-safe `@' '@` syntax for passwords that may contain special characters.

## Property token validation

Detect unset tokens before using them:

```powershell
function Test-PropertyToken {
    param([string]$Value)
    return [string]::IsNullOrEmpty($Value) -or $Value -match '^##.*##$'
}

if (-not (Test-PropertyToken $wmiUser) -and -not (Test-PropertyToken $wmiPass)) {
    $securePass = ConvertTo-SecureString $wmiPass -AsPlainText -Force
    $creds = [PSCredential]::new($wmiUser, $securePass)
}
```

## Hostname preparation

```powershell
# Azure devices: prefer private IP
$hostname = if (-not [string]::IsNullOrEmpty('##system.azure.privateIpAddress##')) {
    '##system.azure.privateIpAddress##'
} else {
    '##system.hostname##'
}

# IP → FQDN for SDK/remoting
if ($hostname -match '\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b') {
    $hostname = [System.Net.Dns]::GetHostbyAddress($hostname).HostName
}
```

## Credential cascade

Try product-specific credentials first, then WMI, then none (local/collector):

```powershell
if (-not (Test-PropertyToken $productUser) -and -not (Test-PropertyToken $productPass)) {
    # use product credentials
} elseif (-not (Test-PropertyToken $wmiUser) -and -not (Test-PropertyToken $wmiPass)) {
    $productUser = $wmiUser
    $productPass = $wmiPass
} else {
    $UseCredentials = $false
}
```

## Sanitize-Output helper

```powershell
function Sanitize-Output {
    param($Metric)
    if ($Metric -is [bool]) { return [int]$Metric }
    if ([string]::IsNullOrWhiteSpace($Metric)) { return 'null' }
    return $Metric
}
```

## Collector-local execution

Skip remoting when the device is the collector:

```powershell
$categories = '##system.categories##'
$isCollector = $categories -split ',' | ForEach-Object { $_.Trim().ToLower() } | Where-Object { $_ -eq 'collector' }

if ($isCollector) {
    # run locally
} else {
    $session = New-PSSession -ComputerName $hostname -Credential $creds
    Invoke-Command -Session $session -ScriptBlock $command
    Remove-PSSession $session
}
```

## Shared AD + collection scriptblock

Use `$isAd` flag when AD and collection share query logic:

```powershell
$isAd = $false  # $true in AD script

if ($isAd) {
    Write-Output "$wildvalue##$wildalias####auto.key=$value"
} else {
    Write-Output "$wildvalue.MetricName=$(Sanitize-Output $value)"
}
```

## Alert properties (DiagnosticSource / RemediationSource)

Use inline `##ALERT.*##` tokens (substituted before execution):

```powershell
$datapointName = "##ALERT.DATAPOINT##"
$currentValue  = "##ALERT.DATAPOINT.VALUE##"
$severity      = "##ALERT.SEVERITY##"
```

Tokens are empty on manual execution. Design fallback logic when alert context is unavailable.

Runtime `alertProps` variable is Groovy-only. See [alert-properties.md](alert-properties.md).

## WinRM remoting

```powershell
$session = New-PSSession -ComputerName $hostname -Credential $creds -ErrorAction SilentlyContinue
if (-not $session) {
    Write-Output "error=connection_failed"
    exit 1
}
Invoke-Command -Session $session -ScriptBlock { INSERT_COMMAND_HERE }
Remove-PSSession $session
```

See `recipes/powershell/winrm-exec/`.

## WMI queries

```powershell
$params = @{
    Namespace  = 'INSERT_WMI_NAMESPACE_HERE'
    Query      = 'INSERT_WQL_QUERY_HERE'
    ComputerName = $hostname
}
if ($creds) { $params.Credential = $creds }

$results = Get-CimInstance @params
foreach ($row in $results) {
    Write-Output "propertyName=$(Sanitize-Output $row.PropertyName)"
}
```

See `recipes/powershell/wmi-query/` and `recipes/powershell/wmi-discovery/`.

## Output format by module type

| Module type | PowerShell output |
|-------------|-------------------|
| DataSource Script | `Write-Output "key=value"` |
| DataSource BatchScript | `Write-Output "instance.key=value"` |
| PropertySource | `Write-Output "auto.property=value"` or `system.categories=Cat` |
| Active Discovery | `Write-Output "wildvalue##wildalias"` |
| ConfigSource Script | Raw config text via `Write-Output` |
| DiagnosticSource | JSON string via `Write-Output` |
| RemediationSource | JSON string via `Write-Output` |

**PropertySource rule:** Only `auto.*` and `system.categories`. No other `system.*` properties.

Full reference: [output-formats.md](output-formats.md)

## JSON output (Diag / Remediation)

```powershell
$output = @{
    data = "Example Output"
    format = "markdown"
    remediationStatus = "true"
} | ConvertTo-Json -Compress

Write-Output $output
exit 0
```

## Write-Output vs Write-Host

**Always use `Write-Output`** for collection script data. `Write-Host` does not reliably send data to stdout and can cause BatchScript timeout issues.

Use `Write-Host` only for gated debug logging (`if ($debug) { Write-Host ... }`).

## Return codes

Exit with code `0` on success. Non-zero on failure.

## Recipes

See `recipes/powershell/` in the monitoring-recipes repo.
