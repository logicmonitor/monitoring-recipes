# Recipe Index

Pointers to script building blocks in the monitoring-recipes repo. Adapt **output format** for your target module type.

All Groovy recipes require **LogicMonitor_Collector_Snippets** and follow [script-structure.md](../references/script-structure.md).

## Groovy

| Pattern | Path | Snippets | Typical module types | Output adaptation | Status |
|---------|------|----------|---------------------|-------------------|--------|
| SNMP walk | `recipes/groovy/snmp-walk/` | proto.snmp, lm.emit | DataSource, PropertySource, AD | DS: `key=value` · PS: `auto.prop=value` · AD: `wild##alias` | Ready |
| SNMP get | `recipes/groovy/snmp-get/` | proto.snmp, lm.emit | DataSource, PropertySource | DS: `key=value` · PS: `auto.prop=value` | Ready |
| SNMP discovery | `recipes/groovy/snmp-discovery/` | proto.snmp, lm.emit | Active Discovery | `wildvalue##wildalias` via `emit.instance` | Ready |
| SSH exec | `recipes/groovy/ssh-exec/` | lm.remote, lm.emit | DataSource, ConfigSource, DiagnosticSource | DS: `key=value` · Config: raw text · Diag: JSON `{data,format}` | Ready |
| SSH interactive config | `recipes/groovy/ssh-interactive-config/` | lm.remote | ConfigSource | Raw config text · BatchScript JSON `data.<wildvalue>.configuration` | Ready |
| HTTP REST | `recipes/groovy/http-rest/` | proto.http, lm.cache, lm.emit | DataSource, PropertySource | DS: `key=value` · PS: `auto.prop=value` | Ready |
| Script Logs | `recipes/groovy/script-logs/` | proto.http, lm.cache | LogSource | JSON `{events:[{message}]}` | Ready |
| Script Events | `recipes/groovy/script-events/` | proto.http | EventSource | JSON `{events:[{happenedOn,severity,message}]}` | Ready |
| JDBC | `recipes/groovy/jdbc/` | lm.sql, lm.emit | DataSource | DS Script: `key=value` · BatchScript: `instance.key=value` | Ready |
| Topology edges | `recipes/groovy/topology-edges/` | lm.topo | TopologySource | `registerEdge` + `generateTopology` | Ready |
| addCategory | `recipes/groovy/add-category/` | lm.emit | PropertySource | `system.categories=...` | Ready |
| addERI | `recipes/groovy/add-eri/` | lm.topo | PropertySource (ERISource) | `emitEri` + `printEriArray` | Ready |
| Diagnostic | `recipes/groovy/diagnostic/` | lm.remote | DiagnosticSource | JSON `{data,format}` + alertProps fallback | Ready |
| Remediation | `recipes/groovy/remediation/` | lm.remote | RemediationSource | JSON `{data,format,remediationStatus}` | Ready |

## PowerShell

| Pattern | Path | Snippets | Typical module types | Output adaptation | Status |
|---------|------|----------|---------------------|-------------------|--------|
| WinRM exec | `recipes/powershell/winrm-exec/` | — | DataSource, DiagnosticSource, RemediationSource | DS: `key=value` · Diag/Rem: JSON via `ConvertTo-Json` | Ready |
| WMI query | `recipes/powershell/wmi-query/` | — | DataSource, PropertySource | DS: `key=value` · PS: `auto.prop=value` | Ready |
| WMI discovery | `recipes/powershell/wmi-discovery/` | — | Active Discovery | `wildvalue##wildalias` per line | Ready |

See [references/output-formats.md](../references/output-formats.md) for full format specs.
See [references/snippets-catalog.md](../references/snippets-catalog.md) for snippet loading details.
