/*******************************************************************************
 * Purpose: Emit TopologySource edges via lm.topo
 * Module types: TopologySource
 * Device properties: system.hostname, predef.externalResourceID, topo.namespace, topo.blacklist
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

// --- Snippet bootstrap ---
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def lmtopo = loader.load("lm.topo", "0")

// --- Configuration ---
debug = false
def keyNamespace = hostProps.get(hostProps.get("topo.namespace", ""), "")
def keyBlacklist = hostProps.get("topo.blacklist", "").tokenize(",")
def edgeType = hostProps.get("topo.edge.type", "INSERT_EDGE_TYPE_HERE")
def fromEri = hostProps.get("predef.externalResourceID")?.tokenize(",")?.first()
    ?: hostProps.get("topo.from.eri", "INSERT_FROM_ERI_HERE")
def toEri = hostProps.get("topo.to.eri", "INSERT_TO_ERI_HERE")
def edges = []

// --- Main flow ---
if (!fromEri || fromEri.startsWith("INSERT_") || !toEri || toEri.startsWith("INSERT_")) {
    debugPrint("Missing from/to ERI — set predef.externalResourceID (via add-eri) and topo.to.eri")
    println lmtopo.generateTopology(edges, keyNamespace, keyBlacklist, null, debug)
    return 0
}

lmtopo.registerEdge(edgeType, fromEri, toEri, edges)
println lmtopo.generateTopology(edges, keyNamespace, keyBlacklist, null, debug)
return 0

// --- Helpers ---
def debugPrint(message) {
    if (debug) println "[DEBUG] ${message}"
}
