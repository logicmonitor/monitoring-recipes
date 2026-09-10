# Collection Modes

See also: [docs/concepts/collection-modes.md](../../../docs/concepts/collection-modes.md)

## Script mode (SCRIPT)

- Runs **once per instance** per poll
- Can use `instanceProps.get()`
- Output: `key=value` (no instance prefix)

## BatchScript mode (BATCHSCRIPT)

- Runs **once per device** per poll — required for scale
- Requires Multi-Instance + Active Discovery
- Output: `wildvalue.key=value` or JSON
- Datapoint keys in the module UI use `##WILDVALUE##.metricName` — **not inside the script**
- Cannot use `instanceProps.get()` or `##WILDVALUE##` in script body
- Use `datasourceinstanceProps` to loop instances (Collector 29.105+). The map
  **key** is the displayed instance name. The AD identifier is
  `instanceProperties.get("wildvalue")`. Emit that, not the map key.

## Script types

| Type | Platform |
|------|----------|
| Embedded Groovy | All Collectors |
| Embedded PowerShell | Windows Collectors |
| External | Collector OS dependent |

## Decision guide

| Scenario | Mode |
|----------|------|
| Few instances | Script |
| Many instances | BatchScript |
| Need per-instance context in script | Script |
| Single API call returns all instances | BatchScript |

## BatchScript pitfalls

- Invalid wildvalue chars (`:`, `#`, `\`, spaces) → NoData; sanitize in AD and output
- Emitting `DisplayedName.metric=` instead of `wildvalue.metric=` → `param not found in output`
- Prefer one upstream request that covers every instance; do not loop HTTP per instance
- PowerShell: use `Write-Output`, not `Write-Host`
- `collector.batchscript.timeout` in agent.conf applies to BatchScript only

## Official docs

- https://www.logicmonitor.com/support/logicmodules/datasources/data-collection-methods/scripted-data-collection-overview
- https://www.logicmonitor.com/support/batchscript-data-collection
