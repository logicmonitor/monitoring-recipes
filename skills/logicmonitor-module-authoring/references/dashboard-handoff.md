# Dashboard handoff

When building dashboards for a new DataSource, use the [logicmonitor-dashboard-authoring](../../logicmonitor-dashboard-authoring/SKILL.md) skill.

## `dataSourceFullName`

From portal import JSON:

```
{displayedAs} ({name})
```

Example: `displayedAs` = `Cluster Health Status`, `name` = `Elasticsearch_Cluster_Health_Status` →

`Cluster Health Status (Elasticsearch_Cluster_Health_Status)`

## Datapoint names

Dashboard widgets reference `dataPointName` values that must match `datapoints[].name` in the LogicModule JSON — not `interpretExpr` unless they are the same.

## Export for dashboard tooling

Dashboard `extract-datapoint-index.py` expects export field `dataPoints` (camelCase). Portal LogicModule JSON uses `datapoints`. When indexing:

- Map `displayedAs` → `displayName`
- Map `datapoints` → `dataPoints` for the index script, or pass a normalized copy.

## Validation chain

1. `validate-module.py` on the module bundle
2. `pack-module.py` then import to portal (or use packed JSON)
3. `validate-dashboard.py --datapoints <exports>` on dashboard JSON
