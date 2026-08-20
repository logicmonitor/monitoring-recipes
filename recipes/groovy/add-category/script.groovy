/*******************************************************************************
 * Purpose: Set system.categories from a host-property or probe match
 * Module types: PropertySource
 * Device properties: system.hostname (plus the probe property you choose)
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

// --- Snippet bootstrap ---
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
def emit = loader.load("lm.emit", "0")

// --- Configuration ---
debug = false
def probeProperty = "INSERT_PROBE_PROPERTY_HERE"
def matchPattern = "INSERT_MATCH_PATTERN_HERE"
def category = "INSERT_CATEGORY_HERE"
def probe = hostProps.get(probeProperty, hostProps.get("system.hostname", ""))

// --- Main flow ---
if (!category || category.startsWith("INSERT_")) {
    debugPrint("Set INSERT_CATEGORY_HERE before running")
    return 1
}

if (probe?.toString() ==~ matchPattern) {
    emit.property("system.categories", category)
} else {
    debugPrint("Probe '${probe}' did not match /${matchPattern}/ — no category set")
}

return 0

// --- Helpers ---
def debugPrint(message) {
    if (debug) println "[DEBUG] ${message}"
}
