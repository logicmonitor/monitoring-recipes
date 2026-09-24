# LogSource starter

LogSource import fields vary by collection method. This starter follows the exported `type: 10` shape used by the repository: `appliesTo`, integer `collectionInterval`, `collectionAttrs`, `logFields`, and `resourceMapping`. Keep `resourceMapping` so emitted events remain associated with the monitored device.

Use `Example_LogSource.json` with `collect.groovy`, then validate the JSON shape with `schema/logsource.schema.json` if `jsonschema` is installed.
