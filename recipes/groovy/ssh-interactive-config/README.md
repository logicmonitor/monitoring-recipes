# ssh-interactive-config

Session-based SSH config collection for ConfigSources (pager + optional enable).

## What this script does

Opens an `lm.remote` session, optionally disables paging and enters privileged exec, then runs a show command and **prints raw text**. That is the ConfigSource Script stdout contract — LogicMonitor diffs the text.

Exchange `SSH_Interactive_Standard` is a full PTY/prompt engine for pagers and custom line-response maps. Use that module when devices need interactive prompt handling this building block does not cover.

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- SSH credentials on the device
- For one-shot commands that are not configs, use [`ssh-exec`](../ssh-exec/)

## Required device properties

| Property | Fallback | Description |
|----------|----------|-------------|
| `system.hostname` | | Target host |
| `ssh.user` | `config.user` | SSH username |
| `ssh.pass` | `config.pass` | SSH password |
| `ssh.cert` | `ssh.publickey` | Private key path |
| `ssh.enable.pass` | `config.enable.pass` | Enable/privileged password |
| `config.commands.standard` | | Show command (or `INSERT_SHOW_COMMAND_HERE`) |
| `config.commands.formatting` | `terminal length 0` | Pager disable command |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_SHOW_COMMAND_HERE` | Command that prints the config (`show running-config`, `cat /etc/ssh/sshd_config`, …) |
| Enable / pager commands | Device-specific (`terminal pager 0`, `enable`, …) |
| BatchScript JSON | Commented block maps `data.<wildvalue>.configuration` |

## Adapting for your module type

| Module type | Output change | Portal notes |
|-------------|---------------|--------------|
| ConfigSource (Script) | `print output` | Default — raw text, 3 MiB limit |
| ConfigSource (BatchScript) | JSON `data.<wildvalue>.configuration` | Multi-instance; see commented block |
| DataSource | Parse → `emit.dp` | Use [`ssh-exec`](../ssh-exec/) |

## Related docs

- [ConfigSource](../../../docs/module-types/configsource.md)
- [ssh-exec](../ssh-exec/)
- [Output Formats](../../../docs/concepts/output-formats.md)
