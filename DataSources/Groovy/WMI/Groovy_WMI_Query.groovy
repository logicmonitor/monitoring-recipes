import com.santaba.agent.groovyapi.win32.WMI

// Set hostname variable.
def hostname = hostProps.get("system.hostname");

// Try the following code.
try
{
    // This is our namespace, we're using the default value here to illustrate
    // how to pass it if your object isn't in root\cimv2
    // This parameter is optional.
    def namespace = "CIMv2";

    // This is our WMI query.
    def wmi_query = 'select * from win32_operatingsystem';

    // You can also pass an optional timeout value, the default is 30 seconds
    def timeout = 30;

    // instantiate the WMI class object
    def wmi_output = WMI.queryAll(hostname, namespace, wmi_query, timeout);

    // maybe you only want the first result (same parameters as queryAll())
    def first_result = WMI.queryFirst(hostname, namespace, wmi_query, timeout)

    // Print out the output of our WMI query.
    println wmi_output;
    println '-----'
    println first_result;

    /*
    The rest of your post processing code goes here.
    */
    
}

// Catch any exceptions that may have occurred.
catch (Exception e)
{
    // Print exception out.
    println e
    return 1;
}

// Exit by returning 0.
return 0;