# Snippet loader

How platform snippets are resolved on the collector. **Do not copy** `loader.groovy` or other snippet source into custom modules — always use the bootstrap below.

See also: [module-snippets.md](module-snippets.md), [snippets-catalog.md](snippets-catalog.md)

## Bootstrap (required in Groovy scripts)

```groovy
import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
```

`getBinding()` is the same binding object as the implicit `binding` in collector scripts (`hostProps`, `instanceProps`, etc.). Load **every** snippet from `modLoader`, not from an unbound loader instance.

## Sharing binding with loaded snippets

Loaded snippets run as separate script objects. Without the parent binding, `emit.dp()` can exit 0 while producing **no** collector-parseable output.

| Approach | When |
|----------|------|
| **`modLoader.withBinding(getBinding())`** (preferred) | Any script that loads one or more snippets |
| **`emit.binding = binding`** after `load("lm.emit")` | Minimal scripts with only `lm.emit` on an unbound loader |

Load `lm.emit` into a local `def emit` and call `emit.dp(...)` / `emit.instance(...)`. Pass it explicitly to top-level helper methods. Use a binding property only for compatibility with legacy helpers that cannot accept the dependency as an argument.

### Troubleshooting

| Symptom | Likely cause |
|---------|----------------|
| Exit 0, empty metric output; `println` in the **parent** collect script works | Snippets loaded without shared binding |
| Exit 0, empty output; snippet load throws | Snippets module missing or not updated on collector |
| `Unable to load Snippet - <name>` | Install **LogicMonitor_Collector_Snippets**; monitor collector host |
| `MissingPropertyException: No such property: emit` (or `httpMod`, `http`, …) inside a **helper method** | Pass the snippet handle into the helper; use a binding property only for legacy code — see [groovy.md](groovy.md#script-scoping-locals-vs-helpers) |

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

```groovy
def emit = modLoader.load("lm.emit", "0")
def snmp = modLoader.load("proto.snmp", "0")
```

Heavy modules may load optional snippets only on code paths that need them (HTTP, JDBC, topology).

## Prerequisites

1. **LogicMonitor_Collector_Snippets** module installed on the collector
2. Monitoring enabled on the **collector host** resource
3. Collector can reach the portal for updates (or snippets already present under `lib/snippets`)

If loads fail in dev, verify snippet module installation and collector host monitoring before rewriting collection logic.
