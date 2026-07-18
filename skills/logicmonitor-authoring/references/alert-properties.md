# Alert Properties

For DiagnosticSource and RemediationSource scripts triggered by alerts. See also [docs/module-types/diagnosticsource.md](../../../docs/module-types/diagnosticsource.md) and [remediationsource.md](../../../docs/module-types/remediationsource.md).

## When available

- Alert-triggered execution via Action Rules / Action Chains
- **Not available:** manual run from Resources, script testing in module editor

Existing scripts without `alertProps` continue to work unchanged.

## Property reference

| Key | Description | Example |
|-----|-------------|---------|
| `alert.datapoint` | Alerting datapoint name | `CPUBusyPercent` |
| `alert.datapoint.value` | Raw value that triggered alert | `95.3` |
| `alert.severity` | Severity level | `critical` |
| `alert.instance` | Instance display name | `Total` |
| `alert.instance.wildvalue` | Instance wildvalue | `eth0` |
| `alert.datasource` | DataSource name | `Linux_SSH_CPU` |
| `alert.id` | Unique alert ID | `DS98765` |
| `alert.threshold` | Breached threshold expression | `>= 90` |
| `alert.resource.id` | Resource ID | `42` |
| `alert.resource.name` | Resource name | `prod-server-01` |
| `alert.external.ticket.id` | External ticket ID | |
| `alert.value.display.name` | Readable status display name | |
| `alert.start.epoch` | Alert start time (unix epoch) | `1781109605` |

## Access patterns

### Inline tokens (Groovy and PowerShell)

Substituted before execution. Empty when run manually.

```groovy
def datapointName = "##ALERT.DATAPOINT##"
def currentValue  = "##ALERT.DATAPOINT.VALUE##"
def severity      = "##ALERT.SEVERITY##"
```

### Runtime variable (Groovy)

```groovy
def dpName   = alertProps.get("alert.datapoint")
def dpValue  = alertProps.get("alert.datapoint.value")
def severity = alertProps.get("alert.severity")
def instance = alertProps.get("alert.instance")

if (dpValue?.toDouble() > 90.0) {
    // targeted remediation based on alert value
}
```

## Manual execution fallback

Always handle missing alert context:

```groovy
if (alertProps.containsKey("alert.datapoint")) {
    // alert-aware path
} else {
    // manual execution fallback
}
```

## Official docs

- https://www.logicmonitor.com/support/alert-properties-for-diagnosticsource-and-remediationsource-scripts
