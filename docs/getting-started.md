# Getting Started

This guide helps you find the right starting point in the Monitoring Recipes repository.

## I want to...

| Goal | Start here |
|------|------------|
| Choose which LogicModule type to build | [Choosing a Module Type](choosing-a-module-type.md) |
| Copy a script for SNMP, SSH, WinRM, etc. | [Recipes index](../recipes/README.md) |
| Understand Active Discovery | [Active Discovery](concepts/active-discovery.md) |
| Understand Script vs BatchScript collection | [Collection Modes](concepts/collection-modes.md) |
| Understand script output formats | [Output Formats](concepts/output-formats.md) |
| Understand module snippets | [Module Snippets](concepts/module-snippets.md) |
| Cache API auth tokens between polls | [Script Cache](concepts/script-cache.md) |
| Use AI to help author a module | [Agent Skill install](../skills/README.md) |
| Learn about a specific module type | [Module Types](module-types/) |

## How recipes work

Each recipe is a **single script building block** — not a complete LogicModule. You copy the script into the LM portal and adapt:

1. **Placeholders** — OIDs, commands, property names marked in the script
2. **Output format** — depends on your module type (DataSource, PropertySource, Active Discovery, etc.)
3. **Portal configuration** — collection method, credentials via device properties

See the recipe README for a module-type adaptation table.

## Official LogicMonitor documentation

This repo adds patterns and pitfalls. For product documentation:

- [LogicMonitor Support](https://www.logicmonitor.com/support/)
- [Creating DataSources](https://www.logicmonitor.com/support/datasources/creating-datasources/)
- [Creating PropertySources](https://www.logicmonitor.com/support/other-logicmodules/propertysources/creating-propertysources/)
