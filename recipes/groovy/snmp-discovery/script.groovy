/*******************************************************************************
 * Purpose: SNMP walk Active Discovery — emit instances via lm.emit
 * Module types: Active Discovery (DataSource)
 * Device properties: system.hostname, SNMP v1/v2/v3 credentials
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
def nameOid = "INSERT_NAME_OID_HERE"
def extraOid = "INSERT_OPTIONAL_PROPERTY_OID_HERE"
def startTime = System.currentTimeMillis()
def timeout = Settings.getSettingInt("collector.batchscript.timeout",
    Settings.getSettingInt("collector.script.timeout", 120)) * 1000
timeout -= 2500

// --- Main flow ---
def snmp = snmpMod.create(host, props, startTime).withRetries(5)
def names = snmp.walk(nameOid)

if (!names || names.isEmpty()) {
    debugPrint("SNMP walk returned no instances for OID ${nameOid}")
    return 1
}

def extras = [:]
if (extraOid && !extraOid.startsWith("INSERT_")) {
    extras = snmp.walk(extraOid) ?: [:]
}

names.each { index, alias ->
    def wildvalue = index.toString()
    def wildalias = (alias ?: wildvalue).toString()
    def description = ""
    def ilps = [:]
    if (extras[index] != null) {
        ilps["auto.discovered.value"] = extras[index].toString()
    }
    emit.instance(wildvalue, wildalias, description, ilps)
}

return 0

// --- Helpers ---
def debugPrint(message) {
    if (debug) println "[DEBUG] ${message}"
}
