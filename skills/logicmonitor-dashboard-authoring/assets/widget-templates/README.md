# Widget templates

Copy a template into your dashboard `widgets[]` array. Replace placeholders:

- `Display Name (LogicModule_Name)` — `dataSourceFullName` from the user's DataSource export
- `DATAPOINT_NAME` / `DATAPOINT_A` — names from `dataPoints[]` in that export
- `DataSource Display Name*` — `dataSourceDisplayName` pattern for `noc` items

Validate the assembled dashboard with `scripts/validate-dashboard.py`.

## Supported widget types (schema)

| Template | `type` | Notes |
|----------|--------|--------|
| [cgraph.json](cgraph.json) | `cgraph` | Trend graph; glob scope on `dataPoints[]` |
| [cgraph-virtual-datapoint.json](cgraph-virtual-datapoint.json) | `cgraph` | GB → bytes via `virtualDataPoints` |
| [bigNumber.json](bigNumber.json) | `bigNumber` | KPI tiles |
| [dynamicTable.json](dynamicTable.json) | `dynamicTable` | Sortable table |
| [dynamicTable-forecast.json](dynamicTable-forecast.json) | `dynamicTable` | Column with `enableForecast` |
| [pieChart.json](pieChart.json) | `pieChart` | Dual `dataPoints` + `pieChartItems` |
| [gauge.json](gauge.json) | `gauge` | Single `dataPoint` + thresholds |
| [noc.json](noc.json) | `noc` | Health list; `dataSourceDisplayName` only |
| [alert.json](alert.json) | `alert` | **Always** start from this file |
| [gmap.json](gmap.json) | `gmap` | Map by resource group |
| [deviceSLA.json](deviceSLA.json) | `deviceSLA` | SLA metrics use `metric` field |
| [text.json](text.json) | `text` | HTML section headers |
| [cloud-widget-stub.json](cloud-widget-stub.json) | `billing`, `cloudRecommendation`, `viz` | Export from portal; stubs are schema placeholders only |

Pairing patterns (layout): [references/layout-conventions.md](../../references/layout-conventions.md).
