# Output Formats

Script output format depends on **what the script is doing**. Getting this wrong is the most common authoring mistake.

## Quick reference

| Module type | Format | Example |
|-------------|--------|---------|
| Active Discovery | `wildvalue##wildalias` | `eth0##GigabitEthernet0/1` |
| DataSource — Script | `key=value` | `cpuPercent=42` |
| DataSource — BatchScript | `instance.key=value` | `disk1.iops=9024` |
| PropertySource | `auto.*` or `system.categories` | `auto.serial=ABC123` |
| ConfigSource — Script | Raw config text | Full file contents to stdout |
| ConfigSource — BatchScript | JSON | `data.instance.configuration` |
| TopologySource | JSON edges | `{"edges": [{"type":"NETWORK","from":"...","to":"..."}]}` |
| EventSource | JSON events | `{"events": [{"happenedOn":"...","severity":"warn","message":"..."}]}` |
| LogSource (Script Logs) | JSON events | `{"events": [{"message":"..."}]}` |
| DiagnosticSource | JSON | `{"data":"...","format":"markdown"}` |
| RemediationSource | JSON | `{"data":"...","format":"markdown","remediationStatus":"true"}` |

---

## Active Discovery

One instance per line:

```
wildvalue##wildalias
wildvalue##wildalias##description
wildvalue##wildalias##description####auto.foo=bar&auto.baz=qux
```

See [Active Discovery](active-discovery.md) for limits and error handling.

---

## DataSource

### Script mode

```
metric1=value1
metric2=value2
```

### BatchScript mode

```
instance1.metric1=value1
instance2.metric1=value2
```

Datapoint keys in module definition: `##WILDVALUE##.metric1`

See [Collection Modes](collection-modes.md).

---

## PropertySource

```
auto.propertyname=value
system.categories=CategoryName
```

**Only `auto.*` and `system.categories` are allowed.** Cannot set other `system.*` properties.

---

## ConfigSource

### Script mode (single instance)

Print raw configuration file contents to stdout. LogicMonitor handles diffing.

### BatchScript mode (multi-instance)

```json
{
  "data": {
    "instance1": {
      "configuration": "Configuration data of instance1"
    },
    "instance2": {
      "configuration": "Configuration data of instance2"
    }
  }
}
```

Config per instance must be at `data.<wildvalue>.configuration`. Invalid wildvalue characters return NoData.

---

## TopologySource

```json
{
  "edges": [
    {
      "type": "NETWORK",
      "from": "source-node-eri",
      "to": "target-node-eri"
    }
  ]
}
```

| Field | Description |
|-------|-------------|
| `type` | Relationship name |
| `from` | Source node ERI |
| `to` | Target node ERI |

---

## EventSource (Script)

```json
{
  "events": [
    {
      "happenedOn": "Fri Jun 05 09:17:47 UTC 2015",
      "severity": "warn",
      "message": "Event message (max 2000 chars)",
      "Source": "optional source"
    }
  ]
}
```

Required per event: `happenedOn`, `severity` (`warn`|`error`|`critical`), `message`.

Limits: 50 events per execution, 100 events per Collector per minute.

---

## LogSource (Script Logs)

```json
{
  "events": [
    {
      "message": "Required log message",
      "timestamp": "optional — defaults to receive time",
      "customField": "optional metadata"
    }
  ]
}
```

- `message` is **mandatory** — unparseable events are discarded
- Exit code **0** required for output to be processed

---

## DiagnosticSource

JSON preferred (plain-text fallback supported):

```json
{"data": "Example Output", "format": "markdown"}
```

| Field | Notes |
|-------|-------|
| `data` | Diagnostic content |
| `format` | Optional — `markdown` or plain text (default) |

```groovy
import groovy.json.JsonOutput
print JsonOutput.toJson([data: "Example Output", format: "markdown"])
return 0
```

---

## RemediationSource

```json
{"data": "Example Output", "format": "markdown", "remediationStatus": "true"}
```

| Field | Notes |
|-------|-------|
| `data` | Remediation output content |
| `format` | Optional — `markdown` or plain text (default) |
| `remediationStatus` | Optional **string** — shown separately in UI; displays N/A if omitted |

Use `JsonOutput.toJson()` in Groovy — avoid manual string building.

---

## Return codes

| Code | Meaning |
|------|---------|
| `0` | Success |
| Non-zero | Failure |

For Active Discovery: non-zero prevents instance removal on transient errors.
For LogSource: non-zero discards all output.

---

## Related module-type docs

- [DataSource](../module-types/datasource.md)
- [PropertySource](../module-types/propertysource.md)
- [ConfigSource](../module-types/configsource.md)
- [TopologySource](../module-types/topologysource.md)
- [EventSource](../module-types/eventsource.md)
- [LogSource](../module-types/logsource.md)
- [DiagnosticSource](../module-types/diagnosticsource.md)
- [RemediationSource](../module-types/remediationsource.md)
