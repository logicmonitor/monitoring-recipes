# TopologySource starter

TopologySource exports use `type: 9`, `collectionIntervalSec`, and a serialized `collectionAttrs` object. Scripted modules store the script reference or embedded script in that serialized object. The script emits a JSON object with an `edges` array; each edge must identify the relationship type and source/target ERIs. The `lm.topo` snippet may produce a richer platform structure, so compare its output to the target portal behavior.
