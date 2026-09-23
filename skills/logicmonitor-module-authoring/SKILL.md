---
name: logicmonitor-module-authoring
description: >-
  Authors LogicMonitor LogicModules (DataSource, LogSource, ConfigSource,
  PropertySource, TopologySource, EventSource, DiagnosticSource,
  RemediationSource) using Groovy or PowerShell. Use when creating or
  editing LogicModule scripts, active discovery, collection output,
  SNMP/SSH/HTTP/WinRM/WMI integration, or choosing between module types.
license: Apache-2.0
compatibility: LogicMonitor Collector; references monitoring-recipes repo
metadata:
  author: logicmonitor
  version: "0.3.0"
---

# LogicMonitor Module Authoring

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

Other module types use their own output models — skip to step 6 for JSON-based types (Event, Log, Topology, Diag, Remediation).

### 4. Choose language

Read the appropriate reference:
- Cross-platform, SNMP, HTTP, SSH, JDBC, JSON modules → [references/groovy.md](references/groovy.md)
- Windows-native, WMI, WinRM → [references/powershell.md](references/powershell.md)

### 5. Apply script structure

Read [references/script-structure.md](references/script-structure.md).

Follow the canonical section order for the chosen language. For Groovy, use the snippet loader bootstrap and prefer platform snippets over raw APIs — see [references/snippets-catalog.md](references/snippets-catalog.md).

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

### 9. Validate before finishing

Checklist:
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

## Reference files

| File | When to read |
|------|--------------|
| [script-structure.md](references/script-structure.md) | Arranging any Groovy or PowerShell script |
| [snippets-catalog.md](references/snippets-catalog.md) | Loading platform snippets (proto.snmp, lm.remote, etc.) |
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
