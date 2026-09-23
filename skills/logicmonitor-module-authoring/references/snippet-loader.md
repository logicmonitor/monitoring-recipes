# Snippet loader

How platform snippets are resolved on the collector. **Do not copy** `loader.groovy` or other snippet source into custom modules — always use the bootstrap below.

See also: [module-snippets.md](module-snippets.md), [snippets-catalog.md](snippets-catalog.md)

## Bootstrap (required in Groovy scripts)

```groovy
import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
```

The object returned by `Snippets.getLoader()` exposes `load(name, minimumVersion)` (and related helpers). Recipes pin a **minimum compatible version** per snippet, for example `loader.load("lm.emit", "0")`.

## Version pins

| Call | Meaning |
|------|---------|
| `load("lm.emit", "0")` | Latest **0.x** version that is ≥ `0` (same major line; higher minors are OK if reverse-compatible) |
| `load("lm.remote", "0.6.0")` | Latest **0.x** ≥ `0.6.0` when major is `0` |
| `load("name", "-1")` | Latest active version (platform default when only name is passed) |

Rules (from platform loader behavior):

- Version argument is a **minimum** within the same **major** version family.
- Invalid version strings throw at load time.
- If the snippet cannot be resolved from cache, filesystem, or portal, load throws: `Unable to load Snippet - <name>`.

## Resolution order (troubleshooting)

When a snippet is missing or stale, the loader roughly:

1. Collector **ScriptCache** entry for `MOD.SnippetLoader.groovy.<name>_<minimumVersion>`
2. If `snippet.auto.update` is not `false`, fetch/update from portal (snippet list + payload), cache under `../lib/snippets/groovy/<name>/<version>.groovy`
3. Fall back to **on-disk** versions under `lib/snippets/groovy/<name>/`
4. Throw if nothing qualifies

On-disk layout (relative to collector install):

```
lib/snippets/groovy/<snippetName>/<version>.groovy
lib/snippets/groovy/RemoteSnippetListCache.xml
```

Linux absolute path is often `/usr/local/logicmonitor/lib/snippets` (see [module-snippets.md](module-snippets.md)).

## Collector settings (reference)

| Setting | Role |
|---------|------|
| `snippet.auto.update` | When `false`, prefer filesystem before network update |
| `snippet.memorycache` | When `false`, skip caching snippet bodies in ScriptCache |
| `snippet.cache.timeout` | Remote snippet list cache TTL (ms); default 86400000 |
| `snippet.read.timeout` | HTTP read timeout for snippet download (ms) |
| `snippet.debug` | When `true`, loader may set a `debug` binding flag |

Agents authoring modules should **not** call `clearCacheForUpdate()` from collection scripts — it is an operator-only cache flush with confirmation code.

## Loading pattern in scripts

Load snippets once after bootstrap; reuse the returned script object:

```groovy
def emit = loader.load("lm.emit", "0")
def snmp = loader.load("proto.snmp", "0")
```

Heavy modules may load optional snippets only on code paths that need them (HTTP, JDBC, topology).

## Prerequisites

1. **LogicMonitor_Collector_Snippets** module installed on the collector
2. Monitoring enabled on the **collector host** resource
3. Collector can reach the portal for updates (or snippets already present under `lib/snippets`)

If loads fail in dev, verify snippet module installation and collector host monitoring before rewriting collection logic.
