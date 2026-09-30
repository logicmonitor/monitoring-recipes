# LogSource

Log ingestion into LM Logs — searchable, alertable, and aggregatable.

## Purpose

Collect and forward log data to LM Logs for querying, alerting, and analytics. The **Script Logs** LogSource type uses a Groovy script to call APIs and import logs on a schedule.

## When to use

- Application logs accessible via API
- Custom log sources that don't fit built-in LogSource types (log files, syslog, Windows events, SNMP traps)
- Any log data you want to search, query, filter, or aggregate

## When NOT to use

If you only need always-on alerting with no search requirement and don't have LM Logs, EventSource may suffice — but LogSource is preferred for new work.

## Script Logs LogSource

### Configuration highlights

| Setting | Notes |
|---------|-------|
| **Collection Interval** | How often the import script runs (default: 1 hour) |
| **Collection script** | Groovy script that calls an API and returns log events |
| **Include Filters** | Filter events by message (Contain, RegexMatch, etc.) — AND by default, OR with EA Collector 38.500+ |
| **Log Fields** | Static, regex, or LM property tokens for metadata tags |
| **Resource Mappings** | Map log properties to monitored resources |

AppliesTo must correctly match target resources. You need Manage permissions on at least one mapped resource.

## Output format

The import script outputs a JSON object with an `events` array. Each event **must** include a `message` field.

```json
{
  "events": [
    {
      "message": "This is the message of the event",
      "customAttribute": "This is a custom attribute"
    }
  ]
}
```

### Rules

- `message` is **mandatory** — events without a parseable message are discarded
- Optional `timestamp` field — if present, used during ingestion; otherwise defaults to receive time
- Script must exit with code **0** for output to be processed; non-zero discards output to avoid partial ingestion
- Additional top-level attributes (e.g. `status`, `message`) can indicate script status

Use `groovy.json.JsonBuilder` or similar to construct output from API responses.

See [Output Formats](../concepts/output-formats.md).

## Official documentation

- [Script Logs LogSource Configuration](https://www.logicmonitor.com/support/script-logs-logsource-configuration)
- [Configuring a LogSource](https://www.logicmonitor.com/support/logicmodules/logsources/creating-logsources/)
