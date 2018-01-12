## Set our hostname variable using token.
$hostname = '##SYSTEM.HOSTNAME##'

## List all exposed Namespace names available on the specified host.  
Get-WmiObject -namespace "root" -class "__Namespace" -ComputerName $hostname | Select-Object -Property Name

## Make sure we fully terminate the script.
exit