# Script ↔ JSON alignment

Enforced by `validate-module.py` when validating a **bundle directory**.

## DataSource datapoints

### `namevalue`

- Script emits `metric_key=value` (or `instance.metric_key=value` for batchscript).
- Datapoint `name` is the metric identifier in graphs/alerts.
- `interpretExpr` must equal the **key** portion of the emitted line (not the instance prefix).

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

The validator scans `collect.*` for:

- `lm.emit('key', ...)`
- `println "key=`
- PowerShell `Write-Output "key=`

Extend scripts to use these patterns when possible so automated checks work.
