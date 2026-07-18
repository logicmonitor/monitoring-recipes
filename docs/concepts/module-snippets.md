# Module Snippets

Module snippets are pieces of reusable code that can contain functions, classes, and data. Snippets are used by newer LogicModules to implement common functionality — such as formatting data for the Collector and generating topologies — as well as device-specific code like interacting with APIs.

## Key facts

- Snippets are **platform-managed** — customers cannot create, modify, or deploy snippets on their own
- Snippets **cannot be created or deployed on their own** — they exist to support LogicModules that reference them
- Reference snippets in your scripts to reduce boilerplate

## What snippets are used for

| Use case | Examples |
|----------|----------|
| Collector data formatting | Standardizing script output for datapoint post-processing |
| Topology generation | Building topology maps from API or SNMP data |
| Device-specific integrations | Shared API client logic across multiple modules |

## Collector requirements

To use modules that depend on snippets:

1. Install the **LogicMonitor_Collector_Snippets** module
2. Enable monitoring on the **Collector host resource**

Collectors automatically check for snippet updates **at least once per day**, or whenever a required snippet is not available locally.

## Where snippets live on the Collector

Snippets are cached locally in the LogicMonitor installation directory:

| Platform | Path |
|----------|------|
| Linux | `/lib/snippets` |
| Windows | `\lib\snippets` |

All snippet versions are maintained within this directory.

## Authoring guidance

When building custom LogicModules:

- Check whether an existing snippet already implements the capability before writing custom code
- If a LogicMonitor-provided module uses snippets, ensure `LogicMonitor_Collector_Snippets` is installed on Collectors that will run it
- Do not replicate snippet logic in recipe scripts — link to official module documentation instead
- For agent authoring, see the skill [snippets catalog](../../skills/logicmonitor-authoring/references/snippets-catalog.md) for which snippets to load and how

## Related concepts

- [Collection Modes](collection-modes.md) — snippets may help format collection output
- [Output Formats](output-formats.md) — script output rules snippets may assist with

## Official documentation

- [LogicMonitor Support](https://www.logicmonitor.com/support/)
