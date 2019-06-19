## Groovy HTTP Examples

[LogicMonitor : Groovy HTTP Support Documentation](https://www.logicmonitor.com/support/terminology-syntax/scripting-support/access-a-website-from-groovy/)

#### Methods

Class: `com.santaba.agent.groovyapi.http.HTTP`

Constructor: `HTTP()`

##### OPEN SESSION
 - `open(java.lang.String host, int port)` - instantiate a non-ssl HTTP session object for use across multiple HTTP transactions
    Will try to extract http credentials from the related hostProps if existed

 - `open(java.lang.String host, int port, boolean ssl)` - instantiate an HTTP session object for use across multiple HTTP transactions
    Will try to extract http credentials from the related hostProps if existed

##### POST

 - `post(java.lang.String url, java.lang.String data)` - do an HTTP POST to the provided url and return the entire response

 - `post(java.lang.String url, java.lang.String data, java.util.Map headers)` - do an HTTP POST to the provided url and return the entire response

##### GET

 - `get(java.lang.String url)` - do an HTTP GET on the provided url and return the entire HTTP response
 
 - `get(java.lang.String url, java.util.Map headers)` - do an HTTP GET on the provided url and return the entire HTTP response
 
 - `get(java.lang.String url, java.lang.String username, java.lang.String password)` - do an HTTP GET on the provided url and return the entire HTTP response

 - `get(java.lang.String url, java.lang.String username, java.lang.String password, java.util.Map headers)` - do an HTTP GET on the provided url and return the entire HTTP response

##### BODY (HTTP GET)

 - `body(java.lang.String url)` - do an HTTP GET on the provided url and return only the HTTP body

 - `body(java.lang.String url, java.lang.String username, java.lang.String password)` - do an HTTP GET on the provided url and return only the HTTP body

##### HEADERS (HTTP GET)

 - `headers(java.lang.String url)` - do an HTTP GET on the provided url and return only the HTTP headers

 - `headers(java.lang.String url, java.lang.String username, java.lang.String password)` - do an HTTP GET on the provided url and return only the HTTP headers



