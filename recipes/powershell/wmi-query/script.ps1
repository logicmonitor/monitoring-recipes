<# Purpose: Execute a WMI query and emit datapoints
   Module types: DataSource, PropertySource
   Device properties: system.hostname, wmi.user, wmi.pass #>

$debug = $false

# --- Property tokens ---
$hostname = if (-not [string]::IsNullOrEmpty('##system.azure.privateIpAddress##')) {
    '##system.azure.privateIpAddress##'
} else {
    '##system.hostname##'
}
$wmiUser = '##wmi.user##'
$wmiPass = @'
##wmi.pass##
'@
$namespace = 'INSERT_WMI_NAMESPACE_HERE'
$query = 'INSERT_WQL_QUERY_HERE'

# --- Functions ---
function Test-PropertyToken {
    param([string]$Value)
    return [string]::IsNullOrEmpty($Value) -or $Value -match '^##.*##$'
}

function Write-DebugMessage {
    param([string]$Message)
    if ($debug) { Write-Host "[DEBUG] $Message" }
}

function Sanitize-Output {
    param($Metric)
    if ($Metric -is [bool]) { return [int]$Metric }
    if ([string]::IsNullOrWhiteSpace($Metric)) { return 'null' }
    return $Metric
}

# --- Hostname prep ---
if ($hostname -match '\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b') {
    try {
        $hostname = [System.Net.Dns]::GetHostbyAddress($hostname).HostName
        Write-DebugMessage "Resolved IP to FQDN: $hostname"
    } catch {
        Write-DebugMessage "Could not resolve IP to FQDN, using IP directly"
    }
}

# --- Credential setup ---
$creds = $null
if (-not (Test-PropertyToken $wmiUser) -and -not (Test-PropertyToken $wmiPass)) {
    $securePass = ConvertTo-SecureString $wmiPass -AsPlainText -Force
    $creds = [PSCredential]::new($wmiUser, $securePass)
    Write-DebugMessage "Using WMI credentials"
} else {
    Write-DebugMessage "No WMI credentials configured, attempting local/default auth"
}

# --- Main flow ---
$params = @{
    Namespace = $namespace
    Query     = $query
}

if ($hostname -and -not (Test-PropertyToken $hostname)) {
    $params.ComputerName = $hostname
}
if ($creds) {
    $params.Credential = $creds
}

try {
    $results = Get-CimInstance @params -ErrorAction Stop
} catch {
    Write-DebugMessage "WMI query failed: $($_.Exception.Message)"
    exit 1
}

if (-not $results) {
    Write-DebugMessage "WMI query returned no results"
    exit 0
}

# Customize: map WMI properties to datapoint keys
# DataSource: Write-Output "metricName=$(Sanitize-Output $value)"
# PropertySource: Write-Output "auto.propertyName=$(Sanitize-Output $value)"
foreach ($row in @($results)) {
    $row.PSObject.Properties | Where-Object { $_.MemberType -eq 'Property' } | ForEach-Object {
        Write-Output "$($_.Name)=$(Sanitize-Output $_.Value)"
    }
}

exit 0
