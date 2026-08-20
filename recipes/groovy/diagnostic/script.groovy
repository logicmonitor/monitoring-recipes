/*******************************************************************************
 * Purpose: Collect on-demand troubleshooting output as DiagnosticSource JSON
 * Module types: DiagnosticSource
 * Device properties: system.hostname, ssh.user, ssh.pass (or ssh.cert)
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import groovy.json.JsonOutput

// --- Snippet bootstrap ---
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
def remote = loader.load("lm.remote", "0.6.0")

// --- Configuration ---
debug = false
def command = "INSERT_COMMAND_HERE"

// --- Main flow ---
def lines = ["# Diagnostic"]
def hasAlert = false
try {
    hasAlert = alertProps?.containsKey("alert.datapoint")
} catch (MissingPropertyException ignored) {
    hasAlert = false
}

if (hasAlert) {
    lines << ""
    lines << "## Alert context"
    lines << "- Datapoint: ${alertProps.get('alert.datapoint')}"
    lines << "- Value: ${alertProps.get('alert.datapoint.value')}"
    lines << "- Severity: ${alertProps.get('alert.severity')}"
    lines << "- Instance: ${alertProps.get('alert.instance')}"
} else {
    lines << ""
    lines << "_Manual run — no alert context._"
}

def output
try {
    output = remote.exec(hostProps, command)
} catch (Exception e) {
    output = "Command failed: ${e.message}"
}

lines << ""
lines << "## Command"
lines << "```"
lines << command
lines << "```"
lines << ""
lines << "## Output"
lines << "```"
lines << (output ?: "(no output)")
lines << "```"

print JsonOutput.toJson([data: lines.join("\n"), format: "markdown"])
return 0
