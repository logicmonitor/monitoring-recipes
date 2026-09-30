import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def emit = modLoader.load("lm.emit", "0")

emit.events([[message: "Example log event"]])
return 0
