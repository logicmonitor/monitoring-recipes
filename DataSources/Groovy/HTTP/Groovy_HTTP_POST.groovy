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

    // set POST headers.
    post_headers =
    [
    "Content-Type": "application/json"
    ]

    // set Payload
    payload = '<INSERT-PAYLOAD>';

    // do the post and collect the response
    postResponse = httpClient.post(url, payload, post_headers);

    // does the response indicate a successful authentication?
    if (!(httpClient.getStatusCode() =~ /200/))
    {
        // no -- report an error, and return a non-zero exit code
        println "authentication failure";
        return (1);
    }

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