# Output Formats

See also: [docs/concepts/output-formats.md](../../../docs/concepts/output-formats.md)

## Quick reference

| Type | Format |
|------|--------|
| Active Discovery | `wildvalue##wildalias` |
| DataSource Script | `key=value` |
| DataSource BatchScript | `wildvalue.key=value` (`wildvalue` from AD, not the displayed name) |
| PropertySource | `auto.*=value` or `system.categories=value` only |
| ConfigSource Script | Raw config text |
| ConfigSource BatchScript | JSON `data.<wildvalue>.configuration` |
| TopologySource | JSON `edges` array with type/from/to ERIs |
| EventSource | JSON `events` array (happenedOn, severity, message required) |
| LogSource | JSON `events` array (`message` required, exit 0) |
| DiagnosticSource | JSON `{data, format}` |
| RemediationSource | JSON `{data, format, remediationStatus}` |

## BatchScript prefix is wildvalue

`emit.dp(wildvalue, "metric", value)` prints `wildvalue.metric=value`. That
prefix must be the Active Discovery **wildvalue** (API id, SNMP index, station
id). It is not the instance display name and not the map key of
`datasourceinstanceProps` (that key is often `DataSourceName-alias`). Using the
display name produces `param not found in output` for every datapoint.

Read wildvalue from `instanceProperties.get("wildvalue")` (Collector 29.105+),
or emit using the same identifier AD already printed.

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
