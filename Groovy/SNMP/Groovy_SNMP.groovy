import com.santaba.agent.groovyapi.snmp.Snmp;

// Define the following variables.
def hostname         = hostProps.get("system.hostname");
def community_string = hostProps.get("snmp.community");
def snmp_version     = hostProps.get('snmp.version');
def timeout          = 3000; // Timeout in milliseconds

/*
Accepted SNMP parameters.
1. Snmp.walk(hostname, snmp_oid);
2. Snmp.walk(hostname, community_string, snmp_version, snmp_oid);
3. Snmp.walk(hostname, community_string, snmp_version, snmp_oid, timeout);
*/

// SNMP OID
def snmp_oid = 'INSERT_OID_HERE' // i.e. : 1.3.6.1.2.1.1.2

// Create SNMP walk.
def snmp_walk = Snmp.walk(hostname, community_string, snmp_version, snmp_oid, timeout);

// Try following code
try
{
    // Execute the snmpwalk and iterate through each line.
    snmp_walk.eachLine()
    { line ->

        // split on the '=' sign and get the oid and value.
        (oid, value) = line.split(/ = /, 2);

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
}

// Successful script execution, return 0;
return 0;