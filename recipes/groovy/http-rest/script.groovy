/*******************************************************************************
 * Purpose: HTTP GET against a REST API with optional token caching
 * Module types: DataSource, PropertySource
 * Device properties: system.hostname, api.url, api.token (optional)
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import com.santaba.agent.util.Settings
import groovy.json.JsonSlurper

// --- Snippet bootstrap ---
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
def httpMod = loader.load("proto.http", "0")
def emit = loader.load("lm.emit", "0")
def cacheMod = loader.load("lm.cache", "0")

// --- Configuration ---
debug = false
def endpoint = hostProps.get("api.url", "INSERT_API_ENDPOINT_HERE")
def tokenProperty = hostProps.get("api.token.property", "api.token")
def cacheKey = "authToken"
def cache = cacheMod.cacheSnippetFactory(null, "http-rest-recipe")
def startTime = System.currentTimeMillis()
def connectTimeout = 30000
def readTimeout = Settings.getSettingInt("collector.script.timeout", 120) * 1000 - 2500

// --- Main flow ---
def token = cache.cacheGet(cacheKey)
if (!token) {
    token = hostProps.get(tokenProperty)
    if (token) {
        cache.cacheSet(cacheKey, token, 3600)
    }
}

def headers = [:]
if (token) {
    headers["Authorization"] = "Bearer ${token}"
}
headers["Accept"] = "application/json"

def http = httpMod.httpSnippetFactory(hostProps)
def response = http.rawGet(endpoint, headers, connectTimeout, readTimeout)

if (response.responseCode >= 400) {
    debugPrint("HTTP ${response.responseCode} from ${endpoint}")
    return 1
}

def body = response.inputStream.text
def slurper = new JsonSlurper()
def parsed = slurper.parseText(body)

// Customize: map API response fields to datapoints or properties
// DataSource: emit.dp("metricName", parsed.someField)
// PropertySource: emit.property("auto.someField", parsed.someField)
if (parsed instanceof Map) {
    parsed.each { key, value ->
        emit.dp(key.toString(), value)
    }
} else {
    emit.dp("response", body)
}

return 0

// --- Helpers ---
def debugPrint(message) {
    if (debug) println "[DEBUG] ${message}"
}
