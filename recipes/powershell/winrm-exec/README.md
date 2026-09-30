# winrm-exec

Execute a remote PowerShell command via WinRM (`Invoke-Command`).

## What this script does

Runs a PowerShell scriptblock on a remote Windows host via WinRM. Includes connection retry, credential handling, and a collector-local execution fork when the device is the collector itself.

## Prerequisites

- WinRM enabled on target (ports 5985/5986)
- PowerShell remoting permissions
- Credentials if querying remotely

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `system.hostname` | Yes | Target Windows host |
| `wmi.user` / `wmi.pass` | No | Credentials for WinRM session |
| `system.categories` | No | Contains `collector` to run locally |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `$command` scriptblock | Replace `INSERT_COMMAND_HERE` section with remote logic |
| Output parsing | Map command output to `Write-Output "key=value"` lines |

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| DataSource (Script) | `Write-Output "key=value"` | Parse remote command output |
| DiagnosticSource | JSON via `ConvertTo-Json` | See commented block at bottom of script |
| RemediationSource | JSON + `remediationStatus` | `{ data, format, remediationStatus }` |

## Related docs

- [DataSource](../../../docs/module-types/datasource.md)
- [DiagnosticSource](../../../docs/module-types/diagnosticsource.md)
- [RemediationSource](../../../docs/module-types/remediationsource.md)
