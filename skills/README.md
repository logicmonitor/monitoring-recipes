# Agent Skills

Installable [Agent Skills](https://agentskills.io/specification) for AI-assisted LogicModule authoring.

## Available skills

| Skill | Description |
|-------|-------------|
| [logicmonitor-module-authoring](logicmonitor-module-authoring/) | End-to-end LogicModule authoring workflow with script structure standards and snippet-first recipes (v0.3.0) |
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

## Dashboard schema

The [logicmonitor-dashboard-authoring](logicmonitor-dashboard-authoring/) skill ships a **portable** copy under `logicmonitor-dashboard-authoring/schema/` and `logicmonitor-dashboard-authoring/scripts/`.

Dashboard JSON Schema and `validate-dashboard.py` live inside [`logicmonitor-dashboard-authoring/schema/`](logicmonitor-dashboard-authoring/schema/) and [`logicmonitor-dashboard-authoring/scripts/`](logicmonitor-dashboard-authoring/scripts/). Maintainers with a local dashboard export corpus may run `scripts/dashboard-schema/extract-enums.py`; output under `schemas/` is gitignored.
