# Active Discovery

See also: [docs/concepts/active-discovery.md](../../../docs/concepts/active-discovery.md), [snippet-loader.md](snippet-loader.md), template `ad.groovy` in [module-templates](../assets/module-templates/README.md).

## Script AD output

One discovered instance per line. Use **`emit.instance(...)`** from the bound `modLoader` bootstrap (same as collection scripts). The snippet formats and sanitizes wildvalue, alias, optional description, and optional **instance level properties (ILPs)**.

```groovy
emit.instance("instance1_id", "instance1_name")
emit.instance("instance3_id", "instance3_name", "instance3_description")
emit.instance("instance5_id", "instance5_name", "instance5_description", [
    fooProperty: "somevalue",
    barProperty: "anothervalue",
])
```

| Overload | Collector line shape |
|----------|----------------------|
| `(wv, alias)` | `wv##alias` |
| `(wv, alias, description)` | `wv##alias##description` |
| `(wv, alias, ilpMap)` | `wv##alias######auto.key=val&...` |
| `(wv, alias, description, ilpMap)` | `wv##alias##description####auto.key=val&...` |

ILP map keys without `auto.` / `predef.` get an `auto.` prefix. Use `&` between properties. Do not emit null or empty ILP values.

Do not use `emit.dp()` in AD scripts — that is for collection metrics.

## Limits

| Constraint | Limit |
|------------|-------|
| Wildvalue | 1024 chars; no `=`, `:`, `\`, `#`, or spaces |
| Wildalias | 255 chars |
| Instance properties (combined) | 49,000 chars |

## Return codes

- **0** — instance list updates
- **Non-zero** — failure; existing instances preserved
- Exit **0** with empty output — **all instances removed** (dangerous)

## Import JSON

`activeDiscovery.discoveryInterval`: **`0m`**, **`15m`**, **`60m`**, or **`1440m`** only. Greenfield default **`60m`**. See [datasource-import-json.md](datasource-import-json.md).

## Official docs

- https://www.logicmonitor.com/support/active-discovery
- https://www.logicmonitor.com/support/logicmodules/datasources/active-discovery/script-active-discovery
