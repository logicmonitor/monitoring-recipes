import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
emit = modLoader.load("lm.emit", "0")

emit.instance("instance-a", "Instance A")
emit.instance("instance-b", "Instance B", "Second instance with description")
emit.instance(
    "instance-c",
    "Instance C",
    "Instance with ILPs",
    [role: "primary", "auto.env": "prod"]
)
return 0
