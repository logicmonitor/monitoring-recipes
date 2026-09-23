# Dashboard Schema Overview

## Top-level document

```json
{
  "name": "Dashboard Name",
  "description": "",
  "widgetsConfigVersion": 2,
  "version": 2,
  "type": "dashboard",
  "santabaRelease": 156,
  "overwriteGroupFields": false,
  "widgetTokens": [
    { "name": "defaultResourceGroup", "value": "*" }
  ],
  "widgets": []
}
```

| Field | Required | Notes |
|-------|----------|-------|
| `name` | yes | Dashboard title |
| `widgets` | yes | Array of widget objects |
| `widgetsConfigVersion` | yes | Always `2` |
| `version` | common | `2` |
| `type` | common | `"dashboard"` |
| `widgetTokens` | common | Resource group and other tokens |
| `santabaRelease` | optional | Portal version at export time |
| `group` | optional | Folder metadata |
| `defaultDashboardFilters` | optional | Filter definitions |

## Widget wrapper

```json
{
  "position": { "col": 1, "row": 1, "sizex": 6, "sizey": 3 },
  "config": { "type": "cgraph", ... }
}
```

Grid is 12 columns wide. `col` and `row` are 1-based.

## Widget config common fields

Every widget config includes:

- `type` — widget discriminator
- `name` — widget title (supports tokens)
- `description` — usually `""`
- `theme` — see [themes-and-colors.md](themes-and-colors.md)
- `interval` — refresh minutes (default `3`)
- `timescale` — data window (default `day`)
- `version` — always `2`
- `displaySettings` — type-specific; often `{}`

## Validation

From the skill root:

```bash
pip install jsonschema
python scripts/validate-dashboard.py my-dashboard.json
```

Schema files: `schema/dashboard.schema.json`, `schema/common.defs.json` (bundled with this skill).
