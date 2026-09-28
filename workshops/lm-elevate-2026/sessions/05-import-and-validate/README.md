# Session 05 — Import and Validate the Finished Modules

## Why this matters

Import the completed modules and verify the full LogicMonitor behavior in the portal.

This is an integration exercise. Do not rebuild the modules from scratch; use the completed JSON files in this session.

## Import this first

Use the [shared import steps](../README.md#importing-a-module) for each file below.

## Import order

1. `addCategory_Training_Fabric`
2. `Training_Fabric_Controller`
3. `Training_Fabric_Node`

Use these refactored JSON files for the final import:

- [`addCategory_Training_Fabric.json`](addCategory_Training_Fabric.json)
- [`Training_Fabric_Controller.json`](Training_Fabric_Controller.json)
- [`Training_Fabric_Node.json`](Training_Fabric_Node.json)

The PropertySource must run before the DataSources can target the resource by category.

## Check your result

1. Confirm the resource has `Training_Fabric` and the `auto.fabric_*` properties.
2. Confirm `Training_Fabric_Controller` applies to the resource.
3. Run controller collection and verify its three datapoints and graph.
4. Run node Active Discovery and verify the three node instances.
5. Run node collection and verify metrics for every node.
6. Review instance properties, datapoint descriptions, units, and graphs.
7. Enable debug only for testing, then return normal runtime behavior.

## Final checklist

- PropertySource enriches the intended resource.
- The targeting rule does not target unrelated resources.
- Controller metrics collect without Active Discovery.
- Node instances have stable wildvalues.
- Node metrics are associated with the correct instances.
- Shared authentication work is cached.
- Finished JSON imports cleanly and matches the tested scripts.

## Key idea

Use the module-authoring skill for future modules. Start with the monitoring contract, prove the raw data flow, then add snippets, caching, graphs, alerts, and validation.
