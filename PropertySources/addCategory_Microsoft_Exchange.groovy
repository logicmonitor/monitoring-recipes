/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/

import com.santaba.agent.groovyapi.win32.WMI

def host = hostProps.get("system.hostname")

try {

    // get a list of running services
    def service_list = WMI.queryAll(host, "select * from win32_service")

    def is_exchange = false

    // enumerate each service as a map
    service_list.each
            { service_map ->

                // enumerate each of the fields in this service map
                service_map.each
                        { key, value ->

                            // is this an exchange service?
                            if ((key == "NAME") && value.contains("MSExchange")) {
                                // yes, flag it
                                is_exchange = true
                            }
                        }
            }

    // did we locate an exchange service?
    if (is_exchange) {
        // yes, add the exchange to system.categories
        println "system.categories=MSExchange"
    }

    return 0

}
catch (Exception e) {
    println e
    return 1
}
