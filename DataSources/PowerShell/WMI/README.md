## PowerShell WMI

Here you will find various useful PowerShell snippets pertaining to WMI.

#### Useful / Common WMI Classes
* System Info: ```Get-WMIObject -Class Win32_ComputerSystem```
* BIOS: ```Get-WMIObject -Class Win32_BIOS```
* Motherboard: ```Get-WMIObject -Class Win32_Baseboard```
* CPU: ```Get-WMIObject -Class Win32_Processor```
* Disks: ```Get-WMIObject -Class Win32_LogicalDisk```


#### Find version of PowerShell
* Show PowerShell Version
    * 
    ```
    PS C:\> $PSVersionTable.PSVersion

    Major  Minor  Build  Revision
    -----  -----  -----  --------
    4      0      -1     -1
    ```
