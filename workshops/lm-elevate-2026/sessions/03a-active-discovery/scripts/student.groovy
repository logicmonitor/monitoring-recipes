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

// Main flow: authenticate, request the node list, and emit instances.
def token = requestJson("${apiBaseUrl}/auth/token", [Authorization: "Basic ${basicAuthHeader(apiUser, apiPassword)}"])
def nodes = requestJson(
    "${apiBaseUrl}/nodes",
    [Authorization: "Bearer ${token.access_token}"]
)

nodes.each { node ->
    // LAB STEP 1: ID is identity, name is presentation, and role/site are ILPs.
    // println "${node.id}##${node.name}##${node.role} at ${node.site}####auto.role=${node.role}&auto.site=${node.site}"
}
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
