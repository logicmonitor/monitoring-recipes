# remediation

Run a corrective command and emit RemediationSource JSON.

## What this script does

Runs a remote command via `lm.remote` and prints `{ "data": "...", "format": "markdown", "remediationStatus": "true" }`. `remediationStatus` is a **string** (not a boolean). The UI shows **N/A** if it is omitted. Failed exec sets it to `"false"`.

Handle missing `alertProps` for manual runs — same rule as [`diagnostic`](../diagnostic/).

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- SSH credentials and permission to run the remediation command
- Action Rules / Action Chains if this should fire automatically on alert

## Required device properties

| Property | Fallback | Description |
|----------|----------|-------------|
| `system.hostname` | | Target host |
| `ssh.user` | `config.user` | SSH username |
| `ssh.pass` / `ssh.cert` | | SSH auth |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_COMMAND_HERE` | Remediation command (`systemctl restart …`, `clear counters`, …) |
| `remediationStatus` | `"true"` / `"false"` today; keep it a string for future states |
| Windows | Use [`winrm-exec`](../../powershell/winrm-exec/) |

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| RemediationSource | `{ data, format, remediationStatus }` | Default |
| DiagnosticSource | Omit `remediationStatus` | Use [`diagnostic`](../diagnostic/) |

## Related docs

- [RemediationSource](../../../docs/module-types/remediationsource.md)
- [Alert properties](../../../skills/logicmonitor-authoring/references/alert-properties.md)
- [diagnostic](../diagnostic/)
