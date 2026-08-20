# Snippets Catalog

When to load which platform snippet, version pins, and entry points. **Do not copy snippet source** — load at runtime via the snippet loader.

See also: [module-snippets.md](module-snippets.md), [script-structure.md](script-structure.md)

## Prerequisites

1. Install **LogicMonitor_Collector_Snippets** module on the collector
2. Enable monitoring on the **Collector host resource**
3. Use the standard bootstrap in every snippet-based Groovy script:

```groovy
import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
```

## Snippet reference

| Snippet | Min version | Load | Primary methods | Recipe |
|---------|-------------|------|-----------------|--------|
| `lm.emit` | `"0"` | `loader.load("lm.emit", "0")` | `.dp()`, `.instance()`, `.property()` | All Groovy output |
| `proto.snmp` | `"0"` | `loader.load("proto.snmp", "0")` | `.create(host).withRetries(5).walk(oid)` | snmp-walk, snmp-get, snmp-discovery |
| `lm.remote` | `"0.6.0"` | `loader.load("lm.remote", "0.6.0")` | `.exec(hostProps, cmd)`, `.create(hostProps).exec(cmd)` | ssh-exec, ssh-interactive-config, diagnostic, remediation |
| `proto.http` | `"0"` | `loader.load("proto.http", "0")` | `.httpSnippetFactory(hostProps)` | http-rest, script-logs, script-events |
| `lm.sql` | `"0"` | `loader.load("lm.sql", "0")` | `.attemptConnection()`, `.runQuery()` | jdbc |
| `lm.cache` | `"0"` | `loader.load("lm.cache", "0")` | `.cacheSnippetFactory(debug, keySuffix)` | http-rest, script-logs |
| `lm.debug` | `"0"` | `loader.load("lm.debug", "0")` | `.create(out)` → `.LMDebugPrint()` | Optional debugging |
| `lm.topo` | `"0"` | `loader.load("lm.topo", "0")` | `.registerEdge()`, `.generateTopology()`, `.emitEri()`, `.printEriArray()` | topology-edges, add-eri |
| `lm.api` | `"0"` | `loader.load("lm.api", "0")` | LM REST API client | Phase 2 — niche |

---

## lm.emit — output formatting

Load once, use for all collector-parseable output:

```groovy
def emit = loader.load("lm.emit", "0")

// DataSource Script mode
emit.dp("cpuUsage", 42)

// DataSource BatchScript mode
emit.dp("instanceId", "cpuUsage", 42)

// Active Discovery
emit.instance("eth0", "GigabitEthernet0", "Uplink", ["auto.speed": "1000"])

// PropertySource
emit.property("auto.vendor", "Cisco")
emit.property("system.categories", "MyCategory")
```

Only `auto.*` and `system.categories` are allowed for PropertySource.

---

## proto.snmp — SNMP operations

```groovy
def snmp = loader.load("proto.snmp", "0").create(host, props, startTime)
    .withRetries(5)

// Walk — returns Map[index: value]
def walkResult = snmp.walk("INSERT_OID_HERE")

// Get — returns scalar value
def value = snmp.get("INSERT_OID_HERE.1")
```

Handles timeout budgeting from collector Settings internally. Prefer over raw `Snmp.walk()` / `Snmp.get()`.

---

## lm.remote — SSH execution

Uses SSHJ (not raw JSCH). Reads credentials from hostProps:

| Property | Fallback |
|----------|----------|
| `ssh.user` | `config.user` |
| `ssh.pass` | `config.pass` |
| `ssh.cert` | `ssh.publickey` |
| `ssh.port` | `22` |

```groovy
def remote = loader.load("lm.remote", "0.6.0")

// One-shot exec
def output = remote.exec(hostProps, "INSERT_COMMAND_HERE")

// Stateful session with debug
def session = remote.create(hostProps).withDebug(out)
def output = session.exec("INSERT_COMMAND_HERE")
```

For ConfigSource collection (pager + enable), see `recipes/groovy/ssh-interactive-config/`. Exchange `SSH_Interactive_Standard` covers full PTY/prompt handling.

---

## proto.http — HTTP client

Proxy-aware HTTP with collector and device proxy settings:

```groovy
def httpMod = loader.load("proto.http", "0")
def http = httpMod.httpSnippetFactory(hostProps)

def response = http.rawGet("https://api.example.com/data", ["Authorization": "Bearer ${token}"])
def body = response.inputStream.text
def statusCode = response.responseCode
```

Methods: `rawGet()`, `rawPost()`, `rawDelete()` — each accepts headers, timeouts, and `ignoreProxy` flag.

---

## lm.sql — JDBC database queries

```groovy
def sql = loader.load("lm.sql", "0")

def user = hostProps.get("jdbc.user")
def pass = hostProps.get("jdbc.pass")
def url  = hostProps.get("jdbc.url")  // jdbc:vendor://host:port/database

Map conn = sql.attemptConnection(user, pass, url)
if (conn.status != "success") {
    println "error=${conn.errors?.join(',')}"
    return 1
}

Map result = sql.runQuery("INSERT_SQL_QUERY_HERE", conn.connection)
if (result.status == "success") {
    result.data.each { row ->
        emit.dp("columnName", row.columnName)
    }
} else if (result.status == "no data") {
    // handle empty result set
} else {
    println "error=${result.error}"
    return 1
}

conn.connection?.close()
return 0
```

`runQuery` returns `{status: 'success'|'no data'|'failed', data, error}`.

---

## lm.cache — ScriptCache wrapper

Pairs with [script-cache.md](script-cache.md). Use for auth tokens between collection intervals:

```groovy
def cacheMod = loader.load("lm.cache", "0")
def cache = cacheMod.cacheSnippetFactory(null, "myModule")

def token = cache.cacheGet("authToken")
if (!token) {
    token = authenticate()
    cache.cacheSet("authToken", token, 3600)  // expiry in seconds
}
```

Requires Collector 29.100+ for ScriptCache API.

---

## lm.topo — topology edges and ERIs

Exchange TopologySources and `addERI_*` PropertySources load `lm.topo` **with** `.withBinding(getBinding())`. Prefer this over hand-built `{ "edges": [...] }` JSON.

```groovy
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def lmtopo = loader.load("lm.topo", "0")

def keyNamespace = hostProps.get(hostProps.get("topo.namespace", ""), "")
def keyBlacklist = hostProps.get("topo.blacklist", "").tokenize(",")
def edges = []

lmtopo.registerEdge("NETWORK", fromEri, toEri, edges)
println lmtopo.generateTopology(edges, keyNamespace, keyBlacklist, null, false)

// ERISource PropertySource
def eriArray = new org.json.JSONArray()
lmtopo.emitEri("docker", 1, ["docker--${hostProps.get('system.displayname')}"], "Container", eriArray)
lmtopo.printEriArray(eriArray, keyNamespace, keyBlacklist)
```

See `recipes/groovy/topology-edges/` and `recipes/groovy/add-eri/`.

---

## Selection guide

| Task | Use snippet | Avoid |
|------|-------------|-------|
| SNMP walk/get | `proto.snmp` | Raw `Snmp.*` without retries |
| SSH one-shot command | `lm.remote` | Raw JSCH |
| HTTP REST API | `proto.http` | Raw `Http` API without proxy handling |
| JDBC query | `lm.sql` | Manual `Sql.newInstance` without error maps |
| Format output | `lm.emit` | Hand-rolled `println "key=value"` |
| Cache auth token | `lm.cache` | File-based token storage |
| Topology edges | `lm.topo` + `lm.topo.snmp` | Custom JSON edge building |
