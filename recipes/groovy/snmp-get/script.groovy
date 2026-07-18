/*******************************************************************************
 * Purpose: SNMP GET by OID (optionally with instance index) via proto.snmp
 * Module types: DataSource, PropertySource
 * Device properties: system.hostname, SNMP credentials
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import com.santaba.agent.util.Settings

// --- Snippet bootstrap ---
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
def snmpMod = loader.load("proto.snmp", "0")
def emit = loader.load("lm.emit", "0")

// --- Configuration ---
debug = false
def host = hostProps.get("system.hostname")
Map props = hostProps.toProperties().collectEntries { k, v -> [(k.toLowerCase()): v] }
def snmpOid = "INSERT_OID_HERE"
def instanceIndex = instanceProps?.get("wildvalue") ?: "INSERT_INSTANCE_INDEX_HERE"
def startTime = System.currentTimeMillis()
def timeout = Settings.getSettingInt("collector.batchscript.timeout",
    Settings.getSettingInt("collector.script.timeout", 120)) * 1000
timeout -= 2500

// --- Main flow ---
def snmp = snmpMod.create(host, props, startTime).withRetries(5)
def oidToGet = "${snmpOid}.${instanceIndex}"
def value = snmp.get(oidToGet)

if (value == null) {
    debugPrint("SNMP GET returned null for OID ${oidToGet}")
    return 1
}

// DataSource Script: emit.dp("metricName", value)
// DataSource BatchScript: emit.dp(wildvalue, "metricName", value)
// PropertySource: emit.property("auto.metricName", value)
emit.dp("metricName", value)

return 0

// --- Helpers ---
def debugPrint(message) {
    if (debug) println "[DEBUG] ${message}"
}
