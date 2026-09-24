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
compatibility: LogicMonitor Collector; self-contained authoring bundle
metadata:
  author: logicmonitor
  version: "0.6.0"
---

# LogicMonitor Module Authoring

| Path | Purpose |
|------|---------|
| `schema/` | JSON Schema for supported import validation |
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
- External/SaaS API (not the device): read [references/external-api-datasource.md](references/external-api-datasource.md)

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

Other module types use their own output models. This bundle includes public-facing field references and starters for Event, Property, Config, Diagnostic, Remediation, Log, and Topology modules. The packer and full semantic validator currently target the import-bundle types listed in [schema/README.md](schema/README.md); LogSource and TopologySource require platform-specific payload handling. Portal exports are useful for confirming account-specific optional fields, but are not required to determine the basic JSON shape. Do not copy portal bookkeeping fields such as `version`, `registryMetadata`, or internal tooling markers into a greenfield bundle.

### 4. Choose language

Read the appropriate reference:
- Cross-platform, SNMP, HTTP, SSH, JDBC, JSON modules → [references/groovy.md](references/groovy.md)
- Windows-native, WMI, WinRM → [references/powershell.md](references/powershell.md)

### 5. Apply script structure

Read [references/script-structure.md](references/script-structure.md).

Follow the canonical section order for the chosen language. For Groovy, use the snippet loader bootstrap — see [references/snippet-loader.md](references/snippet-loader.md) — prefer platform snippets over raw APIs — see [references/snippets-catalog.md](references/snippets-catalog.md) — and follow [references/groovy.md](references/groovy.md#script-scoping-locals-vs-helpers) when helpers call `emit` or other loaded snippets.

### 6. Start from a bundled pattern

Copy the closest starter from [assets/module-templates/README.md](assets/module-templates/README.md) or adapt the bundled HTTP example. The bundled assets are sufficient for standalone use. Use [assets/recipe-index.md](assets/recipe-index.md) only as an optional index when the full monitoring-recipes repository is available.

Adapt placeholders and the required output format; do not copy a collection pattern across module types without changing its output contract.

### 7. Format output correctly

Read [references/output-formats.md](references/output-formats.md) for the chosen module type.

| Module type | Output type |
|-------------|-------------|
| DataSource | `key=value` or `instance.key=value` |
| PropertySource | `auto.*=value` or `system.categories=value` only |
| Active Discovery | Groovy: `emit.instance(...)`; PowerShell: `Write-Output "wv##alias..."` — see [active-discovery.md](references/active-discovery.md) |
| ConfigSource | Raw text (Script) or JSON (BatchScript) |
| TopologySource | JSON `edges` array; do not assume `lm.topo` helper output matches a hand-authored payload |
| EventSource | JSON `events` array with `happenedOn`, `severity`, and `message` |
| LogSource | JSON `events` array with `message`; exit 0 |
| DiagnosticSource / RemediationSource | JSON `{data, format}` (+ `remediationStatus` for Remediation) |

Getting output format wrong is the most common mistake. Match examples exactly.

For DiagnosticSource/RemediationSource alert context, read [references/alert-properties.md](references/alert-properties.md).

### 8. Active Discovery (if multi-instance)

Read [references/active-discovery.md](references/active-discovery.md).

For Groovy, use `emit.instance(...)`; for PowerShell, write the documented discovery line format. **Prefer ILPs:** when discovery returns stable metadata (version, type, role, feature flags), include `auto.*` properties so instances carry filter and grouping context.

### 9. Create module bundle

Read [references/module-deliverable-layout.md](references/module-deliverable-layout.md).

Deliver **one directory per LogicModule**:

- `Vendor_Product_Monitor.json` — datapoints, graphs, metadata
- `collect.groovy` or `collect.ps1` — collection (or single-script module types)
- `ad.groovy` or `ad.ps1` — when `activeDiscovery.params` is used

Copy a starter from [assets/module-templates/README.md](assets/module-templates/README.md). Type-specific JSON fields: [references/import-json-overview.md](references/import-json-overview.md).

### 10. Pack for import

```bash
python3 scripts/pack-module.py path/to/Vendor_Product_Monitor/
```

Inlines script files into JSON `content` fields before portal import. Use `--check` to detect drift without writing.

### 11. Align script ↔ JSON

Read [references/script-json-alignment.md](references/script-json-alignment.md). Datapoint `name` and graph lines use the short metric id; `interpretExpr` must match script output keys — for **batchscript + multi-instance**, use `##WILDVALUE##.<name>` in JSON (not bare `<name>`). Script emits `wildvalue.<name>=...` via `emit.dp(wild, "<name>", value)`.

### 12. Validate

```bash
python3 scripts/validate-module.py path/to/Vendor_Product_Monitor/
```

Uses Python stdlib only (pack sync, datapoints vs `collect.*`, graphs). Heuristic key extraction matches `emit.dp("literal", ...)` — see [script-json-alignment.md](references/script-json-alignment.md).

```bash
python3 scripts/validate-module.py --strict-greenfield path/to/bundle/
```

Optional: `--with-schema` if `jsonschema` is installed; `extract-keys-from-script.py` for key listing.

### 13. Deliver

Hand off the **full bundle directory** (JSON + script files). Run pack before import if the user imports JSON only.

For dashboards on a new DataSource, read [references/dashboard-handoff.md](references/dashboard-handoff.md).

### 14. Final checklist

- [ ] Script follows [script-structure.md](references/script-structure.md) section order
- [ ] No hardcoded credentials — use `hostProps` / device properties
- [ ] Return `0` on success (LogSource discards output on non-zero)
- [ ] Output format matches module type (see step 7)
- [ ] Groovy: `modLoader.withBinding(getBinding())` before snippet loads (or `emit.binding = binding` fallback) — [snippet-loader.md](references/snippet-loader.md)
- [ ] Groovy: keep snippet handles local with `def` and pass them explicitly to helper methods; use binding-style assignment only for legacy compatibility — [groovy.md](references/groovy.md#script-scoping-locals-vs-helpers)
- [ ] Groovy: use `lm.debug` for debug, info, warning, and error messages; never add ad hoc `debugPrint` helpers
- [ ] AD: use the language-specific format; attach useful, nonempty `auto.*` ILPs when available
- [ ] PowerShell: `Write-Output` for data (not `Write-Host`); validate unset `##prop##` tokens
- [ ] `validate-module.py` passes on the bundle after `pack-module.py`
- [ ] JSON aligns with [script-json-alignment.md](references/script-json-alignment.md) and the greenfield rules in [import-json-overview.md](references/import-json-overview.md)
- [ ] LogicModule `name` uses underscores; `displayedAs` uses spaces and contains no internal `-`
- [ ] AppliesTo is specific enough to avoid running on unrelated resources
- [ ] Datapoints have useful descriptions, units, and a graph, alert, or overview use
- [ ] Queries are bounded and efficient; pagination, retries, and timeouts are intentional
- [ ] No credentials, tokens, or sensitive device data are emitted or logged
- [ ] LogSources include resource mapping; ConfigSources define meaningful checks

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
| [dashboard-handoff.md](references/dashboard-handoff.md) | Dashboard handoff data needed after import |
| [external-api-datasource.md](references/external-api-datasource.md) | SaaS/public API DataSources |
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
| [portable-guidance.md](references/portable-guidance.md) | Standalone security, performance, naming, and quality guidance |

## Official documentation

Link to [LogicMonitor Support](https://www.logicmonitor.com/support/) for product-specific portal configuration. This skill covers authoring patterns, not duplicate product docs.
