import org.jboss.remotingjmx.*

import javax.management.remote.JMXConnectorFactory
import javax.management.remote.JMXServiceURL

/*
The JMX MBean Enumerator mimics the bean enumeration capabilities of JConsole, 
but in groovy for non-gui access. 

In addtion to host/port/user/pass, you'll need to configure the JMX type and 
protocol to corresponding how your application server exposes JMX.

Tomcat:
  my_type  = 'rmi'
  my_proto = 'jmxrmi'

JBoss: 
  my_type  = 'remoting-jmx'
  my_proto = ''

WebSphere:
  my_type  = 'iiop'
  my_proto = 'jndi/JMXConnector'

WebLogic:
  my_type  = 'iiop'
  my_proto = 'jndi/weblogic.management.mbeanservers.runtime'
*/


my_host = hostProps.get("system.hostname")
my_port = hostProps.get("jmx.port")
my_user = hostProps.get("jmx.user")
my_pass = hostProps.get("jmx.pass")

my_type = 'remoting-jmx'
my_proto = ''

service_url = 'service:jmx:' + my_type + '://' + my_host + ':' + my_port + '/' + my_proto
println "JMX Service URL: ${service_url}"

jmx_url = new JMXServiceURL(service_url)
connection = JMXConnectorFactory.connect(jmx_url, ['jmx.remote.credentials': [my_user, my_pass] as String[]])

// get the MBeanServer
def mbeanServer = connection.MBeanServerConnection
println "Total MBeans: ${mbeanServer.MBeanCount}"

try
{
    // get a list of bean names from server
    mbeanNames = mbeanServer.queryNames(null, null)
}
catch (Exception mbeanException)
{
    println "Oops! Mbean Query Names Exception: " + mbeanException;
}

// iterate over bean names
mbeanNames.each
{ beanName ->

    // init some variables
    def mbeanObject = null;
    def mbeanAttributeValues = null;
    def mbeanAttributeNames = null;

    println "\n==========\nBEAN NAME: " + beanName

    try
    {
        // get a list of bean objects
        mbeanObject = new GroovyMBean(mbeanServer, beanName)
    }
    catch (Exception mbeanException)
    {
        println "Oops! Mbean Exception: " + mbeanException;
    }

    try
    {
        // get a list of bean attribute names
        mbeanAttributeNames = mbeanObject.listAttributeNames();
    }
    catch (Exception namesException)
    {
        println "Oops! Attr Name Exception: " + namesException;
    }

    try
    {
        // get a list of bean attribute values
        mbeanAttributeValues = mbeanObject.listAttributeValues();
    }
    catch (Exception valuesException)
    {
        println "Oops! Attr Value Exception: " + valuesException;
    }

    // Iterate over this beans attributes
    mbeanAttributeNames.each
    { attrName ->

        println "  ATTRIBUTE:     ${attrName}";
        println "  DESCRIPTION:   ${mbeanObject.describeAttribute(attrName)}";

        // are there values for this attribute?
        if (mbeanAttributeValues)
        {
            // yes. iterate over each attr value
            mbeanAttributeValues.each
            { value ->

                // separate name & value
                (x, y) = value.split(" :");

                // is this the value that corresponds to this attr name?
                if (x == attrName)
                {
                    // yes. print it
                    println "  VALUE: ${y}";
                }
            }
        }
        else
        {
            print "  NO VALUE"
        }

        print "\n"
        print "----------\n"
    }
}

println "DONE!"

return 0;