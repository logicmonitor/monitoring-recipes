// REFACTOR LAB: Node collection: trace token reuse and wildvalue-qualified output.
// Keep the monitoring output names and values unchanged while identifying reusable LogicMonitor helpers.

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import groovy.json.JsonSlurper

// REVIEW 1: Snippets replace repeated platform boilerplate with versioned helpers.
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

// REVIEW 2: Resource inputs stay separate from the request and collection flow.
def hostname = hostProps.get("system.hostname", "").replaceAll('/$', '')
def baseUrl = "https://${hostname}/api/v1"
def user = hostProps.get("fabric.api.user", "")
def pass = hostProps.get("fabric.api.pass", "")

if (!hostname || !user || !pass)
    return 1

// REVIEW 3: One token supports the collection loop; the wildvalue preserves instance identity.
def token = getToken(cache, http, baseUrl, user, pass, lmDebug)
if (!token)
    return 1

def headers = [Authorization: "Bearer ${token}", Accept: "application/json"]
def collectionFailed = false
datasourceinstanceProps.each { instance, props ->
    if (collectionFailed)
        return
    // The wildvalue links this request and every metric to one instance.
    def wild = props.wildvalue
    def response = http.withHeaders(headers).GET("${baseUrl}/nodes/${wild}", 10000, 20000)
    if (response.responseCode == 401) {
        lmDebug.warn("Cached token was rejected; refreshing token")
        token = getToken(cache, http, baseUrl, user, pass, lmDebug, true)
        if (!token) {
            collectionFailed = true
            return
        }
        headers = [Authorization: "Bearer ${token}", Accept: "application/json"]
        response = http.withHeaders(headers).GET("${baseUrl}/nodes/${wild}", 10000, 20000)
    }
    if (response.responseCode >= 400) {
        lmDebug.error("HTTP ${response.responseCode} for node ${wild}")
        collectionFailed = true
        return
    }
    def node = new JsonSlurper().parseText(response.inputStream.text)
    // REVIEW 5: These names must remain aligned with datapoints and final graph lines.
    emit.dp(wild, "health", node.health)
    emit.dp(wild, "cpu_percent", node.cpu_percent)
    emit.dp(wild, "memory_percent", node.memory_percent)
    emit.dp(wild, "interface_count", node.interface_count)
    emit.dp(wild, "error_rate_percent", node.error_rate_percent)
}
return collectionFailed ? 1 : 0

// REVIEW 4: Cache the short-lived token, never the changing node metrics.
def getToken(cache, http, String baseUrl, String user, String pass, lmDebug, Boolean forceRefresh = false) {
    if (forceRefresh)
        cache.cacheRemove("accessToken")
    def token = cache.cacheGet("accessToken")
    if (token)
        return token

    // The password is used only to request a new token and is never logged.
    def basic = "${user}:${pass}".bytes.encodeBase64().toString()
    def response = http.withHeaders(
        [Authorization: "Basic ${basic}", Accept: "application/json"]
    ).GET("${baseUrl}/auth/token", 10000, 20000)
    if (response.responseCode >= 400) {
        lmDebug.error("Authentication failed with HTTP ${response.responseCode}")
        return null
    }
    def tokenResponse = new JsonSlurper().parseText(response.inputStream.text)
    token = tokenResponse.access_token
    def expiresIn = (tokenResponse.expires_in ?: 300) as Integer
    // Leave a safety margin so the token does not expire during collection.
    def cacheTtl = Math.max(1, expiresIn - 60) as Integer
    cache.cacheSet("accessToken", token, cacheTtl)
    lmDebug.debug("Fetched and cached a new training API token for ${cacheTtl}s")
    return token
}
