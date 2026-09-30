import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def emit = modLoader.load("lm.emit", "0")

datasourceinstanceProps.each { instance, props ->
    def wild = props.wildvalue
    emit.dp(wild, "example_metric", 0)
}
return 0
