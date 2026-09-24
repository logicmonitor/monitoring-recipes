# Dashboard handoff

When building dashboards for a new DataSource, hand off the imported DataSource identity and datapoint names to the dashboard author.

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

Dashboard tooling accepts both portal export fields (`displayName`, `dataPoints`) and LogicModule import fields (`displayedAs`, `datapoints`).

## Validation chain

1. `validate-module.py` on the module bundle
2. `pack-module.py` then import to portal (or use packed JSON)
3. Validate the dashboard against the DataSource export or import bundle
