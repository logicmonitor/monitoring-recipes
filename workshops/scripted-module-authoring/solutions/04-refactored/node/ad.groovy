import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import groovy.json.JsonSlurper

def modLoader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def httpMod = modLoader.load("proto.http", "1.0.0")
def emit = modLoader.load("lm.emit", "1.3.0")
def cacheMod = modLoader.load("lm.cache", "0.3.1")
def debugMod = modLoader.load("lm.debug", "2.0.0")
def debug = false
def lmDebug = debugMod.create(hostProps, debug, out)
def cacheDebug = [LMDebugPrint: { message -> lmDebug.debug(message) }]
def cache = cacheMod.cacheSnippetFactory(cacheDebug, "training-fabric")
def http = httpMod.create(hostProps)
def hostname = hostProps.get("system.hostname", "").replaceAll('/$', '')
def baseUrl = "https://${hostname}/api/v1"
def user = hostProps.get("fabric.api.user", "")
def pass = hostProps.get("fabric.api.pass", "")

if (!hostname || !user || !pass)
    return 1

def token = getToken(cache, http, baseUrl, user, pass, lmDebug)
if (!token)
    return 1

def response = http.withHeaders(
    [Authorization: "Bearer ${token}", Accept: "application/json"]
).GET("${baseUrl}/nodes", 10000, 20000)
if (response.responseCode >= 400)
    return 1

def nodes = new JsonSlurper().parseText(response.inputStream.text)
nodes.each { node ->
    emit.instance(node.id, node.name, "${node.role} at ${node.site}", [
        "auto.role": node.role,
        "auto.site": node.site
    ])
}
return 0

def getToken(cache, http, String baseUrl, String user, String pass, lmDebug) {
    def token = cache.cacheGet("accessToken")
    if (token)
        return token

    def basic = "${user}:${pass}".bytes.encodeBase64().toString()
    def response = http.withHeaders(
        [Authorization: "Basic ${basic}", Accept: "application/json"]
    ).GET("${baseUrl}/auth/token", 10000, 20000)
    if (response.responseCode >= 400)
        return null
    token = new JsonSlurper().parseText(response.inputStream.text).access_token
    cache.cacheSet("accessToken", token, 300000)
    lmDebug.debug("Fetched and cached a new training API token")
    return token
}
