# add-category

Set `system.categories` from a host-property match so AppliesTo can target the resource.

## What this script does

Reads a probe property, matches it against a regex, and emits `system.categories=<category>` via `lm.emit`. Exchange `addCategory_*` PropertySources follow this pattern (often with an SNMP/WMI/API probe first). Only `auto.*` and `system.categories` may be set from a PropertySource.

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- PropertySource applied via AppliesTo to candidate resources

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| Probe property | Yes | The property named in `INSERT_PROBE_PROPERTY_HERE` (falls back to `system.hostname`) |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_PROBE_PROPERTY_HERE` | Property to inspect (e.g. `system.sysinfo`, `auto.vendor`) |
| `INSERT_MATCH_PATTERN_HERE` | Groovy regex (`==~`) such as `(?i).*linux.*` |
| `INSERT_CATEGORY_HERE` | Category token to append (e.g. `MyApp`, `NoPing`) |

To always set a category, use `/.*/` as the match pattern.

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| PropertySource | `emit.property("system.categories", category)` | Default |
| PropertySource (metadata) | `emit.property("auto.serial", value)` | Inventory instead of tagging |
| ERISource | `lm.topo.emitEri` | Use [`add-eri`](../add-eri/) |

## Related docs

- [PropertySource](../../../docs/module-types/propertysource.md)
- [Choosing a module type](../../../docs/choosing-a-module-type.md)
