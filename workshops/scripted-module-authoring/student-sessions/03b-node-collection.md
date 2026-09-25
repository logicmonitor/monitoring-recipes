# Session 03B — Collect Metrics from Discovered Instances

## Goal

Use each discovered node instance to collect node-level metrics.

## Build

1. Add the collection script to `Training_Fabric_Node`.
2. Iterate through `datasourceinstanceProps`.
3. Read the current instance wildvalue.
4. Call `https://<system.hostname>/api/v1/nodes/{wildvalue}`.
5. Parse the node response and emit metrics for the current wildvalue.
6. Add matching datapoints and a graph to the DataSource.

## Datapoints

| Datapoint | Description | Unit |
|---|---|---|
| `health` | Node health status | none |
| `cpu_percent` | CPU utilization | `%` |
| `memory_percent` | Memory utilization | `%` |
| `interface_count` | Number of interfaces | count |
| `error_rate_percent` | Node error rate | `%` |

## Validate

Run collection and confirm:

- Every discovered node receives metrics.
- Metrics remain associated with the correct wildvalue.
- Poll Now returns values for all datapoints.
- The node graph contains CPU, memory, and error-rate data.
- No values are emitted under an empty or incorrect instance key.

## Discuss

- How does the collection script know which node to request?
- What would happen if the API returned a node not present in Active Discovery?
- Why should discovery and collection be tested separately?

## Checkpoint

Collection answers: **What is each discovered component doing?**
