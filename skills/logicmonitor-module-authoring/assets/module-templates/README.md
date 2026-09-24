# Module template bundles

Copy a directory as the starting point for a new LogicModule. Each bundle contains:

- `*.json` — import metadata (datapoints, graphs, AD config)
- `collect.groovy` or `collect.ps1` — collection script (source of truth)
- `ad.groovy` or `ad.ps1` — active discovery when required

## Templates

| Directory | Module type |
|-----------|-------------|
| [datasource-script-single/](datasource-script-single/) | DataSource, `script`, single-instance |
| [datasource-script-single-powershell/](datasource-script-single-powershell/) | DataSource, `script`, single-instance, PowerShell |
| [datasource-batchscript-ad/](datasource-batchscript-ad/) | DataSource, `batchscript` + AD |
| [configsource-script-ad/](configsource-script-ad/) | ConfigSource + AD |
| [propertysource-groovy/](propertysource-groovy/) | PropertySource |
| [diagnosticsource-groovy/](diagnosticsource-groovy/) | DiagnosticSource |
| [remediationsource-groovy/](remediationsource-groovy/) | RemediationSource |
| [eventsource-scriptevent/](eventsource-scriptevent/) | EventSource `scriptevent` |
| [logsource-groovy/](logsource-groovy/) | Scripted LogSource field starter |
| [topologysource-groovy/](topologysource-groovy/) | Scripted TopologySource field starter |

## Usage

1. Copy template folder to your module name, e.g. `Vendor_Product_Monitor/`.
2. Rename JSON to `Vendor_Product_Monitor.json` and set `name`, `displayedAs`, `appliesTo`.
3. Replace placeholder datapoint names and graph lines to match `collect.*` keys.
4. From the skill root:

```bash
python3 scripts/pack-module.py path/to/Vendor_Product_Monitor/
python3 scripts/validate-module.py path/to/Vendor_Product_Monitor/
```

5. Deliver the directory (JSON + script files) to the user. The packer and semantic validator cover the import-bundle types listed in `schema/README.md`; LogSource and TopologySource use platform-specific payload shapes, so validate their field JSON against the standalone reference schema and confirm any account-specific optional fields in the portal before import.
