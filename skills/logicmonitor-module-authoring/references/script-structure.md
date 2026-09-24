# Script Structure

Canonical code arrangement for LogicMonitor collector scripts. Follow this layout in all custom Groovy and PowerShell modules.

See also: [snippet-loader.md](snippet-loader.md), [snippets-catalog.md](snippets-catalog.md), [module-snippets.md](module-snippets.md)

## Groovy (snippet-first)

### Section order — collection scripts (collect.groovy)

```
1. Header comment
2. Imports
3. Snippet bootstrap     — modLoader.withBinding(getBinding()); load snippets from modLoader
4. Configuration
5. Main flow
6. Output emission       — emit.dp / JsonOutput.toJson
7. Return code           — return 0 / return 1
8. Helper methods        — after the main flow
```

### Section order — Active Discovery (ad.groovy)

Same as collection, with helper `def` blocks after `return 0` to keep the main flow easy to scan.

### Template (collection)

```groovy
/*******************************************************************************
 * Purpose: <one-line description>
 * Module types: DataSource, PropertySource
 * Device properties: system.hostname, snmp.community (or v3 props)
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import com.santaba.agent.util.Settings
import groovy.json.JsonOutput

// --- Snippet bootstrap ---
def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def emit = modLoader.load("lm.emit", "0")
def lmDebugMod = modLoader.load("lm.debug", "2.0.0")

// --- Configuration ---
def debug = false
def lmDebug = lmDebugMod.create(hostProps, debug, out)
def host = hostProps.get("system.hostname")
Map props = hostProps.toProperties().collectEntries { k, v -> [(k.toLowerCase()): v] }
def startTime = System.currentTimeMillis()
def timeout = Settings.getSettingInt("collector.batchscript.timeout",
    Settings.getSettingInt("collector.script.timeout", 120)) * 1000
timeout -= 2500

// Use lmDebug.debug/info/warn/error for diagnostic messages.

// --- Main flow ---
// ... collection logic ...

// --- Output emission ---
emit.dp("metricName", value)
// emit.dp(wildvalue, "fieldName", value)        // BatchScript
// print JsonOutput.toJson([data: "...", format: "markdown"])

return 0
```

Fallback if `withBinding` is unavailable: unbound loader + `emit.binding = binding` after `load("lm.emit")` — see [snippet-loader.md](snippet-loader.md).

### Template (Active Discovery)

Use bound `modLoader` and `emit.instance(...)` in main flow; helpers may appear after `return 0`.

### Rules

| Rule | Why |
|------|-----|
| `modLoader.withBinding(getBinding())` before any `load()` | Snippet output reaches collector stdout |
| Use `def` for module-local helpers | Matches production module style |
| Keep snippet handles local with `def` and pass them to helper methods | Makes helper dependencies explicit; binding-style globals are a legacy compatibility fallback |
| Use `lm.debug` for diagnostic messages | Keeps debug output collector-aware and out of metric/event data |
| Collection: helpers after `return 0` | Matches the repository Groovy standard and keeps the main flow together |
| Normalize props to lowercase map | SNMP property keys are case-insensitive |
| Timeout from Settings with ~2500ms buffer | Collector needs cleanup window before kill |
| `emit.dp()` / `emit.instance()` after `modLoader.load("lm.emit", "0")` | Sanitized collector output |
| Use `JsonOutput.toJson()` for JSON output | Avoids malformed JSON |
| No hardcoded credentials | Use `hostProps` / device properties |
| Return `0` on success, `1` on failure | AD: non-zero preserves existing instances |

### Anti-patterns

- Do **not** copy snippet source into scripts — load via `modLoader.load()`
- Do **not** hand-roll AD/metric lines when `emit` is loaded — use `emit.dp()` / `emit.instance()`
- Do **not** use raw JSCH for SSH — use `lm.remote` snippet
- Do **not** build JSON with string concatenation
- Prefer `emit.dp` for production metrics; `println "key=value"` is OK for minimal/debug scripts or when snippets are unavailable (parent `println` always hits stdout)
- Do **not** wrap everything in a `main()` function — collector runs top-level script
- Prefer passing `emit`, `http`, `snmp`, and other snippet handles into helper methods; use binding-style assignment without `def` only when compatibility requires it — see [groovy.md](groovy.md#script-scoping-locals-vs-helpers)

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
