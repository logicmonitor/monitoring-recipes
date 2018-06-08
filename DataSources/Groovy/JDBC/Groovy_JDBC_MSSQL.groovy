import groovy.sql.Sql

// Get basic info to connect
def hostname = hostProps.get("system.hostname");
def user = hostProps.get("jdbc.mssql.username");
def pass = hostProps.get("jdbc.mssql.password");
def port = (hostProps.get("jdbc.mssql.port") ? hostProps.get("jdbc.mssql.port").toInteger() : 1433)

try 
{	
	// Construct a SQL instance with a url and a driver

	/*
	This example is using SQL authentication, which is why "integratedSecurity"
	is set to 'false'. This will work from Linux or Windows.

	If you would like to use Integrated Security, script must be executed from from a Windows collector
	logged in with adequate user privileges to access the MS SQL instance. You may then change the url to
	have 'integratedSecurity=true'
	 */
	def url = "jdbc:sqlserver://" + hostname + ":" + port +  ";databaseName=master;integratedSecurity=false";
	// def url = "jdbc:sqlserver://${hostname};instanceName=${instance};integratedSecurity=true"
	
	
	// Microsoft SQL Driver
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
}
catch(Exception e) 
{
	println e
	return 1
}

