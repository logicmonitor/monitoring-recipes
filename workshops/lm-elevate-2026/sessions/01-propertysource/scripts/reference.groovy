import groovy.json.JsonSlurper

// Read connection details from the resource instead of hard-coding them.
def apiHostname = hostProps.get("system.hostname", "").replaceAll('/$', '')
def apiBaseUrl = "https://${apiHostname}/api/v1"
def apiUser = hostProps.get("fabric.api.user", "")
def apiPassword = hostProps.get("fabric.api.pass", "")

if (!apiHostname || !apiUser || !apiPassword)
    return 1

// Authenticate once, then use the bearer token for the controller request.
def token = requestJson(
    "${apiBaseUrl}/auth/token",
    [Authorization: "Basic ${basicAuthHeader(apiUser, apiPassword)}"]
)
def controller = requestJson(
    "${apiBaseUrl}/controller",
    [Authorization: "Bearer ${token.access_token}"]
)

// Print resource properties in the format the Collector expects.
println "system.categories=Training_Fabric"
println "auto.fabric_site=${controller.site}"
println "auto.fabric_version=${controller.version}"
return 0

// Helper methods keep the main PropertySource flow easy to follow.
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
