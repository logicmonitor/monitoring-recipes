# Groovy Scripting

See also: [script-structure.md](script-structure.md), [snippets-catalog.md](snippets-catalog.md), [docs/concepts/collection-modes.md](../../../docs/concepts/collection-modes.md)

## Snippet-first approach

Prefer platform snippets over raw collector APIs. Load via the standard bootstrap:

```groovy
import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
emit = loader.load("lm.emit", "0")
```

`.withBinding(getBinding())` is **required** — snippets write through the calling
script's binding, so without it `emit.*` calls are silently discarded and the
script produces no output while still exiting 0. Assign `emit` without `def` so
helper methods can reach it.

See [snippets-catalog.md](snippets-catalog.md) for all available snippets and version pins.

| Task | Snippet | Avoid |
|------|---------|-------|
| SNMP | `proto.snmp` | Raw `Snmp.*` without retries |
| SSH | `lm.remote` | Raw JSCH |
| HTTP | `URL.openConnection()` helper | `proto.http` — not a real snippet |
| JDBC | `lm.sql` | Manual connection without error maps |
| Output | `lm.emit` | Hand-rolled `println "key=value"` |

Requires **LogicMonitor_Collector_Snippets** module and monitoring enabled on collector host.

## hostProps

Access device/resource properties. Never hardcode credentials.

```groovy
def hostname = hostProps.get('system.hostname')
Map props = hostProps.toProperties().collectEntries { k, v -> [(k.toLowerCase()): v] }
```

Normalize props to lowercase for SNMP key case-insensitivity.

## Context variables by module type

| Variable | Available when | Use for |
|----------|----------------|---------|
| `hostProps` | Always | Device properties, credentials, connection info |
| `instanceProps` | DataSource Script mode (per instance) | Current instance properties |
| `datasourceinstanceProps` | DataSource/ConfigSource BatchScript | Loop all discovered instances |
| `alertProps` | DiagnosticSource / RemediationSource (alert-triggered only) | Alert context — see [alert-properties.md](alert-properties.md) |

**BatchScript:** Cannot use `instanceProps.get()`. Use `datasourceinstanceProps` instead.

```groovy
datasourceinstanceProps.each { instance, instanceProperties ->
    def wildValue = instanceProperties.wildvalue
    // generate metrics for each instance
}
```

## SNMP collection (proto.snmp)

```groovy
import com.santaba.agent.util.Settings

def host = hostProps.get('system.hostname')
Map props = hostProps.toProperties().collectEntries { k, v -> [(k.toLowerCase()): v] }
def startTime = System.currentTimeMillis()

def snmp = loader.load("proto.snmp", "0").create(host, props, startTime).withRetries(5)
def walkResult = snmp.walk('INSERT_OID_HERE')

walkResult.each { index, value ->
    emit.dp("metric_${index}", value)
}

return 0
```

See `recipes/groovy/snmp-walk/` and `recipes/groovy/snmp-get/` for collection, and `recipes/groovy/snmp-discovery/` for Active Discovery.

## SSH execution (lm.remote)

```groovy
def remote = loader.load("lm.remote", "0.6.0")
def output = remote.exec(hostProps, 'INSERT_COMMAND_HERE')
// Parse output and emit via lm.emit
```

See `recipes/groovy/ssh-exec/` for one-shot commands and `recipes/groovy/ssh-interactive-config/` for ConfigSource collection.

## HTTP REST

There is no verified `proto.http` snippet — see [snippets-catalog.md](snippets-catalog.md#http--no-verified-snippet).
Use a binding-scoped helper over `URL.openConnection()`:

```groovy
connectTimeoutMs = 10000
readTimeoutMs = 30000

def getJson(String url) {
    def conn = new URL(url).openConnection()
    conn.setConnectTimeout(connectTimeoutMs)
    conn.setReadTimeout(readTimeoutMs)
    conn.setRequestProperty('User-Agent', 'LM-Module/1.0')
    // check conn.responseCode, then parse conn.inputStream
}
```

See `recipes/groovy/http-rest/` for metrics, `recipes/groovy/script-logs/` for LogSources, and `recipes/groovy/script-events/` for EventSources.

## Output with lm.emit

```groovy
emit = loader.load("lm.emit", "0")

emit.dp("cpuUsage", 42)                              // DataSource Script
emit.dp("instanceId", "cpuUsage", 42)                // BatchScript
emit.instance("eth0", "GigabitEthernet0", "", [:]) // Active Discovery
emit.property("auto.vendor", "Cisco")                // PropertySource
```

## JSON output (Event, Log, Topology, Diag, Remediation, Config BatchScript)

Use `JsonOutput` — do not build JSON strings manually:

```groovy
import groovy.json.JsonOutput

print JsonOutput.toJson([data: "Example Output", format: "markdown"])
return 0
```

## ScriptCache / lm.cache (Collector 29.100+)

Cache auth tokens between polls. See [script-cache.md](script-cache.md) and [snippets-catalog.md](snippets-catalog.md#lmcache--scriptcache-wrapper).

## Timeout budgeting

```groovy
import com.santaba.agent.util.Settings

def timeout = Settings.getSettingInt("collector.batchscript.timeout",
    Settings.getSettingInt("collector.script.timeout", 120)) * 1000
timeout -= 2500  // cleanup buffer
```

## Output format by module type

| Module type | Groovy output |
|-------------|---------------|
| DataSource Script | `emit.dp("key", value)` |
| DataSource BatchScript | `emit.dp(wildvalue, "key", value)` |
| PropertySource | `emit.property("auto.key", value)` or `system.categories=Cat` |
| Active Discovery | `emit.instance(wv, alias, desc, ilpMap)` |
| ConfigSource Script | Print raw config text |
| ConfigSource BatchScript | JSON `data.<wildvalue>.configuration` |
| TopologySource | JSON `edges` array |
| EventSource / LogSource | JSON `events` array |
| DiagnosticSource | JSON `{data, format}` |
| RemediationSource | JSON `{data, format, remediationStatus}` |

Full reference: [output-formats.md](output-formats.md)

## Return codes

Return `0` on success. Non-zero on failure.

- Active Discovery: non-zero preserves existing instances on transient errors
- LogSource: non-zero discards all output

## Recipes

See `recipes/groovy/` in the monitoring-recipes repo. JSON module types: `script-logs`, `script-events`, `topology-edges`, `diagnostic`, `remediation`.
