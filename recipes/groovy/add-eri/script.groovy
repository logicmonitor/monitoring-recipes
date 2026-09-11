/*******************************************************************************
 * Purpose: Assign External Resource IDs (ERIs) for topology mapping
 * Module types: PropertySource (ERISource data type)
 * Device properties: system.displayname, topo.namespace, topo.blacklist
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import org.json.JSONArray

// --- Snippet bootstrap ---
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def lmtopo = loader.load("lm.topo", "0")

// --- Configuration ---
debug = false
def keyNamespace = hostProps.get(hostProps.get("topo.namespace", ""), "")
def keyBlacklist = hostProps.get("topo.blacklist", "").tokenize(",")
def eriNamespace = hostProps.get("topo.eri.namespace", "INSERT_ERI_NAMESPACE_HERE")
def externalResourceType = hostProps.get("topo.ert", "INSERT_ERT_HERE")
def displayName = hostProps.get("system.displayname", hostProps.get("system.hostname"))
def eriValue = hostProps.get("topo.eri.value", "${eriNamespace}--${displayName}")
def priority = 1

// --- Main flow ---
if (!displayName) {
    debugPrint("No system.displayname / system.hostname — cannot build ERI")
    return 1
}

def eriArray = new JSONArray()
lmtopo.emitEri(eriNamespace, priority, [eriValue], externalResourceType, eriArray)
lmtopo.printEriArray(eriArray, keyNamespace, keyBlacklist)
return 0

// --- Helpers ---
def debugPrint(message) {
    if (debug) println "[DEBUG] ${message}"
}
