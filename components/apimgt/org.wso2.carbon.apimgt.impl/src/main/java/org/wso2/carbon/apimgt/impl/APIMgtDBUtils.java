package org.wso2.carbon.apimgt.impl;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.utils.FileUtil;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class APIMgtDBUtils {
    static Log log = LogFactory.getLog(APIMgtDBUtils.class);
    static volatile BasicDataSource dataSource = null;

    /**
     * Initializes the datasource
     * @param dbConfigurationPath db Config path
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     */
    public static void initialize(String dbConfigurationPath) throws APIManagementException {
        String jdbcURL;
        String driver;
        String username;
        String password;
        if (dataSource == null) {
            synchronized (APIMgtDBUtils.class) {
                if (dataSource == null) {
                  DBConfiguration configuration =  getDBConfig(dbConfigurationPath);
                        jdbcURL = configuration.getDbUrl();
                        driver = configuration.getDriverName();
                        username =configuration.getUserName();
                        password = configuration.getPassword();
                    if(jdbcURL==null || driver==null||username==null||password==null){
                        throw new APIManagementException("DB configurations are not properly defined");
                    }
                    dataSource = new BasicDataSource();
                    dataSource.setDriverClassName(driver);
                    dataSource.setUrl(jdbcURL);
                    dataSource.setUsername(username);
                    dataSource.setPassword(password);
                }
            }
        }
    }


    /**
     * Utility method to get a new database connection
     * @return Connection
     * @throws java.sql.SQLException if failed to get Connection
     */
    public static Connection getConnection() throws SQLException {
        if(dataSource != null && !(dataSource.isClosed())){
            return dataSource.getConnection();
        }else{
            throw new SQLException("Datasource is not configured properly.");
       }
    }

    public static void closeAllConnections(PreparedStatement preparedStatement , Connection  connection,
                                        ResultSet resultSet  ){
        closeConnection(connection);
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
        
    }
    public static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - " +
                        e.getMessage(), e);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close result set  - " + e.getMessage(), e);
            }
        }

    }

    public static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.error("Database error. Could not close statement. Continuing with others. - " +
                        e.getMessage(), e);
            }
        }

    }

    private static DBConfiguration getDBConfig(String configPath) throws APIManagementException {
        DBConfiguration dbConfiguration = new DBConfiguration();
        try {
            String config = FileUtil.readFileToString(configPath);

            OMElement omElement = AXIOMUtil.stringToOM(config);
            OMElement dbConfigOMElement= omElement.getFirstChildWithName(new QName("dbConfig"));
            dbConfiguration.setDbUrl(
                    dbConfigOMElement.getFirstChildWithName(new QName("url")).getText());
            dbConfiguration.setDriverName(
                    dbConfigOMElement.getFirstChildWithName(new QName("driverName")).getText());
            dbConfiguration.setUserName(
                    dbConfigOMElement.getFirstChildWithName(new QName("userName")).getText());
            dbConfiguration.setPassword(
                    dbConfigOMElement.getFirstChildWithName(new QName("password")).getText());
        } catch (XMLStreamException e) {
            String msg = "Failed to get db configuration";
            throw new APIManagementException(msg, e);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dbConfiguration;
    }
}
