# Checkpoint map

| Checkpoint | Demonstrates | Expected artifact |
|---|---|---|
| `01-propertysource` | AppliesTo, authentication, metadata, category output | `addCategory_Training_Fabric` |
| `02-controller-single` | Single-instance collection and JSON alignment | `Training_Fabric_Controller` |
| `03a-active-discovery` | Active Discovery, stable wildvalues, instance properties | `Training_Fabric_Node` AD |
| `03b-node-collection` | Instance-scoped collection and datapoint alignment | `Training_Fabric_Node` collection |
| `04-refactored` | `proto.http`, `lm.emit`, `lm.debug`, `lm.cache` | Final controller and node bundles |

Every checkpoint has a working answer under `solutions/`. The answer bundles are deliberately small and use the same names and properties as the student lab.
