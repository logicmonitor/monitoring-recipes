# ConfigSource import JSON

`type: 6`, `dataSourceType: 2`, `collectionMethod: script`.

## Distinct fields

| Field | Typical value |
|-------|----------------|
| `fileFormat` | `arbitrary`, `json`, etc. |
| `configChecks` | Fetch/diff/ignore rules (clone structure from portal export) |
| `multiInstance` | `true` when AD discovers multiple config instances |

## Scripts

Same bundle convention as DataSource:

- `collect.*` → `collectionAttrs`
- `ad.*` → `activeDiscovery.params` when `multiInstance` and `discoveryMethod: ad_script`

Collection script outputs **raw text** (config body) for the instance `wildvalue`. AD outputs `wildvalue##wildalias` lines.

## Config checks

`configChecks` is an array of objects with `type` (`fetch`, `ignore`, …), `severity`, `name`, and `attrs`. For new modules, start from a template export or minimal `fetch` check:

```json
{
  "severity": "warn",
  "name": "RetrievalTest",
  "type": "fetch",
  "triggerInterval": 0,
  "description": ""
}
```

Omit `originId` on new checks unless cloning from an export.
