# Scripted PropertySource, DiagnosticSource, RemediationSource

## PropertySource (`type: 5`)

No `collectionAttrs`. Single script blob:

```json
"script": {
  "type": "groovy",
  "content": ""
}
```

Pack from `collect.groovy` / `collect.ps1`.

Output: `auto.property=value` or `system.categories=value` only — see [output-formats.md](output-formats.md).

## DiagnosticSource (`type: 11`)

Same `script` object. Output JSON: `{ "data": "...", "format": "markdown" }`.

## RemediationSource (`type: 12`)

Same `script` object. Output includes `remediationStatus` as a string.

## Shared top-level fields

| Field | Required |
|-------|----------|
| `name` | yes |
| `description` | yes |
| `appliesTo` | yes |
| `script` | yes |
| `searchKeywords` | recommended |
| `technicalNotes` | optional |
| `group` | optional |

No `datapoints` or `graphs` on these types.
