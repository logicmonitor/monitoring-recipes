# Active Discovery

See [snippet-loader.md](snippet-loader.md) and the `ad.groovy` template in [module-templates](../assets/module-templates/README.md).

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

## Instance level properties (ILPs) — prefer rich discovery

**Default authoring stance:** when discovery already has metadata about an instance (API payload, SNMP column, WMI field), attach **useful `auto.*` ILPs** on the same `emit.instance` line — not only `wildvalue` + `wildalias`.

ILPs are stored on the instance in the portal. They support:

| Use | Example ILPs |
|-----|----------------|
| **Active Discovery filters** | `auto.interface_admin_status=up`, `auto.volume_type=ssd` — exclude instances operators do not want monitored |
| **Group method: instance level property** | `auto.region`, `auto.tier` — auto-group instances in the resource tree |
| **Operator context** | `auto.firmware_version`, `auto.feature_tls13=true`, `auto.pokemon_type=electric` — visible on the instance without opening graphs |
| **Downstream tuning** | Static labels that rarely change (edition, role, pool name) so alerts/dashboards can reference instance props |

### What to emit

| Prefer on AD | Avoid on AD |
|--------------|-------------|
| Identifiers and taxonomy (type, role, edition, region) | High-churn metrics (CPU %, request rate) — use collection datapoints |
| Version strings and build numbers from discovery | Secrets, tokens, passwords |
| Feature/capability flags (`auto.backup_enabled=true`) | Huge blobs (full JSON documents) — summarize or omit |
| Stable enums from the discovered object | Null, empty, or placeholder values |

**Static or slow-changing** discovery fields belong in ILPs. **Time-series** values belong in `collect.*` via `emit.dp`.

### Naming

- Use **`auto.<snake_or_camel>`** keys in the map, or bare keys (snippet adds `auto.`).
- Keep names **module-specific** to avoid clashing with device-level `auto.*` from PropertySources (`auto.pokemon_type`, not `type`).
- Use string values the portal and filters can match (`true`/`false`, semver text, enum labels). Booleans and numbers are serialized by `lm.emit` — stay consistent across rediscoveries.

### Groovy pattern (HTTP / API discovery)

```groovy
emit = modLoader.load("lm.emit", "0")

items.each { item ->
    def wv = sanitizeWildvalue(item.id)   // slug or id — must stay stable for collection
    def alias = item.displayName
    def desc = item.summary ?: ""
    emit.instance(wv, alias, desc, [
        pokemon_type: item.primaryType,           // → auto.pokemon_type
        generation: String.valueOf(item.generation),
        "auto.is_legendary": item.legendary ? "true" : "false",
    ])
}
```

Omit map entries when the value is unknown — do not emit `null` or `""`.

### PowerShell

Same semantics via the `####auto.key=val&...` segment — prefer `Write-Output` with sanitized wildvalue:

```powershell
Write-Output "$wv##$alias##$desc####auto.volume_type=$volType&auto.tier=$tier"
```

See [script-structure.md](script-structure.md) shared AD/collection pattern.

### Portal follow-up

- Document meaningful ILPs in the module **description** or **technical notes** so operators know what filters can target.
- Configure Active Discovery filters and Group method when ILPs justify them.
- Greenfield JSON leaves `"filters": []` and `"groupMethod": "none"`; call out in handoff when operators should add filters after import.

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
