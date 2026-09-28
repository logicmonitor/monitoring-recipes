import groovy.json.JsonSlurper

// Resource inputs: read the API host and credentials from resource properties.
def apiHostname = hostProps.get("system.hostname", "").replaceAll('/$', '')
def apiBaseUrl = "https://${apiHostname}/api/v1"
def apiUser = hostProps.get("fabric.api.user", "")
def apiPassword = hostProps.get("fabric.api.pass", "")

if (!apiHostname || !apiUser || !apiPassword) {
    out.println("LAB CHECK: the resource is missing the workshop connection properties")
    return 0
}

// Main flow: authenticate, request one controller response, and emit datapoints.
def token = requestJson("${apiBaseUrl}/auth/token", [Authorization: "Basic ${basicAuthHeader(apiUser, apiPassword)}"])
def controller = requestJson(
    "${apiBaseUrl}/controller",
    [Authorization: "Bearer ${token.access_token}"]
)

// LAB STEP 1: Match each API field to a Collector output and datapoint.
// println "controller_health=${controller.health}"
// println "node_count=${controller.node_count}"
// println "api_latency_ms=${controller.api_latency_ms}"
return 0

// Helpers: keep authentication and HTTP details out of the main flow.
def basicAuthHeader(String user, String password) {
    return "${user}:${password}".bytes.encodeBase64().toString()
}

def requestJson(String endpoint, Map headers) {
    def connection = new URL(endpoint).openConnection()
    headers.each { key, value -> connection.setRequestProperty(key, value.toString()) }
    connection.setRequestProperty("Accept", "application/json")
    connection.connectTimeout = 10000
    connection.readTimeout = 20000
    if (connection.responseCode >= 400)
        throw new IllegalStateException("HTTP ${connection.responseCode} from ${endpoint}")
    return new JsonSlurper().parseText(connection.inputStream.text)
}
