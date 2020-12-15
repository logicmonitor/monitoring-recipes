/*******************************************************************************
 *  © 2007-2020 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/

import com.santaba.agent.groovyapi.snmp.Snmp

// set hostname variable.
def hostname = hostProps.get("system.hostname")

// OID which contains the serial number of Cisco devices.
def entPhysicalSerialNum = "1.3.6.1.2.1.47.1.1.1.1.11.1"

// Initiate SNMP GET command.
def output = Snmp.get(hostname, entPhysicalSerialNum)

// Print out the serial number.
println "auto.cisco.serial_number=" + output

// exit code 0
return 0