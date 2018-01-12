import com.santaba.agent.groovyapi.win32.WMI

// Set hostname variable.
def hostname = hostProps.get("system.hostname");

// Try the following code.
try
{
    // This is our WMI query.
    def wmi_query = 'select * from win32_operatingsystem';

    // instantiate the WMI class object
    def wmi_output = WMI.queryAll(hostname, wmi_query);

    // Print out the output of our WMI query.
    println wmi_output;

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