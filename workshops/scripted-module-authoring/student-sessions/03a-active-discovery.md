# Session 03A — Build Active Discovery

## Goal

Discover the repeating Training Fabric nodes and create one LogicMonitor instance for each node.

## Build

1. Create or extend the `Training_Fabric_Node` DataSource.
2. Keep the same category and credential targeting used by the controller DataSource.
3. In the Active Discovery script:
   - Request a bearer token.
   - Call `/nodes`.
   - Parse the JSON response.
   - Emit one instance for each node.
   - Use the node ID as the stable wildvalue.
   - Use the node name as the display name.
   - Add `auto.role` and `auto.site` as instance properties.

## Validate

Run Active Discovery and confirm:

- Three instances are created.
- Instance names are readable, such as `edge-01`, `edge-02`, and `core-01`.
- Wildvalues match the API node IDs and remain stable.
- `auto.role` and `auto.site` are present on each instance.

## Discuss

- Why must the wildvalue be stable?
- Why can the display name change without changing instance identity?
- What information belongs on the instance as an ILP?

## Checkpoint

Active Discovery answers: **What repeating components exist?** It does not collect their metrics.
