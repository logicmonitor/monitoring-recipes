import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def emit = modLoader.load("lm.emit", "0")

emit.instance("instance-a", "Instance A", "", [tier: "standard", role: "primary"])
emit.instance("instance-b", "Instance B", "Second instance with description")
emit.instance(
    "instance-c",
    "Instance C",
    "Instance with ILPs for filters/grouping",
    [role: "replica", "auto.env": "prod", feature_x: "enabled"]
)
return 0
