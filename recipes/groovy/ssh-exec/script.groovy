/*******************************************************************************
 * Purpose: Execute a one-shot remote command over SSH via lm.remote
 * Module types: DataSource, ConfigSource, DiagnosticSource
 * Device properties: system.hostname, ssh.user, ssh.pass (or ssh.cert)
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import groovy.json.JsonOutput

// --- Snippet bootstrap ---
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def remote = loader.load("lm.remote", "0.6.0")
def emit = loader.load("lm.emit", "0")
def lmDebugMod = loader.load("lm.debug", "2.0.0")

// --- Configuration ---
def debug = false
def lmDebug = lmDebugMod.create(hostProps, debug, out)
def command = "INSERT_COMMAND_HERE"

// --- Main flow ---
def output
try {
    if (debug) {
        def session = remote.create(hostProps).withDebug(out)
        output = session.exec(command)
    } else {
        output = remote.exec(hostProps, command)
    }
} catch (Exception e) {
    lmDebug.error("SSH exec failed: ${e.message}")
    return 1
}

if (output == null) {
    lmDebug.warn("SSH exec returned no output")
    return 1
}

// DataSource: parse output and emit.dp("metricName", value)
// ConfigSource Script: print raw output (no key=value)
// DiagnosticSource: print JsonOutput.toJson([data: output, format: "markdown"])
emit.dp("commandOutput", output.trim())

return 0
