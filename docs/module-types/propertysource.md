# PropertySource

Programmatic method of configuring device properties and metadata.

## Purpose

Auto-assign properties at the resource level based on script output. Used for inventory tracking, reporting, troubleshooting, and dynamic grouping.

## How it works

1. Script interacts with a device to collect designated information (serial number, firmware, port speed, etc.)
2. Script outputs key-value pairs to stdout
3. LogicMonitor assigns those values as properties on the resource

PropertySources run when:

- The PropertySource is updated
- Active Discovery is manually executed on a matching resource
- The PropertySource is manually run (**More → Run PropertySource**)
- Automatically once per day (or on the **Collect every** schedule for ERISource types)

**Note:** For cloud resources, enable monitoring via a local Collector first.

## When to use

- Inventory metadata (serial, model, firmware)
- Category/tagging via `system.categories`
- Dynamic grouping and appliesTo targeting
- CMDB/ITSM integration
- **ERISource** data type — assign External Resource IDs (ERIs) for topology mapping

## Output format

Print `key=value` lines to stdout. See [Output Formats](../concepts/output-formats.md).

### Allowed properties

| Prefix | Allowed | Example |
|--------|---------|---------|
| `auto.*` | Yes | `auto.serial=SN12345` |
| `system.categories` | Yes | `system.categories=Windows,Production` |
| `system.*` (other) | **No** | Cannot set arbitrary system properties |

Only `auto.*` custom properties and `system.categories` can be set from script output.

### Example output

```
auto.array_name=MY-PURE-001
auto.array_id=12345
auto.version=6.1.0
system.categories=Storage,PureStorage
```

## Portal configuration

| Field | Guidance |
|-------|----------|
| **Name** | Platform/application name; descriptive |
| **Description** | Purpose if name alone isn't sufficient |
| **Applies To** | Use AppliesTo scripting — target via category where possible |
| **Data Type** | `PropertySource` (default) or `ERISource` for topology ERIs |
| **Collect Every** | ERISource only — overrides default daily schedule |

## Script languages

Groovy or PowerShell. Use `hostProps` for device properties and credentials — never hardcode secrets.

## Related recipes

- [`groovy/snmp-walk`](../../recipes/groovy/snmp-walk/) — adapt output to `auto.propertyname=value`
- [`groovy/snmp-get`](../../recipes/groovy/snmp-get/)
- [`groovy/http-rest`](../../recipes/groovy/http-rest/) — API-based inventory

## Official documentation

- [Creating PropertySources](https://www.logicmonitor.com/support/logicmodules/propertysources/creating-propertysources)
- [Examples](https://www.logicmonitor.com/support/logicmodules/propertysources/creating-propertysources#examples)
