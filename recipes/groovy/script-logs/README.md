# script-logs

Fetch log records from an HTTP API and emit **Script Logs** JSON for a LogSource.

## What this script does

Performs a proxy-aware HTTP GET, maps the JSON response into `{ "events": [ { "message": "..." } ] }`, and prints it with `JsonOutput`. `message` is mandatory — rows without one are skipped. Exit code **0** is required or the collector discards the payload.

Modeled after Exchange Script Logs modules (for example Meraki Assurance Alerts), which print `JsonOutput.toJson(["events": output])` after de-duplicating against `lm.cache`.

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- LogSource type **LM Logs: Script Logs**
- API endpoint reachable from the collector

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `api.url` | Yes* | Full API endpoint (*or replace `INSERT_API_ENDPOINT_HERE`) |
| `api.token` | No | Bearer token; cached via `lm.cache` |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_API_ENDPOINT_HERE` | Default URL if `api.url` is unset |
| Row mapping block | Map vendor JSON fields onto `message` / `timestamp` / custom attributes |
| Dedup | Optional: store a hash in `lm.cache` so the same event is not re-ingested |

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| LogSource (Script Logs) | `print JsonOutput.toJson([events: events])` | Default — `message` required |
| EventSource | Add `happenedOn` and `severity` per event | Use [`script-events`](../script-events/) instead |

## Related docs

- [LogSource](../../../docs/module-types/logsource.md)
- [Output Formats](../../../docs/concepts/output-formats.md)
- [http-rest](../http-rest/) — same HTTP client for DataSource metrics
