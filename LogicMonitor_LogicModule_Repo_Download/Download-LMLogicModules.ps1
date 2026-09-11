<#
.SYNOPSIS
    Downloads all LogicModules from a connected LogicMonitor portal into a local search repository.

.DESCRIPTION
    Uses the Logic.Monitor PowerShell module (Connect-LMAccount + portal APIs) to list and export
    every LogicModule in the portal. Files are grouped by type so they can be grepped later.

    Export-LMLogicModule concatenates paths with a Windows backslash, which breaks on macOS.
    This script uses Invoke-LMAPIRequest -OutFile with Join-Path instead, against the same
    /setting/{type}/{id}?format=... endpoints.

.PARAMETER OutputPath
    Root folder for the local repository. Defaults to this script's directory.

.PARAMETER Type
    One or more LogicModule types to download. Default is All.

.PARAMETER UseCachedCredential
    Connect using a cached Logic.Monitor vault account.

.PARAMETER CachedAccountName
    Cached account name. If omitted with -UseCachedCredential, you will be prompted to pick one.

.PARAMETER AccessId
    LMv1 API Access ID.

.PARAMETER AccessKey
    LMv1 API Access Key.

.PARAMETER BearerToken
    Bearer API token (alternative to AccessId/AccessKey).

.PARAMETER AccountName
    Portal subdomain, e.g. "acme" for acme.logicmonitor.com.

.PARAMETER GovCloud
    Connect to an LM GovCloud portal.

.PARAMETER SkipExisting
    Skip modules whose export file already exists (resume a previous run). Does not inspect whether the portal copy changed.

.PARAMETER SkipUnchanged
    Re-list the portal and skip modules whose checksum/version still match the last saved state.
    New and updated modules are downloaded. Use this after you edit LogicModules.

.PARAMETER CheckUpdates
    Compare the portal to the local repo and report added/updated/unchanged modules without downloading.

.PARAMETER ListOnly
    List counts and write inventory files, but do not download export payloads.

.PARAMETER DelayMs
    Optional pause between export requests to stay under API rate limits.

.EXAMPLE
    Connect-LMAccount -UseCachedCredential
    ./Download-LMLogicModules.ps1

.EXAMPLE
    ./Download-LMLogicModules.ps1 -UseCachedCredential -CachedAccountName "production"

.EXAMPLE
    ./Download-LMLogicModules.ps1 -AccessId $id -AccessKey $key -AccountName "myportal"

.EXAMPLE
    ./Download-LMLogicModules.ps1 -Type datasources,eventsources -SkipExisting

.EXAMPLE
    ./Download-LMLogicModules.ps1 -CheckUpdates

.EXAMPLE
    ./Download-LMLogicModules.ps1 -SkipUnchanged
#>
[CmdletBinding(DefaultParameterSetName = 'ExistingSession')]
param(
    [string]$OutputPath = $PSScriptRoot,

    [ValidateSet(
        'All',
        'datasources',
        'propertyrules',
        'propertysources',
        'eventsources',
        'topologysources',
        'configsources',
        'logsources',
        'functions',
        'oids',
        'batchjobs',
        'diagnosticsources',
        'remediationsources'
    )]
    [string[]]$Type = @('All'),

    [Parameter(ParameterSetName = 'Cached')]
    [switch]$UseCachedCredential,

    [Parameter(ParameterSetName = 'Cached')]
    [string]$CachedAccountName,

    [Parameter(Mandatory, ParameterSetName = 'LMv1')]
    [string]$AccessId,

    [Parameter(Mandatory, ParameterSetName = 'LMv1')]
    [string]$AccessKey,

    [Parameter(Mandatory, ParameterSetName = 'Bearer')]
    [string]$BearerToken,

    [Parameter(ParameterSetName = 'SessionSync')]
    [switch]$SessionSync,

    [Parameter(Mandatory, ParameterSetName = 'LMv1')]
    [Parameter(Mandatory, ParameterSetName = 'Bearer')]
    [Parameter(Mandatory, ParameterSetName = 'SessionSync')]
    [string]$AccountName,

    [switch]$GovCloud,
    [switch]$SkipExisting,
    [switch]$SkipUnchanged,
    [switch]$CheckUpdates,
    [switch]$ListOnly,

    [ValidateRange(0, 5000)]
    [int]$DelayMs = 25
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$Type = @(
    foreach ($t in $Type) {
        if ($t -eq 'propertysources') { 'propertyrules' } else { $t }
    }
)

function Get-SafeFileName {
    param(
        [string]$Name,
        [string]$Fallback = 'unnamed'
    )

    if ([string]::IsNullOrWhiteSpace($Name)) {
        $Name = $Fallback
    }

    $invalid = [IO.Path]::GetInvalidFileNameChars()
    $chars = foreach ($ch in $Name.ToCharArray()) {
        if ($invalid -contains $ch) { '_' } else { $ch }
    }
    $safe = -join $chars
    $safe = $safe -replace '\s+', '_'
    if ($safe.Length -gt 120) {
        $safe = $safe.Substring(0, 120)
    }
    return $safe
}

function Get-ObjectProperty {
    param(
        [object]$InputObject,
        [string]$Name
    )

    if ($null -eq $InputObject) {
        return $null
    }

    $property = $InputObject.PSObject.Properties[$Name]
    if ($null -eq $property) {
        return $null
    }
    return $property.Value
}

function Convert-PropertyToString {
    param([object]$Value)

    if ($null -eq $Value) {
        return ''
    }
    if ($Value -is [System.Collections.IEnumerable] -and $Value -isnot [string]) {
        $items = foreach ($item in @($Value)) {
            if ($null -eq $item) { continue }
            if ($item.PSObject.Properties['name'] -and $item.PSObject.Properties['value']) {
                '{0}={1}' -f $item.name, $item.value
            }
            else {
                [string]$item
            }
        }
        return ($items -join ';')
    }
    return [string]$Value
}

function Invoke-LMExportedCommand {
    param(
        [Parameter(Mandatory)]
        [string]$Name,
        [hashtable]$Parameters = @{}
    )

    if (-not $script:LMModule) {
        throw 'No authenticated Logic.Monitor module is loaded.'
    }

    $cmd = $script:LMModule.ExportedCommands[$Name]
    if (-not $cmd) {
        throw "Command $Name is not exported by $($script:LMModule.Name) $($script:LMModule.Version)"
    }

    & $cmd @Parameters
}

function Invoke-InLMModule {
    param(
        [Parameter(Mandatory)]
        [scriptblock]$ScriptBlock,
        [Parameter(ValueFromRemainingArguments)]
        [object[]]$ArgumentList
    )

    if (-not $script:LMModule) {
        throw 'No authenticated Logic.Monitor module is loaded.'
    }

    return & $script:LMModule $ScriptBlock @ArgumentList
}

function Get-AllLMApiItems {
    param(
        [Parameter(Mandatory)]
        [string]$ResourcePath
    )

    $offset = 0
    $size = 1000
    $all = [System.Collections.Generic.List[object]]::new()

    do {
        $page = Invoke-LMExportedCommand -Name 'Invoke-LMAPIRequest' -Parameters @{
            ResourcePath = $ResourcePath
            Method       = 'GET'
            QueryParams  = @{
                size   = $size
                offset = $offset
                sort   = '+id'
            }
        }
        $items = @($page | Where-Object { $null -ne $_ })
        if ($items.Count -eq 0) {
            break
        }
        $all.AddRange($items)
        $offset += $size
    } while ($items.Count -eq $size)

    return $all
}

function Get-LogicModulesByType {
    param(
        [Parameter(Mandatory)]
        [hashtable]$TypeInfo
    )

    $apiType = $TypeInfo.ApiType
    $getter = $TypeInfo.ListCommand

    if ($getter -and (Get-Command $getter -ErrorAction SilentlyContinue)) {
        Write-Verbose "Listing $apiType with $getter via $($script:LMModule.Name)"
        $results = Invoke-LMExportedCommand -Name $getter
        return @($results | Where-Object { $null -ne $_ })
    }

    Write-Verbose "Listing $apiType with Invoke-LMAPIRequest /setting/$apiType"
    return @(Get-AllLMApiItems -ResourcePath "/setting/$apiType")
}

function Get-LMAuthContext {
    foreach ($lmModule in @(Get-Module | Where-Object { $_.ExportedCommands.ContainsKey('Connect-LMAccount') })) {
        try {
            $auth = & $lmModule { $Script:LMAuth }
            if ($auth -and (Get-ObjectProperty $auth 'Valid')) {
                return [pscustomobject]@{
                    Module = $lmModule
                    Auth   = $auth
                    Portal = Get-ObjectProperty $auth 'Portal'
                    Valid  = $true
                    Type   = Get-ObjectProperty $auth 'Type'
                }
            }
        }
        catch {
            continue
        }
    }
    return $null
}

function Sync-LMAuthToLoadedModules {
    param($Auth)

    $targets = @(Get-Module | Where-Object {
            $_.ExportedCommands.ContainsKey('Connect-LMAccount') -or
            $_.ExportedCommands.ContainsKey('Get-LMDatasource') -or
            $_.ExportedCommands.ContainsKey('Invoke-LMAPIRequest')
        })

    foreach ($module in $targets) {
        & $module { param($a) $Script:LMAuth = $a } $Auth
    }
}

function Initialize-LMSession {
    $context = Get-LMAuthContext
    if (-not $context) {
        return $null
    }

    $script:LMModule = $context.Module
    Sync-LMAuthToLoadedModules -Auth $context.Auth
    return $context
}

function Get-ModuleFingerprint {
    param($Module)

    $checksum = Convert-PropertyToString (Get-ObjectProperty $Module 'checksum')
    $version = Convert-PropertyToString (Get-ObjectProperty $Module 'version')
    $auditVersion = Convert-PropertyToString (Get-ObjectProperty $Module 'auditVersion')
    $fingerprint = '{0}|{1}|{2}' -f $checksum, $version, $auditVersion
    return [pscustomobject]@{
        Checksum     = $checksum
        Version      = $version
        AuditVersion = $auditVersion
        Fingerprint  = $fingerprint
        HasIdentity  = -not [string]::IsNullOrWhiteSpace($checksum) -or -not [string]::IsNullOrWhiteSpace($version)
    }
}

function Get-ModuleStateKey {
    param($Type, $Id)
    return ('{0}:{1}' -f $Type, $Id)
}

function New-ModuleStateRecord {
    param($Type, $Id, $Name, $Path, $Fingerprint)

    return [pscustomobject]@{
        Key          = (Get-ModuleStateKey $Type $Id)
        Type         = $Type
        Id           = $Id
        Name         = $Name
        Path         = $Path
        Checksum     = $Fingerprint.Checksum
        Version      = $Fingerprint.Version
        AuditVersion = $Fingerprint.AuditVersion
        Fingerprint  = $Fingerprint.Fingerprint
        SavedAt      = (Get-Date).ToString('o')
    }
}

function Import-SlimLogicModuleIndex {
    param([string]$Path, [string]$ApiType, [hashtable]$Map)

    if (-not (Test-Path -LiteralPath $Path)) {
        return
    }

    $items = @(Get-Content -LiteralPath $Path -Raw -Encoding utf8 | ConvertFrom-Json)
    foreach ($item in $items) {
        $id = Get-ObjectProperty $item 'id'
        if ($null -eq $id) {
            continue
        }
        $fp = Get-ModuleFingerprint $item
        if (-not $fp.HasIdentity) {
            continue
        }
        $key = Get-ModuleStateKey $ApiType $id
        $Map[$key] = New-ModuleStateRecord -Type $ApiType -Id $id -Name (Get-ObjectProperty $item 'name') -Path $null -Fingerprint $fp
    }
}

function Import-PreviousModuleState {
    param([string]$RepoRoot)

    $map = @{}
    $statePath = Join-Path $RepoRoot 'module-state.json'
    $indexRoot = Join-Path $RepoRoot 'logicmodules'

    if (Test-Path -LiteralPath $statePath) {
        $rows = @(Get-Content -LiteralPath $statePath -Raw -Encoding utf8 | ConvertFrom-Json)
        foreach ($row in $rows) {
            $key = Get-ObjectProperty $row 'Key'
            if (-not $key) {
                $key = Get-ModuleStateKey (Get-ObjectProperty $row 'Type') (Get-ObjectProperty $row 'Id')
            }
            if ($key) {
                $map[$key] = $row
            }
        }
    }

    foreach ($typeInfo in $script:TypeCatalog) {
        $folder = Get-TypeFolder $typeInfo
        foreach ($dirName in @($folder, $typeInfo.ApiType) | Select-Object -Unique) {
            $typeDir = Join-Path $indexRoot $dirName
            $slimIndex = Join-Path $typeDir '_index.json'
            Import-SlimLogicModuleIndex -Path $slimIndex -ApiType $typeInfo.ApiType -Map $map

            if (-not $map.Keys.Where({ $_ -like "$($typeInfo.ApiType):*" }, 'First')) {
                $legacyInventory = Join-Path $typeDir '_inventory.json'
                if (Test-Path -LiteralPath $legacyInventory) {
                    $inventorySize = (Get-Item -LiteralPath $legacyInventory).Length
                    if ($inventorySize -lt 5MB) {
                        Import-SlimLogicModuleIndex -Path $legacyInventory -ApiType $typeInfo.ApiType -Map $map
                    }
                }
            }
        }
    }

    $catalogPath = Join-Path $RepoRoot 'catalog.csv'
    if ((Test-Path -LiteralPath $catalogPath) -and ((Get-Item -LiteralPath $catalogPath).Length -gt 0)) {
        $rows = @(Import-Csv -LiteralPath $catalogPath)
        foreach ($row in $rows) {
            $key = Get-ModuleStateKey (Get-ApiTypeName $row.Type) $row.Id
            if ($map.ContainsKey($key)) {
                continue
            }
            $fp = [pscustomobject]@{
                Checksum     = Convert-PropertyToString (Get-ObjectProperty $row 'Checksum')
                Version      = Convert-PropertyToString (Get-ObjectProperty $row 'Version')
                AuditVersion = Convert-PropertyToString (Get-ObjectProperty $row 'AuditVersion')
                Fingerprint  = '{0}|{1}|{2}' -f (Convert-PropertyToString (Get-ObjectProperty $row 'Checksum')), (Convert-PropertyToString (Get-ObjectProperty $row 'Version')), (Convert-PropertyToString (Get-ObjectProperty $row 'AuditVersion'))
                HasIdentity  = $true
            }
            if (-not $fp.Checksum -and -not $fp.Version) {
                continue
            }
            $map[$key] = New-ModuleStateRecord -Type $row.Type -Id $row.Id -Name $row.Name -Path $row.Path -Fingerprint $fp
        }
    }

    return $map
}

function Save-ModuleState {
    param([hashtable]$Map, [string]$Path)

    @($Map.Values) | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $Path -Encoding utf8
}

function Get-ExportFileBaseName {
    param(
        [Parameter(Mandatory)]
        [object]$Module,
        [Parameter(Mandatory)]
        [string]$ApiType
    )

    $name = Get-ObjectProperty $Module 'name'
    if ($ApiType -eq 'oids') {
        $oid = Get-ObjectProperty $Module 'oid'
        $categories = Get-ObjectProperty $Module 'categories'
        if ($oid) {
            $name = $oid
        }
        elseif ($categories) {
            $name = ($categories -replace ',', '_')
        }
    }

    $id = Get-ObjectProperty $Module 'id'
    $safe = Get-SafeFileName -Name $name -Fallback "id-$id"
    return '{0}.{1}' -f $safe, $id
}

function Get-TypeFolder {
    param([hashtable]$TypeInfo)

    $folder = $TypeInfo['Folder']
    if ([string]::IsNullOrWhiteSpace($folder)) {
        return $TypeInfo.ApiType
    }
    return $folder
}

function Get-ApiTypeName {
    param([string]$Name)

    if ($Name -in @('propertysources', 'propertyrules')) {
        return 'propertyrules'
    }
    return $Name
}

$script:TypeCatalog = @(
    @{ ApiType = 'datasources';        Folder = 'datasources';      Extension = 'xml';  Format = 'xml';  ListCommand = 'Get-LMDatasource' }
    @{ ApiType = 'propertyrules';      Folder = 'propertysources';  Extension = 'json'; Format = 'file'; ListCommand = 'Get-LMPropertySource' }
    @{ ApiType = 'eventsources';       Folder = 'eventsources';     Extension = 'xml';  Format = 'xml';  ListCommand = 'Get-LMEventSource' }
    @{ ApiType = 'topologysources';    Folder = 'topologysources';  Extension = 'json'; Format = 'file'; ListCommand = 'Get-LMTopologySource' }
    @{ ApiType = 'configsources';      Folder = 'configsources';    Extension = 'xml';  Format = 'xml';  ListCommand = 'Get-LMConfigSource' }
    @{ ApiType = 'logsources';         Folder = 'logsources';       Extension = 'xml';  Format = 'xml';  ListCommand = 'Get-LMLogSource' }
    @{ ApiType = 'functions';          Folder = 'functions';        Extension = 'json'; Format = 'file'; ListCommand = 'Get-LMAppliesToFunction' }
    @{ ApiType = 'oids';               Folder = 'oids';             Extension = 'json'; Format = 'file'; ListCommand = 'Get-LMSysOIDMap' }
    @{ ApiType = 'batchjobs';          Folder = 'batchjobs';        Extension = 'xml';  Format = 'xml';  ListCommand = $null }
    @{ ApiType = 'diagnosticsources';  Folder = 'diagnosticsources'; Extension = 'json'; Format = 'file'; ListCommand = $null }
    @{ ApiType = 'remediationsources'; Folder = 'remediationsources'; Extension = 'json'; Format = 'file'; ListCommand = $null }
)

$script:LMModule = $null
if (-not (Get-Module | Where-Object { $_.ExportedCommands.ContainsKey('Connect-LMAccount') })) {
    Import-Module Logic.Monitor -ErrorAction Stop
}

$status = Initialize-LMSession

if (-not $status) {
    $connectParams = @{ SkipVersionCheck = $true }
    if ($GovCloud) {
        $connectParams.GovCloud = $true
    }

    switch ($PSCmdlet.ParameterSetName) {
        'Cached' {
            $connectParams.UseCachedCredential = $true
            if ($CachedAccountName) {
                $connectParams.CachedAccountName = $CachedAccountName
            }
        }
        'LMv1' {
            $connectParams.AccessId = $AccessId
            $connectParams.AccessKey = $AccessKey
            $connectParams.AccountName = $AccountName
        }
        'Bearer' {
            $connectParams.BearerToken = $BearerToken
            $connectParams.AccountName = $AccountName
        }
        'SessionSync' {
            $connectParams.SessionSync = $true
            $connectParams.AccountName = $AccountName
        }
        default {
            throw @"
Not connected to a LogicMonitor portal.

Reconnect in this same PowerShell window, then re-run:

    Connect-LMAccount -SessionSync -AccountName 'yourportal'
    ./Download-LMLogicModules.ps1

Or pass the session into this script:

    ./Download-LMLogicModules.ps1 -SessionSync -AccountName 'yourportal'
    ./Download-LMLogicModules.ps1 -UseCachedCredential -CachedAccountName 'your-account'
"@
        }
    }

    Write-Host "Connecting to LogicMonitor..." -ForegroundColor Cyan
    Connect-LMAccount @connectParams | Out-Null
    $status = Initialize-LMSession
    if (-not $status) {
        throw 'Connect-LMAccount did not produce a valid session.'
    }
}

$portal = Get-ObjectProperty $status 'Portal'
Write-Host "Connected to portal: $portal (module: $($script:LMModule.Name) $($script:LMModule.Version))" -ForegroundColor Green

$repoRoot = (Resolve-Path -Path (New-Item -ItemType Directory -Force -Path $OutputPath)).Path
$modulesRoot = Join-Path $repoRoot 'logicmodules'
New-Item -ItemType Directory -Force -Path $modulesRoot | Out-Null

$legacyPropertyDir = Join-Path $modulesRoot 'propertyrules'
$propertySourceDir = Join-Path $modulesRoot 'propertysources'
if ((Test-Path -LiteralPath $legacyPropertyDir) -and -not (Test-Path -LiteralPath $propertySourceDir)) {
    Move-Item -LiteralPath $legacyPropertyDir -Destination $propertySourceDir
    Write-Host "Renamed logicmodules/propertyrules -> logicmodules/propertysources" -ForegroundColor Cyan
}
$statePath = Join-Path $repoRoot 'module-state.json'
$previousState = Import-PreviousModuleState -RepoRoot $repoRoot
$moduleState = @{}
foreach ($key in @($previousState.Keys)) {
    $moduleState[$key] = $previousState[$key]
}

if ($SkipUnchanged -or $CheckUpdates) {
    Write-Host ("Loaded {0} previously saved module fingerprints." -f $previousState.Count) -ForegroundColor Cyan
}

$selectedTypes = if ($Type -contains 'All') {
    $script:TypeCatalog
}
else {
    $script:TypeCatalog | Where-Object { $Type -contains $_.ApiType }
}

$catalog = [System.Collections.Generic.List[object]]::new()
$failures = [System.Collections.Generic.List[object]]::new()
$started = Get-Date

foreach ($typeInfo in $selectedTypes) {
    $apiType = $typeInfo.ApiType
    $folder = Get-TypeFolder $typeInfo
    $typeDir = Join-Path $modulesRoot $folder
    New-Item -ItemType Directory -Force -Path $typeDir | Out-Null

    Write-Host "`nListing $apiType..." -ForegroundColor Cyan
    try {
        $modules = @(Get-LogicModulesByType -TypeInfo $typeInfo)
    }
    catch {
        Write-Warning "Failed to list ${apiType}: $_"
        $failures.Add([pscustomobject]@{
            Type   = $apiType
            Id     = $null
            Name   = '(list)'
            Error  = [string]$_
        })
        continue
    }

    $indexRows = foreach ($item in $modules) {
        $fp = Get-ModuleFingerprint $item
        [pscustomobject]@{
            id           = Get-ObjectProperty $item 'id'
            name         = Get-ObjectProperty $item 'name'
            displayName  = Get-ObjectProperty $item 'displayName'
            description  = Convert-PropertyToString (Get-ObjectProperty $item 'description')
            appliesTo    = Convert-PropertyToString (Get-ObjectProperty $item 'appliesTo')
            group        = Convert-PropertyToString (Get-ObjectProperty $item 'group')
            tags         = Convert-PropertyToString (Get-ObjectProperty $item 'tags')
            checksum     = $fp.Checksum
            version      = $fp.Version
            auditVersion = $fp.AuditVersion
        }
    }
    $indexPath = Join-Path $typeDir '_index.json'
    $indexRows | ConvertTo-Json -Depth 4 | Set-Content -LiteralPath $indexPath -Encoding utf8
    Write-Host "  Found $($modules.Count) $apiType (index: $indexPath)"

    $index = 0
    foreach ($module in $modules) {
        $index++
        $id = Get-ObjectProperty $module 'id'
        $name = Get-ObjectProperty $module 'name'
        $displayName = Get-ObjectProperty $module 'displayName'
        $baseName = Get-ExportFileBaseName -Module $module -ApiType $apiType
        $fileName = '{0}.{1}' -f $baseName, $typeInfo.Extension
        $exportPath = Join-Path $typeDir $fileName
        $relativePath = Join-Path 'logicmodules' (Join-Path $folder $fileName)

        $fp = Get-ModuleFingerprint $module
        $stateKey = Get-ModuleStateKey $apiType $id
        $previous = $null
        if ($previousState.ContainsKey($stateKey)) {
            $previous = $previousState[$stateKey]
        }
        $fileExists = Test-Path -LiteralPath $exportPath
        $fingerprintMatches = $previous -and $fp.HasIdentity -and ((Get-ObjectProperty $previous 'Fingerprint') -eq $fp.Fingerprint)

        Write-Progress -Activity "Exporting $apiType" -Status "$name ($id)" -PercentComplete (($index / [math]::Max($modules.Count, 1)) * 100)

        $exportStatus = 'downloaded'
        $errorText = ''

        if ($ListOnly) {
            $exportStatus = 'listed'
        }
        elseif ($CheckUpdates) {
            if (-not $fileExists -and -not $previous) {
                $exportStatus = 'added'
            }
            elseif (-not $fileExists) {
                $exportStatus = 'missing'
            }
            elseif ($fingerprintMatches) {
                $exportStatus = 'unchanged'
            }
            elseif ($previous) {
                $exportStatus = 'updated'
            }
            else {
                $exportStatus = 'added'
            }
        }
        elseif ($SkipUnchanged -and $fileExists -and $fingerprintMatches) {
            $exportStatus = 'unchanged'
        }
        elseif ($SkipExisting -and $fileExists) {
            $exportStatus = 'skipped'
        }
        else {
            try {
                $queryParams = @{
                    format = $typeInfo.Format
                    v      = '3'
                }
                Invoke-LMExportedCommand -Name 'Invoke-LMAPIRequest' -Parameters @{
                    ResourcePath = "/setting/$apiType/$id"
                    Method       = 'GET'
                    QueryParams  = $queryParams
                    OutFile      = $exportPath
                } | Out-Null
                if (-not (Test-Path -LiteralPath $exportPath)) {
                    throw "Export completed but file was not created: $exportPath"
                }
                if ($previous -and -not $fingerprintMatches) {
                    $exportStatus = 'updated'
                }
                else {
                    $exportStatus = 'downloaded'
                }
            }
            catch {
                $exportStatus = 'failed'
                $errorText = [string]$_
                $failures.Add([pscustomobject]@{
                    Type  = $apiType
                    Id    = $id
                    Name  = $name
                    Error = $errorText
                })
                Write-Warning "Failed $apiType ${name} (${id}): $_"
            }

            if ($DelayMs -gt 0) {
                Start-Sleep -Milliseconds $DelayMs
            }
        }

        $catalog.Add([pscustomobject]@{
            Portal       = $portal
            Type         = $folder
            Id           = $id
            Name         = $name
            DisplayName  = $displayName
            Description  = Convert-PropertyToString (Get-ObjectProperty $module 'description')
            AppliesTo    = Convert-PropertyToString (Get-ObjectProperty $module 'appliesTo')
            Group        = Convert-PropertyToString (Get-ObjectProperty $module 'group')
            Version      = $fp.Version
            AuditVersion = $fp.AuditVersion
            Checksum     = $fp.Checksum
            Tags         = Convert-PropertyToString (Get-ObjectProperty $module 'tags')
            FileName     = $fileName
            Path         = $relativePath
            Status       = $exportStatus
            Error        = $errorText
        })

        if ($exportStatus -in @('downloaded', 'updated', 'unchanged', 'skipped', 'listed') -and $fp.HasIdentity) {
            $moduleState[$stateKey] = New-ModuleStateRecord -Type $apiType -Id $id -Name $name -Path $relativePath -Fingerprint $fp
        }
    }

    Write-Progress -Activity "Exporting $apiType" -Completed
    Save-ModuleState -Map $moduleState -Path $statePath
}

$catalogPath = Join-Path $repoRoot 'catalog.csv'
$catalogJsonPath = Join-Path $repoRoot 'catalog.json'
$summaryPath = Join-Path $repoRoot 'download-summary.json'
$failuresPath = Join-Path $repoRoot 'download-failures.csv'

$catalog | Export-Csv -Path $catalogPath -NoTypeInformation -Encoding utf8
$catalog | ConvertTo-Json -Depth 4 | Set-Content -Path $catalogJsonPath -Encoding utf8
Save-ModuleState -Map $moduleState -Path $statePath

$changesPath = Join-Path $repoRoot 'updates.csv'
$changes = @($catalog | Where-Object { $_.Status -in @('added', 'updated', 'missing') })
if ($changes.Count -gt 0) {
    $changes | Export-Csv -Path $changesPath -NoTypeInformation -Encoding utf8
}
elseif (Test-Path -LiteralPath $changesPath) {
    Remove-Item -LiteralPath $changesPath
}

if ($failures.Count -gt 0) {
    $failures | Export-Csv -Path $failuresPath -NoTypeInformation -Encoding utf8
}
elseif (Test-Path -LiteralPath $failuresPath) {
    Remove-Item -LiteralPath $failuresPath
}

$byType = $catalog | Group-Object Type | ForEach-Object {
    [pscustomobject]@{
        Type       = $_.Name
        Total      = $_.Count
        Downloaded = @($_.Group | Where-Object Status -eq 'downloaded').Count
        Updated    = @($_.Group | Where-Object Status -eq 'updated').Count
        Unchanged  = @($_.Group | Where-Object Status -eq 'unchanged').Count
        Added      = @($_.Group | Where-Object Status -eq 'added').Count
        Skipped    = @($_.Group | Where-Object Status -eq 'skipped').Count
        Listed     = @($_.Group | Where-Object Status -eq 'listed').Count
        Failed     = @($_.Group | Where-Object Status -eq 'failed').Count
    }
}

$summary = [pscustomobject]@{
    Portal        = $portal
    Started       = $started
    Finished      = Get-Date
    OutputPath    = $repoRoot
    ListOnly      = [bool]$ListOnly
    CheckUpdates  = [bool]$CheckUpdates
    SkipUnchanged = [bool]$SkipUnchanged
    Totals        = [pscustomobject]@{
        Modules    = $catalog.Count
        Downloaded = @($catalog | Where-Object Status -eq 'downloaded').Count
        Updated    = @($catalog | Where-Object Status -eq 'updated').Count
        Unchanged  = @($catalog | Where-Object Status -eq 'unchanged').Count
        Added      = @($catalog | Where-Object Status -eq 'added').Count
        Skipped    = @($catalog | Where-Object Status -eq 'skipped').Count
        Listed     = @($catalog | Where-Object Status -eq 'listed').Count
        Failed     = $failures.Count
    }
    ByType        = $byType
}

$summary | ConvertTo-Json -Depth 5 | Set-Content -Path $summaryPath -Encoding utf8

Write-Host "`nDownload complete." -ForegroundColor Green
Write-Host "Portal:     $portal"
Write-Host "Repository: $repoRoot"
Write-Host "Catalog:    $catalogPath"
Write-Host ("Modules:    {0}  downloaded={1}  updated={2}  unchanged={3}  failed={4}" -f `
    $summary.Totals.Modules, $summary.Totals.Downloaded, $summary.Totals.Updated, $summary.Totals.Unchanged, $summary.Totals.Failed)

$byType | Format-Table -AutoSize | Out-String | Write-Host

if ($changes.Count -gt 0) {
    Write-Host "Changed modules: $changesPath" -ForegroundColor Yellow
}

if ($summary.Totals.Modules -eq 0 -and -not $ListOnly -and -not $CheckUpdates) {
    throw 'No LogicModules were listed. Reconnect with Connect-LMAccount in this window and re-run.'
}

Write-Host "Search later with:"
Write-Host "  ./Search-LMLogicModules.ps1 -Pattern 'SNMP'"
Write-Host "  rg -n 'your text' logicmodules catalog.csv"
