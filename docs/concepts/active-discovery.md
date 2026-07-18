# Active Discovery

Active Discovery is the process LogicMonitor uses to discover and identify **instances** for a multi-instance LogicModule. The result is one or more instances that LogicMonitor can collect data against — for example, disks, network interfaces, databases, or services.

Instances appear under their parent LogicModule in the Resources tree.

## When you need Active Discovery

Use Active Discovery when a LogicModule monitors **multiple similar components** on a device. Single-instance DataSources do not require it.

Common examples:

- Network interfaces on a switch
- Volumes on a storage array
- Windows services or logical disks
- JVMs or database instances

## What Active Discovery collects

For each discovered instance, Active Discovery can retrieve:

| Field | Purpose |
|-------|---------|
| **Instance ID (wildvalue)** | Unique identifier used when querying the device for data — e.g., the variable part of an SNMP OID, or a volume ID |
| **Instance name (wildalias)** | Descriptive name shown in the Resources tree |
| **Instance description** | Optional extra context displayed alongside the name |
| **Instance properties** | Optional key-value pairs stored on the instance — serial numbers, port speed, VM metadata, etc. |

Instance IDs must always be **unique** within the DataSource.

Instance property collection is available with **script**, **SNMP**, or **WMI** Active Discovery methods.

## When Active Discovery runs

Active Discovery runs on a schedule defined in the DataSource definition. Schedules vary by module — stable objects (fans, CPUs) may discover once per day; frequently changing objects (storage volumes) may run several times per hour.

Active Discovery also runs when:

- A resource or DataSource is added into monitoring
- A resource or DataSource's properties or configuration change
- It is manually initiated from the Resources tree (useful when you've added a new object and don't want to wait for the next scheduled run)

**Note:** Disabling DataSource monitoring for a resource or group also disables Active Discovery for that DataSource on those resources — instances will not be discovered, updated, or deleted.

## Portal configuration

Active Discovery is configured per DataSource in **My Module Toolbox**:

1. Select the DataSource and click **Edit**
2. Enable **Multi-Instance** (required — Active Discovery cannot be disabled once saved on a LogicModule)
3. Toggle **Enable Active Discovery**
4. Choose a **Discovery method** (SNMP, WMI, HTTP, JDBC, JMX, Perfmon, Port, Script, etc.)
5. Set the **Discovery schedule** — every 15 minutes, hourly, daily, or only on initial apply / config change

### Key portal options

**Disable Discovered Instances** — New instances start in a disabled state in the "Unmonitored" instance group. Useful when you want to tune thresholds before enabling monitoring and avoid alert floods.

**Automatically Delete Instance** — Removes instances from monitoring when a future Active Discovery pass no longer finds them.

- Turn this **off** if you want alerts to persist when an instance disappears (e.g., a TCP port that stops listening).
- When enabled, choose **Delete Immediately** or **Delete After 30 Days** (history retained briefly; useful when hardware is replaced and you want prior history re-associated on rediscovery).

**Group Method** — How instances are organized after discovery:

- **Manual** — organize yourself, or leave ungrouped
- **Regular Expression** — auto-group by pattern
- **Instance level property** — one group per unique property value

## Discovery methods

| Method | Use for |
|--------|---------|
| SNMP | Walk or query OIDs to enumerate objects |
| WMI | Query Windows WMI classes |
| HTTP | Query a REST API endpoint |
| JDBC | Database query |
| JMX | Java MBeans |
| Perfmon | Windows performance counters |
| Port | TCP port availability |
| **Script** | Custom Groovy, PowerShell, or external script |
| COLLECTOR | Discover Collectors as instances |

For custom logic, use **Script** Active Discovery. See [Script output format](#script-active-discovery-output) below.

## Active Discovery filters

Filters control which discovered objects become monitored instances. When filters are configured, **every** discovered object must satisfy **all** filter criteria (filters are combined with AND).

Use filters to:

- Exclude down interfaces from monitoring
- Apply different thresholds to different instance types
- Exclude test/lab environments via instance-level properties

Filter attributes can reference instance-level properties, SNMP OIDs, or WMI query output fields.

**Troubleshooting tip:** If expected instances are missing, check whether a filter is excluding them. A common SNMP scenario: a volume size filter excludes volumes reporting zero size from a specific OID.

---

## Script Active Discovery output

When using the **Script** discovery method, print one instance per line to stdout.

### Basic format

```
instance1_id##instance1_name
instance2_id##instance2_name
```

### With description

```
instance3_id##instance3_name##instance3_description
```

### With instance properties

```
instance5_id##instance5_name##instance5_description####auto.fooProperty=somevalue&auto.barProperty=anothervalue
```

Multiple properties are separated by `&`. Property names typically use the `auto.` prefix.

### Limits and restrictions

| Constraint | Limit |
|------------|-------|
| Wildvalue character limit | 1024 characters |
| Wildalias (instance name) limit | 255 characters |
| Combined instance property size | 49,000 characters total across all properties per instance |

**Wildvalue must not contain:** `=` , `:`, `\`, `#`, or spaces.

### Return codes and error handling

- Return exit code **0** to indicate successful execution. Active Discovery updates the instance list on success.
- Return a **non-zero** exit code on failure (e.g., SNMP timeout). This prevents Active Discovery from removing existing instances due to a transient error.
- Do not assign instance properties with blank/null values — this creates errors in the discovery task log.
- If output is empty or malformed but exit code is 0, **all previously discovered instances will be removed**.

**Best practice:** Use the **Test Active Discovery** button in the portal to verify your script discovers the instances you expect before deploying.

### Example (Groovy SNMP walk adapted for AD)

```groovy
import com.santaba.agent.groovyapi.snmp.Snmp

def hostname = hostProps.get('system.hostname')
def props = hostProps.toProperties()
def snmp_oid = 'INSERT_OID_HERE'

Snmp.walk(hostname, snmp_oid, props, 10000).eachLine { line ->
    (oid, value) = line.split(/ = /, 2)
    // Emit wildvalue##wildalias — sanitize wildvalue for forbidden characters
    println "${oid}##${value}"
}

return 0
```

---

## Related concepts

- [Collection Modes](collection-modes.md) — how collection scripts run after instances are discovered
- [Output Formats](output-formats.md) — collection script output (different from AD output)

## Official documentation

- [Active Discovery](https://www.logicmonitor.com/support/active-discovery)
- [Script Active Discovery](https://www.logicmonitor.com/support/logicmodules/datasources/active-discovery/script-active-discovery)
