/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/

import netapp.manage.NaElement
import netapp.manage.NaException
import netapp.manage.NaServer

// set variables.
hostname = hostProps.get("system.hostname");
username = hostProps.get("netapp.user");
password = hostProps.get("netapp.pass");

// Try the connection.
try {

    // instantiate a connection
    // NOTE : Example OnTAP version here is v1.31.
    NaServer s = new NaServer(hostname, 1, 31);

    s.setServerType(NaServer.SERVER_TYPE_FILER);
    s.setTransportType(NaServer.TRANSPORT_TYPE_HTTPS);
    s.setPort(443); // Port 443 used for HTTPS
    s.setStyle(NaServer.STYLE_LOGIN_PASSWORD);
    s.setAdminUser(username, password);

    // pass the follow api command.
    NaElement api = new NaElement("snapshot-get-iter");

    // limit output to 5000 records.
    api.addNewChild("max-records", "5000");

    // invoke the command.
    NaElement xo = s.invokeElem(api);

    // print it out .
    println xo.toPrettyString("");

    /*
    The output can be easily passed into XmlSlurper for easier parsing.
     */

    // Exit code 0
    return 0;
}

// NetApp Server Exception
catch (NaException e) {
    handleException(e);
    return 1
}

// Unknown host exception.
catch (UnknownHostException e) {
    handleException(e);
    return 1
}

// IO Exceptions.
catch (IOException e) {
    handleException(e);
    return 1
}
