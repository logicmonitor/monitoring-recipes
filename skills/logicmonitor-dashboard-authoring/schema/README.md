# Bundled dashboard schema

Used by `scripts/validate-dashboard.py` (requires `jsonschema`).

| File | Role |
|------|------|
| `dashboard.schema.json` | Top-level dashboard document |
| `common.defs.json` | Widget `oneOf` branches and shared `$defs` |

Validate from the skill root:

```bash
pip install jsonschema
python scripts/validate-dashboard.py path/to/dashboard.json
```

Allowed themes, colors, and authoring conventions live in [references/themes-and-colors.md](../references/themes-and-colors.md). Per-widget datasource field rules: [references/datapoint-references.md](../references/datapoint-references.md).
