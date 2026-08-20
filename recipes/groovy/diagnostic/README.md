# diagnostic

Collect on-demand troubleshooting output as DiagnosticSource JSON.

## What this script does

Runs a remote command via `lm.remote`, wraps the result (plus optional alert context) as markdown, and prints `{ "data": "...", "format": "markdown" }`. Exchange DiagnosticSources (for example Microsoft Powershell Top CPU and Memory) emit the same JSON shape; this recipe is the Groovy equivalent.

`alertProps` is populated only when the module is triggered by an alert. Manual runs and the module editor test path have no alert context — the script handles both.

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- SSH credentials on the device
- Collector that can reach the target

## Required device properties

| Property | Fallback | Description |
|----------|----------|-------------|
| `system.hostname` | | Target host |
| `ssh.user` | `config.user` | SSH username |
| `ssh.pass` / `ssh.cert` | | SSH auth |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_COMMAND_HERE` | Diagnostic command (`ps aux`, `top -bn1`, `show processes cpu`, …) |
| Alert branch | Use `alertProps.get("alert.instance.wildvalue")` to scope the command |
| Windows | Use [`winrm-exec`](../../powershell/winrm-exec/) and `ConvertTo-Json` |

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| DiagnosticSource | `{ data, format }` | Default |
| RemediationSource | Add `remediationStatus` string | Use [`remediation`](../remediation/) |

## Related docs

- [DiagnosticSource](../../../docs/module-types/diagnosticsource.md)
- [Alert properties](../../../skills/logicmonitor-authoring/references/alert-properties.md)
- [ssh-exec](../ssh-exec/)
