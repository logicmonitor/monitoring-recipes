<# © 2007-2019 - LogicMonitor, Inc.  All rights reserved. #>

#######################      Active Discovery      #########################
# Purpose:
# Author:
#------------------------------------------------------------------------------------------------------------
# Prerequisites:
#
#
#Requires -Version 4
#------------------------------------------------------------------------------------------------------------
# Clears the CLI of any text
Clear-Host
# Clears memory of all previous variables
Remove-Variable * -ErrorAction SilentlyContinue
#------------------------------------------------------------------------------------------------------------
# Initialize Variables
$wmi_pass = '##WMI.PASS##'
$wmi_user = '##WMI.USER##'
$hostname = '##SYSTEM.HOSTNAME##'
$collectorName = hostname

# Insert additional variables here

# If the hostname is an IP address query DNS for the FQDN
if ($hostname -match "\b\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}\b") {
    $hostname = [System.Net.Dns]::GetHostbyAddress($hostname).HostName
}

## This script block should contain all code that you want to execute remotely on the target host.
$scriptBlock = {

    # Insert Active Discovery script block here

}

#------------------------------------------------------------------------------------------------------------

try {
    #-----Determin the type of query to make-----
    # check to see if this is monitoring the localhost collector, as we will not need to authenticate.
    if ($hostname -like $collectorName) {
        $response = Invoke-Command -ScriptBlock $scriptBlock
    }
    # are wmi user/pass set -- e.g. are these device props either not substiuted or blank
    elseif (([string]::IsNullOrWhiteSpace($wmi_user) -and [string]::IsNullOrWhiteSpace($wmi_pass)) -or (($wmi_user -like '*WMI.USER*') -and ($wmi_pass -like '*WMI.PASS*'))) {
        # no
        $response = Invoke-Command -ComputerName $hostname -ScriptBlock $scriptBlock
    } else {
        # yes. convert user/password into a credential string
        $remote_pass = ConvertTo-SecureString -String $wmi_pass -AsPlainText -Force;
        $remote_credential = New-Object -typename System.Management.Automation.PSCredential -argumentlist $wmi_user, $remote_pass;
        $response = Invoke-Command -ComputerName $hostname -Credential $remote_credential -ScriptBlock $scriptBlock
    }
    exit 0
} catch {
    # exit code of non 0 will mean the script failed and not overwrite the instances that have already been found
    throw $Error[0].Exception
    exit 1
}
