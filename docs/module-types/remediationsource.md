# RemediationSource

Take corrective action to resolve or respond to alert conditions — manually or automatically.

## Purpose

Execute remediation actions when alerts fire or when triggered manually — e.g. restart a service, clear a cache, or run a runbook script.

## When to use

- Automated remediation via Action Rules and Action Chains
- Manual runbook execution from the portal
- Alert-context-driven corrective actions

## Output format

RemediationSources return **JSON** output (plain-text fallback supported). JSON provides metadata for UI rendering and remediation status.

### JSON structure

```json
{"data": "Example Output", "format": "markdown", "remediationStatus": "true"}
```

| Field | Required | Description |
|-------|----------|-------------|
| `data` | Yes | The remediation output content |
| `format` | No | Render format — `markdown` or plain text (default) |
| `remediationStatus` | No | **String** (not boolean) — shown separately in the UI |

`remediationStatus` is a string because future remediation states may need more than true/false (e.g. partial success, rollback). If omitted, the UI displays **N/A**.

### Groovy example

```groovy
import groovy.json.JsonOutput

print JsonOutput.toJson([
    data: "Service restarted successfully",
    format: "markdown",
    remediationStatus: "true"
])
return 0
```

See [Output Formats](../concepts/output-formats.md).

## Alert properties

Same alert context as DiagnosticSource — use `alertProps` or `##ALERT.*##` tokens when triggered by an alert.

| Property | Example |
|----------|---------|
| `alert.datapoint` | `CPUBusyPercent` |
| `alert.datapoint.value` | `95.3` |
| `alert.instance.wildvalue` | `eth0` |
| `alert.datasource` | `Linux_SSH_CPU` |
| `alert.resource.name` | `prod-server-01` |

Handle missing alert context gracefully for manual execution:

```groovy
if (alertProps.containsKey("alert.datapoint")) {
    // remediation based on alert context
} else {
    // manual execution fallback
}
```

## Related recipes

- [`groovy/remediation`](../../recipes/groovy/remediation/)
- [`powershell/winrm-exec`](../../recipes/powershell/winrm-exec/)

## Official documentation

- [Creating RemediationSources](https://www.logicmonitor.com/support/logicmodules/remediationsources/creating-remediationsources/)
- [Alert Properties for DiagnosticSource and RemediationSource Scripts](https://www.logicmonitor.com/support/alert-properties-for-diagnosticsource-and-remediationsource-scripts)
