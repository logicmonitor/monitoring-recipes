# Session 00 — Workshop Setup

## Goal

Prepare one LogicMonitor resource and Collector for the workshop exercises.

## Steps

1. Create a resource group with the name `LMElevate`
2. Add a resource in expert mode with hostname `lm-elevate-api-indol.vercel.app`.
3. Assign it to the `LMElevate` resource group and a working Collector.
4. Add these resource properties:

   ```text
   fabric.api.user=INSTRUCTOR_PROVIDED_USER
   fabric.api.pass=INSTRUCTOR_PROVIDED_PASS
   ```

   Use the shared values supplied by the instructor
5. Confirm the collector can reach `https://lm-elevate-api-indol.vercel.app/` by performing a `Poll Now` collection of the `Ping-` datasource attached to the newly created resource.

## Validate

- `system.hostname` is the API hostname.
- Both `fabric.api.*` properties are present.
- The resource has a Collector assignment.
- The Collector is online and monitored.

## Checkpoint

Do not continue until the resource and credentials are ready. Later scripts build the API URL from `system.hostname`.
