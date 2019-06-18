## PowerShell

Here you will find various PowerShell examples and reference documentation to aid in LogicModule development using scripted PowerShell.

- [PluralSight PowerShell Essentials](https://www.pluralsight.com/paths/windows-powershell-essentials)
- [PluralSight Advanced PowerShell](https://www.pluralsight.com/paths/powershell-scripting-and-toolmaking)
- [Microsoft Official Module Browser](https://docs.microsoft.com/en-us/powershell/module/)
- [Books](https://donjones.com/books/)
- [PowerShell For Noobs](https://leanpub.com/powershell-4n00bs)
- [PowerShell.org](https://powershell.org/)
- [PowerShell.org : Youtube videos and tutorials](https://www.youtube.com/powershellorg)
- [Tips & Tricks](https://www.logicmonitor.com/support/terminology-syntax/scripting-support/powershell-tips-tricks/)
- [Embedded PowerShell Scripting](https://www.logicmonitor.com/support/terminology-syntax/scripting-support/embedded-powershell-scripting/)

#### Useful WMI Classes

* Information about the System
    * ```Get-WMIObject -Class Win32_ComputerSystem```

* Information about the BIOS
    * ```Get-WMIObject -Class Win32_BIOS```

* Information about the Motherboard
    * ```Get-WMIObject -Class Win32_Baseboard```

* Information about the CPU
    * ```Get-WMIObject -Class Win32_Processor```

* Information about Logical Drives (Includes mapped drives and I believe PSDrives)
    * ```Get-WMIObject -Class Win32_LogicalDisk```

* Note : You can also use the shortform of the command from:
    * ```Get-WMIObject -Class Win32_ComputerSystem``` to ```gwmi Win32_ComputerSystem```

* Show PowerShell Version
    * 
    ```
    PS C:\> $PSVersionTable.PSVersion

    Major  Minor  Build  Revision
    -----  -----  -----  --------
    4      0      -1     -1
    ```

#### Directory Tree:
```
├── PowerShell Examples
│   ├── Powershell_ActiveDiscovery_Example.ps1
│   └── Powershell_Collection_Example.ps1
├── README.md
└── Templates
    ├── Powershell_WMI_ListAllClassInstances.ps1
    ├── Powershell_WMI_ListAllClasses.ps1
    └── Powershell_WMI_ListAllNamespaces.ps1

2 directories, 6 files
```
