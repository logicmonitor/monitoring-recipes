# Facilitator guide

## Learning outcomes

By the end of the hands-on portion, attendees can:

- Design a PropertySource that assigns metadata and a category.
- Use category/property targeting in AppliesTo logic.
- Build a scripted single-instance DataSource.
- Add Active Discovery and instance-level properties.
- Align emitted metrics with datapoints and graph lines.
- Refactor raw HTTP code to platform snippets.
- Cache a short-lived API token safely on the Collector.
- Validate, pack, and smoke-test a complete module bundle.

## Timing

The session has 75 minutes total: approximately 15 minutes of slides and 60 minutes of guided build time.

| Time | Activity | Checkpoint |
|---:|---|---|
| 0–8 | PropertySource targeting and metadata | Category appears on the resource |
| 8–20 | Controller DataSource, single instance | Three controller metrics collect |
| 20–35 | Active Discovery and node collection | Three node instances appear |
| 35–50 | Snippet, debug, and token-cache refactor | Second run uses cached token |
| 50–58 | Datapoints, graphs, alert, pack, validate | Import JSON is complete |
| 58–60 | Review and handoff to the skill | Attendees know the repeatable workflow |

## Teaching emphasis

- Start with the monitoring contract: target, output, datapoints, and failure behavior.
- Keep the first scripts intentionally direct. Explain that the goal is to make the data flow visible before introducing abstractions.
- Treat Active Discovery as a contract: stable wildvalue, readable alias, optional description, and useful ILPs.
- Make the single-to-multi transition explicit: the collection script changes from one response to a loop keyed by the discovered wildvalue.
- Refactor only after the class has a working baseline. Snippets reduce platform boilerplate; they do not replace module design.
- Explain caching as a lifecycle decision. Cache the token, not mutable monitoring data, and give the cache key a module-specific namespace.

## Recovery strategy

- If API access fails, switch to the matching completed checkpoint in `solutions/` and continue the design discussion.
- If snippets are unavailable, finish the raw-HTTP stage and demonstrate the refactored script from the answer bundle.
- If AD is slow, validate the emitted AD lines in the script test window and continue with the prebuilt node instances.
- Never spend the workshop debugging credentials in front of the class; reset the seeded account or use the instructor resource.
