# Session 04 — Refactoring for Platform Practices

## Why this matters

Refactoring should improve consistency, diagnostics, and lifecycle behavior without changing the monitoring contract.

## Import this first

Import the applicable refactor scaffold for the module you are reviewing using the [shared import steps](../README.md#importing-a-module):

- [`propertysource-scaffold.json`](propertysource-scaffold.json)
- [`controller-scaffold.json`](controller-scaffold.json)
- [`node-scaffold.json`](node-scaffold.json)

Each scaffold preserves the established AppliesTo rule so the module remains applicable to the workshop resource; the node scaffold also preserves the online-instance filter. These scaffolds intentionally do not contain graph definitions. Use the baseline scripts from Sessions 01–03 as the “before” implementation. Review the guided scripts, then compare the complete versions:

This is the first session that uses the shared LogicMonitor helpers: `proto.http` for requests, `lm.emit` for output, `lm.debug` for diagnostics, and `lm.cache` for short-lived token caching.

- PropertySource: [`scripts/student-propertysource.groovy`](scripts/student-propertysource.groovy) → [`scripts/reference-propertysource.groovy`](scripts/reference-propertysource.groovy)
- Controller collection: [`scripts/student-controller.groovy`](scripts/student-controller.groovy) → [`scripts/reference-controller.groovy`](scripts/reference-controller.groovy)
- Node Active Discovery: [`scripts/student-node-ad.groovy`](scripts/student-node-ad.groovy) → [`scripts/reference-node-ad.groovy`](scripts/reference-node-ad.groovy)
- Node collection: [`scripts/student-node-collect.groovy`](scripts/student-node-collect.groovy) → [`scripts/reference-node-collect.groovy`](scripts/reference-node-collect.groovy)

## What to do

1. Start with the `REVIEW 1` comments and identify the versioned snippet helpers.
2. Follow `REVIEW 2` through the resource inputs and request construction.
3. Compare the token lifecycle and cache key/TTL decisions at `REVIEW 3` and `REVIEW 4`.
4. Review `REVIEW 5` in the node scripts and confirm property, datapoint, wildvalue, and instance-property names remain compatible.
5. Import the refactored JSON and run the modules twice with debug enabled.

## Check your result

- The intended snippet versions load.
- The second run reuses the bearer token.
- A rejected token is removed and refreshed once.
- PropertySource output remains unchanged.
- Active Discovery output remains unchanged.
- The online-instance filter continues to exclude the offline node.
- Controller and node responses remain fresh.
- Monitoring output is unchanged after the refactor.

## Discuss

- What is reusable LogicMonitor support code versus module-specific behavior?
- Why cache credentials/tokens but not mutable monitoring data?
- Why should a refactor preserve the existing reported values and names?

## Key idea

Use platform primitives to make scripts safer and easier to operate, while treating emitted monitoring data as a compatibility contract.

## If you get stuck

Compare the matching student and reference scripts, then use the completed JSON files in Session 05 to restore a working module before continuing.
