# Session 03B — Instance Collection

## Why this matters

Collection must preserve the identity established by Active Discovery when it requests and emits instance metrics.

## Import this first

Import [`module-scaffold.json`](module-scaffold.json) using the [shared import steps](../README.md#importing-a-module). It carries forward the completed targeting and Active Discovery configuration from Session 03A and includes the working collection script. Review the collection [`scripts/student.groovy`](scripts/student.groovy) alongside it. The complete implementation is [`scripts/reference.groovy`](scripts/reference.groovy).

This session prints each instance datapoint directly as `wildvalue.field=value`. The reusable LogicMonitor output helper is intentionally saved for Session 04.

## What to do

1. Review the supplied instance loop and the wildvalue-qualified requests. The loop visits each discovered instance, and the wildvalue tells the API which node to request.
2. Trace each emitted metric back to its matching datapoint.
3. Define matching datapoints and a node health graph.

## Check your result

- Every discovered node receives metrics.
- Metrics remain associated with the correct instance ID, called the wildvalue.
- No values are emitted under an empty or incorrect instance key.
- The graph contains CPU, memory, and error-rate data.

## Discuss

- How does collection know which node to request?
- What breaks when the wildvalue is omitted from the output key?
- Why are discovery and collection separate contracts?

## Key idea

Active Discovery creates the instances; collection must consistently use those instance identities.

## If you get stuck

Open [`scripts/reference.groovy`](scripts/reference.groovy) and trace the same wildvalue through the request URL and every datapoint output.
