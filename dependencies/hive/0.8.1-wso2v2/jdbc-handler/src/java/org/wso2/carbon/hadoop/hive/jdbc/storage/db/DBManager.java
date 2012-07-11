package org.wso2.carbon.hadoop.hive.jdbc.storage.db;


import org.apache.commons.dbcp.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.hadoop.hive.jdbc.storage.exception.UnsupportedDatabaseException;
import org.wso2.carbon.hadoop.hive.jdbc.storage.utils.Commons;
import org.apache.hadoop.mapred.JobConf;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

public class DBManager {

    private static final Logger log = LoggerFactory.getLogger(DBManager.class);

    private DataSource dataSource;

    public void setDataSource(DataSource dSource) {
        dataSource = dSource;
    }

    public Connection getConnection() throws ClassNotFoundException, SQLException {

        return dataSource.getConnection();
    }

    public DatabaseType getDatabaseName(Connection connection) throws UnsupportedDatabaseException {
        String connectionUrl = null;
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            connectionUrl = metaData.getURL();
        } catch (SQLException e) {
            log.error("Failed to get connection url from DatabaseMetaData", e);
        }
        return getDatabaseType(connectionUrl.split(":")[1]);
    }

    public DatabaseType getDatabaseType(String databaseName) throws UnsupportedDatabaseException {
        DatabaseType databaseType = null;
        if (databaseName.equalsIgnoreCase("mysql")) {
            databaseType = DatabaseType.MYSQL;
        } else if (databaseName.equalsIgnoreCase("microsoft")) {
            databaseType = DatabaseType.SQLSERVER;
        } else if (databaseName.equalsIgnoreCase("oracle")) {
            databaseType = DatabaseType.ORACLE;
        } else if (databaseName.equalsIgnoreCase("h2")) {
            databaseType = DatabaseType.H2;
        } else if (databaseName.equalsIgnoreCase("postgresql")) {
            databaseType = DatabaseType.POSTGRESQL;
        } else {
            throw new UnsupportedDatabaseException("Your database type doesn't support by " +
                                                   "hive jdbc-handler to fetch results");
        }
        return databaseType;
    }

    public void configureDB(JobConf conf) {
        //Configure BasicDataSource
        BasicDataSource basicDataSource = Commons.configureBasicDataSource(conf);
        setDataSource(basicDataSource);

    }
}
