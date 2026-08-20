# snmp-discovery

SNMP walk Active Discovery — emit instances with `lm.emit`.

## What this script does

Walks a name OID via `proto.snmp` and emits one instance per index (`wildvalue##wildalias`). Optionally walks a second OID and stores the value as `auto.discovered.value`. Pairs with [`snmp-get`](../snmp-get/) (Script) or [`snmp-walk`](../snmp-walk/) (BatchScript).

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- Multi-instance DataSource with **Script Active Discovery** enabled
- SNMP credentials on the device

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `system.hostname` | Yes | Target hostname or IP |
| SNMP credentials | Yes | v1/v2 community or v3 properties |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_NAME_OID_HERE` | OID whose walk indexes become wildvalues (e.g. `ifDescr` `1.3.6.1.2.1.2.2.1.2`) |
| `INSERT_OPTIONAL_PROPERTY_OID_HERE` | Extra OID to attach as an instance-level property; leave the placeholder to skip |
| `ilps` map | Add more `auto.*` instance properties |

Wildvalues must not contain spaces, `:`, `=`, `\`, or `#` — `lm.emit` sanitizes them.

## Output format

```
wildvalue##wildalias
wildvalue##wildalias##description
wildvalue##wildalias##description####auto.prop=value
```

## Related docs

- [Active Discovery](../../../docs/concepts/active-discovery.md)
- [snmp-walk](../snmp-walk/) — collection for discovered instances
- [wmi-discovery](../../powershell/wmi-discovery/) — PowerShell AD equivalent
- [DataSource](../../../docs/module-types/datasource.md)
