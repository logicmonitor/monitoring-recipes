# jdbc

Query a database via JDBC using the `lm.sql` snippet.

## What this script does

Connects to a database using JDBC and executes a SQL query, mapping result columns to datapoints via `lm.emit`.

## Prerequisites

- **LogicMonitor_Collector_Snippets** module installed on the collector
- Monitoring enabled on the **Collector host resource**
- JDBC driver available on the collector for your database type
- Database reachable from collector

## Required device properties

| Property | Required | Description |
|----------|----------|-------------|
| `jdbc.user` | Yes | Database username |
| `jdbc.pass` | Yes | Database password |
| `jdbc.url` | Yes | JDBC URL (e.g. `jdbc:mysql://host:3306/dbname`) |

## Customization points

| Placeholder | Description |
|-------------|-------------|
| `INSERT_JDBC_URL_HERE` | Fallback JDBC URL if `jdbc.url` property not set |
| `INSERT_SQL_QUERY_HERE` | SQL query to execute |
| Column mapping block | Map `row.columnName` to specific datapoint keys |

## Adapting for your module type

| Module type | Output change | Example |
|-------------|---------------|---------|
| DataSource (Script) | `emit.dp("columnName", value)` | One row, multiple columns |
| DataSource (BatchScript) | `emit.dp(wildvalue, "column", value)` | Multiple rows as instances |

`lm.sql` returns `{status: 'success'|'no data'|'failed', data, error}`.

## Related docs

- [DataSource](../../../docs/module-types/datasource.md)
- [Collection Modes](../../../docs/concepts/collection-modes.md)
- [Snippets catalog — lm.sql](../../../skills/logicmonitor-module-authoring/references/snippets-catalog.md#lmsql--jdbc-database-queries)
