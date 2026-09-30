# Datapoint and Datasource References

Datasource identity and resource scoping differ by widget type. Start from the matching bundled widget template; do not transpose fields across types.

## Per-widget field matrix

| Widget | DS identity field | Location | Datapoint field | Scope group field | Scope type |
|--------|-------------------|----------|-----------------|-------------------|------------|
| `cgraph` | `dataSourceFullName` | `graphInfo.dataPoints[]` | `dataPointName` | `deviceGroupFullPath` | glob object |
| `bigNumber` | `dataSourceFullName` | `bigNumberInfo.dataPoints[]` | `dataPointName` | `deviceGroupFullPath` | plain string |
| `pieChart` | `dataSourceFullName` | `pieChartInfo.dataPoints[]` | `dataPointName` | `deviceGroupFullPath` | plain string |
| `gauge` | `dataSourceFullName` | `dataPoint` (singular) | `dataPointName` | `deviceGroupFullPath` | plain string |
| `dynamicTable` | `dataSourceFullName` | top-level config | `columns[].dataPointName` | `rows[].groupFullPath` | plain string |
| `noc` | `dataSourceDisplayName` | `items[]` | `dataPointName` | `deviceGroupFullPath` | plain string |
| `deviceSLA` | `dataSourceFullName` | `metrics[]` | `metric` | `groupName` + `deviceName` | plain string |

## `dataSourceFullName` formats

Build from a portal export's `displayName` or an import bundle's `displayedAs`, plus `name`:

| Format | Example | When |
|--------|---------|------|
| `DisplayName (name)` | `Lambda (AWS_Lambda)` | Default for cloud/vendor datasources |
| `prefix-with-dash` | `Nginx-` | Multi-instance AD datasources |
| `plain` | `Ping` | Short name equals reference |
| glob/pattern | rare | Aggregated widgets |

The datapoint index includes aliases for all formats. Prefer the exact identity from the user's source JSON; use the matching bundled template for field placement.

## Scope field types

### `cgraph` — glob objects (required)

```json
"instanceName": { "isGlob": true, "value": "*" },
"deviceDisplayName": { "isGlob": true, "value": "*" },
"deviceGroupFullPath": { "isGlob": true, "value": "##defaultResourceGroup##" }
```

### `bigNumber`, `pieChart`, `gauge`, `dynamicTable`, `noc` — plain strings

```json
"instanceName": "*",
"deviceDisplayName": "*",
"deviceGroupFullPath": "##defaultResourceGroup##"
```

### `dynamicTable` rows — `groupFullPath` not `deviceGroupFullPath`

```json
{
  "dataSourceFullName": "CPU (Microsoft_Windows_CPU)",
  "rows": [{
    "instanceName": "*",
    "label": "##RESOURCENAME##",
    "deviceDisplayName": "*",
    "groupFullPath": "##defaultResourceGroup##"
  }]
}
```

## `noc` — `dataSourceDisplayName` only

Never use `dataSourceFullName` in `noc` items.

| Pattern | Example |
|---------|---------|
| exact | `AWS API Gateway Stages` |
| suffix wildcard | `API Gateway*` |
| alternation | `(Web Check*|Ping Check*)` |
| prefix-with-dash | `Call Manager Cluster-` |
| star | `*` |

Template: [noc.json](../assets/widget-templates/noc.json)

## `pieChart` dual-array

- `pieChartInfo.dataPoints[]` — full `dataSourceFullName` + scope
- `pieChartInfo.pieChartItems[]` — slice by `dataPointName` + `color` + `legend` only

Slice names may differ from `dataPoints[0].dataPointName` when showing a subset.

## `deviceSLA` metrics

Uses `metric` (not `dataPointName`) plus `groupName`, `deviceName`, `instances`, `threshold`.

Template: `assets/widget-templates/deviceSLA.json`

## Capacity values in GB — convert to bytes for display

When a datasource reports capacity in GB (e.g. `UsedUsableCapGB`, `UsableCapacity`), raw values can render with misleading axis labels (e.g. `124.5K` GB). Convert to bytes so LogicMonitor auto-scales to TB/PB.

### `cgraph` — virtual datapoint

Add a `virtualDataPoints[]` entry with RPN `*1024*1024*1024`. Hide the underlying GB datapoint with `display.option: "none"` so only the virtual series is shown.

```json
"virtualDataPoints": [{
  "rpn": "UsedUsableCapGB*1024*1024*1024",
  "name": "UsedUsableBytes",
  "display": { "color": "Auto", "legend": "##RESOURCENAME##", "type": "line", "option": "custom" }
}],
"verticalLabel": "bytes",
"scaleUnit": 1024,
"dataPoints": [{
  "dataPointName": "UsedUsableCapGB",
  "display": { "option": "none", "type": "line", "color": null, "legend": null }
}]
```

Templates: [cgraph-virtual-datapoint.json](../assets/widget-templates/cgraph-virtual-datapoint.json), [dynamicTable.json](../assets/widget-templates/dynamicTable.json) (column `rpn`)

### `dynamicTable` — column RPN

Set `columns[].rpn` to the same multiply expression and clear `unitLabel` (leave empty) so the table auto-formats bytes:

```json
{
  "rpn": "UsedUsableCapGB*1024*1024*1024",
  "dataPointName": "UsedUsableCapGB",
  "unitLabel": "",
  "columnName": "Used Capacity"
}
```

Percent columns (e.g. `(UsedPoolCapacity/TotalPoolCapacity)*100`) are unchanged — only absolute capacity columns need conversion.

## Widget tokens

Use `##defaultResourceGroup##` in scope fields. Set the token value in `widgetTokens` or at dashboard group level when importing.

Common tokens: `##RESOURCENAME##`, `##INSTANCE##`, `##RESOURCEGROUP##`
