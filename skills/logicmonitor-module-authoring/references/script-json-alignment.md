# Script ↔ JSON alignment

Enforced by `validate-module.py` when validating a **bundle directory**.

## DataSource datapoints

### `namevalue`

**Script mode**

- Script emits `metric_key=value`.
- Datapoint `name` is the metric identifier in graphs/alerts.
- `interpretExpr` equals the output line key: `metric_key`.

**BatchScript + `multiInstance: true`**

- Script emits `wildvalue.metric_key=value` (wildvalue must match AD wildvalues; sanitize invalid characters).
- Datapoint `name` is still `metric_key` (graphs/alerts reference `datapoints[].name`).
- `interpretExpr` must be **`##WILDVALUE##.metric_key`** — the portal key token, **not** the bare metric name and **not** used inside the Groovy/PowerShell script.
- The segment after `##WILDVALUE##.` must match the key in `emit.dp(wild, "metric_key", value)` (second string argument).

| Script stdout | `interpretExpr` in JSON |
|---------------|-------------------------|
| `pikachu.http_status=200` | `##WILDVALUE##.http_status` |
| `disk1.iops=9024` | `##WILDVALUE##.iops` |

`validate-module.py` compares the suffix after `##WILDVALUE##.` to keys found in `collect.*`.

### `none` (single output)

- Script prints one value (no `key=`).
- Datapoint: `interpretMethod: none`, `useValue: output`.

### `expression` (virtual)

- No script key; `interpretExpr` holds an RPN/expression referencing other datapoints.
- Graph lines may set `isVirtual: true`.

## Graphs

- Every `graphs[].lines[].datapointName` must exist in `datapoints[].name`.
- Every `graphs[].datapoints[].datapointName` should match a defined datapoint.
- Prefer adding a `graph.datapoints[]` entry per plotted metric with `consolidationFn: average`.

## Active discovery

- `ad.*` output: `wildvalue##wildalias` per line — see [active-discovery.md](active-discovery.md).
- `batchscript` + `multiInstance: true` requires `ad.*` and `activeDiscovery` in JSON.

## Pack sync

- `collectionAttrs.content` must equal `collect.*` file text (after pack).
- `activeDiscovery.params.content` must equal `ad.*` file text.

## PropertySource / Diag / Rem / EventSource

- No datapoint alignment.
- `script.content` must equal `collect.*` after pack.

## Key extraction (validator heuristic)

The validator scans `collect.*` for string-literal metric names:

| Pattern | Example |
|---------|---------|
| `emit.dp("key",` | `emit.dp("stat_hp", value)` |
| `emit.dp(wild, "key",` | BatchScript second argument |
| `println "key=` | Legacy / no-snippet scripts |
| `Write-Output "key=` | PowerShell |
| `lm.emit('key',` | Legacy placeholder only — not production pattern |

**Dynamic keys:** `emit.dp(dpName, value)` where `dpName` is a variable is **not** detected. Use string-literal first arguments for automated validation, or verify datapoints manually.

Keep [scripts/lib.py](../scripts/lib.py) patterns in sync when adding new emit styles.

## Greenfield JSON

Run `validate-module.py --strict-greenfield` on repo bundles to catch export-only fields and AD interval mistakes — see [import-json-overview.md](import-json-overview.md).
