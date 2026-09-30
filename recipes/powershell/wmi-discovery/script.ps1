<# Purpose: WMI-based Active Discovery — enumerate instances
   Module types: Active Discovery (DataSource)
   Device properties: system.hostname, wmi.user, wmi.pass #>

$debug = $false
$isAd = $true

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
$wildvalueProperty = 'INSERT_WILDVALUE_PROPERTY_HERE'
$wildaliasProperty = 'INSERT_WILDALIAS_PROPERTY_HERE'

# --- Functions ---
function Test-PropertyToken {
    param([string]$Value)
    return [string]::IsNullOrEmpty($Value) -or $Value -match '^##.*##$'
}

function Write-DebugMessage {
    param([string]$Message)
    if ($debug) { Write-Host "[DEBUG] $Message" }
}

function Sanitize-Wildvalue {
    param([string]$Value)
    if ([string]::IsNullOrWhiteSpace($Value)) { return 'unknown' }
    return ($Value -replace '[#\\:= ]', '_')
}

# --- Hostname prep ---
if ($hostname -match '\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b') {
    try {
        $hostname = [System.Net.Dns]::GetHostbyAddress($hostname).HostName
    } catch {
        Write-DebugMessage "Using IP address directly"
    }
}

# --- Credential setup ---
$creds = $null
if (-not (Test-PropertyToken $wmiUser) -and -not (Test-PropertyToken $wmiPass)) {
    $securePass = ConvertTo-SecureString $wmiPass -AsPlainText -Force
    $creds = [PSCredential]::new($wmiUser, $securePass)
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
    Write-DebugMessage "WMI discovery failed: $($_.Exception.Message)"
    exit 1
}

if (-not $results) {
    Write-DebugMessage "No instances discovered"
    exit 0
}

foreach ($row in @($results)) {
    $wildvalue = Sanitize-Wildvalue $row.$wildvalueProperty
    $wildalias = $row.$wildaliasProperty
    if ([string]::IsNullOrWhiteSpace($wildalias)) {
        $wildalias = $wildvalue
    }

    # Optional instance-level properties (auto.* prefix)
    # $ilps = @("auto.propertyName=$($row.SomeProperty)")
    # Write-Output "$wildvalue##$wildalias####$([string]::Join('&', $ilps))"

    Write-Output "$wildvalue##$wildalias"
}

exit 0
