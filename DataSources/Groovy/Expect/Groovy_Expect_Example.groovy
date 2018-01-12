// Additional examples found on LogicMonitor support site : https://www.logicmonitor.com/support/terminology-syntax/scripting-support/groovyexpect-text-based-interaction/

// import the logicmonitor expect helper class
import com.santaba.agent.groovyapi.expect.Expect;

// get the hostname and credentials from the device property table
hostname = hostProps.get("system.hostname");
userid = hostProps.get("ssh.user");
passwd = hostProps.get("ssh.pass");

// initiate an ssh connection to the host using the provided credentials
ssh_connection = Expect.open(hostname, userid, passwd);

// wait for the cli prompt, which indicates we've connected
ssh_connection.expect("# ");

// send a command to show the tomcat log file size, along with the newline [enter] character
ssh_connection.send("ls -l /usr/local/tomcat/logs/catalina.out\n");

// wait for the cli prompt to return, which indicates the command has completed
ssh_connection.expect("# ");

// capture all the text up to the expected string. this should look something like
// -rw-r--r-- 1 root root 330885412 Jan 11 20:40 /usr/local/tomcat/logs/catalina.out
cmd_output = ssh_connection.before();

// now that we've capture the data we care about lets exit from the cli
ssh_connection.send("exit\n");

// wait until the external process finishes then close the connection
ssh_connection.expectClose();

// now let's iterate over each line of the we collected
cmd_output.eachLine
{ line ->

    // does this line contain the characters "-rw"
    if ( line =~ /\-rw/ )
    {
        // yes -- this is the line containing the output of our ls command
        // tokenize the cmd output on one-or-more whitespace characters
        tokens = line.split(/\s+/);

        // print the 5th element in the array, which is the size
        println tokens[4];
    }
}

// return with a response code that indicates we ran successfully
return (0);