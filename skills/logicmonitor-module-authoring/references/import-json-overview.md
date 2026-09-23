# Import JSON overview

LogicMonitor portal **LogicModule** imports use a JSON shape that differs from dashboard DataSource exports:

| Portal import | Dashboard widget export |
|---------------|-------------------------|
| `datapoints` (lowercase) | `dataPoints` |
| `displayedAs` | `displayName` |

## Module type (`type` field)

| `type` | Module | Schema file |
|--------|--------|-------------|
| `0` | DataSource | `datasource.schema.json` |
| `1` | EventSource | `eventsource.schema.json` (scripted) |
| `5` | PropertySource | `script-module.schema.json` |
| `6` | ConfigSource | `configsource.schema.json` |
| `11` | DiagnosticSource | `script-module.schema.json` |
| `12` | RemediationSource | `script-module.schema.json` |

## Common top-level fields

| Field | Notes |
|-------|--------|
| `name` | LogicModule name (`Vendor_Product_Monitor`) |
| `displayedAs` | UI display name (DataSource / ConfigSource) |
| `description` | Short description |
| `appliesTo` | AppliesTo function string |
| `searchKeywords` | Comma-separated tags |
| `technicalNotes` | Operator notes (markdown/plain) |
| `group` | Exchange folder; often `""` for new modules |
| `version` | Integer; portal manages on save |

## Omit on new modules

Do not copy from vendor exports:

- `registryMetadata`
- `integrationMetadata`

These are registry/exchange bookkeeping, not required for custom imports.

## Script embedding

Scripts live in bundle files (`collect.*`, `ad.*`). After packing:

```json
"collectionAttrs": { "type": "groovy", "content": "..." }
```

or for PropertySource / Diag / Rem:

```json
"script": { "type": "groovy", "content": "..." }
```

See [module-deliverable-layout.md](module-deliverable-layout.md).

## Type-specific references

| Module | Reference |
|--------|-----------|
| DataSource | [datasource-import-json.md](datasource-import-json.md) |
| ConfigSource | [configsource-import-json.md](configsource-import-json.md) |
| Property / Diag / Rem | [scripted-module-import-json.md](scripted-module-import-json.md) |
| EventSource (script) | [eventsource-import-json.md](eventsource-import-json.md) |
| Script ↔ JSON rules | [script-json-alignment.md](script-json-alignment.md) |
| Dashboards | [dashboard-handoff.md](dashboard-handoff.md) |
