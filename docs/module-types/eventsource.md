# EventSource

Non-numeric event-based data that triggers alerts. Data is **not searchable** in LM Logs.

## Purpose

Detect and alert on events using scripted or built-in collection (log files, Windows events, syslog, SNMP traps). Use **Script EventSource** when other EventSource methods don't fit your custom logging.

## When to use

- Custom logging that can't use standard EventSource methods
- Events you always want to alert on without search/query needs
- Environments without LM Logs

## When NOT to use

**Prefer [LogSource](logsource.md) for new work** when you need searchable, queryable, aggregatable log data in LM Logs.

## Script EventSource output format

Output must be a JSON object with an `events` array:

```json
{
  "events": [
    {
      "happenedOn": "Fri Jun 05 09:17:47 UTC 2015",
      "severity": "warn",
      "message": "This is the first event's message",
      "Source": "This is the source of the first event"
    }
  ]
}
```

### Event fields

| Field | Required | Description |
|-------|----------|-------------|
| `happenedOn` | Yes | Event date/time (see supported formats below) |
| `severity` | Yes | `warn`, `error`, or `critical` (case insensitive) |
| `message` | Yes | Event message (max 2,000 characters) |
| `Source` | No | Event source identifier |
| Custom attributes | No | Any additional key-value pairs per event |

Use `groovy.json.*` (e.g. `JsonBuilder`) to build output if your data isn't already JSON.

### Limits

- Maximum **50 events** per script execution
- Maximum **100 events** per Collector per minute

### Supported `happenedOn` formats

| Format | Example |
|--------|---------|
| ISO-8601 | `2016-01-06T23:48:41.445+08:00` |
| ISO-8601 (no TZ) | `2016-01-06T23:48:41` |
| HTTP | `Wed, 09 Feb 1994 22:23:32 GMT` |
| ctime | `Thu Feb 3 17:03:55 GMT 1994` |
| Common logfile | `03/Feb/1994:17:03:55 -0700` |

See [Output Formats](../concepts/output-formats.md).

## Official documentation

- [Script EventSource](https://www.logicmonitor.com/support/script-eventsource)
- [Creating EventSources](https://www.logicmonitor.com/support/logicmodules/eventsources/creating-eventsources/)
