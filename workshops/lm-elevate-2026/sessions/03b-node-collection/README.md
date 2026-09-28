# Session 03B — Instance Collection

## Teaching point

Collection must preserve the identity established by Active Discovery when it requests and emits instance metrics.

## Starting artifact

Import [`module-scaffold.json`](module-scaffold.json); it carries forward the completed targeting and Active Discovery configuration from Session 03A and includes the working collection script. Review the collection [`scripts/student.groovy`](scripts/student.groovy) alongside it. The complete implementation is [`scripts/reference.groovy`](scripts/reference.groovy).

## Complete

1. Review the supplied `datasourceinstanceProps` loop and the wildvalue-qualified requests.
2. Trace each emitted metric back to its matching datapoint.
3. Define matching datapoints and a node health graph.

## Validate

- Every discovered node receives metrics.
- Metrics remain associated with the correct wildvalue.
- No values are emitted under an empty or incorrect instance key.
- The graph contains CPU, memory, and error-rate data.

## Talk through

- How does collection know which node to request?
- What breaks when the wildvalue is omitted from the output key?
- Why are discovery and collection separate contracts?

## Takeaway

Active Discovery creates the instances; collection must consistently use those instance identities.
