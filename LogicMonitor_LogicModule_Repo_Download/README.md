# LogicMonitor LogicModule repository

Local copy of LogicModules from a LogicMonitor portal, for search and later comparison.

Scripts use the [Logic.Monitor PowerShell module](https://github.com/logicmonitor/lm-powershell-module) against your connected portal.

## Prerequisites

- PowerShell 7 (`pwsh`)
- `Logic.Monitor` module:

```powershell
Install-Module -Name Logic.Monitor -Scope CurrentUser
```

- A portal session in **the same PowerShell window** that runs the download. This repo is already set up for SessionSync:

```powershell
Import-Module /Users/cameron.compton/lm-powershell-module/Dev.Logic.Monitor.psd1 -Force
Import-Module /Users/cameron.compton/Logic.Monitor.SE/Dev.Logic.Monitor.SE.psd1 -Force
Connect-LMAccount -SessionSync -AccountName lmcameroncompton
```

Cached API credentials also work:

```powershell
Connect-LMAccount -UseCachedCredential
```

Or pass credentials into the download script:

```powershell
./Download-LMLogicModules.ps1 -UseCachedCredential -CachedAccountName 'your-account'
./Download-LMLogicModules.ps1 -AccessId $id -AccessKey $key -AccountName 'yourportal'
./Download-LMLogicModules.ps1 -SessionSync -AccountName 'yourportal'
```

Run every command in the **same** `pwsh` session. A new window does not inherit the login.

## First download

Leave any in-progress first download running until it prints `Download complete`.

To start a full export of every LogicModule type:

```powershell
./Download-LMLogicModules.ps1
```

That writes XML/JSON exports under `logicmodules/` plus `catalog.csv` for later search. A full portal can take **45–90 minutes**.

If the first run is interrupted:

```powershell
./Download-LMLogicModules.ps1 -SkipExisting
```

`-SkipExisting` only checks that a local file exists. It does **not** detect portal edits.

Limit to one type if needed:

```powershell
./Download-LMLogicModules.ps1 -Type datasources
```

Valid `-Type` values: `datasources`, `propertysources`, `eventsources`, `topologysources`, `configsources`, `logsources`, `functions`, `oids`, `batchjobs`, `diagnosticsources`.

PropertySources are stored in `logicmodules/propertysources/` (`propertyrules` is still accepted as an alias).

PropertySources only:

```powershell
./Download-LMLogicModules.ps1 -Type propertysources
./Download-LMLogicModules.ps1 -Type propertysources -SkipUnchanged
./Search-LMLogicModules.ps1 -Pattern 'addCategory' -Type propertysources
```

## Check and update after you edit modules

After the first download finishes, later runs compare each portal module’s **checksum** and **version** to `module-state.json` (and `catalog.csv` / `_index.json` if needed). Only new or changed modules are treated as updates.

**See what changed (no download):**

```powershell
./Download-LMLogicModules.ps1 -CheckUpdates
```

This lists every module as `added`, `updated`, `unchanged`, or `missing`. Changed rows are written to `updates.csv`.

**Download only the ones that changed:**

```powershell
./Download-LMLogicModules.ps1 -SkipUnchanged
```

Unchanged files are left in place. New and updated modules are exported again.

Typical loop after you save LogicModules in the portal:

```powershell
Connect-LMAccount -SessionSync -AccountName lmcameroncompton
./Download-LMLogicModules.ps1 -CheckUpdates
./Download-LMLogicModules.ps1 -SkipUnchanged
```

## Search

After files exist locally:

```powershell
./Search-LMLogicModules.ps1 -Pattern 'SNMP_Network_Interfaces'
./Search-LMLogicModules.ps1 -Pattern 'isWindows' -Type datasources
./Search-LMLogicModules.ps1 -Pattern 'disk' -CatalogOnly
```

`-CatalogOnly` searches names, descriptions, appliesTo, and tags in `catalog.csv` instead of file contents.

You can also grep the tree directly:

```powershell
rg -n 'your text' logicmodules catalog.csv
```

## Layout

| Path | Purpose |
| --- | --- |
| `logicmodules/<type>/` | Exported LogicModule files (`.xml` or `.json`) |
| `logicmodules/<type>/_index.json` | Slim per-type index (id, name, checksum, version) |
| `catalog.csv` / `catalog.json` | Searchable catalog of all modules |
| `module-state.json` | Fingerprints used by `-CheckUpdates` / `-SkipUnchanged` |
| `updates.csv` | Added, updated, or missing modules from the last check/sync |
| `download-summary.json` | Counts from the last run |
| `download-failures.csv` | Per-module export errors, if any |
| `Download-LMLogicModules.ps1` | Download / check / incremental update |
| `Search-LMLogicModules.ps1` | Local search helper |

## Notes

- Keep the SessionSync server running if you connect with `-SessionSync`.
- Do not mix Gallery `Logic.Monitor` and `Dev.Logic.Monitor` in a way that splits the login. Connect, then run the download in that same window.
- The first full download does not need `-SkipUnchanged`. Use that only after `catalog.csv` / `module-state.json` exist.
- LogicModule exports can include collection scripts and alert thresholds. Treat this folder as internal.
