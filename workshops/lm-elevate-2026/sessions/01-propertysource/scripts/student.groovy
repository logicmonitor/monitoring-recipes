import groovy.json.JsonSlurper

// Resource inputs: read the API host and credentials from resource properties.
// LAB STEP 1: Read system.hostname instead of hard-coding the API host.
def apiHostname = ""
def apiBaseUrl = "https://${apiHostname}/api/v1"
def apiUser = hostProps.get("fabric.api.user", "")
def apiPassword = hostProps.get("fabric.api.pass", "")

if (!apiHostname || !apiUser || !apiPassword) {
    out.println("LAB CHECK: add system.hostname and the workshop credentials")
    return 0
}

// Main flow: authenticate, request controller metadata, and emit resource properties.
def token = requestJson("${apiBaseUrl}/auth/token", [Authorization: "Basic ${basicAuthHeader(apiUser, apiPassword)}"])
def controller = requestJson(
    "${apiBaseUrl}/controller",
    [Authorization: "Bearer ${token.access_token}"]
)

// LAB STEP 2: Uncomment these Collector output lines and explain their purposes.
// println "system.categories=Training_Fabric"
// println "auto.fabric_site=${controller.site}"
// println "auto.fabric_version=${controller.version}"
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
