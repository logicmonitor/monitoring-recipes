/*******************************************************************************
 * Purpose: JDBC database query via lm.sql
 * Module types: DataSource
 * Device properties: jdbc.user, jdbc.pass, jdbc.url
 ******************************************************************************/

import com.santaba.agent.groovy.utils.GroovyScriptHelper as GSH
import com.logicmonitor.mod.Snippets

// --- Snippet bootstrap ---
def loader = GSH.getInstance(GroovySystem.version)
    .getScript("Snippets", Snippets.getLoader())
    .withBinding(getBinding())
def sql = loader.load("lm.sql", "0")
def emit = loader.load("lm.emit", "0")
def lmDebugMod = loader.load("lm.debug", "2.0.0")

// --- Configuration ---
def debug = false
def lmDebug = lmDebugMod.create(hostProps, debug, out)
def user = hostProps.get("jdbc.user")
def pass = hostProps.get("jdbc.pass")
def jdbcUrl = hostProps.get("jdbc.url", "INSERT_JDBC_URL_HERE")
def query = "INSERT_SQL_QUERY_HERE"

// --- Main flow ---
Map conn = sql.attemptConnection(user, pass, jdbcUrl)
if (conn.status != "success") {
    emit.dp("error", conn.errors?.join(",") ?: "connection_failed")
    return 1
}

Map result = sql.runQuery(query, conn.connection)

try {
    if (result.status == "success") {
        result.data.each { row ->
            // Customize: map columns to datapoints
            // DataSource Script: emit.dp("columnName", row.columnName)
            // BatchScript: emit.dp(wildvalue, "columnName", row.columnName)
            row.each { col, val ->
                emit.dp(col.toString(), val)
            }
        }
    } else if (result.status == "no data") {
        lmDebug.info("Query returned no rows")
    } else {
        emit.dp("error", result.error)
        return 1
    }
} finally {
    conn.connection?.close()
}

return 0
