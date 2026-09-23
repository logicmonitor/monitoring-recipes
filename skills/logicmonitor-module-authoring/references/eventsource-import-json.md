# Scripted EventSource import JSON

`type: 1`, `collectionMethod: scriptevent`.

## Scripted fields

```json
"collectionAttrs": {
  "type": "groovy",
  "content": ""
}
```

Pack from `collect.*`. Script output: JSON `events` array — see [output-formats.md](output-formats.md).

## Alert behavior fields

| Field | Notes |
|-------|--------|
| `severity` | e.g. `warn`, `error`, `critical` |
| `clearAfterAck` | boolean |
| `clearAfterMin` | integer minutes |
| `doMapping` | boolean |

Optional: `alertSubject`, `alertBody`, `filters`.

## Non-script EventSources

Many built-in EventSources use other `collectionMethod` values (`wineventlog`, `awshealth`, …). Do not hand-author those; export from the portal and clone JSON structure.
