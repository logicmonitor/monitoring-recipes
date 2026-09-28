// REFACTOR LAB: PropertySource: trace snippet-backed HTTP, output, diagnostics, and token caching.
// Keep the monitoring output names and values unchanged while identifying reusable LogicMonitor helpers.

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import groovy.json.JsonSlurper

// Load versioned LogicMonitor helpers for HTTP, output, diagnostics, and caching.
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

// Read connection details from the resource instead of hard-coding them.
def hostname = hostProps.get("system.hostname", "").replaceAll('/$', '')
def baseUrl = "https://${hostname}/api/v1"
def user = hostProps.get("fabric.api.user", "")
def pass = hostProps.get("fabric.api.pass", "")

if (!hostname || !user || !pass)
    return 1

// Main flow: obtain the token, request controller metadata, and enrich the resource.
def token = getToken(cache, http, baseUrl, user, pass, lmDebug)
if (!token)
    return 1
def response = http.withHeaders(
    [Authorization: "Bearer ${token}", Accept: "application/json"]
).GET("${baseUrl}/controller", 10000, 20000)
if (response.responseCode == 401) {
    lmDebug.warn("Cached token was rejected; refreshing token")
    token = getToken(cache, http, baseUrl, user, pass, lmDebug, true)
    if (!token)
        return 1
    response = http.withHeaders(
        [Authorization: "Bearer ${token}", Accept: "application/json"]
    ).GET("${baseUrl}/controller", 10000, 20000)
}
if (response.responseCode >= 400) {
    lmDebug.error("HTTP ${response.responseCode} from controller endpoint")
    return 1
}

def controller = new JsonSlurper().parseText(response.inputStream.text)
emit.property("system.categories", "Training_Fabric")
emit.property("auto.fabric_site", controller.site)
emit.property("auto.fabric_version", controller.version)
return 0

// Cache helper: cache the short-lived token, never the changing controller data.
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

