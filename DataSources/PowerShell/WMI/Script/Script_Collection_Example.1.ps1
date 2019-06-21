## Set our hostname variable using token.
$hostname = '##SYSTEM.HOSTNAME##'
$wildvalue = '##WILDVALUE##'

## Set our namespace 
$namespace = "root\cimv2"

## Set your query.
$query = "SELECT * FROM Win32_LogicalDisk WHERE NAME='$wildvalue'"

## Set variable to get out wmi objects
$output = Get-WmiObject -Namespace $namespace -ComputerName $hostname -Query $query

Write-Host "Size=$($output.Size)"
Write-Host "FreeSpace=$($output.FreeSpace)"

## Make sure we fully terminate the script.
exit