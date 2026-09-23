# Recipes

Script building blocks organized by **language**, then **pattern**. Each recipe is a single script you copy into the LM portal and adapt.

Groovy recipes use platform snippets (`proto.snmp`, `lm.remote`, etc.) and require the **LogicMonitor_Collector_Snippets** module.

## Index

| Recipe | Language | Pattern | Module types | Status |
|--------|----------|---------|--------------|--------|
| [snmp-walk](groovy/snmp-walk/) | Groovy | SNMP | DataSource, PropertySource, AD | Ready |
| [snmp-get](groovy/snmp-get/) | Groovy | SNMP | DataSource, PropertySource | Ready |
| [ssh-exec](groovy/ssh-exec/) | Groovy | SSH | DataSource, ConfigSource, DiagnosticSource | Ready |
| [http-rest](groovy/http-rest/) | Groovy | HTTP | DataSource, PropertySource | Ready |
| [jdbc](groovy/jdbc/) | Groovy | JDBC | DataSource | Ready |
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

For code organization rules, see [script-structure.md](../skills/logicmonitor-module-authoring/references/script-structure.md).

## Adding a recipe

See [CONTRIBUTING.md](../CONTRIBUTING.md).
