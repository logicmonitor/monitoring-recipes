# Recipes

Script building blocks organized by **language**, then **pattern**. Each recipe is a single script you copy into the LM portal and adapt.

Groovy recipes use platform snippets (`proto.snmp`, `lm.remote`, etc.) and require the **LogicMonitor_Collector_Snippets** module.

## Index

| Recipe | Language | Pattern | Module types | Status |
|--------|----------|---------|--------------|--------|
| [snmp-walk](groovy/snmp-walk/) | Groovy | SNMP | DataSource, PropertySource, AD | Ready |
| [snmp-get](groovy/snmp-get/) | Groovy | SNMP | DataSource, PropertySource | Ready |
| [snmp-discovery](groovy/snmp-discovery/) | Groovy | SNMP | Active Discovery | Ready |
| [ssh-exec](groovy/ssh-exec/) | Groovy | SSH | DataSource, ConfigSource, DiagnosticSource | Ready |
| [ssh-interactive-config](groovy/ssh-interactive-config/) | Groovy | SSH | ConfigSource | Ready |
| [http-rest](groovy/http-rest/) | Groovy | HTTP | DataSource, PropertySource | Ready |
| [script-logs](groovy/script-logs/) | Groovy | HTTP | LogSource | Ready |
| [script-events](groovy/script-events/) | Groovy | HTTP | EventSource | Ready |
| [jdbc](groovy/jdbc/) | Groovy | JDBC | DataSource | Ready |
| [topology-edges](groovy/topology-edges/) | Groovy | Topology | TopologySource | Ready |
| [add-category](groovy/add-category/) | Groovy | Property | PropertySource | Ready |
| [add-eri](groovy/add-eri/) | Groovy | Topology | PropertySource (ERISource) | Ready |
| [diagnostic](groovy/diagnostic/) | Groovy | SSH | DiagnosticSource | Ready |
| [remediation](groovy/remediation/) | Groovy | SSH | RemediationSource | Ready |
| [winrm-exec](powershell/winrm-exec/) | PowerShell | WinRM | DataSource, DiagnosticSource, RemediationSource | Ready |
| [wmi-query](powershell/wmi-query/) | PowerShell | WMI | DataSource, PropertySource | Ready |
| [wmi-discovery](powershell/wmi-discovery/) | PowerShell | WMI | Active Discovery | Ready |

## Recipe structure

```
recipes/<language>/<pattern>/
├── README.md       # Use case, prerequisites, module-type adaptation table
├── recipe.yaml     # Machine-readable metadata
└── script.<ext>    # The snippet (script.groovy or script.ps1)
```

## How to use a recipe

1. Open the recipe README
2. Copy `script.groovy` or `script.ps1`
3. Replace placeholders (`INSERT_OID_HERE`, etc.)
4. Adapt output format for your module type (see the adaptation table in the README)
5. Paste into the appropriate script field in the LM portal

For code organization rules, see [script-structure.md](../skills/logicmonitor-authoring/references/script-structure.md).

## Adding a recipe

See [CONTRIBUTING.md](../CONTRIBUTING.md).
