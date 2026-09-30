import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def lmTopo = modLoader.load("lm.topo", "0")
def debug = false
def lmDebugMod = modLoader.load("lm.debug", "2.0.0")
def lmDebug = lmDebugMod.create(hostProps, debug, out)
def keyNamespace = hostProps.get(hostProps.get("topo.namespace", ""), "")
def keyBlacklist = hostProps.get("topo.blacklist", "").tokenize(",")
def edges = []

// Add one edge per discovered relationship; use stable ERIs and a meaningful type.
lmTopo.registerEdge("Example", "device:source", "device:target", edges)
lmDebug.debug("Generated ${edges.size()} synthetic topology edge(s)")
print lmTopo.generateTopology(edges, keyNamespace, keyBlacklist, debug)
return 0
