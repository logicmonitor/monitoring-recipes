# Portable Module Guidance

These rules are product-facing guidance and do not require the internal `modules` repository or CoreTools.

## Scope and performance

- Make `appliesTo` as specific as possible; broad expressions cause unnecessary discovery and collection work.
- Prefer one bulk request and local parsing over repeated per-item requests when the response size is reasonable.
- Bound API calls with timeouts, pagination limits, and intentional retry behavior.
- Avoid complex datapoints when the calculation can be performed once in the collection script.
- Do not monitor unrelated devices through a resource that does not own the data.

## Data quality

- Give every datapoint a concise description that includes units and meaningful status mappings.
- Use canonical units, especially bytes and seconds, and avoid rounding values before collection.
- Ensure every datapoint is used by a graph, overview graph, alert, or another documented consumer.
- Preserve existing datapoint names when editing a deployed module; renaming them breaks historical continuity.
- Use stable categories and property names. `auto.*` properties are appropriate for ephemeral discovery metadata; credentials never belong in properties emitted by a module.

## Security and failure behavior

- Never hardcode, print, or log passwords, API keys, tokens, or sensitive device data.
- Request the least privilege needed and document any elevated permission requirement.
- Return `0` only after valid output is produced and return a nonzero code after a collection failure.
- Catch expected failures specifically and print an actionable, non-secret error message.
- In Groovy, send diagnostics through the `lm.debug` snippet so debug/log settings control where messages go; do not mix diagnostic text into collector data.
- Use `Write-Output` for PowerShell data; do not use `Write-Host` for collector output.

## Module-specific reminders

- LogSources must map emitted logs back to the monitored device; include the standard `system.deviceId` mapping unless the use case requires another documented key.
- ConfigSources should include at least a retrieval check and an ignore rule for unstable content such as timestamps when applicable.
- PropertySources that only set a category should exclude resources that already have that category. Do not use that exclusion when the source also maintains other properties.
- Active Discovery should emit useful, stable instance metadata when it helps filtering or grouping, without turning discovery into an unbounded inventory dump.
