// Minimal HTTP + emit pattern (copy into collect.groovy; not a runnable module alone)
import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import com.santaba.agent.util.Settings

def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
httpMod = modLoader.load("proto.http", "0")
emit = modLoader.load("lm.emit", "0")

def endpoint = hostProps.get("api.url", "https://api.example.com/status")
def readTimeout = Settings.getSettingInt("collector.script.timeout", 120) * 1000 - 2500
def http = httpMod.httpSnippetFactory(hostProps)
def response = http.rawGet(endpoint, ["Accept": "application/json"], 30000, readTimeout)

if (response.responseCode >= 400) {
    return 1
}

emit.dp("example_metric", response.responseCode)
return 0
