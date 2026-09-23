---
name: logicmonitor-module-authoring
description: >-
  Authors LogicMonitor LogicModules (DataSource, LogSource, ConfigSource,
  PropertySource, TopologySource, EventSource, DiagnosticSource,
  RemediationSource) using Groovy or PowerShell. Use when creating or
  editing LogicModule scripts, active discovery, collection output,
  import JSON bundles (datapoints, graphs), SNMP/SSH/HTTP/WinRM/WMI
  integration, or choosing between module types.
license: Apache-2.0
compatibility: LogicMonitor Collector; references monitoring-recipes repo
metadata:
  author: logicmonitor
  version: "0.4.0"
---

# LogicMonitor Module Authoring

| Path | Purpose |
|------|---------|
| `schema/` | JSON Schema for import validation |
| `scripts/` | `pack-module.py`, `validate-module.py`, `extract-keys-from-script.py` |
| `assets/module-templates/` | Example bundles (JSON + `collect.*` / `ad.*`) |
| `references/` | Import JSON, script structure, output formats |

## Workflow

Follow these steps in order. Read reference files only when the step requires it.

### 1. Clarify the goal

Determine:
- What data is being collected (numeric metrics, logs, config, properties, topology, diagnostics, remediation)
- Where it comes from (SNMP, SSH, HTTP, WMI, WinRM, file system)
- Whether it is multi-instance (requires Active Discovery)

### 2. Choose module type

If unsure, read [references/module-types.md](references/module-types.md).

Quick rules:
- Numeric time-series → DataSource
- Searchable logs → LogSource (prefer over EventSource for new work)
- Config files → ConfigSource
- Device metadata → PropertySource
- Device relationships → TopologySource
- Troubleshooting on alert → DiagnosticSource
- Corrective action on alert → RemediationSource

### 3. Choose collection approach

For **DataSource** and **ConfigSource**, read [references/collection-modes.md](references/collection-modes.md).

- Built-in methods (SNMP, WMI) when sufficient
- Scripted (Groovy/PowerShell) for complex logic or APIs
- Script mode: runs per instance; BatchScript: runs once per device (multi-instance)

Other module types use their own output models — see import JSON references for Event, Log, Topology, Diag, Remediation.

### 4. Choose language

Read the appropriate reference:
- Cross-platform, SNMP, HTTP, SSH, JDBC, JSON modules → [references/groovy.md](references/groovy.md)
- Windows-native, WMI, WinRM → [references/powershell.md](references/powershell.md)

### 5. Apply script structure

Read [references/script-structure.md](references/script-structure.md).

Follow the canonical section order for the chosen language. For Groovy, use the snippet loader bootstrap — see [references/snippet-loader.md](references/snippet-loader.md) — and prefer platform snippets over raw APIs — see [references/snippets-catalog.md](references/snippets-catalog.md).

### 6. Find a recipe

Check [assets/recipe-index.md](assets/recipe-index.md) for matching patterns in `recipes/<language>/<pattern>/`.

Start from the recipe script. Adapt placeholders and **output format** for your module type. Do not write from scratch if a recipe exists.

### 7. Format output correctly

Read [references/output-formats.md](references/output-formats.md) for the chosen module type.

| Module type | Output type |
|-------------|-------------|
| DataSource | `key=value` or `instance.key=value` |
| PropertySource | `auto.*=value` or `system.categories=value` only |
| Active Discovery | `wildvalue##wildalias` |
| ConfigSource | Raw text (Script) or JSON (BatchScript) |
| TopologySource | JSON `edges` array |
| EventSource / LogSource | JSON `events` array |
| DiagnosticSource / RemediationSource | JSON `{data, format}` (+ `remediationStatus` for Remediation) |

Getting output format wrong is the most common mistake. Match examples exactly.

For DiagnosticSource/RemediationSource alert context, read [references/alert-properties.md](references/alert-properties.md).

### 8. Active Discovery (if multi-instance)

Read [references/active-discovery.md](references/active-discovery.md).

Output: `wildvalue##wildalias` per line. Wildvalue must not contain spaces, `:`, `=`, `\`, or `#`.

### 9. Create module bundle

Read [references/module-deliverable-layout.md](references/module-deliverable-layout.md).

Deliver **one directory per LogicModule**:

- `Vendor_Product_Monitor.json` — datapoints, graphs, metadata
- `collect.groovy` or `collect.ps1` — collection (or single-script module types)
- `ad.groovy` or `ad.ps1` — when `activeDiscovery.params` is used

Copy a starter from [assets/module-templates/README.md](assets/module-templates/README.md). Type-specific JSON fields: [references/import-json-overview.md](references/import-json-overview.md).

### 10. Pack for import

```bash
python scripts/pack-module.py path/to/Vendor_Product_Monitor/
```

Inlines script files into JSON `content` fields before portal import. Use `--check` to detect drift without writing.

### 11. Align script ↔ JSON

Read [references/script-json-alignment.md](references/script-json-alignment.md). Datapoint names, `interpretExpr`, and graph lines must match keys emitted from `collect.*`.

### 12. Validate

```bash
python scripts/validate-module.py path/to/Vendor_Product_Monitor/
```

Uses Python stdlib only (pack sync, datapoints vs `collect.*`, graphs). Optional JSON Schema pass: `--with-schema` if `jsonschema` is installed.

Optional: `python scripts/extract-keys-from-script.py path/to/bundle/ --batchscript`

### 13. Deliver

Hand off the **full bundle directory** (JSON + script files). Run pack before import if the user imports JSON only.

For dashboards on a new DataSource, read [references/dashboard-handoff.md](references/dashboard-handoff.md).

### 14. Validate scripts (checklist)

- [ ] Script follows [script-structure.md](references/script-structure.md) section order
- [ ] No hardcoded credentials — use `hostProps` / device properties
- [ ] Return `0` on success (LogSource discards output on non-zero)
- [ ] Output format matches module type (see step 7)
- [ ] PropertySource: only `auto.*` and `system.categories` — no other `system.*`
- [ ] JSON modules: use `JsonOutput.toJson()` in Groovy, not manual string building
- [ ] Diag/Remediation: handle missing `alertProps` on manual execution
- [ ] Groovy: use `lm.emit` for key=value / AD output; load snippets via loader (not copied source)
- [ ] Groovy: timeout from Settings with buffer; `proto.snmp` / `lm.remote` over raw APIs
- [ ] PowerShell: `Write-Output` for data (not `Write-Host`); validate unset `##prop##` tokens
- [ ] BatchScript: use `Write-Output` not `Write-Host` in PowerShell
- [ ] Placeholders replaced with actual values
- [ ] API auth tokens: consider `lm.cache` or `ScriptCache` on Collector 29.100+ — see [references/script-cache.md](references/script-cache.md)
- [ ] Module Snippets referenced (not copied) where applicable — see [references/snippets-catalog.md](references/snippets-catalog.md)
- [ ] `validate-module.py` passes on the bundle after `pack-module.py`
- [ ] DataSource: every `namevalue` datapoint matches a collect script key; graph lines reference defined datapoints
- [ ] Each datapoint has `originId` before import

## Reference files

| File | When to read |
|------|--------------|
| [module-deliverable-layout.md](references/module-deliverable-layout.md) | Bundle layout, collect/ad files, pack workflow |
| [import-json-overview.md](references/import-json-overview.md) | Portal import JSON shape and type codes |
| [datasource-import-json.md](references/datasource-import-json.md) | DataSource datapoints, graphs, AD |
| [configsource-import-json.md](references/configsource-import-json.md) | ConfigSource JSON |
| [scripted-module-import-json.md](references/scripted-module-import-json.md) | Property / Diag / Rem JSON |
| [eventsource-import-json.md](references/eventsource-import-json.md) | Scripted EventSource JSON |
| [script-json-alignment.md](references/script-json-alignment.md) | Script keys vs datapoints and graphs |
| [dashboard-handoff.md](references/dashboard-handoff.md) | Dashboard `dataSourceFullName` after import |
| [script-structure.md](references/script-structure.md) | Arranging any Groovy or PowerShell script |
| [snippet-loader.md](references/snippet-loader.md) | Bootstrap, version pins, collector resolution |
| [snippets-catalog.md](references/snippets-catalog.md) | Snippet APIs (proto.snmp, lm.emit, lm.remote, etc.) |
| [module-types.md](references/module-types.md) | Choosing or confirming module type |
| [output-formats.md](references/output-formats.md) | Formatting script output |
| [active-discovery.md](references/active-discovery.md) | Multi-instance discovery |
| [collection-modes.md](references/collection-modes.md) | DataSource/ConfigSource Script vs BatchScript |
| [alert-properties.md](references/alert-properties.md) | DiagnosticSource / RemediationSource alert context |
| [groovy.md](references/groovy.md) | Groovy APIs, snippets, variables, JSON output |
| [powershell.md](references/powershell.md) | PowerShell, WinRM, WMI, output |
| [module-snippets.md](references/module-snippets.md) | Platform snippet rules and requirements |
| [script-cache.md](references/script-cache.md) | Caching auth tokens between polls (Collector 29.100+) |

## Official documentation

Link to [LogicMonitor Support](https://www.logicmonitor.com/support/) for product-specific portal configuration. This skill covers authoring patterns, not duplicate product docs.
