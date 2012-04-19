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
    
    private static Log log = LogFactory.getLog(APIMgtDBUtils.class);
    private static volatile BasicDataSource dataSource = null;

    /**
     * Initializes the data source
     *
     * @param dbConfigurationPath db Config path
     * @throws APIManagementException if an error occurs while loading DB configuration
     */
    public static void initialize(String dbConfigurationPath) throws APIManagementException {
        if (dataSource != null) {
            return;
        }

        synchronized (APIMgtDBUtils.class) {
            if (dataSource == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Initializing data source");
                }
                DBConfiguration configuration = getDBConfig(dbConfigurationPath);
                String dbUrl = configuration.getDbUrl();
                String driver = configuration.getDriverName();
                String username = configuration.getUserName();
                String password = configuration.getPassword();
                if (dbUrl == null || driver == null || username == null || password == null) {
                    throw new APIManagementException("Required DB configuration parameters unspecified");
                }
                dataSource = new BasicDataSource();
                dataSource.setDriverClassName(driver);
                dataSource.setUrl(dbUrl);
                dataSource.setUsername(username);
                dataSource.setPassword(password);
            }
        }        
    }


    /**
     * Utility method to get a new database connection
     *
     * @return Connection
     * @throws java.sql.SQLException if failed to get Connection
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource != null && !dataSource.isClosed()) {
            return dataSource.getConnection();
        }
        throw new SQLException("Data source is not configured properly.");
    }

    public static void closeAllConnections(PreparedStatement preparedStatement, Connection connection,
                                           ResultSet resultSet) {
        closeConnection(connection);
        closeResultSet(resultSet);
        closeStatement(preparedStatement);
    }

    public static void closeConnection(Connection dbConnection) {
        if (dbConnection != null) {
            try {
                dbConnection.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close statement. Continuing with others. - " +
                        e.getMessage(), e);
            }
        }
    }

    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close result set  - " + e.getMessage(), e);
            }
        }

    }

    public static void closeStatement(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                log.warn("Database error. Could not close statement. Continuing with others. - " +
                        e.getMessage(), e);
            }
        }

    }

    private static DBConfiguration getDBConfig(String configPath) throws APIManagementException {
        DBConfiguration dbConfiguration = new DBConfiguration();
        try {
            String config = FileUtil.readFileToString(configPath);

            OMElement omElement = AXIOMUtil.stringToOM(config);
            OMElement dbConfigOMElement = omElement.getFirstChildWithName(new QName("dbConfig"));
            dbConfiguration.setDbUrl(
                    dbConfigOMElement.getFirstChildWithName(new QName("url")).getText());
            dbConfiguration.setDriverName(
                    dbConfigOMElement.getFirstChildWithName(new QName("driverName")).getText());
            dbConfiguration.setUserName(
                    dbConfigOMElement.getFirstChildWithName(new QName("userName")).getText());
            dbConfiguration.setPassword(
                    dbConfigOMElement.getFirstChildWithName(new QName("password")).getText());
        } catch (XMLStreamException e) {
            handleException("Failed to get db configuration", e);
        } catch (IOException e) {
            handleException("Error while reading the configuration file: " + configPath, e);
        }
        return dbConfiguration;
    }
    
    private static void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg,e);
        throw new APIManagementException(msg, e);
    }
}
