# wmi-discovery

WMI-based Active Discovery — enumerate instances for multi-instance DataSources.

## What this script does

Queries WMI to discover monitorable instances and emits Active Discovery output lines (`wildvalue##wildalias`). Pairs with `wmi-query` for per-instance collection.

## Prerequisites

- WMI access to the target host
- Multi-instance DataSource with Script Active Discovery enabled

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `system.hostname` | Yes | Target Windows host |
| `wmi.user` / `wmi.pass` | No | Credentials for remote WMI |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_WMI_NAMESPACE_HERE` | WMI namespace (e.g. `root\cimv2`) |
| `INSERT_WQL_QUERY_HERE` | WQL query returning instances (e.g. `SELECT Name, DisplayName FROM Win32_Service WHERE State='Running'`) |
| `INSERT_WILDVALUE_PROPERTY_HERE` | WMI property for wildvalue (e.g. `Name`) |
| `INSERT_WILDALIAS_PROPERTY_HERE` | WMI property for wildalias (e.g. `DisplayName`) |

Wildvalues are sanitized — `#`, `\`, `:`, `=`, spaces replaced with `_`.

## Output format

```
wildvalue##wildalias
wildvalue##wildalias##description
wildvalue##wildalias##description####auto.prop1=val&auto.prop2=val
```

## Related docs

- [Active Discovery](../../../docs/concepts/active-discovery.md)
- [wmi-query](../wmi-query/) — collection script for discovered instances
- [DataSource](../../../docs/module-types/datasource.md)
