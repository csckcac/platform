package org.wso2.carbon.hive.storage.db;


import org.apache.commons.dbcp.BasicDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DBManager {

    private DataSource dataSource;

    public DataSource createDataSource(String driverClass, String connectionUrl, String userName,
                                       String password){
        BasicDataSource basicDataSource = new BasicDataSource();
        basicDataSource.setDriverClassName(driverClass);
        basicDataSource.setUrl(connectionUrl);
        basicDataSource.setUsername(userName);
        basicDataSource.setPassword(password);
        return basicDataSource;
    }

    public void setDataSource(DataSource dSource){
        dataSource = dSource;
    }


    public Connection getConnection() throws ClassNotFoundException, SQLException {

        return dataSource.getConnection();
    }

    public void configureDB(DatabaseProperties databaseProperties) {
        if(databaseProperties.getDataSourceName()==null){
            setDataSource(createDataSource(databaseProperties.getDriverClass(),
                                           databaseProperties.getConnectionUrl(),
                                           databaseProperties.getUserName(),
                                           databaseProperties.getPassword()));
        }
    }
}
