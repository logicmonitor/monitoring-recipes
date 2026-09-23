# Agent Skills

Installable [Agent Skills](https://agentskills.io/specification) for AI-assisted LogicModule authoring.

## Available skills

| Skill | Description |
|-------|-------------|
| [logicmonitor-module-authoring](logicmonitor-module-authoring/) | LogicModule authoring: scripts, import JSON bundles, schema validation (v0.4.0) |
| [logicmonitor-dashboard-authoring](logicmonitor-dashboard-authoring/) | Self-contained dashboard JSON authoring (schema, scripts, widget templates; v0.2.2) |

## Installation

### Cursor

Copy or symlink the skill directory:

```bash
# Project-scoped
mkdir -p .cursor/skills
cp -r skills/logicmonitor-module-authoring .cursor/skills/
cp -r skills/logicmonitor-dashboard-authoring .cursor/skills/

# Or global (portable — no need to clone monitoring-recipes for dashboard authoring)
cp -r skills/logicmonitor-dashboard-authoring ~/.cursor/skills/
```

### Other Agent Skills-compatible tools

Follow your tool's documentation for skill directory location. The skill follows the open standard — a directory containing `SKILL.md` with YAML frontmatter.

### Clone and symlink

```bash
git clone https://github.com/logicmonitor/monitoring-recipes.git
ln -s "$(pwd)/monitoring-recipes/skills/logicmonitor-module-authoring" ~/.cursor/skills/logicmonitor-module-authoring
```

## Validation

LogicModule bundles (from the skill root):

```bash
python skills/logicmonitor-module-authoring/scripts/pack-module.py path/to/ModuleBundle/
python skills/logicmonitor-module-authoring/scripts/validate-module.py path/to/ModuleBundle/
```

Optional: add `--with-schema` after `pip install jsonschema` for JSON Schema validation.

A local [`LogicModules/`](../LogicModules/) export tree (gitignored) can refresh bundled schema:

```bash
python scripts/logicmodule-schema/extract-schema.py
```

If you have the [skills-ref](https://agentskills.io/specification) validator installed:

```bash
skills-ref validate ./skills/logicmonitor-module-authoring
```

## Usage

Invoke the skill when asking your AI agent to help author or edit LogicModule scripts. The skill guides the agent through module type selection, language choice, recipe selection, and output formatting.

Example prompts:

- "Help me create a DataSource that SNMP walks interface OIDs"
- "Write a PropertySource script to collect serial number via SNMP"
- "Should I use EventSource or LogSource for Windows event logs?"
- "Build a dashboard JSON for these exported DataSources"
- "Create a cgraph and dynamicTable for Lambda invocation metrics"

## Bundled schema and scripts

| Skill | Schema | Tools |
|-------|--------|--------|
| [logicmonitor-module-authoring](logicmonitor-module-authoring/) | `logicmonitor-module-authoring/schema/` | `pack-module.py`, `validate-module.py` |
| [logicmonitor-dashboard-authoring](logicmonitor-dashboard-authoring/) | `logicmonitor-dashboard-authoring/schema/` | `validate-dashboard.py` |

Maintainers with local export corpora (`LogicModules/`, `Dashboards/`, gitignored) can regenerate module schema via `scripts/logicmodule-schema/extract-schema.py`. Dashboard enum extraction may use `scripts/dashboard-schema/`; output under `schemas/` is gitignored.
