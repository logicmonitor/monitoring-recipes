# Output Formats

See also: [docs/concepts/output-formats.md](../../../docs/concepts/output-formats.md)

## Quick reference

| Type | Format |
|------|--------|
| Active Discovery | `wildvalue##wildalias` (+ optional `##description`, `####auto.*=...&...` ILPs) — use `emit.instance()` |
| DataSource Script | `key=value` via `emit.dp("key", value)` |
| DataSource BatchScript | `instance.key=value` via `emit.dp(wild, "key", value)` |
| PropertySource | `auto.*=value` or `system.categories=value` only via `emit.property()` |
| ConfigSource Script | Raw config text |
| ConfigSource BatchScript | JSON `data.<wildvalue>.configuration` |
| TopologySource | JSON `edges` array with type/from/to ERIs |
| EventSource | JSON `events` array (happenedOn, severity, message required) |
| LogSource | JSON `events` array (`message` required, exit 0) |
| DiagnosticSource | JSON `{data, format}` |
| RemediationSource | JSON `{data, format, remediationStatus}` |

Groovy: bound `modLoader` + `emit` — [snippet-loader.md](snippet-loader.md).

## PropertySource rule

Only `auto.*` and `system.categories`. No other `system.*` properties.

## DiagnosticSource / RemediationSource

```groovy
import groovy.json.JsonOutput
print JsonOutput.toJson([data: "output", format: "markdown", remediationStatus: "true"])
return 0
```

`remediationStatus` is a string. DiagnosticSource omits it; RemediationSource UI shows N/A without it.

## Alert-triggered diag/remediation

Use `alertProps.get("alert.datapoint")` or `##ALERT.DATAPOINT##` tokens. Empty on manual run — handle both paths.

## Return codes

`0` = success. Non-zero = failure. AD: non-zero preserves instances. LogSource: non-zero discards output.
