## Active Discovery

When returning discovered instances, we want to return in the following format, where our WILDVALUE and WILDALIAS are seperated by '##'.
```
Intel[R] 82574L Gigabit Network Connection##Intel[R] 82574L Gigabit Network Connection
```

## Batchscript Collection

When returning batchscript collection output, the output should follow this format: '##WILDVALUE##.datapointName=datapointValue'
```
Intel[R] 82574L Gigabit Network Connection.packetThroughput=118055428
```

[https://www.logicmonitor.com/support/datasources/data-collection-methods/batchscript-data-collection/]