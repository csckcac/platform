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

package org.wso2.carbon.identity.oauth2.dao;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.persistence.JDBCPersistenceManager;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.oauth2.IdentityOAuth2Exception;
import org.wso2.carbon.identity.oauth2.model.AuthzCodeValidationDataDO;
import org.wso2.carbon.identity.oauth2.util.OAuth2Util;

import java.sql.*;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * Data Access Layer functionality for Token management in OAuth 2.0 implementation. This includes
 * storing and retrieving access tokens, authorization codes and refresh tokens.
 */
public class TokenMgtDAO {

    private static final Log log = LogFactory.getLog(TokenMgtDAO.class);
    
    public void storeAuthorizationCode(String authzCode, String consumerKey, String scopeString,
                                       String authorizedUser, Timestamp timeStamp,
                                       long validityPeriod) throws IdentityOAuth2Exception {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(SQLQueries.STORE_AUTHORIZATION_CODE);
            prepStmt.setString(1, authzCode);
            prepStmt.setString(2, consumerKey);
            prepStmt.setString(3, scopeString);
            prepStmt.setString(4, authorizedUser);
            prepStmt.setTimestamp(5, timeStamp, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            prepStmt.setLong(6, validityPeriod);
            prepStmt.execute();
            connection.commit();
        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuth2Exception(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + SQLQueries.STORE_AUTHORIZATION_CODE);
            log.error(e.getMessage(), e);
            throw new IdentityOAuth2Exception("Error when storing the access code for consumer key : " + consumerKey);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    public void storeAccessToken(String accessToken, String refreshToken, String consumerKey, String authzUser,
                                        Timestamp timeStamp, long validityPeriod, String scopeString, String tokenState) throws IdentityOAuth2Exception {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(SQLQueries.STORE_ACCESS_TOKEN);
            prepStmt.setString(1, accessToken);
            prepStmt.setString(2, refreshToken);
            prepStmt.setString(3, consumerKey);
            prepStmt.setString(4, authzUser);
            prepStmt.setTimestamp(5, timeStamp, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
            prepStmt.setLong(6, validityPeriod);
            prepStmt.setString(7, scopeString);
            prepStmt.setString(8, tokenState);
            prepStmt.execute();
            connection.commit();
        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuth2Exception(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + SQLQueries.STORE_ACCESS_TOKEN);
            log.error(e.getMessage(), e);
            throw new IdentityOAuth2Exception("Error when storing the access code for consumer key : " + consumerKey);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

    public AuthzCodeValidationDataDO validateAuthorizationCode(String consumerKey, String authorizationKey) throws IdentityOAuth2Exception {
        AuthzCodeValidationDataDO validationDataDO = new AuthzCodeValidationDataDO();
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet resultSet;

        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(SQLQueries.VALIDATE_AUTHZ_CODE);
            prepStmt.setString(1, consumerKey);
            prepStmt.setString(2, authorizationKey);
            resultSet = prepStmt.executeQuery();

            if (resultSet.next()) {
                validationDataDO.setAuthorizedUser(resultSet.getString(1));
                validationDataDO.setScope(OAuth2Util.buildScopeArray(resultSet.getString(2)));
                validationDataDO.setIssuedTime(resultSet.getTimestamp(3,
                        Calendar.getInstance(TimeZone.getTimeZone("UTC"))));
                validationDataDO.setValidityPeriod(resultSet.getLong(4));
            }

        } catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuth2Exception(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + SQLQueries.VALIDATE_AUTHZ_CODE);
            log.error(e.getMessage(), e);
            throw new IdentityOAuth2Exception("Error when validating the authorization code", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }

        return validationDataDO;
    }

    public void cleanUpAuthzCode(String authzCode) throws IdentityOAuth2Exception {
        Connection connection = null;
        PreparedStatement prepStmt = null;

        try {
            connection = JDBCPersistenceManager.getInstance().getDBConnection();
            prepStmt = connection.prepareStatement(SQLQueries.REMOVE_AUTHZ_CODE);
            prepStmt.setString(1, authzCode);

            prepStmt.execute();
            connection.commit();

        }  catch (IdentityException e) {
            String errorMsg = "Error when getting an Identity Persistence Store instance.";
            log.error(errorMsg, e);
            throw new IdentityOAuth2Exception(errorMsg, e);
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + SQLQueries.REMOVE_AUTHZ_CODE);
            log.error(e.getMessage(), e);
            throw new IdentityOAuth2Exception("Error when cleaning up the authorization code", e);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
    }

}
