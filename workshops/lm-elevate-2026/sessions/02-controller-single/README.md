# Session 02 — Single-Instance DataSource

## Teaching point

Separate the collection contract from the implementation: the script emits keys, while datapoints and graphs describe and present those keys.

## Starting artifact

Import [`module-scaffold.json`](module-scaffold.json); it already contains the guided student script. Review [`scripts/student.groovy`](scripts/student.groovy) alongside it. The complete implementation is [`scripts/reference.groovy`](scripts/reference.groovy).

## Complete

1. Review the supplied `/controller` request and response fields.
2. Complete the three marked datapoint emissions.
3. Set AppliesTo to:

   ```text
   hasCategory("Training_Fabric") && fabric.api.user && fabric.api.pass
   ```

4. Define datapoints with matching names, descriptions, units, and sensible ranges.
5. Add an overview graph for the controller metrics.

## Validate

- Collection succeeds without Active Discovery.
- All three datapoints populate.
- `node_count` matches the API response.
- `api_latency_ms` changes between polls.
- The graph reflects the same output contract.

## Talk through

- Why is this a single-instance DataSource?
- Why is there no wildvalue in the output?
- Which decisions belong in JSON and which belong in the script?

## Takeaway

Good module design keeps emitted keys, datapoint definitions, and graphs aligned.
