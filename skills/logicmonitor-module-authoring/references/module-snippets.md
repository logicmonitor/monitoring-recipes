# Module Snippets

## What they are

Reusable platform code (functions, classes, data) used by newer LogicModules for common functionality: data formatting, topology generation, API interactions.

## Rules for agents

- Snippets are **platform-managed** — customers cannot create, modify, or deploy them
- Snippets cannot exist standalone — they support modules that reference them
- **Do not copy** snippet source into custom scripts or this repository (local `example-code/snippets-ref` exports are for maintainer distillation only)
- Check if a snippet already solves the problem before writing custom code
- Load via the collector bootstrap — see [snippet-loader.md](snippet-loader.md)
- See [snippets-catalog.md](snippets-catalog.md) for which snippet to load, version pins, and API entry points

## Collector requirements

1. Install **LogicMonitor_Collector_Snippets** module
2. Enable monitoring on the **Collector host resource**
3. Collectors check for updates daily, or when a required snippet is missing locally

## On-disk location

| Platform | Path |
|----------|------|
| Linux | `<collector-install>/lib/snippets` (often `/usr/local/logicmonitor/lib/snippets`) |
| Windows | `<collector-install>\\lib\\snippets` |

All snippet versions are cached in this directory.
