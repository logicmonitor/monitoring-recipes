# Session 04 — Refactoring for Platform Practices

## Teaching point

Refactoring should improve consistency, diagnostics, and lifecycle behavior without changing the monitoring contract.

## Starting artifact

Import the applicable refactor scaffold for the module you are reviewing:

- [`propertysource-scaffold.json`](propertysource-scaffold.json)
- [`controller-scaffold.json`](controller-scaffold.json)
- [`node-scaffold.json`](node-scaffold.json)

Each scaffold preserves the established AppliesTo rule so the module remains applicable to the workshop resource. Use the baseline scripts from Sessions 01–03 as the “before” implementation. Review the guided scripts, then compare the complete versions:

- PropertySource: [`scripts/student-propertysource.groovy`](scripts/student-propertysource.groovy) → [`scripts/reference-propertysource.groovy`](scripts/reference-propertysource.groovy)
- Controller collection: [`scripts/student-controller.groovy`](scripts/student-controller.groovy) → [`scripts/reference-controller.groovy`](scripts/reference-controller.groovy)
- Node Active Discovery: [`scripts/student-node-ad.groovy`](scripts/student-node-ad.groovy) → [`scripts/reference-node-ad.groovy`](scripts/reference-node-ad.groovy)
- Node collection: [`scripts/student-node-collect.groovy`](scripts/student-node-collect.groovy) → [`scripts/reference-node-collect.groovy`](scripts/reference-node-collect.groovy)

## Complete

1. Identify the shared platform plumbing in all four scripts: HTTP, output, debug, and cache.
2. Trace how the PropertySource, controller collection, Active Discovery, and node collection each use that plumbing.
3. Review the cache key and TTL decisions in the guided scripts.
4. Compare the before/after output contract; confirm property, datapoint, and instance keys did not change.
5. Import the completed refactored JSON and run the modules twice with debug enabled.

## Validate

- The intended snippet versions load.
- The second run reuses the bearer token.
- A rejected token is removed and refreshed once.
- PropertySource output remains unchanged.
- Active Discovery output remains unchanged.
- Controller and node responses remain fresh.
- Monitoring output is unchanged after the refactor.

## Talk through

- What is reusable platform plumbing versus module-specific behavior?
- Why cache credentials/tokens but not mutable monitoring data?
- Why should a refactor preserve the existing output contract?

## Takeaway

Use platform primitives to make scripts safer and easier to operate, while treating emitted monitoring data as a compatibility contract.
