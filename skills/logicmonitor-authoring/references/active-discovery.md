# Active Discovery

See also: [docs/concepts/active-discovery.md](../../../docs/concepts/active-discovery.md)

## Output format

One instance per line:

```
WILDVALUE##WILDALIAS
```

With description:

```
WILDVALUE##WILDALIAS##DESCRIPTION
```

With instance properties:

```
WILDVALUE##WILDALIAS##DESCRIPTION####auto.foo=bar&auto.baz=qux
```

## Limits

| Constraint | Limit |
|------------|-------|
| Wildvalue | 1024 chars; no `=`, `:`, `\`, `#`, or spaces |
| Wildalias | 255 chars |
| Instance properties (combined) | 49,000 chars total |

## Return codes

- **0** = success; instance list updates
- **Non-zero** = failure; existing instances preserved (use on SNMP timeout, etc.)
- Empty/malformed output + exit 0 = **all instances removed** — dangerous

Snippet-based AD must use `.withBinding(getBinding())` before `emit.instance`.
Without it the script exits 0 with no lines and LogicMonitor deletes every
instance. Transient failures must `return 1` **and** `println` the reason
(debug-gated prints are invisible in the debug pane).

Prefer `emit.instance(wildvalue, alias, description, ilpMap)` over hand-built
`##` lines so wildvalues and ILPs are sanitized.

Never assign instance properties with blank values.

## Discovery methods

SNMP, WMI, HTTP, JDBC, JMX, Perfmon, Port, Script, COLLECTOR.

For custom enumeration, use Script AD. Reuse protocol recipes but change output to `wildvalue##wildalias`.

## Official docs

- https://www.logicmonitor.com/support/active-discovery
- https://www.logicmonitor.com/support/logicmodules/datasources/active-discovery/script-active-discovery
