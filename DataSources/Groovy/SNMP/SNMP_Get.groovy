/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/

import com.santaba.agent.groovyapi.snmp.Snmp;

// Set environment variables.
def hostname = hostProps.get('system.hostname');
def props = hostProps.toProperties()
def snmp_timeout = 10000

// Set SNMP OID.
def snmp_oid = 'INSERT_OID_HERE'; // i.e. : 1.3.6.1.2.1.1.2

// Try the following code.
try {
    /*
    The following SNMP GET will handle v1 , v2 and v3. 
    Props contains a map of ALL host properties and the SNMP GET method will automatically
    handle the proper connection based on which SNMP version is configured.
    */

    // Create SNMP GET with defined variables and map.
    sampleGet = Snmp.get(hostname, snmp_oid, props, snmp_timeout);

    // print out the SNMP GET value.
    println sampleGet

    // Successful script execution, return 0;
    return 0;
}

// Catch any exception that may have occurred.
catch (Exception e) {
    // Print the exception.
    println e

    // Exit with return code 1;
    return 1;
}
