# Script Cache

Collector 29.100+ only. Cache auth tokens between collection intervals using `ScriptCache`.

## API

```groovy
def scriptCache = this.class.classLoader
    .loadClass("com.santaba.agent.util.script.ScriptCache")
    .getCache()

scriptCache.set("token1", "value", 300000)  // optional expireIn in ms
def token = scriptCache.get("token1")
scriptCache.remove("token1")
```

| Method | Notes |
|--------|-------|
| `getCache()` | Get cache instance |
| `set(key, value, [expireIn])` | `expireIn` in milliseconds |
| `get(key)` | Returns null if missing/expired |
| `remove(key)` | Delete key |

Default TTL: `collector.script.cache.timeout` in `agent.conf` (minutes).

## Backward-compatible pattern

Wrap in try/catch — fall back to file-based tokens on older Collectors:

```groovy
try {
    def scriptCache = this.class.classLoader
        .loadClass("com.santaba.agent.util.script.ScriptCache")
        .getCache()
    def token = scriptCache.get("token1")
    if (token == null) {
        token = fetchNewToken()
        scriptCache.set("token1", token)
    }
} catch (Exception ex) {
    // legacy file-based fallback
}
```

## When to use

API auth tokens, session cookies, OAuth tokens — anything valid across multiple polls.

## Official docs

- https://www.logicmonitor.com/support/collectors/collector-configurations/collector-script-caching
