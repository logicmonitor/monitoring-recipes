# Groovy Scripting

See [script-structure.md](script-structure.md), [snippets-catalog.md](snippets-catalog.md), and [collection-modes.md](collection-modes.md).

## Snippet-first approach

Prefer platform snippets over raw collector APIs. Load via the standard bootstrap:

```groovy
import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def emit = modLoader.load("lm.emit", "0")
```

See [snippet-loader.md](snippet-loader.md) for bootstrap and version pins, and [snippets-catalog.md](snippets-catalog.md) for snippet APIs.

## Script scoping (locals vs helpers)

Collector Groovy runs as a **Script** subclass. Top-level `def helper(...) { }` blocks are methods on that class, not nested functions. Prefer ordinary local variables and pass snippet handles into helpers explicitly. This keeps dependencies visible and avoids mutable script-level state.

```groovy
def emit = modLoader.load("lm.emit", "0")
def httpMod = modLoader.load("proto.http", "0")

def emitPokemonMetrics(emit, String wildvalue, int httpStatus) {
    emit.dp(wildvalue, "http_status", httpStatus)
}
```

Closures defined in the main script can capture local snippet handles, but explicit parameters are clearer for reusable helpers. Use binding-style assignment without `def` only when existing helper code must access a script property directly, or when preserving compatibility with a legacy module. In that case, document the exception locally.

**Symptom:** `groovy.lang.MissingPropertyException: No such property: emit for class: ScriptNN` inside a top-level method means the method is relying on a local variable. Prefer passing `emit` as an argument; changing it to a binding property (`emit = ...`) is the compatibility fallback, not the default pattern.

| Task | Snippet | Avoid |
|------|---------|-------|
| SNMP | `proto.snmp` | Raw `Snmp.*` without retries |
| SSH | `lm.remote` | Raw JSCH |
| HTTP | `proto.http` | Raw `Http` without proxy handling |
| JDBC | `lm.sql` | Manual connection without error maps |
| Output | `emit` (loaded `lm.emit` snippet) | Hand-rolled `println "key=value"` |
| Events JSON | `emit.events(...)` | Manual `events` JSON strings |
| Large JSON/XML slice | `lm.parse` | Fragile regex-only parsing |

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

def snmp = modLoader.load("proto.snmp", "0").create(host, props, startTime).withRetries(5)
def walkResult = snmp.walk('INSERT_OID_HERE')

walkResult.each { index, value ->
    emit.dp("metric_${index}", value)
}

return 0
```

For a starting pattern, see [Bundled Patterns](../assets/recipe-index.md).

## SSH execution (lm.remote)

```groovy
def remote = modLoader.load("lm.remote", "0.6.0")
def output = remote.exec(hostProps, 'INSERT_COMMAND_HERE')
// Parse output and emit via emit.dp(...)
```

See [assets/recipe-index.md](../assets/recipe-index.md) for optional repository recipes.

## HTTP REST (proto.http)

```groovy
def httpMod = modLoader.load("proto.http", "0")
def http = httpMod.httpSnippetFactory(hostProps)
def response = http.rawGet('https://api.example.com/endpoint', ['Authorization': 'Bearer token'])
```

For a portable HTTP starting point, see [assets/examples/http-rest-collect-snippet.groovy](../assets/examples/http-rest-collect-snippet.groovy).

## Output (`emit`)

After bootstrap in [snippet-loader.md](snippet-loader.md):

```groovy
def emit = modLoader.load("lm.emit", "0")

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
| TopologySource | JSON `edges` array; start with the bundled TopologySource starter and validate ERI/ERT semantics |
| EventSource | JSON `events` array with `happenedOn`, `severity`, and `message` |
| LogSource | JSON `events` array with `message`; exit 0 |
| DiagnosticSource | JSON `{data, format}` |
| RemediationSource | JSON `{data, format, remediationStatus}` |

Full reference: [output-formats.md](output-formats.md)

## Return codes

Return `0` on success. Non-zero on failure.

- Active Discovery: non-zero preserves existing instances on transient errors
- LogSource: non-zero discards all output

## Patterns

Use [module templates](../assets/module-templates/README.md), bundled [examples](../assets/examples/), and [Bundled Patterns](../assets/recipe-index.md).
