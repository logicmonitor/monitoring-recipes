# DataSource

Time-series metric data — the most common LogicModule type.

## Purpose

Collect numeric metrics over time for graphing, alerting, and reporting.

## When to use

- CPU, memory, disk, network throughput
- Application performance counters
- Status codes and availability metrics
- Any numeric value that changes over time

## When NOT to use

- Logs or searchable events → [LogSource](logsource.md)
- Device metadata/inventory → [PropertySource](propertysource.md)
- Configuration file content → [ConfigSource](configsource.md)

## Collection methods

| Type | Use when |
|------|----------|
| Built-in (SNMP, WMI, HTTP, JDBC, etc.) | Standard protocol collection is sufficient |
| Scripted (Groovy, PowerShell, external) | Complex logic, APIs with auth, aggregating multiple sources |

See [Collection Modes](../concepts/collection-modes.md) for Script vs BatchScript.

## Output format

| Mode | Output |
|------|--------|
| Script | `key=value` per line |
| BatchScript | `instance.key=value` per line |

See [Output Formats](../concepts/output-formats.md).

## Style guidelines (best practices)

Distilled from LogicMonitor's DataSource style guidelines:

### Naming

- **Name** is the unique key — use `Vendor_Product_Monitor` (e.g. `PaloAlto_FW_Sessions`, `Cisco_Nexus_Temperature`)
- No spaces in the Name field
- **Display Name** is for the device tree only — can be shorter (e.g. Name `EMC_VNX_LUNs`, Display Name `LUNs`)

### AppliesTo and scheduling

- Target via `hasCategory()` where possible; set `system.categories` with a PropertySource or SNMP SysOID Map
- Avoid overly broad appliesTo (e.g. `isLinux()` for everything) or overly narrow (single host)
- Match poll interval to the metric: 1 min for fast-changing/critical (CPU, ping loss); 5+ min for slower-changing metrics

### Active Discovery

- Use AD only for true multi-instance monitoring, or single instances that frequently disappear/reappear
- For a single static instance, prefer a PropertySource to set a device property instead
- Document AD filters in comment fields
- Use device property substitution (`##jmx.port##`) instead of hardcoded values
- Carefully choose **Automatically Delete Instance** — disable it when a failure would cause AD to stop detecting the instance (e.g. TCP port-based discovery)

### Datapoints

- Name datapoints after the source object's name (SNMP OID name, WMI property name)
- Include human-readable descriptions with units
- Set valid value ranges to prevent spurious alerts
- Every datapoint should be used in a graph, complex datapoint, or alert
- Use complex datapoints for alert/report calculations; virtual datapoints for display-only transforms
- Custom alert messages on thresholded datapoints — include context and remediation steps

### Graphs

- Use proper names; prefix overview graphs (e.g. `LUN Throughput Overview`)
- Y-axis labels should include units (°C, %, bps, count)
- Set min/max appropriately (usually min = 0)
- Use Display Priority to surface the most important graphs first

## Related recipes

- [`groovy/snmp-walk`](../../recipes/groovy/snmp-walk/)
- [`groovy/snmp-get`](../../recipes/groovy/snmp-get/)
- [`groovy/snmp-discovery`](../../recipes/groovy/snmp-discovery/)
- [`powershell/wmi-query`](../../recipes/powershell/wmi-query/)
- [`powershell/wmi-discovery`](../../recipes/powershell/wmi-discovery/)

## Official documentation

- [Creating DataSources](https://www.logicmonitor.com/support/datasources/creating-datasources/)
- [DataSource Style Guidelines](https://www.logicmonitor.com/support/logicmodules/datasources/creating-managing-datasources/datasource-style-guidelines)
