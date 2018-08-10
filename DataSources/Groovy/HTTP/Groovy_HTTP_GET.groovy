import com.santaba.agent.groovyapi.http.HTTP
import groovy.json.JsonSlurper

hostname = hostProps.get("system.hostname")
user = hostProps.get("tintri.user")
pass = hostProps.get("tintri.pass")

try
{
    // instantiate an http client object for the target system
    httpClient = HTTP.open(hostname, 443);

    // set the url.
    url = "https://" + hostname + "/<INSERT-API-ENDPOINT>";

    getResponse = httpClient.get(url)

    body = httpClient.getResponseBody();

    response_obj = new JsonSlurper().parseText(body);

    return 0;
}
catch (Exception e)
{
    println e
    return 1
}
finally
{
    // ensure we close our session.
    httpClient.close()
}