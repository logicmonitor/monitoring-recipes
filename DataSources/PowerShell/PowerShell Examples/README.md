## Active Discovery

When returning discovered instances, we want to return in the following format, where our WILDVALUE and WILDALIAS are seperated by '##'. 

Note : Wildvalues should NOT have spaces in them. A common way to address this is to santize the whitespaces with underscores. 
```
Intel[R]_82574L_Gigabit_Network_Connection##Intel[R] 82574L Gigabit Network Connection
```

## Batchscript Collection

When returning batchscript collection output, the output should follow this format: '##WILDVALUE##.datapointName=datapointValue'
```
Intel[R]_82574L_Gigabit_Network_Connection.packetThroughput=118055428
```


[Additional Examples and Support Documentation](https://www.logicmonitor.com/support/datasources/data-collection-methods/batchscript-data-collection/)
