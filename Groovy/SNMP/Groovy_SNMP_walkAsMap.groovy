import com.santaba.agent.groovyapi.snmp.Snmp

def hostname = hostProps.get("system.hostname");
def community = hostProps.get("snmp.community");
def version = hostProps.get("snmp.version");

// Set our OID.
def snmp_oid = 'INSERT_OID_HERE' // i.e. : 1.3.6.1.2.1.1.2

def snmp_walkAsMap = Snmp.walkAsMap(hostname, snmp_oid, [:])

// Try following code
try
{
    // Execute the snmpwalk and iterate through each line.
    snmp_walkAsMap.each
    { key, value ->

        println "${key}=${value}"

        /*
        Data post processing code goes here.
         */
    }
}

// Catch any exception that may have occurred.
catch (Exception e)
{
    // Print the exception.
    println e

    // Exit with return code 1;
    return 1;
}

// Successful script execution, return 0;
return 0;