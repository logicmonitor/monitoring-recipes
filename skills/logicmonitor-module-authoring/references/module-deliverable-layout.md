# Module deliverable layout

## Directory per LogicModule

```
Vendor_Product_Monitor/
  Vendor_Product_Monitor.json
  collect.groovy          # or collect.ps1
  ad.groovy               # or ad.ps1 — only when activeDiscovery uses ad_script
```

| File | Role |
|------|------|
| `*.json` | Portal import document: metadata, datapoints, graphs, AD settings |
| `collect.*` | **Source of truth** for collection (or single-script module types) |
| `ad.*` | **Source of truth** for active discovery output (`wildvalue##wildalias`) |

## Script file → JSON path

| Module shape | `collect.*` inlined into | `ad.*` inlined into |
|--------------|--------------------------|---------------------|
| DataSource (`type: 0`) | `collectionAttrs` | `activeDiscovery.params` |
| ConfigSource (`type: 6`) | `collectionAttrs` | `activeDiscovery.params` |
| EventSource `scriptevent` (`type: 1`) | `collectionAttrs` | — |
| PropertySource (`type: 5`) | `script` | — |
| DiagnosticSource (`type: 11`) | `script` | — |
| RemediationSource (`type: 12`) | `script` | — |

Pack with:

```bash
python scripts/pack-module.py path/to/Vendor_Product_Monitor/
```

Use `--check` to verify JSON `content` matches files without writing.

## Language pairing

| File | `type` field in JSON |
|------|----------------------|
| `collect.groovy`, `ad.groovy` | `groovy` |
| `collect.ps1`, `ad.ps1` | `powershell` |

Do not mix Groovy and PowerShell in one module.

## Workflow

1. Author `collect.*` (and `ad.*` if needed) using [script-structure.md](script-structure.md) and [output-formats.md](output-formats.md).
2. Create or copy JSON from [assets/module-templates/](../assets/module-templates/README.md); define datapoints and graphs to match script keys.
3. Run `pack-module.py` before portal import.
4. Run `validate-module.py` on the bundle directory.
5. Deliver the **whole directory** to the user (scripts + JSON).

Embedded `content` strings in exports from the portal are derived artifacts; in this repo they are regenerated from the script files.
