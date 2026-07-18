# Collector Script Caching

Store authentication tokens and other session data in a Collector-side cache between collection intervals, instead of reading/writing token files on every poll.

> Requires Collector version **29.100 or higher**.

## Overview

Collector scripts often need authentication tokens to communicate with APIs or services. In older Collectors, tokens were stored in files — every LogicModule that needed a token performed file read/write operations each collection cycle.

Collector 29.100+ provides `ScriptCache` to persist key-value data in memory on the Collector until it expires. This reduces I/O and simplifies token reuse across collection intervals and modules.

## ScriptCache API

| Method | Description |
|--------|-------------|
| `ScriptCache.getCache()` | Returns the cache object |
| `ScriptCache.set(String key, String value, [long expireIn])` | Store a value. `expireIn` is optional, in **milliseconds** (e.g. `3000` = 3 seconds) |
| `ScriptCache.get(String key)` | Retrieve a cached value, or `null` if missing/expired |
| `ScriptCache.remove(String key)` | Remove a key from the cache |

### Expiration

- Per-key: pass `expireIn` (milliseconds) to `set()`
- Default: `collector.script.cache.timeout` in `agent.conf` (value is in **minutes**)

```properties
collector.script.cache.timeout=30   # 30 minutes default TTL
```

## Backward-compatible pattern

`ScriptCache` is not available on Collectors older than 29.100. Use dynamic class loading with a fallback:

```groovy
def scriptCache
try {
    scriptCache = this.class.classLoader
        .loadClass("com.santaba.agent.util.script.ScriptCache")
        .getCache()

    String token = scriptCache.get("token1")
    if (token == null) {
        // Authenticate, fetch new token
        token = fetchNewToken()
        scriptCache.set("token1", token)
    }
    // Use token for API calls
} catch (Exception ex) {
    // Fallback for older Collectors — file-based token storage
}
```

If the class cannot be loaded, fall back to your legacy file-based approach.

## When to use ScriptCache

- API session tokens that remain valid across multiple collection intervals
- OAuth or bearer tokens with a known TTL
- Any auth handshake you want to avoid repeating every poll cycle

## When NOT to use ScriptCache

- One-shot credentials that change every request
- Collectors below version 29.100 (use fallback pattern or file-based storage)
- Secrets that should not persist in Collector memory — evaluate your security requirements

## Related concepts

- [Collection Modes](collection-modes.md) — when scripts run (per-instance vs per-device)
- HTTP/API recipes often benefit from ScriptCache — see [`groovy/http-rest`](../../recipes/groovy/http-rest/)

## Official documentation

- [Collector Script Caching](https://www.logicmonitor.com/support/collectors/collector-configurations/collector-script-caching)
