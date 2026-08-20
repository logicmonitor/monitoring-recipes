# Monitoring Recipes

Best-practice script snippets, documentation, and Agent Skills for building [LogicMonitor LogicModules](https://www.logicmonitor.com/support/).

## What's in this repo

| Directory | What it is | Who it's for |
|-----------|------------|----------------|
| [`recipes/`](recipes/) | Copy-paste Groovy and PowerShell script building blocks | Engineers writing module scripts |
| [`docs/`](docs/) | Module-type guides, concepts, and decision trees | Engineers learning LM module authoring |
| [`skills/`](skills/) | Installable [Agent Skill](https://agentskills.io/specification) for AI-assisted authoring | Engineers using Cursor, Claude Code, or other Agent Skills-compatible tools |
| [`LogicMonitor_LogicModule_Repo_Download/`](LogicMonitor_LogicModule_Repo_Download/) | Portal export/search scripts for mining Exchange modules | Engineers comparing recipes to live LogicModules |

## Quick start

1. **Pick a pattern** — browse [`recipes/`](recipes/) by language (Groovy or PowerShell), then by protocol (SNMP, SSH, WinRM, etc.)
2. **Copy the script** — adapt placeholders and output format for your module type (see the recipe README)
3. **Read the docs** — not sure which module type to use? Start with [`docs/choosing-a-module-type.md`](docs/choosing-a-module-type.md)

### Using the Agent Skill

Install the `logicmonitor-authoring` skill for AI-assisted module development. See [`skills/README.md`](skills/README.md).

## Project status

**Last updated:** 2026-08-20  
**Branch:** `doc-overhaul`  
**Status:** Phase 2 recipes added — LogSource, EventSource, TopologySource, ConfigSource session, ERISource, DiagnosticSource, RemediationSource

## Help & documentation

- [LogicMonitor Support](https://www.logicmonitor.com/support/)
- [LogicMonitor Communities](https://communities.logicmonitor.com/)

## Contributing

Pull requests welcome. See [CONTRIBUTING.md](CONTRIBUTING.md).
