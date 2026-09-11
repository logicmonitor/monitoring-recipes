<#
.SYNOPSIS
    Searches a local LogicModule repository created by Download-LMLogicModules.ps1.

.PARAMETER Pattern
    Text or regex to search for.

.PARAMETER Type
    Limit search to one or more LogicModule types.

.PARAMETER Path
    Repository root. Defaults to this script's directory.

.PARAMETER CatalogOnly
    Search catalog.csv (name, description, appliesTo, tags) instead of export file contents.

.PARAMETER SimpleMatch
    Treat Pattern as literal text instead of a regular expression.

.EXAMPLE
    ./Search-LMLogicModules.ps1 -Pattern 'SNMP_Network_Interfaces'

.EXAMPLE
    ./Search-LMLogicModules.ps1 -Pattern 'appliesTo.*isWindows' -Type datasources
#>
[CmdletBinding()]
param(
    [Parameter(Mandatory, Position = 0)]
    [string]$Pattern,

    [ValidateSet(
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
    [string[]]$Type,

    [string]$Path = $PSScriptRoot,

    [switch]$CatalogOnly,
    [switch]$SimpleMatch
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-SearchTypeNames {
    param([string[]]$Requested)

    foreach ($t in $Requested) {
        if ($t -in @('propertysources', 'propertyrules')) {
            'propertysources'
            'propertyrules'
        }
        else {
            $t
        }
    }
}

$repoRoot = (Resolve-Path -Path $Path).Path
$catalogPath = Join-Path $repoRoot 'catalog.csv'
$modulesRoot = Join-Path $repoRoot 'logicmodules'

if ($CatalogOnly) {
    if (-not (Test-Path -LiteralPath $catalogPath)) {
        throw "Catalog not found: $catalogPath. Run Download-LMLogicModules.ps1 first."
    }

    $rows = Import-Csv -Path $catalogPath
    if ($Type) {
        $matchTypes = @(Get-SearchTypeNames $Type | Select-Object -Unique)
        $rows = $rows | Where-Object { $matchTypes -contains $_.Type }
    }

    $selectParams = @{
        Pattern     = $Pattern
        SimpleMatch = $SimpleMatch
        Property    = @('Type', 'Id', 'Name', 'DisplayName', 'Description', 'AppliesTo', 'Tags', 'Path')
    }

    $hits = $rows | Select-String @selectParams
    if (-not $hits) {
        Write-Host "No catalog matches for '$Pattern'."
        return
    }

    $hits | ForEach-Object {
        [pscustomobject]@{
            Type    = $_.Type
            Id      = $_.Id
            Name    = $_.Name
            Path    = $_.Path
            MatchOn = $_.AppliesTo, $_.Description, $_.Tags, $_.DisplayName, $_.Name |
                Where-Object { $_ -and ($_ -match $Pattern -or ($SimpleMatch -and $_ -like "*$Pattern*")) } |
                Select-Object -First 1
        }
    } | Format-Table -AutoSize
    return
}

if (-not (Test-Path -LiteralPath $modulesRoot)) {
    throw "LogicModule folder not found: $modulesRoot. Run Download-LMLogicModules.ps1 first."
}

$searchRoots = if ($Type) {
    foreach ($t in @(Get-SearchTypeNames $Type | Select-Object -Unique)) {
        Join-Path $modulesRoot $t
    }
}
else {
    @($modulesRoot)
}

$files = foreach ($root in $searchRoots) {
    if (Test-Path -LiteralPath $root) {
        Get-ChildItem -Path $root -Recurse -File -Include *.xml, *.json, *.csv |
            Where-Object { $_.Name -ne '_inventory.json' }
    }
}

if (-not $files) {
    throw "No LogicModule files found under $modulesRoot."
}

$selectParams = @{
    Path        = $files.FullName
    Pattern     = $Pattern
    SimpleMatch = $SimpleMatch
}

$results = Select-String @selectParams
if (-not $results) {
    Write-Host "No matches for '$Pattern'."
    return
}

$results |
    Select-Object Path, LineNumber, Line |
    Format-Table -Wrap -AutoSize
