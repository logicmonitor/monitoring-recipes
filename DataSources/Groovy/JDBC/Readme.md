## LogicMonitor Support Pages

#### [Authentication & Credentials](https://www.logicmonitor.com/support/getting-started/advanced-logicmonitor-setup/defining-authentication-credentials/)

#### [Active Discovery](https://www.logicmonitor.com/support/datasources/active-discovery/jdbc-active-discovery/)

#### [Data Collection](https://www.logicmonitor.com/support/datasources/data-collection-methods/jdbc-data-collection/)

## Supported Databases (default port):
* MySQL (3306)
  * URL: ```jdbc:mysql://<hostname>:<port>```
  * Driver: ```org.mariadb.jdbc.Driver```

* Microsoft SQL (1433) - [JDBC : Building the connection URL](https://docs.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-2017)
  * URL: 
    * Integrated Security - ```jdbc:sqlserver://<hostname>:<port>;databaseName=<database_name>;integratedSecurity=true;```
    * JDBC Credentials - ```jdbc:sqlserver://<hostname>:<port>;```
  * Driver: ```com.microsoft.sqlserver.jdbc.SQLServerDriver```

* Oracle (1521)
  * URL: ```jdbc:oracle:thin:@//<hostname>:<port>/<database_name>```
  * Driver: ```oracle.jdbc.driver.OracleDriver```

* Postgres (1433)
  * URL: ```jdbc:postgresql://<hostname>:<port>/<database_name>```
  * Driver: ```org.postgresql.Driver```

* DB2 (5432)
  * URL: ```jdbc:db2://<hostname>:<port>/<database_name>```
  * Driver: ```com.ibm.db2.jcc.DB2Driver```

* Sybase 
  * URL: ```jdbc:jtds:sybase://<hostname>/<database_name>```
  * Driver: ```com.sybase.jdbc4.jdbc.SybDriver```

