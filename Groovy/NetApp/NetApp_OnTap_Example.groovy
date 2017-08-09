import com.santaba.agent.groovyapi.http.*
import com.santaba.agent.groovyapi.jmx.*
import netapp.manage.NaElement
import netapp.manage.NaException
import netapp.manage.NaServer
import org.xbill.DNS.*

// set variables.
hostname   = hostProps.get("system.hostname");
username   = hostProps.get("netapp.user");
password   = hostProps.get("netapp.pass");

// Try the connection.
try
{
    // instantiate a connection
    NaServer s = new NaServer(hostname, 1 , 31);
    s.setServerType(NaServer.SERVER_TYPE_FILER);
    s.setTransportType(NaServer.TRANSPORT_TYPE_HTTPS);
    s.setPort(443);
    s.setStyle(NaServer.STYLE_LOGIN_PASSWORD);
    s.setAdminUser(username, password);

    // pass the follow api command.
    NaElement api = new NaElement("snapshot-get-iter");

    // limit output to 5000 records.
    api.addNewChild("max-records","5000");

    // invoke the command.
    NaElement xo = s.invokeElem(api);

    // print it out .
    println xo.toPrettyString("");
}

// Netapp Server Exception
catch (NaException e)
{
    handleException(e);
}

// Unknown host exception.
catch (UnknownHostException e)
{
    handleException(e);
}

// IO Exceptions.
catch (IOException e)
{
    handleException(e);
}

// Exit code 0
return 0;