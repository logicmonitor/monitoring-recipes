# Scripted Module Authoring Workshop

This workshop builds a small set of LogicMonitor modules for a hosted, vendor-neutral **Enterprise Fabric Controller** API.

The exercise is intentionally staged:

1. A PropertySource authenticates to the controller and assigns useful metadata.
2. A single-instance DataSource collects controller health.
3. The DataSource becomes multi-instance with Active Discovery for fabric nodes.
4. The scripts are refactored to use LogicMonitor snippets, structured debug logging, and Collector-side token caching.
5. The final JSON is assembled, graphed, validated, and tested on a Collector.

The API is not intended to model a particular vendor. Its controller/node shape mirrors common enterprise modules such as APIC, VMware, F5, and Kubernetes while keeping the code small enough for a live class.

## Prerequisites

- A LogicMonitor resource with hostname `lm-elevate-api-indol.vercel.app`, a Collector assigned, and the workshop credential properties configured; see the [student setup session](sessions/00-setup/README.md).
- The `LogicMonitor_Collector_Snippets` module installed on the Collector host.
- Collector 29.100 or newer for the cache exercise.
- A workshop API URL and seeded username/password from the instructor.
- Permission to create or edit LogicModules and run test collection/Active Discovery.

## Materials

- [Student sessions](sessions/)
- [Solutions](solutions/README.md)
- [Script maintenance](scripts/README.md)

The module-authoring skill remains the reusable follow-up tool. These workshop files provide the deliberate teaching sequence and the known-good answer bundles.
