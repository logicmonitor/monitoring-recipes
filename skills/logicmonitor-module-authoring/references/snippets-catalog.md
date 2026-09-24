# Snippets catalog

Which platform snippet to load, version pins, and **documented entry points** (distilled from platform snippet sources — not copied into this repo).

See also: [snippet-loader.md](snippet-loader.md), [module-snippets.md](module-snippets.md), [script-structure.md](script-structure.md)

## Prerequisites

1. Install **LogicMonitor_Collector_Snippets** on the collector
2. Enable monitoring on the **Collector host** resource
3. Use the bound bootstrap in [snippet-loader.md](snippet-loader.md)

## Snippet instance patterns

After `def modLoader = ...withBinding(getBinding())`, load snippets from **`modLoader`**:

| Snippet | After `modLoader.load(...)` | Typical next step |
|---------|----------------------------|-------------------|
| `lm.emit` | Script object | `emit.dp()` / `.instance()` / `.property()` / `.events()` |
| `proto.http` | Module object | `.httpSnippetFactory(hostProps)` → HTTP client |
| `proto.snmp` | Module object | `.create(host, props, startTime).withRetries(n)` |
| `lm.remote` | Module object | `.exec(hostProps, cmd)` or `.create(props)` |
| `lm.debug` | Module object | `.create(hostProps, debug, out)` → debug object |
| `lm.cache` | Module object | `.cacheSnippetFactory(lmDebug, logCacheContext)` |
| `lm.parse` | Module object | `.getJsonStringNode()` / `.getXMLStringNode()` |

Factories are **not** interchangeable with the `emit` snippet object — each platform snippet documents its factory or method entry point.

## Quick reference

| Snippet | Min version | Load | Primary API | Typical module types |
|---------|-------------|------|-------------|----------------------|
| `lm.emit` | `"1.3.0"` | `modLoader.load("lm.emit", "1.3.0")` | `.dp()`, `.instance()`, `.property()`, `.events()` | DataSource, AD, PropertySource, Event/Log |
| `proto.snmp` | `"0.2.0"` | `loader.load("proto.snmp", "0.2.0")` | `.create(host, props?, startTime?).withRetries(n).walk/get` | DataSource |
| `lm.remote` | `"0.7.1"` | `loader.load("lm.remote", "0.7.1")` | `.exec()`, `.create(props).exec()`, `.sftp()`, `.scp()`, `.shell()` | DataSource, ConfigSource |
| `proto.http` | `"1.0.0"` | `loader.load("proto.http", "1.0.0")` | `.httpSnippetFactory(hostProps)` → `rawGet/rawPost/rawDelete` | DataSource, PropertySource |
| `lm.sql` | `"0.1.0"` | `loader.load("lm.sql", "0.1.0")` | `.attemptConnection()`, `.runQuery()`, `.validatePorts()` | DataSource |
| `lm.cache` | `"0.3.1"` | `loader.load("lm.cache", "0.3.1")` | `.cacheSnippetFactory(debugSnip, keySuffix)` | HTTP auth caching |
| `lm.debug` | `"2.0.0"` | `loader.load("lm.debug", "2.0.0")` | `.create(hostProps, debug, out)` → `.debug/.info/.warn/.error` | All scripted |
| `lm.parse` | `"0.0.1"` | `loader.load("lm.parse", "0.0.1")` | `.getJsonStringNode()`, `.getXMLStringNode()` | DataSource (API/XML) |
| `lm.bitsandbobs` | `"0.2.0"` | `loader.load("lm.bitsandbobs", "0.2.0")` | `.probeTcpPort()`, `.keepAlive(hostProps)`, `.timer()` | DataSource, long scripts |
| `lm.topo` | `"0.4.4"` | `loader.load("lm.topo", "0.4.4")` | `.registerEdge()`, `.generateTopology()` | TopologySource |
| `lm.api` | `"0.2.3"` | `loader.load("lm.api", "0.2.3")` | `.lmApiSnippetFactory(hostProps, http, debug)` | Netscan, portal lookups |

`lm.topo.snmp` and other protocol-specific topology helpers may exist as separate snippet names in the platform catalog; prefer official TopologySource modules as templates before reimplementing.

---

## `lm.emit` — collector-parseable output

Load with `def emit = modLoader.load("lm.emit", "0")` after [snippet-loader.md](snippet-loader.md) bootstrap. Call `emit.dp`, `emit.instance`, `emit.property`, or `emit.events` — not hand-built `println` lines. Pass `emit` explicitly to top-level helper methods; use a binding property only for legacy helpers. See [groovy.md](groovy.md#script-scoping-locals-vs-helpers).

### DataSource

```groovy
def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def emit = modLoader.load("lm.emit", "0")

emit.dp("cpuUsage", 42)                        // Script: key=value
emit.dp("eth0", "ifInOctets", 12345)           // BatchScript: instance.field=value
```

Booleans become `1.0` / `0.0`. Null/empty values are emitted with collector-safe rules. Field and wildvalue names are sanitized (`# \ : =` and spaces in wildvalues → `_`).

### Active Discovery

See [active-discovery.md](active-discovery.md) for `emit.instance` overloads and ILPs. **Prefer** the `(wv, alias, description, ilpMap)` overload when discovery exposes stable metadata (`auto.version`, feature flags, types) for filters and instance context — not metrics.

### PropertySource

```groovy
emit.property("auto.vendor", "Cisco")
emit.property("system.categories", "MyCategory")
```

Only `auto.*` and `system.categories` are valid for PropertySource output.

### EventSource

```groovy
emit.events([
    [happenedOn: System.currentTimeMillis(), severity: "warn", message: "Example"]
])
```

EventSource events require `happenedOn`, `severity`, and `message`.

### LogSource

```groovy
emit.events([
    [message: "Example"]
])
```

LogSource events require `message` and a zero exit code. `emit.events()` uses `JsonOutput`; do not hand-build the `events` JSON when the snippet is available.

### Netscan (advanced)

Legacy line format: `emit.resource(ip, displayName)` and overloads with host props / group.

Enhanced JSON netscan: list of maps with keys `hostname`, `displayname`, `hostProps`, `groupName`, `collectorId` — invalid keys and disallowed `hostProps` (e.g. `auto.*`, most `system.*`) are stripped/sanitized by the snippet.

### hostProps parsing helpers (on emit object)

| Method | Purpose |
|--------|---------|
| `parseCsl(input)` | Comma-separated list → `List` (e.g. categories) |
| `parseBool(input)` / `parseBool(input, default)` | `"true"`/`"1"`/`"false"`/`"0"` |
| `parseInt` / `parseFloat` | Typed parsing with optional defaults |

---

## proto.snmp — SNMP walk/get with retries and timeout budget

```groovy
def host = hostProps.get("system.hostname")
Map props = hostProps.toProperties().collectEntries { k, v -> [(k.toLowerCase()): v] }

def snmp = modLoader.load("proto.snmp", "0").create(host, props, System.currentTimeMillis())
    .withRetries(5)

def walkResult = snmp.walk("INSERT_OID")   // Map index → value
def scalar     = snmp.get("INSERT_OID.1")
```

Notes:

- `create(host)` works with defaults; pass lowercase **props** map for SNMP v3/community props.
- Timeout is derived from `collector.script.timeout` minus an internal buffer (default 1000 ms).
- Optional `withThreads(n)` + `addWalk`/`addGet` + `fetch` batch multiple OIDs; use only when necessary — threading has overhead.
- Failed operations can throw `SNMP Error: ...` after retries; handle or fail the script with `return 1`.

Prefer this over raw `Snmp.walk` / `Snmp.get` without retry/time budgeting.

---

## lm.remote — SSH (SSHJ)

Credential resolution from `hostProps`:

| Property | Fallback |
|----------|----------|
| `ssh.user` | `config.user` |
| `ssh.pass` | `config.pass` |
| `ssh.cert` | `ssh.publickey` |
| `ssh.cert.pass` | `ssh.publickey.pass` |
| `ssh.port` | `22` |
| `ssh.challenge` / `auto.ssh.challenge` | `prompt=>response` pairs (comma-separated) |
| `ssh.preferredauthentications` / `auto.ssh.preferredauthentications` | collector SSH auth order |

```groovy
def remote = modLoader.load("lm.remote", "0.6.0")

def output = remote.exec(hostProps, "INSERT_COMMAND")

def session = remote.create(hostProps).withDebug(out).withKeepAlive(true)
output = session.exec("INSERT_COMMAND")
```

Also available: `remote.sftp(hostProps, remotePath)`, `remote.scp(hostProps, remotePath)`, and `session.shell()` for interactive ConfigSource-style sessions (allocate PTY, manage shell lifecycle, call `exit()` when done).

Host key verification uses a promiscuous verifier (platform default for monitoring scripts). Do not reimplement with raw JSCH.

---

## proto.http — proxy-aware HTTP

```groovy
def httpMod = modLoader.load("proto.http", "0")
def http = httpMod.httpSnippetFactory(hostProps)

def conn = http.rawGet("https://api.example.com/data",
    ["Authorization": "Bearer ${token}"],
    60000, 60000, false)

def body = conn.inputStream.text
def status = conn.responseCode
```

| Method | Notes |
|--------|--------|
| `rawGet(endpoint, headers, …)` | Optional query map overload |
| `rawPost(endpoint, headers, body, …)` | UTF-8 body |
| `rawDelete(endpoint, headers, body?, …)` | Optional body |

Proxy: honored when **device** `proxy.enable` (default true if unset) **and** collector `proxy.enable` are true. `proxy.exclude` on device or collector supports `|`/`glob *` host patterns. Pass `ignoreProxy: true` to bypass.

---

## lm.sql — JDBC

```groovy
def sql = modLoader.load("lm.sql", "0")

def conn = sql.attemptConnection(
    hostProps.get("jdbc.user"),
    hostProps.get("jdbc.pass"),
    hostProps.get("jdbc.url"))   // jdbc:vendor://host:port/db

if (conn.status != "success") {
    println "error=${conn.errors?.join(',')}"
    return 1
}

def result = sql.runQuery("SELECT …", conn.connection)
if (result.status == "success") {
    result.data.each { row -> emit.dp("col", row.col) }
} else if (result.status == "no data") {
    // empty result set
} else {
    println "error=${result.error}"
    return 1
}

conn.connection?.close()
```

`runQuery` status: `success` | `no data` | `failed`. Connection status: `success` | `failed`.

`validatePorts("3306,foo,42")` → `"3306,42"` for comma-separated port lists.

---

## lm.cache — ScriptCache wrapper

Requires Collector **29.100+** (`ScriptCache`). See [script-cache.md](script-cache.md).

The factory expects a **debug helper** with `LMDebugPrint(String)` (used internally). When not debugging, pass a no-op:

```groovy
def cacheMod = modLoader.load("lm.cache", "0")
def debugSnip = [LMDebugPrint: { msg -> if (debug) println msg }]
def cache = cacheMod.cacheSnippetFactory(debugSnip, "myModule")

def token = cache.cacheGet("authToken")
if (!token) {
    token = authenticate()
    cache.cacheSet("authToken", token, 300000) // expiry milliseconds
}
```

| Method | Purpose |
|--------|---------|
| `cacheGet` / `cacheSet` / `cacheRemove` | String values; keys prefixed with `keySuffix` |
| `cacheGetJson` | Parse JSON or return raw string |
| `filterData` / `filterJsonToString` | Shrink large objects before caching |

---

## lm.debug — structured debug logging

```groovy
def dbgMod = loader.load("lm.debug", "2.0.0")
def dbg = dbgMod.create(hostProps, debug, out)

dbg.debug("detail")
dbg.info("progress")
dbg.warn("recoverable")
dbg.error("failure")
```

Device properties: `debug.log` (`1`/`true`), `debug.logFile`, `debug.log.level` (1–4). Fluent: `withDebug()`, `withOutput()`, `withLogging()`, `withLogFile("../logs/…")`.

Pair with `lm.cache` via a small `LMDebugPrint` adapter (see above), not by assuming `LMDebugPrint` exists on the debug object.

---

## lm.parse — substring extraction from large responses

When full `JsonSlurper` / XML parser is heavy or fragile:

```groovy
def parse = modLoader.load("lm.parse", "0")
def node = parse.getJsonStringNode(bigJson, '"items":', false)
def xmlFrag = parse.getXMLStringNode(bigXml, "<entry>", false)
```

`multi: true` returns concatenated matches. Useful for trimming API payloads before metric emission.

---

## lm.bitsandbobs — misc collector helpers

```groovy
def b = modLoader.load("lm.bitsandbobs", "0")

if (b.probeTcpPort(host, 443)) { … }

b.keepAlive(hostProps)   // flags system.deviceId alive for long BatchScripts

def (result, ms) = b.timer({ expensiveCall() })
```

`getProxyInfo()` exists but **proto.http** is the supported path for HTTP proxy behavior in new modules.

---

## lm.topo — TopologySource JSON

Build edges in memory, then render:

```groovy
def topo = modLoader.load("lm.topo", "0")
def edges = []

topo.registerEdge(edges, "fromEri", "toEri", "dependsOn")
// Or use extended registerEdge overloads for displayType, instances, health metadata

def json = topo.generateTopology(edges, "myNamespace", [], false)
print json
return 0
```

`generateTopology` returns pretty-printed JSON with `vertices` and `edges` maps. Namespace prefixes ERIs and applies blacklist prefixes. Additional helpers (`eriPreProcessor`, `propsToErt`, LLDP/CDP processors) support LM-style topology modules — copy patterns from an official TopologySource when possible. Keep the `lm.topo` handle local and pass it into helper methods that build edges.

---

## lm.api — LogicMonitor REST from the collector

```groovy
def httpMod = modLoader.load("proto.http", "0")
def http = httpMod.httpSnippetFactory(hostProps)
def dbg = modLoader.load("lm.debug", "2.0.0").create(hostProps, false, out)
def apiMod = modLoader.load("lm.api", "0")
def api = apiMod.lmApiSnippetFactory(hostProps, http, dbg)
```

Credentials on device: `logicmonitor.access.id` / `logicmonitor.access.key` (aliases `lmaccess.id` / `lmaccess.key`).

Common methods: `getPortalDevices`, `findPortalDevice*`, `apiGetV2`, `apiGetManyV2` (pagination), `generateLMTokenAuth` for signed requests. Use for netscan dedupe and portal-side lookups — not for routine per-poll metric collection unless required.

---

## Selection guide

| Task | Use snippet | Avoid |
|------|-------------|-------|
| SNMP walk/get | `proto.snmp` | Raw `Snmp.*` without retries/time budget |
| SSH command / file | `lm.remote` | Raw JSCH |
| HTTP REST | `proto.http` | Raw `Http` without proxy/exclude handling |
| JDBC | `lm.sql` | Ad-hoc `Sql.newInstance` without status maps |
| Script output | `emit` (`lm.emit` snippet) | Hand-rolled `println` key=value / AD lines |
| Events JSON | `emit.events(...)` | Manual JSON string for events |
| Auth token reuse | `lm.cache` | Local files on collector |
| Debug | `lm.debug` | Unconditional `println` in production |
| Slice JSON/XML | `lm.parse` | Regex-only parsing of large bodies |
| Topology | `lm.topo` | Hand-built edge JSON without sanitization |
| Portal API | `lm.api` | Hard-coded portal URLs without signing |
