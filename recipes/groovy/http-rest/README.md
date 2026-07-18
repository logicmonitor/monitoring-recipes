# http-rest

HTTP GET against a REST API using `proto.http` with optional token caching via `lm.cache`.

## What this script does

Performs an HTTP GET request with proxy-aware connection handling. Optionally caches auth tokens between collection intervals using `lm.cache` (Collector 29.100+).

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- API endpoint reachable from collector (or proxy configured)

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `api.url` | Yes* | Full API endpoint URL (*or replace `INSERT_API_ENDPOINT_HERE`) |
| `api.token` | No | Bearer token for Authorization header |
| `proxy.enable` | No | Device-level proxy override |
| `proxy.host` / `proxy.port` | No | Device proxy settings |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_API_ENDPOINT_HERE` | Default endpoint if `api.url` property not set |
| Response parsing block | Map JSON fields to `emit.dp()` or `emit.property()` |

For POST requests or custom auth flows, extend using `http.rawPost()` — see [snippets-catalog](../../../skills/logicmonitor-authoring/references/snippets-catalog.md).

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| DataSource (Script) | `emit.dp("metricName", value)` | Map API JSON fields |
| PropertySource | `emit.property("auto.name", value)` | Inventory/metadata from API |
| DataSource (BatchScript) | `emit.dp(wildvalue, "field", value)` | Multi-instance from API array |

## Related docs

- [DataSource](../../../docs/module-types/datasource.md)
- [PropertySource](../../../docs/module-types/propertysource.md)
- [Script Cache](../../../docs/concepts/script-cache.md)
