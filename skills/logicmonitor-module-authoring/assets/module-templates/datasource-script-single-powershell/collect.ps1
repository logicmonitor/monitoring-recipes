<#
  Purpose: Single-instance PowerShell DataSource starter.
  Collector output must contain only numeric name=value data.
#>

$hostname = '##system.hostname##'.Trim()
if ([string]::IsNullOrWhiteSpace($hostname) -or $hostname -like '*system.hostname*') {
    Write-Error 'system.hostname is not configured'
    exit 1
}

Write-Output 'example_metric=0'
exit 0
