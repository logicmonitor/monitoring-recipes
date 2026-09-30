# Session 02 — Single-Instance DataSource

## Why this matters

Separate the collection contract from the implementation: the script emits keys, while datapoints describe and present those keys. Graphs are added in Session 05.

## Import this first

Import [`module-scaffold.json`](module-scaffold.json) using the [shared import steps](../README.md#importing-a-module). It already contains the guided student script. Review [`scripts/student.groovy`](scripts/student.groovy) alongside it. The complete implementation is [`scripts/reference.groovy`](scripts/reference.groovy).

## What to do

1. Review the supplied `/controller` request and response fields.
2. Complete the three marked datapoint emissions.
3. Set AppliesTo to:

   ```text
   hasCategory("Training_Fabric") && fabric.api.user && fabric.api.pass
   ```

4. Define datapoints with matching names, descriptions, units, and sensible ranges.

## Check your result

- Collection succeeds without Active Discovery.
- All three datapoints populate.
- `node_count` matches the API response.
- `api_latency_ms` changes between polls.
- The module contains matching datapoints for each reported value.

## Discuss

- Why is this a single-instance DataSource?
- Why is there no wildvalue in the output?
- Which decisions belong in the module settings and which belong in the script?

## Key idea

Good module design keeps emitted keys and datapoint definitions aligned.

## If you get stuck

Open [`scripts/reference.groovy`](scripts/reference.groovy) and compare the three output lines with the datapoint names in the module.
