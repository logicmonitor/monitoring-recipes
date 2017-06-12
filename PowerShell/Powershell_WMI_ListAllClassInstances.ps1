## Set our hostname variable using token.
$hostname = '##SYSTEM.HOSTNAME##'

## Set our namespace 
$namespace = "root\cimv2"

## Set out WMI class.
$classname = "insert_class_name" ## i.e. 'Win32_DiskPartition'

## List WMI object names available from the specified namespace.
Get-WmiObject -namespace $namespace -ComputerName $hostname -class $classname

## Make sure we fully terminate the script.
exit