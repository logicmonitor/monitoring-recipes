/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/


import com.santaba.agent.groovyapi.expect.Expect;

def show_commands
def configs = [:]
def desired_show_commands = [
        "running-config",
        "startup-config",
        "version",
        "inventory"
]
def host, user, pass        // These will get set further down in the script.
def enable_pass = null      // If the user has specified an enable password, this will get set further down.
def priv_exec_mode = false  // Notes what EXEC mode we are in.  Initially we assume USER EXEC.
try {
    host = hostProps.get("system.hostname")
}
catch (all) {
    println "(debug::fatal) Could not retrieve the system.hostname value.  Exiting."
    return 1
}
try {
    user = hostProps.get("ssh.user")
}
catch (all) {
    println "(debug::fatal) Could not retrieve the ssh.user device property.  This is required for authentication.  Exiting."
    return 1
}
try {
    pass = hostProps.get("ssh.pass")
}
catch (all) {
    println "(debug::fatal) Could not retrieve the ssh.pass device property.  This is required for authentication.  Exiting."
    return 1
}
try {
    enable_pass = hostProps.get("ssh.enable.pass")
}
catch (all) {
    println "(debug) Could not retrieve the ssh.enable.pass device property.  This is most likely fine, and implies that the" +
            " provided user already has sufficient default privileges."
}
// open an ssh connection and wait for the prompt
cli = Expect.open(host, user, pass);
cli.expect(">", "#");
// Check to see what the previous expect command matched.  This will us which user mode we have been dropped into.
if (cli.matched() == "#") {
    priv_exec_mode = true
}
// Let's determine the console prompt, sans the exec mode identifier.
def prompt = ""
cli.before().eachLine() { line -> prompt = line }
// If we are not in privileged exec mode, we need to be in order to show the configurations.
if (!priv_exec_mode) {
    // We need privileged exec mode in order to grab the config.
    cli.send("enable\n")
    // Next check for the Password: prompt.  If we get a timeout exception thrown, then something with the enable step failed.
    try {
        cli.expect("Password:")
    }
    catch (TimeoutException) {
        println "(debug::fatal) Timed out waiting for the Password prompt.  Exiting."
        return 1
    }
    // If we made it this far, we have received the prompt for a password.  If an enable_password has been specified, use it.
    if (enable_pass) {
        cli.send("${enable_pass}\n")
    } else {
        cli.send("${pass}\n")
    }
    try {
        cli.expect("#")
    }
    catch (TimeoutException) {
        println "(debug::fatal) Timed out waiting for PRIV EXEC prompt (#) after providing the enable password.  Exiting."
        return 1
    }
    catch (all) {
        println "(debug:fatal) Something occurred while waiting for the PRIV EXEC prompt.  Exiting."
        println "${all.getMessage()}"
        return 1
    }
    priv_exec_mode = true
}
// ensure the page-by-page view doesn't foul the config output
cli.send("terminal pager 0\n");
cli.expect("#");
cli.send("show running-config terminal\n")
cli.expect("#")
def terminal_width = cli.before()
// display the config
cli.send("show ?");
cli.expect("# show")
show_commands = cli.before()
// logout from the device
cli.send("\nexit\n");
cli.expect("# exit");
// close the ssh connection handle then print the config
cli.expectClose();
show_commands.eachLine { entry ->
    if (!entry.trim().startsWith("show ?") && !entry.trim().startsWith(prompt) && !entry.isEmpty()) {
        if (entry.trim().tokenize().size() >= 2) {
            def cmd = entry.trim().tokenize()[0]
            def desc = entry.trim().tokenize()[1..-1].join(" ")
            if (desired_show_commands.contains(cmd.toString())) {
                configs << ["${cmd}": "${desc}"]
            }
        }
    }
}
configs.each { config -> println "${config.key}##${config.key}##${config.value}" }
return 0;