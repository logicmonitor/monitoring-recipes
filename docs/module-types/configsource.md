# ConfigSource

Access, collect, and alert on changes to configuration files.

## Purpose

Monitor configuration files for changes. ConfigSources use embedded Groovy or PowerShell scripts to retrieve config data and alert when it drifts from a known baseline.

## When to use

- Network device running configs
- Application configuration files
- Compliance and change-detection use cases
- Multi-instance configs (e.g. per-interface config on a firewall)

## Limits

- Individual config files cannot exceed **3 MiB**
- For cloud resources, enable monitoring via a local Collector

## Collection methods

| Method | Multi-instance | Output |
|--------|----------------|--------|
| **SCRIPT** | Optional | Raw configuration text to stdout (single instance) |
| **BATCHSCRIPT** | Required (enabled by default) | JSON (see below) |

Default collection schedule: 1 hour. Options: 4 hours, 8 hours, 1 day.

## Output format

### Script mode (single instance)

Print the raw configuration file contents to stdout. LogicMonitor handles diffing and change detection.

### BatchScript mode (multi-instance)

JSON output with configuration per instance:

```json
{
  "data": {
    "instance1": {
      "configuration": "Configuration data of instance1"
    },
    "instance2": {
      "configuration": "Configuration data of instance2"
    }
  }
}
```

Configuration for each instance must be at the path `data.<wildvalue>.configuration`. Use `##WILDVALUE##` in module definitions to map instances.

Invalid wildvalue characters (`:`, `#`, `\`, spaces) return NoData — sanitize in Active Discovery and script output.

See [Output Formats](../concepts/output-formats.md) for the full reference.

## Portal tabs

| Tab | Purpose |
|-----|---------|
| **Info** | Name, collection method (SCRIPT/BATCHSCRIPT), schedule, multi-instance settings |
| **AppliesTo** | Which resources receive this ConfigSource |
| **Active Discovery** | Required for multi-instance — discover config file instances |
| **Collection** | Groovy, PowerShell, or uploaded external script |
| **Config Check** | Rules to alert on config changes |

For SSH/Telnet config retrieval, use embedded Groovy with Expect syntax. Set SSH credentials as device properties before running.

## Related recipes

- [`groovy/ssh-exec`](../../recipes/groovy/ssh-exec/) — retrieve remote config via SSH one-shot exec
- [`groovy/ssh-interactive-config`](../../recipes/groovy/ssh-interactive-config/) — session-based show/enable collection

## Official documentation

- [ConfigSource Configuration](https://www.logicmonitor.com/support/configsource-configuration)
