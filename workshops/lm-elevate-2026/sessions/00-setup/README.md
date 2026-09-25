# Session 00 — Workshop Setup

## Goal

Prepare one LogicMonitor resource and Collector for the workshop exercises.

## Steps

1. Add a resource in expert mode with hostname `lm-elevate-api-indol.vercel.app`.
2. Assign it to the workshop device group and a working Collector.
3. Add these resource properties:

   ```text
   fabric.api.user=YOUR_WORKSHOP_USER
   fabric.api.pass=YOUR_WORKSHOP_PASSWORD
   ```

   Use the shared values supplied by the instructor and protect the password property when available.
4. Confirm the Collector has `LogicMonitor_Collector_Snippets` installed.
5. Confirm the resource can reach `https://lm-elevate-api-indol.vercel.app/api/v1/auth/token`.

## Validate

- `system.hostname` is the API hostname.
- Both `fabric.api.*` properties are present.
- The resource has a Collector assignment.
- The Collector is online and monitored.

## Checkpoint

Do not continue until the resource and credentials are ready. Later scripts build the API URL from `system.hostname`.
