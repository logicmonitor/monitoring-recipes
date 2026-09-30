# LogicMonitor Dashboard Authoring Skill

This directory is a standalone skill bundle for creating and validating LogicMonitor dashboard import JSON.

Run commands from this directory:

```bash
python3 scripts/validate-dashboard.py dashboard.json
python3 scripts/validate-dashboard.py dashboard.json --datapoints path/to/datasource.json
```

The validator accepts either portal DataSource exports (`displayName`, `dataPoints`) or LogicModule import bundles (`displayedAs`, `datapoints`). Ordinary widgets can be authored from the bundled templates. Cloud widgets require a portal-exported widget configuration because their `widgetConfig` is opaque and account-specific.
