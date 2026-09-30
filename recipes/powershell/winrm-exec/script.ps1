<# Purpose: Execute a remote PowerShell command via WinRM
   Module types: DataSource, DiagnosticSource, RemediationSource
   Device properties: system.hostname, wmi.user, wmi.pass #>

$debug = $false

# --- Property tokens ---
$hostname = if (-not [string]::IsNullOrEmpty('##system.azure.privateIpAddress##')) {
    '##system.azure.privateIpAddress##'
} else {
    '##system.hostname##'
}
$categories = '##system.categories##'
$wmiUser = '##wmi.user##'
$wmiPass = @'
##wmi.pass##
'@

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
    } catch {
        Write-DebugMessage "Using IP address directly: $hostname"
    }
}

# --- Credential setup ---
$creds = $null
if (-not (Test-PropertyToken $wmiUser) -and -not (Test-PropertyToken $wmiPass)) {
    $securePass = ConvertTo-SecureString $wmiPass -AsPlainText -Force
    $creds = [PSCredential]::new($wmiUser, $securePass)
}

# --- Collector-local fork ---
$isCollector = $categories -split ',' |
    ForEach-Object { $_.Trim().ToLower() } |
    Where-Object { $_ -eq 'collector' }

# --- Remote command scriptblock ---
$command = {
    # INSERT_COMMAND_HERE — replace with your remote logic
  $result = Get-Service -Name 'Spooler' -ErrorAction SilentlyContinue
  if ($result) {
    Write-Output "serviceStatus=$(if ($result.Status -eq 'Running') { 1 } else { 0 })"
  } else {
    Write-Output "serviceStatus=null"
  }
}

# --- Main flow ---
$maxAttempts = 2
$attemptSleep = 2
$session = $null

if ($isCollector) {
    Write-DebugMessage "Running locally on collector"
    & $command
} else {
    for ($attempt = 1; $attempt -le $maxAttempts; $attempt++) {
        Write-DebugMessage "Connection attempt $attempt of $maxAttempts"
        try {
            if ($creds) {
                $session = New-PSSession -ComputerName $hostname -Credential $creds -ErrorAction Stop
            } else {
                $session = New-PSSession -ComputerName $hostname -ErrorAction Stop
            }
            break
        } catch {
            Write-DebugMessage "Connection failed: $($_.Exception.Message)"
            Start-Sleep -Seconds $attemptSleep
        }
    }

    if (-not $session) {
        Write-DebugMessage "Unable to establish WinRM session"
        exit 1
    }

    try {
        Invoke-Command -Session $session -ScriptBlock $command -ErrorAction Stop
    } finally {
        Remove-PSSession $session -ErrorAction SilentlyContinue
    }
}

exit 0

# --- DiagnosticSource JSON variant (uncomment and adapt) ---
# $output = Invoke-Command -Session $session -ScriptBlock $command
# $json = @{ data = ($output -join "`n"); format = "markdown" } | ConvertTo-Json -Compress
# Write-Output $json
