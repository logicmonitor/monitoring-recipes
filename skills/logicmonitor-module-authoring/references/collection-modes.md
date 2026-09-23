# Collection Modes

See also: [docs/concepts/collection-modes.md](../../../docs/concepts/collection-modes.md)

## Script mode (SCRIPT)

- Runs **once per instance** per poll
- Can use `instanceProps.get()`
- Output: `key=value` (no instance prefix)

## BatchScript mode (BATCHSCRIPT)

- Runs **once per device** per poll — required for scale
- Requires Multi-Instance + Active Discovery
- Output: `instance.key=value` or JSON
- Datapoint keys use `##WILDVALUE##.metricName` — **not inside the script**
- Cannot use `instanceProps.get()` or `##WILDVALUE##` in script body
- Use `datasourceinstanceProps` to loop all instances in Groovy

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
- PowerShell: use `Write-Output`, not `Write-Host`
- `collector.batchscript.timeout` in agent.conf applies to BatchScript only

## Official docs

- https://www.logicmonitor.com/support/logicmodules/datasources/data-collection-methods/scripted-data-collection-overview
- https://www.logicmonitor.com/support/batchscript-data-collection
