# Workshop solutions

These are known-good answer bundles for facilitator recovery and post-workshop reference.

| Bundle | Purpose |
|---|---|
| `01-propertysource/` | Authenticated `addCategory_Training_Fabric` PropertySource |
| `02-controller-single/` | Single-instance controller DataSource |
| `03-node-multi/` | Multi-instance node DataSource with AD |
| `04-refactored/` | Snippet and token-cache implementation for controller and node modules |

The refactored PropertySource is under `04-refactored/propertysource/`.

The solution bundles contain external source scripts plus packed import JSON. Run `pack-module.py --check` before distributing a modified bundle.
