/*******************************************************************************
 * © 2007-2020 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/

import com.santaba.agent.groovyapi.http.HTTP
import groovy.json.JsonSlurper

hostname = hostProps.get("system.hostname")
user = hostProps.get("vmanage.user")
password = hostProps.get("vmanage.pass")
port = hostProps.get("vmanage.port")
url = "https://${hostname}:${port}/"
headers = ["Content-Type": "application/x-www-form-urlencoded"]

httpClient = HTTP.open(hostname, 443)

def loggedIn = false
def success = false

try {
    loggedIn = login()
    if (loggedIn) {

        def alarms = httpGET("dataservice/alarms/count")

        if (alarms != null) {

            println "count=" + alarms?.data?.last().count
            println "cleared_count=" + alarms?.data?.last().cleared_count

            success = true
        } else {
            println "Could not get response from endpoint: ${url}"
        }
    } else {
        println "Could not log in using endpoint: ${url}"
    }
} catch (Exception e) {
    println e
} finally {
    httpClient.close()
}

return success ? 0 : 1

////////////////// HELPER FUNCTIONS //////////////////

/*
 * This helper function handles our HTTP POST to log in and save a token for subsequent HTTP GETs.
 */

def login() {

    def headers = (["Content-Type": "application/x-www-form-urlencoded"])
    def encodePass = URLEncoder.encode(password, "UTF-8")
    def payload = 'j_username=' + user + '&j_password=' + encodePass

    def postResponse = httpClient.post("${url}/j_security_check", payload, headers)

    if (httpClient.getStatusCode() == 200) {

        def cookie = httpClient.getHeader("Set-Cookie")
        if (cookie != null) {
            headers = ["Content-Type": "application/json", "Accept": "application/json", "Cookie": cookie]
            return true
        }
    } else {
        println "STATUS CODE:\n${httpClient.getStatusCode()}\n\nRESPONSE:\n${httpClient.getResponseBody()}"
    }
    return false
}


/*
 * This helper function handles any HTTP GETs.
 */

def httpGET(endpoint) {

    def get_endpoint = url + endpoint
    httpClient.get(get_endpoint)

    if (httpClient.getStatusCode() == 200) {

        return new JsonSlurper().parseText(httpClient.getResponseBody())
    } else {
        println "STATUS CODE:\n${httpClient.getStatusCode()}\n\nRESPONSE:\n${httpClient.getResponseBody()}"
    }
}