# Module Types

## DataSource

- **Purpose:** Time-series numeric metrics
- **Output:** `key=value` (Script) or `instance.key=value` (BatchScript)
- **Style:** `Vendor_Product_Monitor` naming; category-based appliesTo
- **External / SaaS APIs:** [external-api-datasource.md](external-api-datasource.md)
- **Docs:** https://www.logicmonitor.com/support/logicmodules/datasources/creating-managing-datasources/datasource-style-guidelines

## PropertySource

- **Purpose:** Set device properties and metadata
- **Output:** `auto.property=value` or `system.categories=value` only — **not** arbitrary `system.*`
- **Runs:** Daily, on update, manual run, or on AD execution
- **Docs:** https://www.logicmonitor.com/support/logicmodules/propertysources/creating-propertysources

## ConfigSource

- **Purpose:** Monitor configuration files for changes
- **Output:** Raw text (Script) or JSON `data.<wildvalue>.configuration` (BatchScript)
- **Limit:** 3 MiB per config file
- **Docs:** https://www.logicmonitor.com/support/configsource-configuration

## TopologySource

- **Purpose:** Define device relationships for topology maps
- **Output:** JSON `{"edges": [{"type":"...","from":"eri","to":"eri"}]}`; use a portal export as the JSON-shell reference
- **Docs:** https://www.logicmonitor.com/support/topologysources-scripts

## LogSource

- **Purpose:** Ingest logs into LM Logs (preferred over EventSource)
- **Output (Script Logs):** JSON `{"events": [{"message":"..."}]}` — message required, exit 0
- **Docs:** https://www.logicmonitor.com/support/script-logs-logsource-configuration

## EventSource

- **Purpose:** Non-searchable events that always alert
- **Output (Script):** JSON `{"events": [{"happenedOn":"...","severity":"warn|error|critical","message":"..."}]}`
- **Limits:** 50 events/exec, 100 events/collector/minute
- **Prefer LogSource** for new searchable log work
- **Docs:** https://www.logicmonitor.com/support/script-eventsource

## DiagnosticSource

- **Purpose:** Troubleshooting data on alert or manual trigger
- **Output:** JSON `{"data":"...","format":"markdown"}` (plain-text fallback ok)
- **Alert context:** `alertProps` / `##ALERT.*##` tokens when alert-triggered
- **Docs:** https://www.logicmonitor.com/support/alert-properties-for-diagnosticsource-and-remediationsource-scripts

## RemediationSource

- **Purpose:** Corrective actions on alert or manual trigger
- **Output:** JSON `{"data":"...","format":"markdown","remediationStatus":"true"}`
- **remediationStatus:** String (not boolean) — UI shows N/A if omitted
- **Alert context:** Same as DiagnosticSource
- **Docs:** https://www.logicmonitor.com/support/alert-properties-for-diagnosticsource-and-remediationsource-scripts

## JSON creation tip (Groovy)

```groovy
import groovy.json.JsonOutput
print JsonOutput.toJson([data: "output", format: "markdown"])
return 0
```
