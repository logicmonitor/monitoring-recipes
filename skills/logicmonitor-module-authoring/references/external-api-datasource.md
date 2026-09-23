# External / SaaS API DataSources

Use when the monitored target is **not** the device under `system.hostname` (public APIs, SaaS, aggregator endpoints).

## AppliesTo

- Prefer a **category** the operator assigns to logical API targets: `hasCategory("pokeapi")`, `hasCategory("my_saas_api")`.
- Do not require SNMP or ping reachability to `system.hostname` unless the resource truly represents that host.
- Portal **exports** often replace category appliesTo with device-specific expressions — keep category form in repo bundles.

## Collector and network

- HTTPS calls run from the **collector** (egress). Ensure firewall/proxy allows outbound access.
- Use `proto.http` via bound `modLoader` — see [snippets-catalog.md](snippets-catalog.md) `httpSnippetFactory(hostProps)` for proxy-related device properties.

## Device properties

| Property | Role |
|----------|------|
| `api.url` / custom | Base URL or full endpoint |
| `api.token` / `api.token.property` | Bearer or API key (never hardcode in script) |
| Custom `auto.*` | Species name, tenant id, query params — set via PropertySource or manual props |

## Script pattern

See [assets/examples/http-rest-collect-snippet.groovy](../assets/examples/http-rest-collect-snippet.groovy) and `recipes/groovy/http-rest/` in the monitoring-recipes repo root.

- Bound `modLoader.withBinding(getBinding())`
- `emit.dp("literal_metric_name", value)` with **string-literal** names when using `validate-module.py` (dynamic keys are not extracted)

## Import JSON

Greenfield rules: [import-json-overview.md](import-json-overview.md), [module-deliverable-layout.md](module-deliverable-layout.md).
