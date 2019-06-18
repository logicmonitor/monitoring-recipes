## Groovy Java Database Connectivity (JDBC)

Used to connect to various types of databases in order to execute certain commands and/or queries.

## LogicMonitor Support Pages

#### [Authentication & Credentials](https://www.logicmonitor.com/support/getting-started/advanced-logicmonitor-setup/defining-authentication-credentials/)

#### [Active Discovery](https://www.logicmonitor.com/support/datasources/active-discovery/jdbc-active-discovery/)

#### [Data Collection](https://www.logicmonitor.com/support/datasources/data-collection-methods/jdbc-data-collection/)

## Supported Databases:

#### MySQL
  * Default Port: __3306__
  * URL: ```jdbc:mysql://<hostname>:<port>```
  * Driver: ```org.mariadb.jdbc.Driver```

#### Microsoft SQL - [JDBC : Building the connection URL](https://docs.microsoft.com/en-us/sql/connect/jdbc/building-the-connection-url?view=sql-server-2017)
  * Default Port: __1433__
  * URL: 
    * Integrated Security - ```jdbc:sqlserver://<hostname>:<port>;databaseName=<database_name>;integratedSecurity=true;```
    * JDBC Credentials - ```jdbc:sqlserver://<hostname>:<port>;```
  * Driver: ```com.microsoft.sqlserver.jdbc.SQLServerDriver```

#### Oracle
  * Default Port: __1521__
  * URL: ```jdbc:oracle:thin:@//<hostname>:<port>/<database_name>```
  * Driver: ```oracle.jdbc.driver.OracleDriver```

#### Postgres
  * Default Port: __5432__
  * URL: ```jdbc:postgresql://<hostname>:<port>/<database_name>```
  * Driver: ```org.postgresql.Driver```

#### IBM DB2
  * Default Port: __50000__
  * URL: ```jdbc:db2://<hostname>:<port>/<database_name>```
  * Driver: ```com.ibm.db2.jcc.DB2Driver```

#### SAP Sybase
  * Default Port: __2638__
  * URL: ```jdbc:jtds:sybase://<hostname>/<database_name>```
  * Driver: ```com.sybase.jdbc4.jdbc.SybDriver```

