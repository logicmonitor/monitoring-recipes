/*******************************************************************************
 *  © 2007-2019 - LogicMonitor, Inc. All rights reserved.
 ******************************************************************************/

import groovy.sql.Sql

// set host variables.
def hostname = hostProps.get("system.hostname")
def user = hostProps.get("jdbc.mysql.username")
def pass = hostProps.get("jdbc.mysql.password")
def port = (hostProps.get("jdbc.mysql.port") ? hostProps.get("jdbc.mysql.port").toInteger() : 3306)

// construct the URL.
def url = "jdbc:mysql://" + hostname + ":" + port

// MariaDB driver (works for both MariaDB & MySQL)
def driver = "org.mariadb.jdbc.Driver"

// Create a connection to the SQL server
sql = Sql.newInstance(url, user, pass, driver)

// Iterate over query results and list the databases
sql.eachRow('SHOW TABLES')
        { output ->

            // Create an instance for each database
            println output
        }

// Close the connection
sql.close()

return 0