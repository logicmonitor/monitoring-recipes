# snmp-walk

Walk an SNMP OID subtree using `proto.snmp` and emit results via `lm.emit`.

## What this script does

Performs an SNMP walk against a target device using the platform `proto.snmp` snippet. Handles SNMP v1, v2, and v3 based on device properties. Includes retry logic and collector timeout budgeting.

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- SNMP credentials configured on the device in LogicMonitor
- Collector with SNMP access to the target

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `system.hostname` | Yes | Target device hostname or IP |
| SNMP v1/v2 | | `snmp.community` |
| SNMP v3 | | `snmp.security`, `snmp.auth`, `snmp.priv`, etc. |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_OID_HERE` | Root OID to walk (e.g. `1.3.6.1.2.1.1`) |

Uncomment the appropriate `emit` line in `script.groovy` for your module type.

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| DataSource (Script) | `emit.dp("metricName", value)` | `emit.dp("sysDescr", value)` |
| DataSource (BatchScript) | `emit.dp(wildvalue, "field", value)` | `emit.dp("eth0", "status", value)` |
| PropertySource | `emit.property("auto.name", value)` | `emit.property("auto.sysDescr", value)` |
| Active Discovery | `emit.instance(wv, alias, desc, ilpMap)` | `emit.instance(index, alias, "", [:])` |

## Related docs

- [DataSource](../../../docs/module-types/datasource.md)
- [PropertySource](../../../docs/module-types/propertysource.md)
- [Active Discovery](../../../docs/concepts/active-discovery.md)
- [Snippets catalog](../../../skills/logicmonitor-authoring/references/snippets-catalog.md)
- [snmp-discovery](../snmp-discovery/) — dedicated Active Discovery variant
