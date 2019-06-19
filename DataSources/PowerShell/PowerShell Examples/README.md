## PowerShell Examples



#### Active Discovery
The purpose of Active Discovery is to simply discover our instances for which we would like to collect data for (Disks, Volumes, Interfaces, Power Supplies, etc).

The output of the Active Discovery script should follow this format:

```
WILDVALUE##WILDALIAS
```



**Note : The wildvalue should not contain any of the following characters. **
 * Blank spaces ' '
 * Colon ' : '
 * Equals ' = '
 * Backslash ' \ '
 
```
Intel[R]_82574L_Gigabit_Network_Connection##Intel[R] 82574L Gigabit Network Connection
```


#### Batchscript Collection
In most cases, batchscript collection method is the preferred method of gathering data within our collection script.

Batchscript has a slightly different output format, which allows it to be quite a bit more efficient 

If you compare the output of batchscript vs script, you will see the major difference 


#### Script Collection

When returning batchscript collection output, the output should follow this format: '##WILDVALUE##.datapointName=datapointValue'
```
Intel[R]_82574L_Gigabit_Network_Connection.packetThroughput=118055428
```


[Additional Examples and Support Documentation](https://www.logicmonitor.com/support/datasources/data-collection-methods/batchscript-data-collection/)
