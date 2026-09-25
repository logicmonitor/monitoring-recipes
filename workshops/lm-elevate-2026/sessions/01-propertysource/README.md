# Session 01 — Build the PropertySource

Starter script: [`scripts/collect.groovy`](scripts/collect.groovy)

## Goal

Build `addCategory_Training_Fabric` to identify the API resource and add metadata that later DataSources can use for targeting.

## Build

1. Create a PropertySource named `addCategory_Training_Fabric`.
2. Use this AppliesTo:

   ```text
   fabric.api.user && fabric.api.pass && !hasCategory("Training_Fabric")
   ```

3. In the script:
   - Build the API URL from `system.hostname` using HTTPS.
   - Request a bearer token from `/auth/token`.
   - Request `/controller`.
   - Emit `system.categories=Training_Fabric`.
   - Emit `auto.fabric_site` and `auto.fabric_version`.
   - Return a nonzero status for authentication or API errors.

## Validate

Run the script against the workshop resource and confirm:

- The script exits successfully.
- `system.categories` contains `Training_Fabric`.
- `auto.fabric_site` is `training-east`.
- `auto.fabric_version` is `7.4.2`.

## Discuss

- Why should the PropertySource add metadata instead of hard-coding it into every DataSource?
- Why should it stop targeting a resource after the category is present?
- Which properties are used for targeting, and which are informational?

## Checkpoint

The resource is enriched and ready to become the target of the workshop DataSources.
