# Groovy & PowerShell 

Here you will find various Groovy and PowerShell code snippets and examples.

### Active Discovery
Active Discovery is the process by which LogicMonitor determines all of the similar components of a particular type on a given system, what we call DataSource instances. The output of an Active Discovery process is one or more instances for which we can collect a particular type of data.


The output of the Active Discovery script should follow this format:

```
WILDVALUE##WILDALIAS
```

**Note : WILDVALUE should not contain any of the following characters. **
 * Blank spaces ' '
 * Colon ' : '
 * Equals ' = '
 * Backslash ' \ '
 * Hash ' # '
 
```
instance1_id##instance1_name
```

You can optionally add a description to each instance as follows:
```
instance3_id##instance3_name##instance3_description
```

Furthermore, you may collect instance properties with your Active Discovery script using this syntax:
```
instance5_id##instance5_name##instance5_description####auto.fooProperty=somevalue&auto.barProperty=anothervalue
```


For more information on Active Discovery, visit our support site : 
 * [What Is Active Discovery?](https://www.logicmonitor.com/support/datasources/active-discovery/what-is-active-discovery/)
 * [Scripted Active Discovery](https://www.logicmonitor.com/support/datasources/active-discovery/script-active-discovery/)


### Batchscript Collection
With __BATCHSCRIPT__ mode, the collection script is run only once per collection interval -- regardless of how many instances have been discovered. 

This is much more efficient, especially when you have many instances from which you need to collect data. 

In this case, the collection script needs to provide output that looks like:

```
instance1.key1=value1
instance1.key2=value2
instance1.key3=value3

instance2.key1=value1
instance2.key2=value2
instance2.key3=value3

volume1.read_throughput=12345
volume1.write_throughput=12345
volume1.read_latency=67890
volume1.write_latency=67890

volume2.read_throughput=12345
volume2.write_throughput=12345
volume2.read_latency=67890
volume2.write_latency=67890
```

For more information on Batchscript Collection, visit our support site : [Batchscript Collection](https://www.logicmonitor.com/support/datasources/data-collection-methods/batchscript-data-collection/)


#### Script Collection
In standard __SCRIPT__ mode, the collection script is run for each of the DataSource instances at each collection interval. 

This means that in a multi-instance DataSource that has discovered 5 instances, the collection script will be run 5 times at every collection interval. Each of these data collection “tasks” is independent from one another. 

So the collection script would provide output along the lines of:

```
key1=value1
key2=value2
key3=value3

read_throughput=12345
write_throughput=12345
read_latency=67890
write_latency=67890
```

For more information on Script Collection, visit our support site : [Script Collection](https://www.logicmonitor.com/support/datasources/data-collection-methods/scripted-data-collection-overview/)
