# topology-edges

Register TopologySource edges with `lm.topo` and print collector JSON.

## What this script does

Loads `lm.topo`, registers a single `from` → `to` edge, and prints `lmtopo.generateTopology(...)`. Exchange TopologySources (Docker, BGP, F5 HA, and others) all follow this pattern rather than hand-building `{ "edges": [...] }`.

Pair with [`add-eri`](../add-eri/) so both ends have External Resource IDs.

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- ERISource PropertySources applied so vertices exist

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `predef.externalResourceID` | Yes* | Source node ERI (*or set `topo.from.eri`) |
| `topo.to.eri` | Yes | Target node ERI |
| `topo.edge.type` | No | Relationship name (default placeholder `INSERT_EDGE_TYPE_HERE`) |
| `topo.namespace` / `topo.blacklist` | No | Passed through to `generateTopology` |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_EDGE_TYPE_HERE` | Edge type such as `NETWORK`, `Routing`, `Compute`, `Cluster` |
| `INSERT_FROM_ERI_HERE` / `INSERT_TO_ERI_HERE` | Fallback ERIs when properties are unset |
| Registration loop | Walk SNMP/API neighbors and call `registerEdge` per peer |

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| TopologySource | `registerEdge` + `generateTopology` | Default |
| PropertySource (ERISource) | `emitEri` / `printEriArray` | Use [`add-eri`](../add-eri/) |

## Related docs

- [TopologySource](../../../docs/module-types/topologysource.md)
- [add-eri](../add-eri/)
- [Snippets catalog — lm.topo](../../../skills/logicmonitor-authoring/references/snippets-catalog.md)
