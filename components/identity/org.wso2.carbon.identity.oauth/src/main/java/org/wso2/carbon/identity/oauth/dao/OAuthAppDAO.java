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

package org.wso2.carbon.identity.oauth.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.OAuthAppDO;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthConstants;
import org.wso2.carbon.identity.oauth.OAuthUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Based data access layer for OAuth Consumer Applications.
 */
public class OAuthAppDAO {

    public static final Log log = LogFactory.getLog(OAuthAppDAO.class);

    public void addOAuthApplication(OAuthAppDO consumerAppDO) throws IdentityOAuthAdminException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        if (!isDuplicateApplication(consumerAppDO.getUserName(), consumerAppDO.getTenantId(), consumerAppDO)) {
            try {
                connection = JDBCPersistenceManager.getInstance().getDBConnection();
                prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.ADD_OAUTH_APP);
                prepStmt.setString(1, consumerAppDO.getOauthConsumerKey());
                prepStmt.setString(2, consumerAppDO.getOauthConsumerSecret());
                prepStmt.setString(3, consumerAppDO.getUserName());
                prepStmt.setInt(4, consumerAppDO.getTenantId());
                prepStmt.setString(5, consumerAppDO.getApplicationName());
                prepStmt.setString(6, consumerAppDO.getOauthVersion());
                prepStmt.setString(7, consumerAppDO.getCallbackUrl());
                prepStmt.execute();
                connection.commit();

            } catch (IdentityException e) {
                String errorMsg = "Error when getting an Identity Persistence Store instance.";
                log.error(errorMsg, e);
                throw new IdentityOAuthAdminException(errorMsg, e);
            } catch (SQLException e) {
                log.error("Error when executing the SQL : " + SQLQueries.OAuthAppDAOSQLQueries.ADD_OAUTH_APP);
                log.error(e.getMessage(), e);
                throw new IdentityOAuthAdminException("Error when adding a new OAuth consumer application.");
            } finally {
                IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
            }
        } else {
            throw new IdentityOAuthAdminException("Error when adding the consumer application. " +
                    "An application with the same name already exists.");
        }
    }

    public String[] addOAuthConsumer(String username, int tenantId) throws IdentityOAuthAdminException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        String sqlStmt = null;
        String consumerKey;
        String consumerSecret = OAuthUtil.getRandomNumber();

        do {
            consumerKey = OAuthUtil.getRandomNumber();
        }
        while (isDuplicateConsumer(consumerKey));

        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            sqlStmt = SQLQueries.OAuthAppDAOSQLQueries.ADD_OAUTH_CONSUMER;
            prepStmt = connection.prepareStatement(sqlStmt);
            prepStmt.setString(1, consumerKey);
            prepStmt.setString(2, consumerSecret);
            prepStmt.setString(3, username);
            prepStmt.setInt(4, tenantId);
            // it is assumed that the OAuth version is 1.0a because this is required with OAuth 1.0a
            prepStmt.setString(5, OAuthConstants.OAuthVersions.VERSION_1A);
            prepStmt.execute();

            connection.commit();

        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuthAdminException(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + sqlStmt);
            log.error(e.getMessage(), e);
            throw new IdentityOAuthAdminException("Error when adding a new OAuth consumer.");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return new String[]{consumerKey, consumerSecret};
    }

    public OAuthAppDO[] getOAuthConsumerAppsOfUser(String username, int tenantId) throws IdentityOAuthAdminException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;
        OAuthAppDO[] oauthAppsOfUser;
        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.GET_APPS_OF_USER);
            prepStmt.setString(1, username);
            prepStmt.setInt(2, tenantId);

            rSet = prepStmt.executeQuery();
            List<OAuthAppDO> oauthApps = new ArrayList<OAuthAppDO>();
            while (rSet.next()) {
                if (rSet.getString(3) != null && rSet.getString(3).length() > 0) {
                    OAuthAppDO oauthApp = new OAuthAppDO();
                    oauthApp.setUserName(username);
                    oauthApp.setTenantId(tenantId);
                    oauthApp.setOauthConsumerKey(rSet.getString(1));
                    oauthApp.setOauthConsumerSecret(rSet.getString(2));
                    oauthApp.setApplicationName(rSet.getString(3));
                    oauthApp.setOauthVersion(rSet.getString(4));
                    oauthApp.setCallbackUrl(rSet.getString(5));
                    oauthApps.add(oauthApp);
                }
            }
            oauthAppsOfUser = oauthApps.toArray(new OAuthAppDO[oauthApps.size()]);
        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuthAdminException(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + SQLQueries.OAuthAppDAOSQLQueries.GET_APPS_OF_USER);
            log.error(e.getMessage(), e);
            throw new IdentityOAuthAdminException("Error when reading the application information from the persistence store.");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return oauthAppsOfUser;
    }

    public OAuthAppDO getAppInformation(String consumerKey) throws IdentityOAuthAdminException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;
        OAuthAppDO oauthApp = null;

        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.GET_APP_INFO);
            prepStmt.setString(1, consumerKey);

            rSet = prepStmt.executeQuery();
            List<OAuthAppDO> oauthApps = new ArrayList<OAuthAppDO>();
            while (rSet.next()) {
                if (rSet.getString(3) != null && rSet.getString(3).length() > 0) {
                    oauthApp = new OAuthAppDO();
                    oauthApp.setOauthConsumerKey(consumerKey);
                    oauthApp.setOauthConsumerSecret(rSet.getString(1));
                    oauthApp.setApplicationName(rSet.getString(2));
                    oauthApp.setOauthVersion(rSet.getString(3));
                    oauthApp.setCallbackUrl(rSet.getString(4));
                    oauthApps.add(oauthApp);
                }
            }
        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuthAdminException(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + SQLQueries.OAuthAppDAOSQLQueries.GET_APP_INFO);
            log.error(e.getMessage(), e);
            throw new IdentityOAuthAdminException("Error when reading the application information from the persistence store.");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return oauthApp;
    }

    public void updateConsumerApplication(OAuthAppDO oauthAppDO) throws IdentityOAuthAdminException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.UPDATE_CONSUMER_APP);
            prepStmt.setString(1, oauthAppDO.getCallbackUrl());
            prepStmt.setString(2, oauthAppDO.getOauthConsumerKey());
            prepStmt.setString(3, oauthAppDO.getOauthConsumerSecret());

            int count = prepStmt.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("No. of records updated for updating consumer application. : " + count);
            }
            connection.commit();

        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuthAdminException(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + SQLQueries.OAuthAppDAOSQLQueries.UPDATE_CONSUMER_APP);
            log.error(e.getMessage(), e);
            throw new IdentityOAuthAdminException("Error updating the consumer application.");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    public void removeConsumerApplication(String consumerKey) throws IdentityOAuthAdminException {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.REMOVE_APPLICATION);
            prepStmt.setString(1, consumerKey);

            prepStmt.execute();
            connection.commit();

        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuthAdminException(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + SQLQueries.OAuthAppDAOSQLQueries.REMOVE_APPLICATION);
            log.error(e.getMessage(), e);
            throw new IdentityOAuthAdminException("Error removing the consumer application.");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    private boolean isDuplicateApplication(String username, int tenantId, OAuthAppDO consumerAppDTO) throws IdentityOAuthAdminException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;

        boolean isDuplicateApp = false;

        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.CHECK_EXISTING_APPLICATION);
            prepStmt.setString(1, username);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, consumerAppDTO.getApplicationName());

            rSet = prepStmt.executeQuery();
            if (rSet.next()) {
                isDuplicateApp = true;
            }
        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuthAdminException(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + SQLQueries.OAuthAppDAOSQLQueries.CHECK_EXISTING_APPLICATION);
            log.error(e.getMessage(), e);
            throw new IdentityOAuthAdminException("Error when reading the application information from the persistence store.");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return isDuplicateApp;
    }

    private boolean isDuplicateConsumer(String consumerKey) throws IdentityOAuthAdminException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;

        boolean isDuplicateConsumer = false;

        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(SQLQueries.OAuthAppDAOSQLQueries.CHECK_EXISTING_CONSUMER);
            prepStmt.setString(1, consumerKey);

            rSet = prepStmt.executeQuery();
            if (rSet.next()) {
                isDuplicateConsumer = true;
            }
        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuthAdminException(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + SQLQueries.OAuthAppDAOSQLQueries.CHECK_EXISTING_CONSUMER);
            log.error(e.getMessage(), e);
            throw new IdentityOAuthAdminException("Error when reading the application information from the persistence store.");
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, rSet, prepStmt);
        }
        return isDuplicateConsumer;
    }

}
