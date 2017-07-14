import groovy.sql.Sql

// Get basic info to connect
def hostname = hostProps.get("system.hostname");
def user = hostProps.get("jdbc.mssql.username");
def pass = hostProps.get("jdbc.mssql.password");
def port = hostProps.get("jdbc.mssql.port");

// Construct a SQL instance with a url and a driver
// This example is using SQL authentication, which is why "integratedSecurity"
// is set to 'false'. This will work from Linux or Windows.
def url = "jdbc:sqlserver://" + hostname + ":" + port +  ";databaseName=master;integratedSecurity=false";
def driver = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

// Create a connection to the SQL server
sql = Sql.newInstance(url, user, pass, driver )

// Iterate over query results and list the databases
sql.eachRow( 'SELECT [name] FROM [master].[sys].[databases]' )
{ database ->

    // Create an instance for each database
    println database.name + "##" + database.name
}

// Close the connection
sql.close()

return 0;
