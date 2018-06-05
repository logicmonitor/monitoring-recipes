// Lists all of the supported SMI-S classes, and their properties.

// You can set 'filter' to a complete or partial Class name to restrict.
// Regardless of the filter, a complete list of Classes (only) will be listed
// after the results for easier review.

import javax.cim.*
import javax.security.auth.Subject
import javax.wbem.*
import javax.wbem.client.*

def hostname = hostProps.get("system.hostname"); // IP of the Data Collector, Enterprise Manager
def cim_port = hostProps.get("cim.port");
def cim_user = hostProps.get("cim.user")
def cim_pass = hostProps.get("cim.pass")
def namespace = hostProps.get("cim.namespace")

// Set the filter to a class to only show properties for that one.
def filter = ""; // "CMPL_StorageConfigurationService"

/////////////////////////////////////////
def classes = []; // Build a list of classes to show at end.

URL cimomUrl = new URL("https://" + hostname + ":" + cim_port);

final WBEMClient client = WBEMClientFactory.getClient(WBEMClientConstants.PROTOCOL_CIMXML);
final CIMObjectPath path = new CIMObjectPath(cimomUrl.getProtocol(), cimomUrl.getHost(), String.valueOf(cimomUrl.getPort()), null, null, null);
final Subject subject = new Subject();
subject.getPrincipals().add(new UserPrincipal(cim_user));
subject.getPrivateCredentials().add(new PasswordCredential(cim_pass));

try
{
    client.initialize(path, subject, Locale.getAvailableLocales());
}
catch (Exception e)
{
    e.printStackTrace();
    print e.toString();
}

println "All Class objects and properties, unless 'filter' set: "

try
{
    final CloseableIterator<CIMClass> iterator = client.enumerateClasses(new CIMObjectPath("", namespace), true, false, false, true);
    try
    {
        while (iterator.hasNext())
        {
            final CIMClass objIter = iterator.next();
            //println (objIter.toString());

            className = objIter.getObjectPath().toString();
            // Store the class for later.
            classes << className;

            // If filtered, only show that class and it's properties.
            if (filter == "" || className.toLowerCase().contains(filter.toLowerCase()))
            {
                println objIter.getObjectPath();

                final Iterator iterator2 = objIter.getProperties().iterator();

                while (iterator2.hasNext())
                {
                    final CIMClassProperty propIter = iterator2.next();
                    println('\t' + propIter.getName());
                }
            }
        }
    }
    finally
    {
        iterator.close();
    }
}
catch (WBEMException e)
{
    e.printStackTrace();
    print e.toString();
}

// At the end, prints out just the classes for easier review.
println "All classes: \n";

classes.sort().each
{ it ->

    print it + "\n";
};

return 0;