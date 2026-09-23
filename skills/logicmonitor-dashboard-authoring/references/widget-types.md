# Widget Types (schema-supported)

Supported `type` values are enforced by `schema/common.defs.json`. Prefer `cgraph` and `dynamicTable` for new metric dashboards.

**Templates:** [assets/widget-templates/README.md](../assets/widget-templates/README.md)

## Metric widgets

### `cgraph` (custom graph)

Template: `cgraph.json`, `cgraph-virtual-datapoint.json`

Config block: `graphInfo`

Required: `dataPoints[]`, `virtualDataPoints`, `minValue`, `maxValue`, `topX`, `verticalLabel`, `aggregate`, `desc`, `scaleUnit`, `globalConsolidateFunction`

Each `dataPoints[]` entry needs `display.type` (`line`, `stack`, `area`, `column`) and glob-object scope fields.

### `bigNumber`

Template: `bigNumber.json`

Config block: `bigNumberInfo` with `dataPoints[]`, `bigNumberItems[]`, `counters[]`, `virtualDataPoints[]`

`bigNumberItems[]` maps display (labels, thresholds) to `dataPointName` values in `dataPoints[]`.

Counters use `appliesTo` Groovy expressions instead of datasources when counting resources.

### `dynamicTable`

Templates: `dynamicTable.json`, `dynamicTable-forecast.json`

Top-level: `dataSourceFullName`, `columns[]`, `rows[]`, `forecast`, `topX`, `sortOrder`

Columns define `dataPointName`, `displayType` (`raw`, `percent`, `colorBar`), thresholds.

### `pieChart`

Template: `pieChart.json`

Config block: `pieChartInfo` with both `dataPoints[]` and `pieChartItems[]`.

### `gauge`

Template: `gauge.json`

Singular `dataPoint` object plus `legend`, `minValue`, `maxValue`, `displayUnit`, `displayType`, `peakTimeRange`, `showPeak`.

## Status widgets

### `noc`

Template: `noc.json`

Alert status table. Uses `items[]` with `dataSourceDisplayName` (not `dataSourceFullName`).

Flags: `ackChecked`, `sdtChecked`, `displayWarnAlert`, `displayErrorAlert`, `displayCriticalAlert`, `sortBy`, `displayColumn`.

### `alert`

Template: `alert.json` — **always** start from this file.

Alert list. Uses `filters` and `displaySettings`.

#### Required shape (portal-safe)

`displaySettings.sort` must be a **string**, not an object:

```json
"sort": "-startEpoch"
```

Using `{ "columnKey": "alert-severity", "order": "descending" }` imports but crashes at render with `TypeError: n.replace is not a function`.

`displaySettings.playSound` must be an **object**, not a boolean:

```json
"playSound": {
  "criticalAlertAudioFileName": "",
  "errorAlertAudioFileName": "",
  "warningAlertAudioFileName": "",
  "shouldPlay": false
}
```

#### Filters

Use the full filter key set (`severity`, `sdted`, `chain`, `instance`, `dataPoint`, `rule`, `acked`, `dependencyRoutingState`, `dependencyRole`, `host`, `keyword`, `dataSource`, `cleared`, `group`).

- `filters.group` uses **URL-encoded** tokens: `%23%23defaultResourceGroup%23%23*` (not raw `##defaultResourceGroup##`)
- `filters.cleared` is typically `"no"` for active alerts only

### `gmap`

Template: `gmap.json`

Map overlay. Uses `mapPoints[]` with `deviceGroupFullPath` and `deviceDisplayName`.

### `deviceSLA`

Template: `deviceSLA.json`

SLA widget. Uses `metrics[]` with `metric`, `threshold`, `groupName`, `deviceName`.

## Content widgets

### `text`

Template: `text.json`

HTML content in `content` field.

## Cloud widgets

### `billing`, `cloudRecommendation`, `viz`

See `cloud-widget-stub.json`. These types rely on portal-specific `widgetConfig` JSON. **Export from LogicMonitor** and adjust scope; schema-only stubs are not sufficient for production dashboards.
