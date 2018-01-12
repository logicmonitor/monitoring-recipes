import com.santaba.agent.groovyapi.snmp.Snmp;

// set hostname variable.
def hostname = hostProps.get("system.hostname")

// Wrap code in try/catch in case execution experiences an error.
try
{
    // OID which contains the serial number of Cisco devices.
    def entPhysicalSerialNum = "1.3.6.1.2.1.47.1.1.1.1.11.1"

    // Initiate SNMP GET command.
    def output = Snmp.get(hostname, entPhysicalSerialNum);

    // Print out the serial number.
    println "auto.Cisco_Serial_Number=" + output
}

// Catch the exception.
catch (Exception e)
{
    // print out the exception.
    println e;
    return 1;
}

// exit code 0
return 0;