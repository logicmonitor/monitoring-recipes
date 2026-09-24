/*******************************************************************************
 * Purpose: Walk an SNMP OID subtree and emit results via lm.emit
 * Module types: DataSource, PropertySource, Active Discovery
 * Device properties: system.hostname, SNMP v1/v2/v3 credentials
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets
import com.santaba.agent.util.Settings

// --- Snippet bootstrap ---
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def snmpMod = loader.load("proto.snmp", "0")
def emit = loader.load("lm.emit", "0")
def lmDebugMod = loader.load("lm.debug", "2.0.0")

// --- Configuration ---
def debug = false
def lmDebug = lmDebugMod.create(hostProps, debug, out)
def host = hostProps.get("system.hostname")
Map props = hostProps.toProperties().collectEntries { k, v -> [(k.toLowerCase()): v] }
def snmpOid = "INSERT_OID_HERE"
def startTime = System.currentTimeMillis()
def timeout = Settings.getSettingInt("collector.batchscript.timeout",
    Settings.getSettingInt("collector.script.timeout", 120)) * 1000
timeout -= 2500

// --- Main flow ---
def snmp = snmpMod.create(host, props, startTime).withRetries(5)
def walkResult = snmp.walk(snmpOid)

if (!walkResult || walkResult.isEmpty()) {
    lmDebug.warn("SNMP walk returned no results for OID ${snmpOid}")
    return 1
}

walkResult.each { index, value ->
  // DataSource Script: emit.dp("metric_${index}", value)
  // DataSource BatchScript: emit.dp(wildvalue, "metricName", value)
  // PropertySource: emit.property("auto.snmp_${index}", value)
  // Active Discovery: emit.instance(index.toString(), "Instance ${index}", "", [:])
  emit.dp("oid_${index}", value)
}

return 0
