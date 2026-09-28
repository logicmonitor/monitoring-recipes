import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import groovy.json.JsonSlurper

def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def emit = modLoader.load("lm.emit", "1.3.0")
def hostname = hostProps.get("system.hostname", "").replaceAll('/$', '')
def baseUrl = "https://${hostname}/api/v1"
def user = hostProps.get("fabric.api.user", "")
def pass = hostProps.get("fabric.api.pass", "")

if (!hostname || !user || !pass) {
    out.println("LAB CHECK: the resource is missing the workshop connection properties")
    return 0
}

def token = readJson("${baseUrl}/auth/token", [Authorization: "Basic ${basicAuth(user, pass)}"])
def controller = readJson(
    "${baseUrl}/controller",
    [Authorization: "Bearer ${token.access_token}"]
)

// LAB STEP 1: Match each API field to a datapoint in the module JSON.
// emit.dp("controller_health", controller.health)
// emit.dp("node_count", controller.node_count)
// emit.dp("api_latency_ms", controller.api_latency_ms)
return 0

def basicAuth(String user, String pass) {
    return "${user}:${pass}".bytes.encodeBase64().toString()
}

def readJson(String endpoint, Map headers) {
    def connection = new URL(endpoint).openConnection()
    headers.each { key, value -> connection.setRequestProperty(key, value.toString()) }
    connection.setRequestProperty("Accept", "application/json")
    connection.connectTimeout = 10000
    connection.readTimeout = 20000
    if (connection.responseCode >= 400)
        throw new IllegalStateException("HTTP ${connection.responseCode} from ${endpoint}")
    return new JsonSlurper().parseText(connection.inputStream.text)
}
