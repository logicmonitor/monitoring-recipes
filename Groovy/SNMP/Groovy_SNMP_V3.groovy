import com.santaba.agent.groovyapi.snmp.Snmp;

// Scripted Groovy SNMP V3 Example

// Set SNMP environment variables.
def snmp_host      = hostProps.get('system.hostname');
def snmp_security  = hostProps.get('snmp.security');
def snmp_privtype  = hostProps.get('snmp.priv');
def snmp_authtype  = hostProps.get('snmp.auth');
def snmp_version   = hostProps.get('snmp.version');
def snmp_authtoken = hostProps.get('snmp.authtoken');
def snmp_privtoken = hostProps.get('snmp.privtoken');
def snmp_timeout   = 3000 // SNMP Timeout in milliseconds.

// Set SNMP OID.
def snmp_oid       = 'INSERT_OID_HERE'; // i.e. : 1.3.6.1.2.1.1.2

// Create parameters map which wiil be passed in during the SNMP walk.
def snmp_parameters_map = [:]
snmp_parameters_map["snmp.version"]   = snmp_version;
snmp_parameters_map["snmp.security"]  = snmp_security;
snmp_parameters_map["snmp.auth"]      = snmp_authtype;
snmp_parameters_map["snmp.authToken"] = snmp_authtoken;
snmp_parameters_map["snmp.priv"]      = snmp_privtype;
snmp_parameters_map["snmp.privToken"] = snmp_privtoken;

// Create SNMP walk with defined variables and map.
snmp_out = Snmp.walk(snmp_host, snmp_oid, snmp_parameters_map, snmp_timeout);

// Try the following code.
try
{
    // Iterate through each line of the snmp output
    snmp_out.eachLine()
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
