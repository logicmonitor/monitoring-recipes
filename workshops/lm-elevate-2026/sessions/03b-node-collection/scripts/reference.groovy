import groovy.json.JsonSlurper

// Read connection details from the resource instead of hard-coding them.
def apiHostname = hostProps.get("system.hostname", "").replaceAll('/$', '')
def apiBaseUrl = "https://${apiHostname}/api/v1"
def apiUser = hostProps.get("fabric.api.user", "")
def apiPassword = hostProps.get("fabric.api.pass", "")

if (!apiHostname || !apiUser || !apiPassword)
    return 1

// Authenticate once before visiting each discovered instance.
def token = requestJson(
    "${apiBaseUrl}/auth/token",
    [Authorization: "Basic ${basicAuthHeader(apiUser, apiPassword)}"]
)
def bearer = [Authorization: "Bearer ${token.access_token}"]
def collectionFailed = false

datasourceinstanceProps.each { instance, props ->
    if (collectionFailed)
        return
    // The wildvalue links this collection request to one discovered instance.
    def wild = props.wildvalue
    try {
        def node = requestJson("${apiBaseUrl}/nodes/${wild}", bearer)
        println "${wild}.health=${node.health}"
        println "${wild}.cpu_percent=${node.cpu_percent}"
        println "${wild}.memory_percent=${node.memory_percent}"
        println "${wild}.interface_count=${node.interface_count}"
        println "${wild}.error_rate_percent=${node.error_rate_percent}"
    } catch (Exception ignored) {
        // Do not emit partial metrics when one instance request fails.
        collectionFailed = true
    }
}
return collectionFailed ? 1 : 0

// Helper methods keep the multi-instance collection flow easy to follow.
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
