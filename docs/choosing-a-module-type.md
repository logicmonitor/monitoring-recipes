# Choosing a Module Type

LogicModules are the building blocks of monitoring in LogicMonitor. Pick the type that matches **what you're collecting** and **how you'll use it**.

## Quick reference

| Module type | Collects | Searchable | Output format | Guide |
|-------------|----------|------------|---------------|-------|
| [DataSource](module-types/datasource.md) | Time-series metrics | Yes (graphs, alerts) | `key=value` or `instance.key=value` | Most common — CPU, memory, throughput |
| [LogSource](module-types/logsource.md) | Logs → LM Logs | Yes (query, alert, aggregate) | JSON `events` with `message` | **Preferred for logs** |
| [EventSource](module-types/eventsource.md) | Events (alert only) | No | JSON `events` with severity | Only when search isn't needed |
| [ConfigSource](module-types/configsource.md) | Config file contents | Diff-based alerting | Raw text or JSON | Config drift, compliance |
| [PropertySource](module-types/propertysource.md) | Device metadata | Properties on device | `auto.*` or `system.categories` | Inventory, grouping, CMDB |
| [TopologySource](module-types/topologysource.md) | Device relationships | Topology maps | JSON `edges` | Dependencies, service maps |
| [DiagnosticSource](module-types/diagnosticsource.md) | Troubleshooting data | On-demand / alert-triggered | JSON `{data, format}` | Process lists, debug dumps |
| [RemediationSource](module-types/remediationsource.md) | Corrective actions | On-demand / alert-triggered | JSON `{data, format, remediationStatus}` | Restart services, runbooks |

All output format details: [Output Formats](concepts/output-formats.md)

## Decision guide

```
What are you collecting?
│
├─ Numeric metrics over time? ──────────────► DataSource
│
├─ Log or event data?
│   ├─ Need to search/query/aggregate? ─────► LogSource (preferred)
│   └─ Only alert, never search? ───────────► EventSource
│
├─ Configuration files? ────────────────────► ConfigSource
│
├─ Device properties / inventory? ──────────► PropertySource
│
├─ Relationships between devices? ──────────► TopologySource
│
└─ Respond to an alert or manual request?
    ├─ Gather diagnostic info? ─────────────► DiagnosticSource
    └─ Take corrective action? ─────────────► RemediationSource
```

## EventSource vs LogSource

**Prefer LogSource for new work.** LogSources ingest into LM Logs where data can be queried, alerted on, and aggregated. EventSources are not searchable — use them only when you need events that should **always** generate alerts and search is never required.

## PropertySource property rules

PropertySources can only set:

- `auto.*` custom properties (e.g. `auto.serial=ABC123`)
- `system.categories` (special property for tagging and appliesTo)

You **cannot** set other `system.*` properties from script output.

## Diagnostic vs Remediation

Both return JSON with `data` and optional `format` (`markdown` or plain text). RemediationSource adds `remediationStatus` as a **string** shown separately in the UI.

When triggered by alerts, both can access alert context via `alertProps` / `##ALERT.*##` tokens. Handle missing alert context for manual runs.

## Module Snippets

Platform-managed reusable code used by newer LogicModules. Customers cannot modify snippets. See [Module Snippets](concepts/module-snippets.md).

## Next steps

1. Read the module-type page for your choice
2. Check [Output Formats](concepts/output-formats.md) for exact script output syntax
3. Find a matching recipe in [`recipes/`](../recipes/README.md)
