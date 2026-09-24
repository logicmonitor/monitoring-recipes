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
python3 scripts/validate-module.py path/to/ModuleBundle/
python3 scripts/pack-module.py path/to/ModuleBundle/

# Optional stricter JSON Schema check (maintainers):
pip install jsonschema
python3 scripts/validate-module.py --with-schema path/to/ModuleBundle/
```

The schema is bundled for optional validation. It is not a source-code generator and requires no files outside this skill folder to use.
