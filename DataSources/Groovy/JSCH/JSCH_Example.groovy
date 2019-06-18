/*******************************************************************************
 * © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/

import com.jcraft.jsch.JSch

def host = hostProps.get("system.hostname")
def user = hostProps.get("ssh.user")
def pass = hostProps.get("ssh.pass")
def port = hostProps.get("ssh.port") ?: 22
def cert = hostProps.get("ssh.cert") ?: '~/.ssh/id_rsa' // cert path may be passed in, otherwise will reference default path.

def session = null;

try {

    // instantiate JSCH object.
    def jsch = new JSch()

    // do we have an user and no pass ?
    if (user && !pass) {

        // Yes, so lets try connecting via cert.
        jsch.addIdentity(cert)
    }

    // create session.
    session = jsch.getSession(user, host, port)

    // given we are running non-interactively, we will automatically accept new host keys.
    session.setConfig("StrictHostKeyChecking", "no")

    // is host configured with a user & password?
    if (pass) {
        // set password.
        session.setPassword(pass)
    }

    // connect
    session.connect()

    // command to output all information about the Linux host.
    def linux_version_cmd = "uname -s; uname -n; uname -r; uname -v; uname -m; uname -p; uname -i; uname -o;"

    // save command output and split on newlines .
    def command_lines = getCommandOutput(session, linux_version_cmd).readLines()

    if (command_lines) {

        // Kernel
        println "auto.linux.kernel.name=${command_lines[0]}"
        println "auto.linux.kernel.release=${command_lines[2]}"
        println "auto.linux.kernel.version=${command_lines[3]}"

        // Hardware
        println "auto.linux.hardware.name=${command_lines[4]}"
        println "auto.linux.hardware.platform=${command_lines[6]}"

        // node, processor and OS.
        println "auto.linux.node.name=${command_lines[1]}"
        println "auto.linux.system.processor.type=${command_lines[5]}"
        println "auto.linux.os.name=${command_lines[7]}"
    }

    // CPU Info
    def cpu_info_command = 'get_cpuinfo() { target_line=$(grep -m1 ^"$1" /proc/cpuinfo); echo "${target_line##*: }";}; get_cpuinfo model\\ name; get_cpuinfo vendor_id; get_cpuinfo cpu\\ MHz; get_cpuinfo cpu\\ cores;'

    def cpu_info_output = getCommandOutput(session, cpu_info_command).readLines()

    if (cpu_info_output) {
        println "auto.processor.model_name=" + cpu_info_output[0]
        println "auto.processor.manufacturer=" + cpu_info_output[1]
        println "auto.processor.max_clock_speed=" + cpu_info_output[2]
        println "auto.processor.cores_count=" + cpu_info_output[3]
    }


    def os_info_command = 'get_osinfo() { target_line=$(grep -m1 ^"$1"= /etc/os-release); echo "${target_line##*=}";}; get_osinfo NAME; get_osinfo PRETTY_NAME; get_osinfo VERSION; get_osinfo VERSION_ID;'
    def os_info_output = getCommandOutput(session, os_info_command).readLines()

    if (os_info_output) {
        println "auto.linux.distro=" + os_info_output[0].replace("\"", "")
        println "auto.os.version.description=" + os_info_output[1].replace("\"", "")
        println "auto.linux.distro.version=" + os_info_output[2].replace("\"", "")
        println "auto.os.version=" + os_info_output[3].replace("\"", "")
    }

    def disk_space_command = 'df --output=\'size\' -l --block-size=g /;df --output=\'size\' -l --block-size=m /;'
    def disk_space_output = getCommandOutput(session, disk_space_command).readLines()

    if (disk_space_output) {
        println "auto.linux.root.disk_space.gb=" + disk_space_output[1].replace("G", "")
        println "auto.linux.root.disk_space.mb=" + disk_space_output[3].replace("M", "")
    }

    return 0
}
finally {
    // ensure we disconnect the session.
    if (session) {
        session.disconnect()
    }
}

/**
 * Helper method for executing commands.
 */
def getCommandOutput(session, input_command) {
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