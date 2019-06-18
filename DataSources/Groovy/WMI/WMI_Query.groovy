/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/

import com.santaba.agent.groovyapi.win32.WMI

// Set hostname variable.
def hostname = hostProps.get("system.hostname");

// Try the following code.
try {

    // This is our namespace, we're using the default value here to illustrate
    // how to pass it if your object isn't in root\cimv2
    // This parameter is optional.
    def namespace = "CIMv2";

    // You can also pass an optional timeout value, the default is 30 seconds
    def timeout = 30;

    // This is our WMI query.
    def wmi_query = 'select * from win32_operatingsystem';

    /*
    - Query All: captures all values of the given query, returns map.
        - WMI.queryAll(hostname, namespace, wmi_query, timeout)

    - Query First: captures only the first output value of the query, returns map.
        - WMI.queryFirst(hostname, namespace, wmi_query, timeout)
     */

    def wmi_output_all = WMI.queryAll(hostname, namespace, wmi_query, timeout);
    def wmi_output_first = WMI.queryFirst(hostname, namespace, wmi_query, timeout);

    // Print out the output of our WMI query.
    println '*** Query all output:'
    println wmi_output_all;

    println '*** Query first output:'
    println wmi_output_first;

    // example of iterating through the map of values.
    wmi_output_all.each {
        k, v ->

            // print k=v
            println "${k}=${v}"
    }

    /*
    - The rest of your post processing code goes here.
    */

    // Exit by returning 0.
    return 0;
}

// Catch any exceptions that may have occurred.
catch (Exception e) {
    // Print exception out.
    println e
    return 1;
}
