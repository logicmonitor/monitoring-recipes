# DiagnosticSource

Collector information useful for troubleshooting issues or specific alert conditions.

## Purpose

Gather diagnostic data on demand or automatically when alerts fire — e.g. process lists during high CPU, connection tables, or service status dumps.

## When to use

- Alert-triggered diagnostics (Action Rules / Action Chains)
- Manual troubleshooting from the Resources page
- Context-driven investigation based on what breached a threshold

## Output format

DiagnosticSources return **JSON** output. Plain-text fallback is supported, but JSON enables metadata for how the response is rendered in the UI.

### JSON structure

```json
{"data": "Example Output", "format": "markdown"}
```

| Field | Required | Description |
|-------|----------|-------------|
| `data` | Yes | The diagnostic output content |
| `format` | No | How to render `data` — `markdown` or plain text (default) |

### Groovy example

Keep JSON creation simple — use `JsonOutput` rather than manual string building:

```groovy
import groovy.json.JsonOutput

print JsonOutput.toJson([data: "Example Output", format: "markdown"])
return 0
```

See [Output Formats](../concepts/output-formats.md).

## Alert properties

When triggered by an alert, scripts have access to alert context via `alertProps` (Groovy) or `##ALERT.*##` tokens.

| Property | Example |
|----------|---------|
| `alert.datapoint` | `CPUBusyPercent` |
| `alert.datapoint.value` | `95.3` |
| `alert.severity` | `critical` |
| `alert.instance` | `Total` |
| `alert.instance.wildvalue` | `eth0` |
| `alert.datasource` | `Linux_SSH_CPU` |
| `alert.id` | `DS98765` |
| `alert.threshold` | `>= 90` |
| `alert.resource.name` | `prod-server-01` |
| `alert.start.epoch` | `1781109605` |

### Access patterns

**Inline tokens** (substituted before execution):

```groovy
def datapointName = "##ALERT.DATAPOINT##"
def currentValue  = "##ALERT.DATAPOINT.VALUE##"
```

**Runtime variable** (Groovy):

```groovy
def dpName  = alertProps.get("alert.datapoint")
def dpValue = alertProps.get("alert.datapoint.value")
```

### Manual execution

When run manually (no alert context), `alertProps` and `##ALERT.*##` tokens are empty. Design scripts to handle both cases:

```groovy
if (alertProps.containsKey("alert.datapoint")) {
    // alert-aware path
} else {
    // manual execution fallback
}
```

Alert properties are also unavailable during script testing in the module editor.

## Official documentation

- [Creating DiagnosticSources](https://www.logicmonitor.com/support/logicmodules/diagnosticsources/creating-diagnosticsources/)
- [Alert Properties for DiagnosticSource and RemediationSource Scripts](https://www.logicmonitor.com/support/alert-properties-for-diagnosticsource-and-remediationsource-scripts)
