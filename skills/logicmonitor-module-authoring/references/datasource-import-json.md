# DataSource import JSON

`type: 0`, `dataSourceType: 1`.

## Collection mode

| `collectionMethod` | Runs | Script output |
|--------------------|------|----------------|
| `script` | Per instance (or device if single-instance) | `key=value` per line |
| `batchscript` | Once per device | `instance.key=value` per line |

| `multiInstance` | AD | Bundle files |
|-----------------|----|--------------|
| `false` | Usually omitted | `collect.*` only |
| `true` | Required (`discoveryMethod: ad_script`) | `collect.*` + `ad.*` |

## `collectionAttrs`

```json
"collectionAttrs": {
  "type": "groovy",
  "content": ""
}
```

Content comes from `collect.groovy` / `collect.ps1` via `pack-module.py`.

## Active discovery

When `batchscript` + multi-instance:

**`discoveryInterval`:** portal accepts only **`0m`**, **`15m`**, **`60m`**, **`1440m`**. **Greenfield default: `60m`** so AD runs on a schedule after import. Use `0m` only when AD should run manually (device update / on-demand), not for typical multi-instance modules. Do **not** use `"1d"` or other minute strings.

```json
"activeDiscovery": {
  "discoveryMethod": "ad_script",
  "enabled": true,
  "discoveryInterval": "60m",
  "groupMethod": "none",
  "deleteInactiveInstances": false,
  "autoDeleteInstances": true,
  "disableDiscoveredInstances": false,
  "showDeletedInstanceDays": 0,
  "filters": [],
  "params": { "type": "groovy", "content": "" }
}
```

`params.content` from `ad.*`.

## Datapoints

Typical scripted numeric datapoint (`namevalue`):

```json
{
  "name": "metric_name",
  "description": "",
  "dataType": 7,
  "type": "gauge",
  "interpretMethod": "namevalue",
  "interpretExpr": "metric_name",
  "useValue": "output",
  "maxDigits": 4,
  "noData": "Do not trigger an alert",
  "clearInterval": 0,
  "triggerInterval": 0,
  "originId": "REPLACE_WITH_UNIQUE_ID",
  "statusDisplayNames": []
}
```

| Field | Guidance |
|-------|----------|
| `interpretMethod` | `namevalue` when script emits `key=value`; `none` + `useValue: output` for single raw value |
| `type` | `gauge`, `counter`, or `derive` |
| `dataType` | `7` for numeric (corpus default) |
| `threshold` | Alert expression string on datapoint, e.g. `"= 1"` |
| `originId` | Required in exports; generate unique ID per datapoint |

## Graphs

```json
{
  "name": "Graph Title",
  "title": "Graph Title",
  "verticalLabel": "",
  "min": 0,
  "max": 100,
  "timeScale": "1day",
  "displayPriority": 1,
  "scale1024": false,
  "rigid": false,
  "lines": [
    {
      "datapointName": "metric_name",
      "legend": "Metric",
      "color": "blue",
      "type": "line",
      "isVirtual": false
    }
  ],
  "datapoints": [
    {
      "name": "metric_name",
      "datapointName": "metric_name",
      "consolidationFn": "average"
    }
  ]
}
```

Line `type`: `line`, `stack`, or `area`. Use corpus color names (`blue`, `green`, `orange`, …).

## Intervals

`collectionInterval` as duration string: `1m`, `3m`, `1h`, `1d`, etc.
