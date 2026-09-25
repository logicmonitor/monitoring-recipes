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

if (!hostname || !user || !pass)
    return 1

def token = readJson("${baseUrl}/auth/token", [Authorization: "Basic ${basicAuth(user, pass)}"])
def bearer = [Authorization: "Bearer ${token.access_token}"]
def collectionFailed = false

datasourceinstanceProps.each { instance, props ->
    if (collectionFailed)
        return
    def wild = props.wildvalue
    try {
        def node = readJson("${baseUrl}/nodes/${wild}", bearer)
        emit.dp(wild, "health", node.health)
        emit.dp(wild, "cpu_percent", node.cpu_percent)
        emit.dp(wild, "memory_percent", node.memory_percent)
        emit.dp(wild, "interface_count", node.interface_count)
        emit.dp(wild, "error_rate_percent", node.error_rate_percent)
    } catch (Exception ignored) {
        collectionFailed = true
    }
}
return collectionFailed ? 1 : 0

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
