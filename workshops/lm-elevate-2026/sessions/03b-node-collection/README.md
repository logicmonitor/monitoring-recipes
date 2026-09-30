# Session 03B — Instance Collection

## Why this matters

Collection must preserve the identity established by Active Discovery when it requests and emits instance metrics.

## Import this first

Import [`module-scaffold.json`](module-scaffold.json) using the [shared import steps](../README.md#importing-a-module). It carries forward the completed targeting and Active Discovery filter from Session 03A. Review the collection [`scripts/student.groovy`](scripts/student.groovy) alongside it. The complete implementation is [`scripts/reference.groovy`](scripts/reference.groovy).

## What to do

1. Review the supplied instance loop and the wildvalue-qualified requests. The loop visits each discovered instance, and the wildvalue tells the API which node to request.
2. Uncomment the five marked output lines.
3. Trace each emitted metric back to its matching datapoint.
4. Confirm that the offline node is not present because the Active Discovery filter keeps only `auto.status=online`.

## Check your result

- Every discovered online node receives metrics.
- Metrics remain associated with the correct instance ID, called the wildvalue.
- No values are emitted under an empty or incorrect instance key.
- The module contains matching datapoints for each reported metric.

## Discuss

- How does collection know which node to request?
- What breaks when the wildvalue is omitted from the output key?
- Why are discovery and collection separate contracts?

## Key idea

Active Discovery creates the instances; collection must consistently use those instance identities.

## If you get stuck

Open [`scripts/reference.groovy`](scripts/reference.groovy) and trace the same wildvalue through the request URL and every datapoint output.
