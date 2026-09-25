# Student lab

Work through the sessions in order. Detailed student instructions are split into individual checkpoints so each concept can be tested before the next one is introduced.

- [Session 00 — Workshop setup](student-sessions/00-setup.md)
- [Session 01 — PropertySource](student-sessions/01-propertysource.md)
- [Session 02 — Single-instance DataSource](student-sessions/02-controller-single.md)
- [Session 03A — Active Discovery](student-sessions/03a-active-discovery.md)
- [Session 03B — Node collection](student-sessions/03b-node-collection.md)
- [Session 04 — Refactor](student-sessions/04-refactor.md)
- [Session 05 — Import and validate](student-sessions/05-import-and-validate.md)

## Setup

### 1. Add a LogicMonitor resource

In the LogicMonitor portal, add a new resource (expert mode) using the supplied information. Set the resource hostname to `lm-elevate-api-indol.vercel.app`; the labs use `system.hostname` to build the HTTPS API URL.

- Add the resource under the workshop device group.
- Assign the resource to the Collector you will use for testing.
- Confirm the Collector host is monitored and has `LogicMonitor_Collector_Snippets` installed.

### 2. Add the workshop properties

Open the resource’s **Properties** tab and add these resource-level properties:

```text
fabric.api.user=YOUR_WORKSHOP_USER
fabric.api.pass=YOUR_WORKSHOP_PASSWORD
```

Note: Use the shared workshop values supplied by the instructor

Keep `fabric.api.pass` scoped to the workshop resource and store it as a protected/password property when the portal offers that option.

### 3. Verify the resource

Before creating modules, confirm the resource has `system.hostname`, the two `fabric.api.*` credential properties, a Collector assignment, and access to:

```text
https://lm-elevate-api-indol.vercel.app/api/v1/auth/token
```

## Exercise 1 — PropertySource

Create `addCategory_Training_Fabric`.

- AppliesTo: require the two `fabric.api.*` credential properties and exclude resources already categorized as `Training_Fabric`.
- Query `/auth/token`, then `/controller`.
- Emit `system.categories=Training_Fabric`.
- Emit `auto.fabric_site` and `auto.fabric_version` from the controller response.
- Return a nonzero status on authentication or API failure.

**Checkpoint:** the resource has the `Training_Fabric` category and both `auto.*` properties.

## Exercise 2 — Single-instance DataSource

Create `Training_Fabric_Controller`.

- AppliesTo: `hasCategory("Training_Fabric") && fabric.api.user && fabric.api.pass`.
- Use `/controller`.
- Emit `controller_health`, `node_count`, and `api_latency_ms`.
- Add descriptions, units, sensible ranges, and an overview graph.

**Checkpoint:** test collection returns numeric values matching the API response.

## Exercise 3 — Multi-instance DataSource

Extend the design into `Training_Fabric_Node`.

- Use `/nodes` for Active Discovery.
- Use the node ID as the wildvalue and the node name as the alias.
- Add `auto.role` and `auto.site` as ILPs.
- Collect `/nodes/{wildvalue}` for each discovered node.
- Emit `health`, `cpu_percent`, `memory_percent`, `interface_count`, and `error_rate_percent`.
- Add a graph for node resource/error health.

**Checkpoint:** three instances are discovered and each receives metrics.

## Exercise 4 — Refactor

Replace the direct HTTP implementation with:

- `proto.http` 1.0.0 for requests.
- `lm.emit` 1.3.0 for collector output.
- `lm.debug` 2.0.0 for controlled diagnostics.
- `lm.cache` 0.3.1 for the bearer token.

**Checkpoint:** enable debug, run twice, and show that the second run uses the cached token.

## Exercise 5 — Import and validate

Import the finished PropertySource and DataSource JSON in this order:

1. `addCategory_Training_Fabric`
2. `Training_Fabric_Controller`
3. `Training_Fabric_Node`

Confirm the PropertySource adds the category and metadata, the controller DataSource collects its three metrics, Active Discovery creates three nodes, and node collection populates metrics for every instance.
