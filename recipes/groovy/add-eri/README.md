# add-eri

Assign External Resource IDs (ERIs) so TopologySources can draw edges to this resource.

## What this script does

Loads `lm.topo` and prints an ERI array with `emitEri` / `printEriArray`. Exchange `addERI_*` PropertySources use this exact pair. Set the PropertySource **Data Type** to **ERISource** in the portal (that schedule overrides the default daily PropertySource run).

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- Matching [`topology-edges`](../topology-edges/) TopologySource to consume the ERIs

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `system.displayname` | Yes | Used in the default ERI (`namespace--displayname`) |
| `topo.eri.namespace` | Yes* | ERI namespace (*or `INSERT_ERI_NAMESPACE_HERE`, e.g. `docker`, `storage`) |
| `topo.ert` | Yes* | External Resource Type (*or `INSERT_ERT_HERE`, e.g. `Container`, `PhysicalServer`) |
| `topo.namespace` / `topo.blacklist` | No | Passed through to `printEriArray` |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_ERI_NAMESPACE_HERE` | Prefix used by `emitEri` and default ERI construction |
| `INSERT_ERT_HERE` | Vertex type shown on topology maps |
| `topo.eri.value` | Override the full ERI string |
| `priority` | Lower numbers win when multiple ERIs collide |

## Adapting for your module type

| Module type | Output change | Notes |
|-------------|---------------|-------|
| PropertySource (ERISource) | `emitEri` + `printEriArray` | Default — do not println `auto.*` here |
| TopologySource | `registerEdge` + `generateTopology` | Use [`topology-edges`](../topology-edges/) |

## Related docs

- [PropertySource](../../../docs/module-types/propertysource.md)
- [TopologySource](../../../docs/module-types/topologysource.md)
- [Snippets catalog — lm.topo](../../../skills/logicmonitor-authoring/references/snippets-catalog.md)
