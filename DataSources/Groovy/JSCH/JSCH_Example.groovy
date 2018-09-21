import com.jcraft.jsch.JSch

host = hostProps.get("system.hostname")
user = hostProps.get("ssh.user")
pass = hostProps.get("ssh.pass")
port = hostProps.get("ssh.port") ?: 22
cert = hostProps.get("ssh.cert") ?: '~/.ssh/id_rsa'

try
{
    // define the command we want to execute.
    def command = 'cat /proc/stat; echo -n "Cores:";nproc --all; echo -n "load:"; uptime'

    // execute command and save output to variable.
    def command_output = getCommandOutput(command)

    // print output
    println command_output

    /*
    Input all post processing here.
     */

    return 0
}
catch (Exception e)
{
    println "Unexpected Exception : " + e
    return 1;
}

/**
 * Helper method which handles executing the command.
 * @param input_command
 * @return output of command
 */
def getCommandOutput(String input_command)
{
    // instantiate JSCH object.
    def jsch = new JSch()

    // do we have an user and no pass ?
    if (user && !pass)
    {
        // Yes, so lets try connecting via cert.
        jsch.addIdentity(cert)
    }

    // create session.
    def session = jsch.getSession(user, host, port)

    // given we are running non-interactively, we will automatically accept new host keys.
    session.setConfig("StrictHostKeyChecking", "no");

    // is host configured with a user & password?
    if (pass)
    {
        // set password.
        session.setPassword(pass);
    }

    // connect
    session.connect()

    // execute command.
    def channel = session.openChannel("exec")
    channel.setCommand(input_command)

    // collect command output.
    def commandOutput = channel.getInputStream()
    channel.connect()

    def output = commandOutput.text;

    // disconnect
    channel.disconnect()

    return output
}