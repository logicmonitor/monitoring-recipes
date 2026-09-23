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

## Portal round-trip

1. Author bundle in repo (`collect.*`, `ad.*`, JSON) with greenfield rules in [import-json-overview.md](import-json-overview.md) (AD `discoveryInterval` default **`60m`**).
2. `pack-module.py` → import JSON into portal.
3. Set AD schedule, appliesTo refinements, and alerts in the UI.
4. **Export once** from portal and keep as **reference** for AD intervals (`0m`, `15m`, `60m`, `1440m`), optional fields, and graph shapes.
5. Continue editing **script files** as source of truth; re-pack before the next import.

Embedded `content` in portal exports is a snapshot; repo bundles regenerate it via `pack-module.py`.
