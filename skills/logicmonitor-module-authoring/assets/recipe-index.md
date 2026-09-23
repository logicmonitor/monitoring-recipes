# Recipe Index

Pointers to script building blocks in the monitoring-recipes repo. Adapt **output format** for your target module type.

All Groovy recipes require **LogicMonitor_Collector_Snippets** and follow [script-structure.md](../references/script-structure.md).

## Groovy

| Pattern | Path | Snippets | Typical module types | Output adaptation | Status |
|---------|------|----------|---------------------|-------------------|--------|
| SNMP walk | `recipes/groovy/snmp-walk/` | proto.snmp, lm.emit | DataSource, PropertySource, AD | DS: `key=value` · PS: `auto.prop=value` · AD: `wild##alias` | Ready |
| SNMP get | `recipes/groovy/snmp-get/` | proto.snmp, lm.emit | DataSource, PropertySource | DS: `key=value` · PS: `auto.prop=value` | Ready |
| SSH exec | `recipes/groovy/ssh-exec/` | lm.remote, lm.emit | DataSource, ConfigSource, DiagnosticSource | DS: `key=value` · Config: raw text · Diag: JSON `{data,format}` | Ready |
| HTTP REST | `recipes/groovy/http-rest/` | proto.http, lm.cache, lm.emit | DataSource, PropertySource | DS: `key=value` · PS: `auto.prop=value` | Ready |
| JDBC | `recipes/groovy/jdbc/` | lm.sql, lm.emit | DataSource | DS Script: `key=value` · BatchScript: `instance.key=value` | Ready |

## PowerShell

| Pattern | Path | Snippets | Typical module types | Output adaptation | Status |
|---------|------|----------|---------------------|-------------------|--------|
| WinRM exec | `recipes/powershell/winrm-exec/` | — | DataSource, DiagnosticSource, RemediationSource | DS: `key=value` · Diag/Rem: JSON via `ConvertTo-Json` | Ready |
| WMI query | `recipes/powershell/wmi-query/` | — | DataSource, PropertySource | DS: `key=value` · PS: `auto.prop=value` | Ready |
| WMI discovery | `recipes/powershell/wmi-discovery/` | — | Active Discovery | `wildvalue##wildalias` per line | Ready |

See [references/output-formats.md](../references/output-formats.md) for full format specs.
See [references/snippets-catalog.md](../references/snippets-catalog.md) for snippet loading details.
