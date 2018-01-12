import com.santaba.agent.collector3.CollectorDb
import com.santaba.agent.groovyapi.snmp.Snmp

// variable to hold system hostname
def host = hostProps.get('system.hostname');

/*
 The following variable will grab all necessary SNMP properties to initiate a walk.
 Compatible with SNMP v1, v2 and v3
 */
def props = hostProps.toProperties().findAll { it.key.contains('snmp') };

def timeout = 10000 // 10 sec timeout.

// define maps we will walk.
def exampleNameWalkAsMap = Snmp.walkAsMap(host, "INSERT_OID_HERE", props, timeout);
def exampleDataWalkAsMap = Snmp.walkAsMap(host, "INSERT_OID_HERE", props, timeout);

// Throw into a try/catch
try
{
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

