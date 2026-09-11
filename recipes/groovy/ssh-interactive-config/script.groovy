/*******************************************************************************
 * Purpose: Session-based SSH config collection (enable + show command)
 * Module types: ConfigSource
 * Device properties: system.hostname, ssh.user, ssh.pass (or ssh.cert), ssh.enable.pass
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
def showCommand = hostProps.get("config.commands.standard", "INSERT_SHOW_COMMAND_HERE")
def enableCommand = hostProps.get("auto.config.escalation.command", "enable")
def enablePass = hostProps.get("ssh.enable.pass", hostProps.get("config.enable.pass", ""))
def disablePager = hostProps.get("config.commands.formatting", "terminal length 0")

// --- Main flow ---
def session
def output
try {
    session = debug ? remote.create(hostProps).withDebug(out) : remote.create(hostProps)

    if (disablePager && !disablePager.startsWith("INSERT_")) {
        try {
            session.exec(disablePager)
        } catch (Exception ignored) {
            debugPrint("Pager command skipped: ${ignored.message}")
        }
    }

    if (enablePass) {
        try {
            session.exec("${enableCommand}\n${enablePass}")
        } catch (Exception ignored) {
            debugPrint("Enable step skipped: ${ignored.message}")
        }
    }

    output = session.exec(showCommand)
} catch (Exception e) {
    debugPrint("SSH config collection failed: ${e.message}")
    return 1
}

if (output == null) {
    debugPrint("SSH exec returned no output")
    return 1
}

// ConfigSource Script: print raw text (default)
print output

// ConfigSource BatchScript (multi-instance): uncomment and map wildvalues
// def wildvalue = instanceProps?.get("wildvalue") ?: "running-config"
// print JsonOutput.toJson([data: [(wildvalue): [configuration: output.toString()]]])

return 0

// --- Helpers ---
def debugPrint(message) {
    if (debug) println "[DEBUG] ${message}"
}
