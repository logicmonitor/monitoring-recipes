# script-events

Fetch events from an HTTP API and emit **scripted EventSource** JSON.

## What this script does

Performs a proxy-aware HTTP GET and prints `{ "events": [ { happenedOn, severity, message } ] }`. Prefer [`script-logs`](../script-logs/) when the data should be searchable in LM Logs.

Modeled after Exchange scripted EventSources (for example Slack Events), which build an `events` array and `println JsonOutput.toJson(outMap)`.

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- EventSource collection method **Script Event**
- API endpoint reachable from the collector

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `api.url` | Yes* | Full API endpoint (*or replace `INSERT_API_ENDPOINT_HERE`) |
| `api.token` | No | Bearer token |
| `event.severity` | No | Fallback severity (`warn`, `error`, or `critical`) |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_API_ENDPOINT_HERE` | Default URL if `api.url` is unset |
| Field mapping | Map vendor JSON onto `happenedOn`, `severity`, `message`, `Source` |
| Poll window | Exchange modules often drop events older than one collection interval |

## Adapting for your module type

| Module type | Output change | Notes |
|-------------|---------------|-------|
| EventSource | Default JSON | Required: `happenedOn`, `severity`, `message`. Max 50 events/exec |
| LogSource | Drop `happenedOn`/`severity`; keep `message` | Use [`script-logs`](../script-logs/) |

## Related docs

- [EventSource](../../../docs/module-types/eventsource.md)
- [LogSource](../../../docs/module-types/logsource.md) — preferred for new searchable log work
- [Output Formats](../../../docs/concepts/output-formats.md)
