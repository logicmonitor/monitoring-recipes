# Script Structure

Canonical code arrangement for LogicMonitor collector scripts. Follow this layout in all custom Groovy and PowerShell modules.

See also: [snippets-catalog.md](snippets-catalog.md), [module-snippets.md](module-snippets.md)

## Groovy (snippet-first)

### Section order

```
1. Header comment        — purpose, supported module types, key device properties
2. Imports               — GSH, Snippets, domain imports
3. Snippet bootstrap     — loader + version-pinned snippet loads
4. Configuration         — hostProps reads, normalized props map, timeouts
5. Main flow             — linear top-level logic (no main() wrapper)
6. Output emission       — lm.emit or JsonOutput.toJson (never hand-built JSON)
7. Return code           — return 0 (success) / return 1 (failure)
8. Helper defs           — def functions at bottom (valid even after return in AD scripts)
```

### Template

```groovy
/*******************************************************************************
 * Purpose: <one-line description>
 * Module types: DataSource, PropertySource, Active Discovery
 * Device properties: system.hostname, snmp.community (or v3 props)
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import com.santaba.agent.util.Settings
import groovy.json.JsonOutput

// --- Snippet bootstrap ---
// withBinding is required: snippets write through the calling script's binding.
// Without it, emit.* calls return without error and produce no output.
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
emit = loader.load("lm.emit", "0")

// --- Configuration ---
debug = false
def host = hostProps.get("system.hostname")
Map props = hostProps.toProperties().collectEntries { k, v -> [(k.toLowerCase()): v] }
def startTime = System.currentTimeMillis()
def timeout = Settings.getSettingInt("collector.batchscript.timeout",
    Settings.getSettingInt("collector.script.timeout", 120)) * 1000
timeout -= 2500

// --- Main flow ---
// ... collection logic ...

// --- Output emission ---
emit.dp("metricName", value)                    // DataSource Script: key=value
// emit.dp(wildvalue, "fieldName", value)        // DataSource BatchScript: instance.field=value
// emit.instance(wv, alias, desc, [auto.k: v])   // Active Discovery
// emit.property("auto.name", value)             // PropertySource
// print JsonOutput.toJson([data: "...", format: "markdown"])  // Diag/Remediation

return 0

// --- Helpers (below return is valid in Groovy AD scripts) ---
def debugPrint(message) {
    if (debug) println "[DEBUG] ${message}"
}
```

### Rules

| Rule | Why |
|------|-----|
| Use `def` for module-local helpers | Matches production module style |
| Normalize props to lowercase map | SNMP property keys are case-insensitive |
| Timeout from Settings with ~2500ms buffer | Collector needs cleanup window before kill |
| Use `lm.emit` for key=value and AD lines | Sanitizes wildvalues, fields, nulls |
| Use `JsonOutput.toJson()` for JSON output | Avoids malformed JSON |
| No hardcoded credentials | Use `hostProps` / device properties |
| `debug` flag + conditional logging | Collector troubleshooting without noise |
| Return `0` on success, `1` on failure | AD: non-zero preserves existing instances |

### Anti-patterns

- Do **not** copy snippet source into scripts — load via `loader.load()`
- Do **not** use raw JSCH for SSH — use `lm.remote` snippet
- Do **not** build JSON with string concatenation
- Do **not** use `println` for hand-rolled `key=value` when `lm.emit` is available
- Do **not** wrap everything in a `main()` function — collector runs top-level script

---

## PowerShell

### Section order

```
1. Header comment
2. Property tokens        — ##prop## with multiline-safe @' '@ for passwords
3. Token validation       — detect unset placeholders
4. Hostname prep          — IP→FQDN, Azure IP fallback
5. Credential setup       — PSCredential + cascade
6. Functions              — connection, sanitization, debug
7. Core logic             — inline or $command scriptblock
8. Cleanup                — Remove-PSSession, disconnect SDKs
9. exit 0
```

### Template

```powershell
<# Purpose: <one-line description>
   Module types: DataSource, PropertySource
   Device properties: system.hostname, wmi.user, wmi.pass #>

$debug = $false

# --- Property tokens ---
$hostname = if (-not [string]::IsNullOrEmpty('##system.azure.privateIpAddress##')) {
    '##system.azure.privateIpAddress##'
} else {
    '##system.hostname##'
}
$wmiUser = '##wmi.user##'
$wmiPass = @'
##wmi.pass##
'@

# --- Token validation ---
function Test-PropertyToken {
    param([string]$Value)
    return [string]::IsNullOrEmpty($Value) -or $Value -match '^##.*##$'
}

# --- Hostname prep ---
if ($hostname -match '\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b') {
    $hostname = [System.Net.Dns]::GetHostbyAddress($hostname).HostName
}

# --- Credential setup ---
$creds = $null
if (-not (Test-PropertyToken $wmiUser) -and -not (Test-PropertyToken $wmiPass)) {
    $securePass = ConvertTo-SecureString $wmiPass -AsPlainText -Force
    $creds = [PSCredential]::new($wmiUser, $securePass)
}

# --- Functions ---
function Write-DebugMessage { param([string]$Message); if ($debug) { Write-Host "[DEBUG] $Message" } }
function Sanitize-Output {
    param($Metric)
    if ($Metric -is [bool]) { return [int]$Metric }
    if ([string]::IsNullOrWhiteSpace($Metric)) { return 'null' }
    return $Metric
}

# --- Core logic ---
# ... query / remote execution ...

Write-Output "metricName=$(Sanitize-Output $value)"
exit 0
```

### Rules

| Rule | Why |
|------|-----|
| `Write-Output` for all collection data | `Write-Host` does not reliably reach stdout; causes BatchScript timeouts |
| Multiline-safe password tokens (`@' '@`) | Passwords may contain special characters |
| Detect unset `##prop##` tokens before use | Unsubstituted tokens break remote connections |
| Credential cascade: product → wmi → none | Flexible auth without hardcoding |
| `Sanitize-Output` for null/bool values | Collector expects consistent datapoint values |
| Collector-local fork via `system.categories` | Skip remoting when script runs on collector host |
| `exit 0` on success | Non-zero signals failure to collector |
| Never set global `$ErrorActionPreference = 'Stop'` | LM expects partial output, not terminating errors |

### Anti-patterns

- Do **not** use `Write-Host` for datapoint output (debug logging only)
- Do **not** hardcode credentials
- Do **not** assume property tokens were substituted — always validate
- Do **not** leave sessions open — `Remove-PSSession` in cleanup

---

## Shared AD + collection pattern (PowerShell)

When AD and collection share the same query logic, use a shared scriptblock and `$isAd` flag:

```powershell
$isAd = $false  # Set $true in AD script

# Inside shared logic:
if ($isAd) {
    Write-Output "$wildvalue##$wildalias####auto.key=$value"
} else {
    Write-Output "$wildvalue.MetricName=$(Sanitize-Output $value)"
}
```

Sanitize wildvalues for AD: replace `#`, `\`, `:`, `=`, spaces with `_`.
