# Bundled LogicModule schema

Used by `scripts/validate-module.py --with-schema` (optional `jsonschema` package).

| File | Role |
|------|------|
| `logicmodule.schema.json` | Entry `oneOf` for import documents |
| `datasource.schema.json` | DataSource (`type: 0`) |
| `configsource.schema.json` | ConfigSource (`type: 6`) |
| `script-module.schema.json` | PropertySource / Diagnostic / Remediation |
| `eventsource.schema.json` | Scripted EventSource (`scriptevent`) |
| `logicmodule.common.defs.json` | Shared `$defs` and corpus enum stats (`x-corpusStats`) |

Validate from the skill root:

```bash
python scripts/validate-module.py path/to/ModuleBundle/
python scripts/pack-module.py path/to/ModuleBundle/

# Optional stricter JSON Schema check (maintainers):
pip install jsonschema
python scripts/validate-module.py --with-schema path/to/ModuleBundle/
```

## Regenerating from local exports

Maintainers with a local [`LogicModules/`](../../../LogicModules/) tree (gitignored) can refresh enums and stats:

```bash
python scripts/logicmodule-schema/extract-schema.py
```

Run from the repository root. Output is written here; commit the updated schema files with the skill.
