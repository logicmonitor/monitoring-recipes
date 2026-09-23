---
name: logicmonitor-dashboard-authoring
description: >-
  Authors importable LogicMonitor dashboard JSON from exported DataSource
  LogicModules. Use when creating dashboards, dashboard widgets, datapoint
  references, dataSourceFullName formatting, or validating dashboard JSON
  against the bundled dashboard schema.
license: Apache-2.0
compatibility: LogicMonitor portal dashboard import; self-contained skill (schema + scripts bundled)
metadata:
  author: logicmonitor
  version: "0.2.2"
---

# LogicMonitor Dashboard Authoring

Copy the whole `logicmonitor-dashboard-authoring/` directory to `~/.cursor/skills/` or `.cursor/skills/`.

| Path | Purpose |
|------|---------|
| `schema/` | JSON Schema for validation (`dashboard.schema.json`, `common.defs.json`) |
| `scripts/` | `validate-dashboard.py`, `extract-datapoint-index.py` |
| `assets/widget-templates/` | Per-widget JSON starters |
| `references/` | Themes, layout, datasource naming, widget notes |

## Workflow

### 1. Inputs

- Target scope (`widgetTokens`, usually `defaultResourceGroup`)
- Exported **DataSource JSON** from LogicMonitor (datapoints + naming)

### 2. Datapoint index (optional)

```bash
pip install jsonschema
python scripts/extract-datapoint-index.py path/to/ds-exports/ -o datapoint-index.json
```

Field rules: [references/datapoint-references.md](references/datapoint-references.md).

### 3. Widget templates

Use [assets/widget-templates/README.md](assets/widget-templates/README.md). Do not transpose patterns across widget types.

### 4. Author

```json
{
  "name": "My Dashboard",
  "description": "",
  "widgetsConfigVersion": 2,
  "version": 2,
  "type": "dashboard",
  "widgetTokens": [{ "name": "defaultResourceGroup", "value": "*" }],
  "widgets": []
}
```

Defaults: `theme: "newSolidDarkBlue"`, `interval: 3`, `timescale: "day"`, `version: 2`, `displaySettings: {}`. Themes/colors: [references/themes-and-colors.md](references/themes-and-colors.md).

### 5. Validate

```bash
python scripts/validate-dashboard.py my-dashboard.json --schema-only
python scripts/validate-dashboard.py --datapoints path/to/ds-exports/ my-dashboard.json
```

### 6. Deliver

One import-ready `.json` file.

## Hard rules

- Never invent graph colors — use `Auto` or [references/themes-and-colors.md](references/themes-and-colors.md)
- Never reference datapoints not in the user's DataSource exports
- `noc`: `dataSourceDisplayName` only — never `dataSourceFullName`
- `cgraph`, `bigNumber`, `pieChart`, `gauge`, `dynamicTable`: `dataSourceFullName` — never `dataSourceDisplayName`
- `dynamicTable` rows: `groupFullPath` — never `deviceGroupFullPath`
- `cgraph` scope fields: glob objects `{ "isGlob": true, "value": "..." }` — never plain strings
- `alert`: clone [assets/widget-templates/alert.json](assets/widget-templates/alert.json); `sort` is string `"-startEpoch"`; `playSound` is an object
- Cloud widgets (`billing`, `cloudRecommendation`, `viz`): export from portal — do not hand-author `widgetConfig`

## References (read when needed)

| File | Content |
|------|---------|
| [datapoint-references.md](references/datapoint-references.md) | Datasource identity, scope, capacity RPN |
| [widget-types.md](references/widget-types.md) | Per-type notes (templates hold the shapes) |
| [schema-overview.md](references/schema-overview.md) | Top-level document, grid |
| [themes-and-colors.md](references/themes-and-colors.md) | Allowed themes and colors |
| [layout-conventions.md](references/layout-conventions.md) | Grid layouts |
