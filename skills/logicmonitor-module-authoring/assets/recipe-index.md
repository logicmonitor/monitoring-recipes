# Bundled Patterns

Use one of these files as the starting point. Every target is included in this skill.

| Pattern | Starter | Typical module types |
|---------|---------|----------------------|
| DataSource Script | [datasource-script-single](module-templates/datasource-script-single/) | Single-instance DataSource |
| DataSource BatchScript + AD | [datasource-batchscript-ad](module-templates/datasource-batchscript-ad/) | Multi-instance DataSource |
| ConfigSource + AD | [configsource-script-ad](module-templates/configsource-script-ad/) | ConfigSource |
| PropertySource | [propertysource-groovy](module-templates/propertysource-groovy/) | PropertySource |
| EventSource | [eventsource-scriptevent](module-templates/eventsource-scriptevent/) | EventSource |
| DiagnosticSource | [diagnosticsource-groovy](module-templates/diagnosticsource-groovy/) | DiagnosticSource |
| RemediationSource | [remediationsource-groovy](module-templates/remediationsource-groovy/) | RemediationSource |
| HTTP REST | [http-rest-collect-snippet.groovy](examples/http-rest-collect-snippet.groovy) | DataSource, PropertySource |

Use [output formats](../references/output-formats.md) for the module-specific contract and [snippet loading](../references/snippets-catalog.md) for Groovy APIs.
