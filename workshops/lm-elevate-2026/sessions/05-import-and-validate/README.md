# Session 05 — Import and Validate the Finished Modules

## Why this matters

Import the completed modules and verify the full LogicMonitor behavior in the portal, including datapoints, instance properties, targeting, and graphs.

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
3. Run controller collection and verify its three datapoints and standard graph.
4. Run node Active Discovery and verify that three online node instances are created while the offline node is filtered out.
5. Review `auto.role`, `auto.site`, and `auto.status` on each discovered instance.
6. Run node collection and verify every metric maps to the correct wildvalue and instance.
7. Review datapoint descriptions, units, standard graphs, and the node overview graph.
8. Poll again and confirm values update, graphs render, and cached authentication continues to work.
9. Enable debug only for testing, then return normal runtime behavior.

## Final checklist

- PropertySource enriches the intended resource.
- The targeting rule does not target unrelated resources.
- Controller metrics collect without Active Discovery.
- Node instances have stable wildvalues.
- Offline instances are excluded by the Active Discovery filter.
- Node metrics are associated with the correct instances.
- Standard and overview graphs render from the collected datapoints.
- Shared authentication work is cached.
- Finished JSON imports cleanly and matches the tested scripts.

## Key idea

Validate the complete module suite by checking targeting, output, instance identity, properties, datapoints, and graphs together.
