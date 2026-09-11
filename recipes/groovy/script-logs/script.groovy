/*******************************************************************************
 * Purpose: Pull log events from an HTTP API and emit Script Logs JSON
 * Module types: LogSource (Script Logs)
 * Device properties: api.url, api.token (optional)
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import com.santaba.agent.util.Settings
import groovy.json.JsonOutput
import groovy.json.JsonSlurper

// --- Snippet bootstrap ---
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
def httpMod = loader.load("proto.http", "0")
def cacheMod = loader.load("lm.cache", "0")

// --- Configuration ---
debug = false
def endpoint = hostProps.get("api.url", "INSERT_API_ENDPOINT_HERE")
def tokenProperty = hostProps.get("api.token.property", "api.token")
def cache = cacheMod.cacheSnippetFactory(null, "script-logs-recipe")
def connectTimeout = 30000
def readTimeout = Settings.getSettingInt("collector.script.timeout", 120) * 1000 - 2500

// --- Main flow ---
def token = cache.cacheGet("authToken")
if (!token) {
    token = hostProps.get(tokenProperty)
    if (token) {
        cache.cacheSet("authToken", token, 3600)
    }
}

def headers = ["Accept": "application/json"]
if (token) {
    headers["Authorization"] = "Bearer ${token}"
}

def http = httpMod.httpSnippetFactory(hostProps)
def response = http.rawGet(endpoint, headers, connectTimeout, readTimeout)

if (response.responseCode >= 400) {
    debugPrint("HTTP ${response.responseCode} from ${endpoint}")
    return 1
}

def parsed = new JsonSlurper().parseText(response.inputStream.text)
def rows = parsed instanceof List ? parsed : (parsed?.events ?: parsed?.items ?: [parsed])

def events = []
rows.each { row ->
    if (!(row instanceof Map)) {
        return
    }
    // message is mandatory — events without it are discarded
    def message = row.message ?: row.msg ?: row.title ?: row.description
    if (!message) {
        return
    }
    def event = [message: message.toString()]
    if (row.timestamp) {
        event.timestamp = row.timestamp.toString()
    }
    // Optional custom fields become LM Logs metadata
    // event.customField = row.someField
    events << event
}

// Empty events array is valid — still exit 0 so the collector keeps the payload
print JsonOutput.toJson([events: events])
return 0

// --- Helpers ---
def debugPrint(message) {
    if (debug) println "[DEBUG] ${message}"
}
