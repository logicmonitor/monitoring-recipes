## Set our hostname variable using token.
$hostname = '##SYSTEM.HOSTNAME##'

## Set our namespace 
$namespace = "root\cimv2"

## Set our WMI class.
$class = "Win32_PerfRawData_Tcpip_NetworkInterface"

## Set variable to get out wmi objects
$output = Get-WmiObject -namespace $namespace -ComputerName $hostname -class $class

## Iterate through each output from our query.
foreach ($instance in $output)
{   
    ## Set variables.
    $name = $instance.Name
    $packetThroughput = $instance.PacketsPersec

    ## Use defined name as our reference wildvalue
    ## print everything as 'wildvalue.key=value' 
    Write-Host "$name.packetThroughput=$packetThroughput"
}

## Make sure we fully terminate the script.
exit