# Agent Skills

Installable [Agent Skills](https://agentskills.io/specification) for AI-assisted LogicModule authoring.

## Available skills

| Skill | Description |
|-------|-------------|
| [logicmonitor-authoring](logicmonitor-authoring/) | End-to-end LogicModule authoring workflow with script structure standards and snippet-first recipes (v0.5.0) |

## Installation

### Cursor

Copy or symlink the skill directory:

```bash
# Project-scoped (recommended when working in a cloned repo)
mkdir -p .cursor/skills
cp -r skills/logicmonitor-authoring .cursor/skills/

# Or global
cp -r skills/logicmonitor-authoring ~/.cursor/skills/
```

### Other Agent Skills-compatible tools

Follow your tool's documentation for skill directory location. The skill follows the open standard — a directory containing `SKILL.md` with YAML frontmatter.

### Clone and symlink

```bash
git clone https://github.com/logicmonitor/monitoring-recipes.git
ln -s "$(pwd)/monitoring-recipes/skills/logicmonitor-authoring" ~/.cursor/skills/logicmonitor-authoring
```

## Validation

If you have the [skills-ref](https://agentskills.io/specification) validator installed:

```bash
skills-ref validate ./skills/logicmonitor-authoring
```

## Usage

Invoke the skill when asking your AI agent to help author or edit LogicModule scripts. The skill guides the agent through module type selection, language choice, recipe selection, and output formatting.

Example prompts:

- "Help me create a DataSource that SNMP walks interface OIDs"
- "Write a PropertySource script to collect serial number via SNMP"
- "Should I use EventSource or LogSource for Windows event logs?"
