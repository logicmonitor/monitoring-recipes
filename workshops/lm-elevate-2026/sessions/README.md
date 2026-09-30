# Student sessions

Complete the sessions in order. Each session provides a starting JSON file, a small guided script exercise, and a reference implementation for discussion.

1. [Workshop setup](00-setup/README.md)
2. [Resource enrichment](01-propertysource/README.md)
3. [Single-instance DataSource](02-controller-single/README.md)
4. [Active Discovery](03a-active-discovery/README.md)
5. [Instance collection](03b-node-collection/README.md)
6. [Refactoring for platform practices](04-refactor/README.md)
7. [Import and validate](05-import-and-validate/README.md)

The goal is to practice module design and validation. The supplied scripts provide the implementation details; students complete only the marked concepts.

## Importing a module

Use these steps whenever a session asks you to import a JSON file:

1. Open the LogicMonitor `Modules` page.
2. Select `Import from file`.
3. Choose the JSON file named in the session guide.
4. If LogicMonitor reports:

   > This module has a conflict with a module that is currently installed.

   select `View data comparison`.
5. Select `Overwrite installed module`.
6. Continue with the session’s validation steps.

The workshop intentionally reuses module names as the design develops. Re-importing a module replaces the earlier workshop version with the next checkpoint version.

## Words used in the labs

- **Targeting rule (`AppliesTo`):** decides which resources receive a module.
- **Wildvalue:** the stable ID LogicMonitor uses to identify one instance.
- **Instance property:** extra information stored with one discovered instance, such as its role or site.
- **Datapoint:** one measurement reported by a module.
- **Instance-level property:** metadata attached to one discovered instance, such as its role, site, or online/offline status.
- **Active Discovery filter:** a rule that decides which discovered records become monitored instances.
- **Overview graph:** a multi-instance graph that compares data across the highest-value instances.
