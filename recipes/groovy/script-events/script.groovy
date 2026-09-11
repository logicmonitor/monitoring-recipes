/*******************************************************************************
 * Purpose: Pull events from an HTTP API and emit scripted EventSource JSON
 * Module types: EventSource (Script Event)
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

// --- Configuration ---
debug = false
def endpoint = hostProps.get("api.url", "INSERT_API_ENDPOINT_HERE")
def token = hostProps.get("api.token")
def defaultSeverity = hostProps.get("event.severity", "warn")
def connectTimeout = 30000
def readTimeout = Settings.getSettingInt("collector.script.timeout", 120) * 1000 - 2500

// --- Main flow ---
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
    def message = row.message ?: row.msg ?: row.title ?: row.description
    if (!message) {
        return
    }
    events << [
        happenedOn: (row.happenedOn ?: row.timestamp ?: row.date ?: new Date().toString()).toString(),
        severity  : (row.severity ?: defaultSeverity).toString(),
        message   : message.toString(),
        Source    : (row.source ?: row.src ?: hostProps.get("system.hostname")).toString()
    ]
}

// Limits: 50 events per execution, 100 per collector per minute
if (events.size() > 50) {
    events = events.take(50)
}

print JsonOutput.toJson([events: events])
return 0

// --- Helpers ---
def debugPrint(message) {
    if (debug) println "[DEBUG] ${message}"
}
