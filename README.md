# Monitoring Recipes

Best-practice script snippets, documentation, and Agent Skills for building [LogicMonitor LogicModules](https://www.logicmonitor.com/support/).

## What's in this repo

| Directory | What it is | Who it's for |
|-----------|------------|----------------|
| [`recipes/`](recipes/) | Copy-paste Groovy and PowerShell script building blocks | Engineers writing module scripts |
| [`docs/`](docs/) | Module-type guides, concepts, and decision trees | Engineers learning LM module authoring |
| [`skills/`](skills/) | Installable [Agent Skills](https://agentskills.io/specification), including dashboard JSON schema and validation in [`logicmonitor-dashboard-authoring`](skills/logicmonitor-dashboard-authoring/) | Engineers using Cursor, Claude Code, or other Agent Skills-compatible tools |

## Quick start

1. **Pick a pattern** — browse [`recipes/`](recipes/) by language (Groovy or PowerShell), then by protocol (SNMP, SSH, WinRM, etc.)
2. **Copy the script** — adapt placeholders and output format for your module type (see the recipe README)
3. **Read the docs** — not sure which module type to use? Start with [`docs/choosing-a-module-type.md`](docs/choosing-a-module-type.md)

### Using the Agent Skill

Install the `logicmonitor-module-authoring` skill for AI-assisted module development, or `logicmonitor-dashboard-authoring` for dashboard JSON. See [`skills/README.md`](skills/README.md).

## Project status

**Last updated:** 2026-07-18  
**Branch:** `doc-overhaul` (uncommitted)  
**Status:** Phase 1 complete — recipes implemented; Phase 2 expansion next

## Help & documentation

- [LogicMonitor Support](https://www.logicmonitor.com/support/)
- [LogicMonitor Communities](https://communities.logicmonitor.com/)

## Contributing

Pull requests welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).
