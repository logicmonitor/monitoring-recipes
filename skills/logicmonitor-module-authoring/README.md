# LogicMonitor Module Authoring Skill

This directory is a standalone skill bundle for creating LogicMonitor LogicModule import bundles.

## Use

Copy this directory into the skill location supported by your agent. Run commands from this directory:

```bash
python3 scripts/pack-module.py path/to/ModuleName/
python3 scripts/validate-module.py path/to/ModuleName/
```

The default validator uses only the Python standard library. Install `jsonschema` and add `--with-schema` for bundled JSON Schema validation.

## Inputs and outputs

Author one directory per module containing JSON plus source scripts. `pack-module.py` embeds source scripts into the import JSON; the source files remain the editable source of truth.

Templates and references cover DataSource, ConfigSource, EventSource, PropertySource, DiagnosticSource, RemediationSource, LogSource, and TopologySource authoring. The packer and semantic validator cover the import-bundle types listed in `schema/README.md`; LogSource and TopologySource have standalone field references but still require platform-specific payload handling. Portal exports are optional for ordinary modules and are only needed to confirm account-specific or opaque platform fields.

See `SKILL.md` for the workflow and `references/portable-guidance.md` for security, performance, and quality requirements.
