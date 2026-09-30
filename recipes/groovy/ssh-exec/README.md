# ssh-exec

Execute a one-shot remote command over SSH using the `lm.remote` snippet (SSHJ).

## What this script does

Connects to a remote device via SSH and executes a single command, returning output for parsing. Uses the platform `lm.remote` snippet — not raw JSCH.

For interactive shell sessions (ConfigSource with prompt handling), see Phase 2 `ssh-interactive-config`.

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- SSH credentials on the device
- Network access from collector to target on SSH port

## Required device properties

| Property | Fallback | Description |
|----------|----------|-------------|
| `system.hostname` | | Target host |
| `ssh.user` | `config.user` | SSH username |
| `ssh.pass` | `config.pass` | SSH password |
| `ssh.cert` | `ssh.publickey` | Private key path |
| `ssh.cert.pass` | `ssh.publickey.pass` | Key passphrase |
| `ssh.port` | `22` | SSH port |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_COMMAND_HERE` | Command to execute remotely |

## Adapting for your module type

| Module type | Output change | Portal notes |
|-------------|---------------|--------------|
| DataSource (Script) | Parse output → `emit.dp("key", value)` | Groovy script collection |
| ConfigSource (Script) | `print output` (raw text, no key=value) | Return config file contents |
| DiagnosticSource | `JsonOutput.toJson([data: output, format: "markdown"])` | JSON required |

## Related docs

- [DataSource](../../../docs/module-types/datasource.md)
- [ConfigSource](../../../docs/module-types/configsource.md)
- [DiagnosticSource](../../../docs/module-types/diagnosticsource.md)
- [Snippets catalog — lm.remote](../../../skills/logicmonitor-module-authoring/references/snippets-catalog.md#lmremote--ssh-execution)
