# Session 03A — Active Discovery

## Teaching point

Active Discovery defines identity and topology; it does not collect performance metrics.

## Starting artifact

Import [`module-scaffold.json`](module-scaffold.json); it already contains the guided Active Discovery script and a no-op collection entry. Review the Active Discovery [`scripts/student.groovy`](scripts/student.groovy) alongside it. The complete implementation is [`scripts/reference.groovy`](scripts/reference.groovy).

## Complete

1. Review the `/nodes` response and identify the stable ID, display name, and useful instance properties.
2. Complete the marked `emit.instance` call.
3. Use the node ID as the wildvalue, the node name as the alias, and `auto.role` plus `auto.site` as ILPs.
4. Run Active Discovery.

The collection script remains a basic `return 0` placeholder. Collection is completed in Session 03B after the instances exist.

## Validate

- Three instances are created.
- Wildvalues remain stable across repeated discovery runs.
- Aliases are readable.
- `auto.role` and `auto.site` appear on each instance.

## Talk through

- Why is a stable wildvalue more important than a pretty display name?
- What belongs on an instance as an ILP?
- Why should discovery and collection be tested separately?

## Takeaway

Discovery answers “what exists?” and establishes the identity used by collection.
