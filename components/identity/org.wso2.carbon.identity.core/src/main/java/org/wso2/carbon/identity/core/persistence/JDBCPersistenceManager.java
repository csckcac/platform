/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.core.persistence;

import org.apache.axiom.om.OMElement;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityConfigParser;

import javax.xml.namespace.QName;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * This class is used for handling identity meta data persistence in the Identity JDBC Store. During
 * the server start-up, it checks whether the database is created, if not it creates one. It reads
 * the data source properties from the identity.xml.
 * This is implemented as a singleton. An instance of this class can be obtained through
 * JDBCPersistenceManager.getInstance() method.
 */
public class JDBCPersistenceManager {
    
    private BasicDataSource dataSource; 

    private static Log log = LogFactory.getLog(JDBCPersistenceManager.class);
    private static JDBCPersistenceManager instance;

    private static final int DEFAULT_MAX_ACTIVE = 40;
    private static final int DEFAULT_MAX_WAIT = 1000 * 60;
    private static final int DEFAULT_MIN_IDLE = 5;
    private static final int DEFAULT_MAX_IDLE = 6;

    private JDBCPersistenceManager() throws IdentityException {
        try {
            OMElement persistenceManagerConfigElem = IdentityConfigParser.getInstance().getConfigElement("JDBCPersistenceManager");

            if(persistenceManagerConfigElem == null){
                String errorMsg = "Identity Persistence Manager configuration is not available in identity.xml file. " +
                        "Terminating the JDBC Persistence Manager initialization. This may affect certain functionality.";
                log.error(errorMsg);
                throw new IdentityException(errorMsg);
            }

            Map<String, String> propertyMap = getJDBCConnectionProperties(persistenceManagerConfigElem);
            dataSource = new BasicDataSource();
            populateDataSource(propertyMap, dataSource);
        } catch (ServerConfigurationException e) {
            log.error("Error when reading the JDBC Configuration from the file.", e);
            throw new IdentityException("Error when reading the JDBC Configuration from the file.", e);
        }
    }

    /**
     * Get an instance of the JDBCPersistenceManager. It implements a lazy initialization with double
     * checked locking, because it is initialized first by identity.core module during the start up.
     * @return  JDBCPersistenceManager instance
     * @throws IdentityException Error when reading the data source configurations
     */
    public static JDBCPersistenceManager getInstance() throws IdentityException {
        if (instance == null) {
            synchronized (JDBCPersistenceManager.class) {
                if (instance == null) {
                    instance = new JDBCPersistenceManager();
                }
            }
        }
        return instance;
    }

    public void initializeDatabase() throws Exception {
            IdentityDBInitializer dbUtil = new IdentityDBInitializer(dataSource);
            try {
                    dbUtil.createRegistryDatabase();
            } catch (Exception e) {
                String msg = "Error when creating the Identity database";
                throw new Exception(msg, e);
            }
    }

    private void populateDataSource(Map<String, String> propertyMap, BasicDataSource dataSource) {
        dataSource.setDriverClassName(propertyMap.get(IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_DRIVER_NAME));
        dataSource.setUrl(propertyMap.get(IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_URL));
        dataSource.setUsername(propertyMap.get(IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_USERNAME));
        dataSource.setPassword(propertyMap.get(IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_PASSWORD));

        if (propertyMap.containsKey(IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_MAX_ACTIVE)) {
            dataSource.setMaxActive(Integer.parseInt(propertyMap.get(
                    IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_MAX_ACTIVE)));
        } else {
            dataSource.setMaxActive(DEFAULT_MAX_ACTIVE);
        }

        if (propertyMap.containsKey(IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_MAX_WAIT)) {
            dataSource.setMaxWait(Long.parseLong(propertyMap.get(
                    IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_MAX_WAIT)));
        } else {
            dataSource.setMaxActive(DEFAULT_MAX_WAIT);
        }

        if (propertyMap.containsKey(IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_MIN_IDLE)) {
            dataSource.setMinIdle(Integer.parseInt(propertyMap.get(
                    IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_MIN_IDLE)));
        } else {
            dataSource.setMinIdle(DEFAULT_MIN_IDLE);
        }

        if (propertyMap.containsKey(IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_MAX_IDLE)) {
            dataSource.setMaxIdle(Integer.parseInt(propertyMap.get(
                    IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_MAX_IDLE)));
        } else {
            dataSource.setMaxIdle(DEFAULT_MAX_IDLE);
        }

        if (propertyMap.containsKey(IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_VALIDATION_QUERY)) {
            dataSource.setValidationQuery(propertyMap.get(
                    IdentityPersistenceConstants.JDBCPersistenceManagerConstants.JDBC_CONFIG_PROPS_VALIDATION_QUERY));
        }
    }


    private Map<String, String> getJDBCConnectionProperties(OMElement configElement) throws IdentityException {
        Map<String, String> propertyMap = new HashMap<String, String>();
        OMElement configElem = configElement.getFirstChildWithName(new QName(
                IdentityConfigParser.IDENTITY_DEFAULT_NAMESPACE,"Configuration"));
        
        if(configElem == null){
            String errorMsg = "Configuration Element is not available for JDBC Persistence Manager in identity.xml file. " +
                    "Terminating the JDBC Persistence Manager initialization. This might affect certain features.";
            log.error(errorMsg);
            throw new IdentityException(errorMsg);
        }

        if (configElem.getChildrenWithLocalName("Property") != null) {
            for (Iterator propElements = configElem.getChildrenWithLocalName("Property");
                 propElements.hasNext(); ) {
                OMElement element = (OMElement) propElements.next();
                String propName = element.getAttributeValue(new QName("name"));
                if (propName != null) {
                    propertyMap.put(propName, element.getText());
                }
            }
        }
        return propertyMap;
    }

    /**
     * Returns an database connection for Identity data source.
     * @return Database connection
     * @throws IdentityException Exception occurred when getting the data source.
     */
    public Connection getDBConnection() throws IdentityException {
        try {
            Connection dbConnection = dataSource.getConnection();
            dbConnection.setAutoCommit(false);
            dbConnection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            return dbConnection;
        } catch (SQLException e) {
            String errMsg = "Error when getting a database connection object from the Identity data source.";
            log.error(errMsg, e);
            throw new IdentityException(errMsg, e);
        }
    }

}
