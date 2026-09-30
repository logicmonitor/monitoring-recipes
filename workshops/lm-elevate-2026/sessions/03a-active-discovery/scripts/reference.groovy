import groovy.json.JsonSlurper

// Read connection details from the resource instead of hard-coding them.
def apiHostname = hostProps.get("system.hostname", "").replaceAll('/$', '')
def apiBaseUrl = "https://${apiHostname}/api/v1"
def apiUser = hostProps.get("fabric.api.user", "")
def apiPassword = hostProps.get("fabric.api.pass", "")

if (!apiHostname || !apiUser || !apiPassword)
    return 1

// Authenticate once, then ask the API which node instances exist.
def token = requestJson(
    "${apiBaseUrl}/auth/token",
    [Authorization: "Basic ${basicAuthHeader(apiUser, apiPassword)}"]
)
def nodes = requestJson(
    "${apiBaseUrl}/nodes",
    [Authorization: "Bearer ${token.access_token}"]
)

nodes.each { node ->
    // The ID is identity; the name is presentation; role and site are context.
    // Status is emitted as an instance property so the module can filter offline nodes.
    println "${node.id}##${node.name}##${node.role} at ${node.site}####auto.role=${node.role}&auto.site=${node.site}&auto.status=${node.status}"
}
return 0

// Helper methods keep the discovery flow easy to follow.
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
