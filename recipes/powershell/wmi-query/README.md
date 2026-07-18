# wmi-query

Query WMI and emit datapoints using `Get-CimInstance`.

## What this script does

Executes a WQL query against a local or remote Windows host and maps WMI properties to `key=value` output lines.

## Prerequisites

- WMI access to the target host
- WMI service running on target
- Appropriate DCOM/WMI permissions
- Credentials if querying remotely

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `system.hostname` | Yes | Target Windows host |
| `wmi.user` | No | WMI username for remote queries |
| `wmi.pass` | No | WMI password |
| `system.azure.privateIpAddress` | No | Used instead of hostname for Azure devices |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_WMI_NAMESPACE_HERE` | WMI namespace (e.g. `root\cimv2`) |
| `INSERT_WQL_QUERY_HERE` | WQL query (e.g. `SELECT Name, State FROM Win32_Service WHERE Name='Spooler'`) |
| Property mapping loop | Filter/map specific properties to datapoint keys |

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| DataSource (Script) | `Write-Output "metricName=value"` | Map WMI properties to datapoints |
| PropertySource | `Write-Output "auto.propertyName=value"` | Only `auto.*` prefix allowed |
| DataSource (BatchScript) | `Write-Output "instance.metricName=value"` | Prefix with instance wildvalue |

## Related docs

- [DataSource](../../../docs/module-types/datasource.md)
- [PropertySource](../../../docs/module-types/propertysource.md)
