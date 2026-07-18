# snmp-get

SNMP GET a specific OID (with optional instance index) using `proto.snmp`.

## What this script does

Retrieves a single SNMP value by OID using the platform `proto.snmp` snippet with retry logic. Designed for per-instance collection where the instance index is known from Active Discovery.

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- SNMP credentials configured on the device
- For Script mode: instances discovered via Active Discovery (`instanceProps.wildvalue`)

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `system.hostname` | Yes | Target device hostname or IP |
| SNMP credentials | Yes | Per device SNMP configuration |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_OID_HERE` | Base OID without index (e.g. `1.3.6.1.2.1.2.2.1.10`) |
| `INSERT_INSTANCE_INDEX_HERE` | SNMP index when not using `instanceProps` (Script mode uses `wildvalue`) |
| `metricName` | Datapoint key name in emit call |

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| DataSource (Script) | `emit.dp("metricName", value)` | Per-instance via `instanceProps.wildvalue` |
| DataSource (BatchScript) | `emit.dp(wildvalue, "field", value)` | Loop `datasourceinstanceProps` |
| PropertySource | `emit.property("auto.name", value)` | Single property probe |

## Related docs

- [DataSource](../../../docs/module-types/datasource.md)
- [PropertySource](../../../docs/module-types/propertysource.md)
- [Collection Modes](../../../docs/concepts/collection-modes.md)
