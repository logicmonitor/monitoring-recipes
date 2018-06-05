# LogicMonitor Support Pages

### Authentication Credentials : https://www.logicmonitor.com/support/getting-started/advanced-logicmonitor-setup/defining-authentication-credentials/

### Active Discovery : https://www.logicmonitor.com/support/datasources/active-discovery/jdbc-active-discovery/

### Data Collection : https://www.logicmonitor.com/support/datasources/data-collection-methods/jdbc-data-collection/

Supported Databases (default port):
- MySQL (3306)
```
jdbc:mysql://<hostname>:<port>
```
- Microsoft SQL (1433)
```
jdbc:sqlserver://<hostname>:<port>;
jdbc:sqlserver://<hostname>:<port>;integratedSecurity=true
```
- Oracle (1521)
```
jdbc:oracle:thin:<hostname>:<port>/<database_name>
```
- Postgres (1433)
```
jdbc:postgresql://<hostname>:<port>/<database_name>
```
- DB2 (5432)
```
jdbc:jtds:db2://<hostname>/<database_name>
jdbc:db2://<hostname>:<port>/<database_name>
```
- Sybase 
```
jdbc:jtds:sybase://<hostname>/<database_name>
```
