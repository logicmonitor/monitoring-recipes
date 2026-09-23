# Layout Conventions

## Grid

- 12 columns (`sizex` max 12)
- `col`, `row` are 1-based
- Typical widget heights: `sizey: 2` (text/header), `3` (graph/KPI), `4-8` (tables, NOC)

## Common layouts

### Overview row (KPIs + graph)

```
[col 1-3, sizex 3] bigNumber | bigNumber | bigNumber | [col 10, sizex 3] bigNumber
[col 1, sizex 12] cgraph trend
```

### Table + NOC sidebar

```
[col 1, sizex 8] dynamicTable
[col 9, sizex 4] noc
```

### Full-width alert strip

```
[col 1, sizex 12, sizey 3] alert
```

## Naming conventions

- Include scope in title: `Top ##defaultResourceGroup## Lambda Functions by Invocations`
- Trend graphs: suffix `(Trend)`
- NOC widgets: `Alert Status` or `Resource Health`

## Tokens in titles and scope

| Token | Use |
|-------|-----|
| `##defaultResourceGroup##` | Resource group scope |
| `##RESOURCENAME##` | Device/resource name in labels |
| `##INSTANCE##` | Instance name in labels |
| `##RESOURCEGROUP##` | Group name |

See [assets/widget-templates/README.md](../assets/widget-templates/README.md) for template pairings.
