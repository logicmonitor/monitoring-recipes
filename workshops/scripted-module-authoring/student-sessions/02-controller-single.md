# Session 02 — Build the Single-Instance DataSource

## Goal

Collect controller-level metrics from one API response without Active Discovery.

## Build

1. Create a DataSource named `Training_Fabric_Controller`.
2. Use this AppliesTo:

   ```text
   hasCategory("Training_Fabric") && fabric.api.user && fabric.api.pass
   ```

3. In the collection script:
   - Request a bearer token.
   - Call `/controller`.
   - Parse the JSON response.
   - Emit `controller_health`, `node_count`, and `api_latency_ms`.
4. Define matching datapoints in the DataSource JSON.
5. Add descriptions, units, sensible ranges, and an overview graph.

## Validate

Run collection with Poll Now and confirm:

- No Active Discovery script is required.
- The three datapoints populate.
- `node_count` matches the controller response.
- `api_latency_ms` is numeric and changes over time.
- The overview graph contains the controller metrics.

## Discuss

- What makes this DataSource single-instance?
- Why does it not need a wildvalue?
- Which parts are script behavior, and which parts are JSON metadata?

## Checkpoint

The controller is monitored as one logical object. Active Discovery is introduced only for the repeating node components.
