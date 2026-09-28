# Session 01 — Resource Enrichment

## Teaching point

A PropertySource should discover reusable resource metadata once so later modules can target and describe the resource consistently.

## Starting artifact

Import [`module-scaffold.json`](module-scaffold.json). It already contains the guided student script and is disabled with `appliesTo: false()` until the targeting rule is reviewed.

Use [`scripts/student.groovy`](scripts/student.groovy) for the guided exercise. The complete implementation is [`scripts/reference.groovy`](scripts/reference.groovy).

## Complete

1. Review the supplied request flow before changing code.
2. Fill the marked `system.hostname` lookup.
3. Uncomment the three output lines and identify which values are targeting metadata versus informational metadata.
4. Change the module’s AppliesTo from `false()` to:

   ```text
   fabric.api.user && fabric.api.pass && !hasCategory("Training_Fabric")
   ```

5. Run the PropertySource once against the workshop resource.

## Validate

- `Training_Fabric` is added as a category.
- `auto.fabric_site` and `auto.fabric_version` are present.
- A second run no longer targets the resource because the category is already present.

## Talk through

- Why is category assignment a better targeting mechanism than repeating API-specific logic in every DataSource?
- Why should a PropertySource be idempotent?
- What should happen when credentials or the API response are unavailable?

## Takeaway

The script performs enrichment; the module metadata controls where and when that enrichment runs.
