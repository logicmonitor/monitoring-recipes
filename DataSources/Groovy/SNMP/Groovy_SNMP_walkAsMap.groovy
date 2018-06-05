import com.santaba.agent.groovyapi.snmp.Snmp

// variable to hold system hostname
def host = hostProps.get('system.hostname');
def props = hostProps.toProperties()
def timeout = 10000 // 10 sec timeout.

// Throw into a try/catch
try
{
    /*
    The following SNMP walkAsMap will handle v1 , v2 and v3. 
    Props contains a map of ALL host properties and the SNMP walk method will automatically
    handle the proper connection based on which SNMP version is configured.
    */

    // define maps we will walk.
    def exampleNameWalkAsMap = Snmp.walkAsMap(host, "INSERT_OID_HERE", props, timeout);
    def exampleDataWalkAsMap = Snmp.walkAsMap(host, "INSERT_OID_HERE", props, timeout);

    exampleNameWalkAsMap.each
    { key, val ->

        /*
        Insert your additional data processing here.

        Example:
            - If you have multiple maps, you can easily
            correlate data between them by referencing the key.
         */

        println "exampleData=" + exampleDataWalkAsMap[key]

    }

    // execution was successful, return 0;
    return 0;
}

catch (Exception e)
{
    // if exception is caught, print it out and return 1;
    println e;
    return 1;
}

