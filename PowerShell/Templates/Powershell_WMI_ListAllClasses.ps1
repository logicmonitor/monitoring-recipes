## Set our hostname variable using token.
$hostname = '##SYSTEM.HOSTNAME##'

## Set our namespace 
$namespace = "root\cimv2"

## List WMI object names available from the specified namespace.
Get-WmiObject -namespace $namespace -ComputerName $hostname -list | Select Name

## Make sure we fully terminate the script.
exit