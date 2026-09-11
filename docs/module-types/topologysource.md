# TopologySource

Gather information used to define logical relationships among monitored devices.

## Purpose

Build topology maps by defining edges (relationships) between resources. TopologySources output JSON describing connections between nodes identified by their External Resource IDs (ERIs).

## When to use

- Service dependency mapping
- Application-to-infrastructure relationships
- Custom topology beyond auto-discovered connections
- Network routing/adjacency relationships

## Active Discovery

Predefined methods (SNMP, WMI, HTTP) cover most cases. Use [Script Active Discovery](../concepts/active-discovery.md) when custom enumeration is needed.

ERISource PropertySources can assign ERIs to resources that topology edges reference.

## Output format

TopologySource scripts output JSON with an `edges` array:

```json
{
  "edges": [
    {
      "type": "NETWORK",
      "from": "nyc-cisco-asr100-core-router",
      "to": "nyc-cisco-nexus9000-switch"
    },
    {
      "type": "Routing",
      "from": "nyc-aironet-c1600-ap-1",
      "to": "nyc-cisco-nexus9000-switch"
    }
  ]
}
```

| Field | Description |
|-------|-------------|
| `type` | Edge/relationship name (e.g. `NETWORK`, `Routing`) |
| `from` | Source node ERI |
| `to` | Target node ERI |

See [Output Formats](../concepts/output-formats.md).

## Related recipes

- [`groovy/topology-edges`](../../recipes/groovy/topology-edges/)
- [`groovy/add-eri`](../../recipes/groovy/add-eri/) — ERISource PropertySource for vertex IDs

## Official documentation

- [TopologySources Scripts](https://www.logicmonitor.com/support/topologysources-scripts)
- [TopologySource Configuration](https://www.logicmonitor.com/support/logicmodules/topologysources/creating-topologysources/)
