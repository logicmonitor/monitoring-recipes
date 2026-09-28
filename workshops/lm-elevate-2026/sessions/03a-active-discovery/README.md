# Session 03A — Active Discovery

## Why this matters

Active Discovery defines identity and topology; it does not collect performance metrics.

## Import this first

Import [`module-scaffold.json`](module-scaffold.json) using the [shared import steps](../README.md#importing-a-module). It already contains the guided Active Discovery script and a no-op collection entry. Review the Active Discovery [`scripts/student.groovy`](scripts/student.groovy) alongside it. The complete implementation is [`scripts/reference.groovy`](scripts/reference.groovy).

## What to do

1. Review the `/nodes` response and identify the stable ID, display name, and useful instance properties.
2. Complete the marked `emit.instance` call.
3. Use the node ID as the wildvalue, the node name as the alias, and `auto.role` plus `auto.site` as instance properties (ILPs).
4. Run Active Discovery.

The collection script remains a basic `return 0` placeholder. Collection is completed in Session 03B after the instances exist.

## Check your result

- Three instances are created.
- Wildvalues—the stable IDs for the instances—remain unchanged across repeated discovery runs.
- Aliases are readable.
- `auto.role` and `auto.site` appear on each instance.

## Discuss

- Why is a stable wildvalue more important than a pretty display name?
- What information is useful to store on each instance as an instance property?
- Why should discovery and collection be tested separately?

## Key idea

Discovery answers “what exists?” and establishes the identity used by collection.

## If you get stuck

Open [`scripts/reference.groovy`](scripts/reference.groovy) and compare the `emit.instance` arguments with the node fields returned by `/nodes`.
