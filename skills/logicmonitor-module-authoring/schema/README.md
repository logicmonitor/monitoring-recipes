# Bundled LogicModule schema

Used by `scripts/validate-module.py --with-schema` (optional `jsonschema` package).

| File | Role |
|------|------|
| `logicmodule.schema.json` | Entry `oneOf` for import documents |
| `datasource.schema.json` | DataSource (`type: 0`) |
| `configsource.schema.json` | ConfigSource (`type: 6`) |
| `script-module.schema.json` | PropertySource / Diagnostic / Remediation |
| `eventsource.schema.json` | Scripted EventSource (`scriptevent`) |
| `logsource.schema.json` | Standalone LogSource field reference |
| `topologysource.schema.json` | Standalone TopologySource field reference |
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

The LogSource and TopologySource schemas document the public authoring fields and intentionally omit local CoreTools metadata. Their portal payloads vary by account and product release, so keep the bundled field shape as the portable baseline and use a portal export only to confirm optional account-specific fields.
