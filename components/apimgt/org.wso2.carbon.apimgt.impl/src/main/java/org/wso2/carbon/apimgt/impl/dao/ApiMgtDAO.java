/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.impl.dao;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.UserApplicationAPIUsage;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyInfoDTO;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.impl.utils.APIVersionComparator;
import org.wso2.carbon.apimgt.impl.utils.LRUCache;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityDatabaseUtil;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.identity.oauth.IdentityOAuthAdminException;
import org.wso2.carbon.identity.oauth.OAuthConstants;
import org.wso2.carbon.identity.oauth.OAuthUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.*;
import java.util.*;


/**
 * This class represent the ApiMgtDAO.
 */
public class ApiMgtDAO {

    private static final Log log = LogFactory.getLog(ApiMgtDAO.class);

    /**
     *
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to initialize the db config
     */
    public ApiMgtDAO() throws APIManagementException {
        APIMgtDBUtil.initialize();
    }

    /**
     * Get access token key for given userId and API Identifier
     *
     * @param userId     id of the user
     * @param applicationName name of the Application
     * @param identifier APIIdentifier
     * @param keyType Type of the key required                  
     * @return Access token
     * @throws APIManagementException if failed to get Access token
     * @throws org.wso2.carbon.identity.base.IdentityException if failed to get tenant id
     */
    public String getAccessKeyForAPI(String userId, String applicationName, APIInfoDTO identifier, 
                                     String keyType) throws APIManagementException, IdentityException {

        String accessKey = null;

        //get the tenant id for the corresponding domain
        String tenantAwareUserId = MultitenantUtils.getTenantAwareUsername(userId);
        int tenantId = IdentityUtil.getTenantIdOFUser(userId);

        if (log.isDebugEnabled()) {
            log.debug("Searching for: " + identifier.getAPIIdentifier() + ", User: " + tenantAwareUserId +
                    ", ApplicationName: " + applicationName + ", Tenant ID: " + tenantId);
        }

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery =
                "SELECT " +
                        "   SKM.ACCESS_TOKEN AS ACCESS_TOKEN " +
                        "FROM " +
                        "   AM_SUBSCRIPTION SP," +
                        "   AM_API API," +
                        "   AM_SUBSCRIBER SB," +
                        "   AM_APPLICATION APP, " +
                        "   AM_SUBSCRIPTION_KEY_MAPPING SKM " +
                        "WHERE " +
                        "   SB.USER_ID=? " +
                        "   AND SB.TENANT_ID=? " +
                        "   AND API.API_PROVIDER=? " +
                        "   AND API.API_NAME=?" +
                        "   AND API.API_VERSION=?" +
                        "   AND APP.NAME=? " +
                        "   AND SKM.KEY_TYPE=? " +
                        "   AND API.API_ID = SP.API_ID"   +
                        "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                        "   AND APP.APPLICATION_ID = SP.APPLICATION_ID " +
                        "   AND SP.SUBSCRIPTION_ID = SKM.SUBSCRIPTION_ID ";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, tenantAwareUserId);
            ps.setInt(2, tenantId);
            ps.setString(3, identifier.getProviderId());
            ps.setString(4, identifier.getApiName());
            ps.setString(5, identifier.getVersion());
            ps.setString(6, applicationName);
            ps.setString(7, keyType);

            rs = ps.executeQuery();

            while (rs.next()) {
                accessKey = rs.getString(APIConstants.SUBSCRIPTION_FIELD_ACCESS_TOKEN);
            }
        } catch (SQLException e) {
            log.error("Error when executing the SQL query to read the access key for user : "
                    + userId + "of tenant(id) : " + tenantId, e);
            throw new APIManagementException("Error when executing the SQL query to read the" +
                    " access key ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return accessKey;
    }

    public String getAccessKeyForApplication(String userId, String applicationName,
                                     String keyType) throws APIManagementException, IdentityException {

        String accessKey = null;

        //get the tenant id for the corresponding domain
        String tenantAwareUserId = MultitenantUtils.getTenantAwareUsername(userId);
        int tenantId = IdentityUtil.getTenantIdOFUser(userId);

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery =
                "SELECT " +
                        "   AKM.ACCESS_TOKEN AS ACCESS_TOKEN " +
                        "FROM " +
                        "   AM_SUBSCRIBER SB," +
                        "   AM_APPLICATION APP, " +
                        "   AM_APPLICATION_KEY_MAPPING AKM " +
                        "WHERE " +
                        "   SB.USER_ID=? " +
                        "   AND SB.TENANT_ID=? " +
                        "   AND APP.NAME=? " +
                        "   AND AKM.KEY_TYPE=? " +
                        "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                        "   AND APP.APPLICATION_ID = AKM.APPLICATION_ID";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, tenantAwareUserId);
            ps.setInt(2, tenantId);
            ps.setString(3, applicationName);
            ps.setString(4, keyType);
            rs = ps.executeQuery();

            while (rs.next()) {
                accessKey = rs.getString(APIConstants.SUBSCRIPTION_FIELD_ACCESS_TOKEN);
            }
        } catch (SQLException e) {
            log.error("Error when executing the SQL query to read the access key for user : "
                    + userId + "of tenant(id) : " + tenantId, e);
            throw new APIManagementException("Error when executing the SQL query to read the" +
                    " access key ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return accessKey;
    }

    /**
     * Get Subscribed APIs for given userId
     *
     * @param userId id of the user
     * @return APIInfoDTO[]
     * @throws APIManagementException if failed to get Subscribed APIs
     * @throws org.wso2.carbon.identity.base.IdentityException
     *                                if failed to get tenant id
     */
    public APIInfoDTO[] getSubscribedAPIsOfUser(String userId) throws APIManagementException,
            IdentityException {
        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userId);
        int tenantId = IdentityUtil.getTenantIdOFUser(userId);
        List<APIInfoDTO> apiInfoDTOList = new ArrayList<APIInfoDTO>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = "SELECT " +
                "   API.API_PROVIDER AS API_PROVIDER," +
                "   API.API_NAME AS API_NAME," +
                "   API.API_VERSION AS API_VERSION " +
                "FROM " +
                "   AM_SUBSCRIPTION SP, " +
                "   AM_API API," +
                "   AM_SUBSCRIBER SB, " +
                "   AM_APPLICATION APP " +
                "WHERE " +
                "   SB.USER_ID = ? " +
                "   AND SB.TENANT_ID = ? " +
                "   AND SB.SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                "   AND APP.APPLICATION_ID=SP.APPLICATION_ID " +
                "   AND API.API_ID = SP.API_ID";
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, tenantAwareUsername);
            ps.setInt(2, tenantId);
            rs = ps.executeQuery();
            while (rs.next()) {
                APIInfoDTO infoDTO = new APIInfoDTO();
                infoDTO.setProviderId(rs.getString("API_PROVIDER"));
                infoDTO.setApiName(rs.getString("API_NAME"));
                infoDTO.setVersion(rs.getString("API_VERSION"));
                apiInfoDTOList.add(infoDTO);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new APIManagementException(e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return apiInfoDTOList.toArray(new APIInfoDTO[apiInfoDTOList.size()]);
    }

    /**
     * Get API key information for given API
     *
     * @param apiInfoDTO API info
     * @return APIKeyInfoDTO[]
     * @throws APIManagementException if failed to get key info for given API
     */
    public APIKeyInfoDTO[] getSubscribedUsersForAPI(APIInfoDTO apiInfoDTO)
            throws APIManagementException {

        APIKeyInfoDTO[] apiKeyInfoDTOs = null;
        // api_id store as "providerName_apiName_apiVersion" in AM_SUBSCRIPTION table
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = "SELECT " +
                "   SB.USER_ID, " +
                "   SB.TENANT_ID " +
                "FROM " +
                "   AM_SUBSCRIBER SB, " +
                "   AM_APPLICATION APP, " +
                "   AM_SUBSCRIPTION SP, " +
                "   AM_API API " +
                "WHERE " +
                "   API.API_PROVIDER = ? " +
                "   AND API.API_NAME = ?" +
                "   AND API.API_VERSION = ?" +
                "   AND SP.APPLICATION_ID = APP.APPLICATION_ID " +
                "   AND APP.SUBSCRIBER_ID=SB.SUBSCRIBER_ID " +
                "   AND API.API_ID = SP.API_ID";
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, apiInfoDTO.getProviderId());
            ps.setString(2, apiInfoDTO.getApiName());
            ps.setString(3, apiInfoDTO.getVersion());
            rs = ps.executeQuery();
            List<APIKeyInfoDTO> apiKeyInfoList = new ArrayList<APIKeyInfoDTO>();
            while (rs.next()) {
                String userId = rs.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID);
                //int tenantId = rs.getInt(APIConstants.SUBSCRIBER_FIELD_TENANT_ID);
                // If the tenant Id > 0, get the tenant domain and append it to the username.
                //if (tenantId > 0) {
                //  userId = userId + "@" + APIKeyMgtUtil.getTenantDomainFromTenantId(tenantId);
                //}
                APIKeyInfoDTO apiKeyInfoDTO = new APIKeyInfoDTO();
                apiKeyInfoDTO.setUserId(userId);
                // apiKeyInfoDTO.setStatus(rs.getString(3));
                apiKeyInfoList.add(apiKeyInfoDTO);
            }
            apiKeyInfoDTOs = apiKeyInfoList.toArray(new APIKeyInfoDTO[apiKeyInfoList.size()]);

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new APIManagementException(e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return apiKeyInfoDTOs;
    }

    /**
     * This method is to update the access token
     *
     * @param userId     id of the user
     * @param apiInfoDTO Api info
     * @param statusEnum Status of the access key
     * @throws APIManagementException if failed to update the access token
     * @throws org.wso2.carbon.identity.base.IdentityException
     *                                if failed to get tenant id
     */
    public void changeAccessTokenStatus(String userId, APIInfoDTO apiInfoDTO,
                                        String statusEnum)
            throws APIManagementException, IdentityException {
        String tenantAwareUsername = MultitenantUtils.getTenantAwareUsername(userId);
        int tenantId = 0;
        IdentityUtil.getTenantIdOFUser(userId);
        Connection conn = null;
        PreparedStatement ps = null;
        String sqlQuery = "UPDATE" +
                " IDENTITY_OAUTH2_ACCESS_TOKEN IAT , AM_SUBSCRIBER SB," +
                " AM_SUBSCRIPTION SP , AM_APPLICATION APP, AM_API API" +
                " SET IAT.TOKEN_STATE=?" +
                " WHERE SB.USER_ID=?" +
                " AND SB.TENANT_ID=?" +
                " AND API.API_PROVIDER=?" +
                " AND API.API_NAME=?" +
                " AND API.API_VERSION=?" +
                " AND SP.ACCESS_TOKEN=IAT.ACCESS_TOKEN" +
                " AND SB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID" +
                " AND APP.APPLICATION_ID = SP.APPLICATION_ID" +
                " AND API.API_ID = SP.API_ID";
        try {

            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, statusEnum);
            ps.setString(2, tenantAwareUsername);
            ps.setInt(3, tenantId);
            ps.setString(4, apiInfoDTO.getProviderId());
            ps.setString(5, apiInfoDTO.getApiName());
            ps.setString(6, apiInfoDTO.getVersion());

            int count = ps.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("Number of rows being updated : " + count);
            }
            conn.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException e1) {
                log.error("Failed to rollback the changeAccessTokenStatus operation", e);
            }
            throw new APIManagementException(e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, null);
        }
    }

    /**
     * Validate the provided key against the given API. First it will validate the key is valid
     * , ACTIVE and not expired.
     *
     * @param context     Requested Context
     * @param version     version of the API
     * @param accessToken Provided Access Token
     * @return APIKeyValidationInfoDTO instance with authorization status and tier information if
     *         authorized.
     * @throws APIManagementException Error when accessing the database or registry.
     */
    public APIKeyValidationInfoDTO validateKey(String context, String version, String accessToken)
            throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("A request is received to process the token : " + accessToken + " to access" +
                    " the context URL : " + context);
        }
        APIKeyValidationInfoDTO keyValidationInfoDTO = new APIKeyValidationInfoDTO();
        String status;
        String tier;
        String type;
        String username;
        
        // First check whether the token is valid, active and not expired.
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        String applicationSqlQuery = "SELECT " +
                "   IAT.VALIDITY_PERIOD, " +
                "   IAT.TIME_CREATED ," +
                "   IAT.TOKEN_STATE," +
                "   SUB.TIER_ID," +
                "   SUBS.USER_ID," +
                "   APP.APPLICATION_ID," +
                "   AKM.KEY_TYPE" +
                " FROM " +
                "   IDENTITY_OAUTH2_ACCESS_TOKEN IAT," +
                "   AM_SUBSCRIPTION SUB," +
                "   AM_SUBSCRIBER SUBS," +
                "   AM_APPLICATION APP," +
                "   AM_APPLICATION_KEY_MAPPING AKM," +
                "   AM_API API" +
                " WHERE " +
                "   AKM.ACCESS_TOKEN = ? " +
                "   AND API.CONTEXT = ? " +
                "   AND API.API_VERSION = ? " +
                "   AND IAT.ACCESS_TOKEN=AKM.ACCESS_TOKEN " +
                "   AND APP.APPLICATION_ID = APP.APPLICATION_ID" +
                "   AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
                "   AND APP.SUBSCRIBER_ID = SUBS.SUBSCRIBER_ID" +
                "   AND API.API_ID = SUB.API_ID" +
                "   AND AKM.APPLICATION_ID=APP.APPLICATION_ID";

        String sqlQuery = "SELECT " +
                "   IAT.VALIDITY_PERIOD, " +
                "   IAT.TIME_CREATED ," +
                "   IAT.TOKEN_STATE," +
                "   SUB.TIER_ID," +
                "   SUBS.USER_ID," +
                "   APP.APPLICATION_ID," +
                "   SKM.KEY_TYPE" +
                " FROM " +
                "   IDENTITY_OAUTH2_ACCESS_TOKEN IAT," +
                "   AM_SUBSCRIPTION SUB," +
                "   AM_SUBSCRIBER SUBS," +
                "   AM_APPLICATION APP," +
                "   AM_SUBSCRIPTION_KEY_MAPPING SKM," +
                "   AM_API API" +
                " WHERE " +
                "   SKM.ACCESS_TOKEN = ? " +
                "   AND API.CONTEXT = ? " +
                "   AND API.API_VERSION = ? " +
                "   AND IAT.ACCESS_TOKEN=SKM.ACCESS_TOKEN " +
                "   AND SUB.SUBSCRIPTION_ID = SKM.SUBSCRIPTION_ID" +
                "   AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
                "   AND APP.SUBSCRIBER_ID = SUBS.SUBSCRIBER_ID" +
                "   AND API.API_ID = SUB.API_ID";
        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(applicationSqlQuery);
            ps.setString(1, accessToken);
            ps.setString(2, context);
            ps.setString(3, version);
            rs = ps.executeQuery();
            if (rs.next()) {
                status = rs.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_STATE);
                tier = rs.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID);
                type = rs.getString(APIConstants.SUBSCRIPTION_KEY_TYPE);
                username = rs.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID);
                // Check whether the token is ACTIVE
                if (APIConstants.TokenStatus.ACTIVE.equals(status)) {
                    keyValidationInfoDTO.setAuthorized(true);
                    keyValidationInfoDTO.setTier(tier);
                    keyValidationInfoDTO.setType(type);
                    keyValidationInfoDTO.setUsername(username);
                    return keyValidationInfoDTO;
                }
            }

            rs.close();
            ps.close();

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, accessToken);
            ps.setString(2, context);
            ps.setString(3, version);
            rs = ps.executeQuery();

            if (rs.next()) { // access token is valid.
                //timestamp = rs.getTimestamp(APIConstants.IDENTITY_OAUTH2_FIELD_TIME_CREATED);
                //validityPeriod = rs.getLong(APIConstants.IDENTITY_OAUTH2_FIELD_VALIDITY_PERIOD);
                status = rs.getString(APIConstants.IDENTITY_OAUTH2_FIELD_TOKEN_STATE);
                tier = rs.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID);
                type = rs.getString(APIConstants.SUBSCRIPTION_KEY_TYPE);
                username = rs.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID);
            } else { // invalid token.
                if (log.isDebugEnabled()) {
                    log.debug("Invalid Access Token is provided : " + accessToken);
                }
                keyValidationInfoDTO.setAuthorized(false);
                return keyValidationInfoDTO;
            }

            // Check whether the token is ACTIVE
            if (APIConstants.TokenStatus.ACTIVE.equals(status)) {
                keyValidationInfoDTO.setAuthorized(true);
                keyValidationInfoDTO.setTier(tier);
                keyValidationInfoDTO.setType(type);
                keyValidationInfoDTO.setUsername(username);
                return keyValidationInfoDTO;
            }

        } catch (SQLException e) {
            log.error("Error when executing the SQL ");
            log.error(e.getMessage(), e);
            throw new APIManagementException(e.getMessage(), e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return keyValidationInfoDTO;
    }

    public void addSubscriber(Subscriber subscriber) throws APIManagementException {
    	Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
        	conn = APIMgtDBUtil.getConnection();
        	String query = "INSERT" +
                    " INTO AM_SUBSCRIBER (USER_ID, TENANT_ID, EMAIL_ADDRESS, DATE_SUBSCRIBED)" +
                    " VALUES (?,?,?,?)";
            ps = conn.prepareStatement(query);
            ps.setString(1, subscriber.getName());
            ps.setInt(2, subscriber.getTenantId());
            ps.setString(3, subscriber.getEmail());
            ps.setTimestamp(4, new Timestamp(subscriber.getSubscribedDate().getTime()));
            ps.executeUpdate();

            int subscriberId = 0;
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                subscriberId = rs.getInt(1);
            }
            subscriber.setId(subscriberId);

            // Add default application
            Application defaultApp = new Application(APIConstants.DEFAULT_APPLICATION_NAME, subscriber);
            addApplication(defaultApp, subscriber.getName());

        } catch (SQLException e) {
			String msg = "Error in adding new subscriber: " + e.getMessage();
			log.error(msg, e);
			throw new APIManagementException(msg, e);
		} finally {
			APIMgtDBUtil.closeAllConnections(ps, conn, rs);
		}
    }

    public void updateSubscriber(Subscriber subscriber) throws APIManagementException {
    	Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
        	conn = APIMgtDBUtil.getConnection();
        	String query = "UPDATE" +
                    " AM_SUBSCRIBER SET USER_ID=?, TENANT_ID=?, EMAIL_ADDRESS=?, DATE_SUBSCRIBED=?" +
                    " WHERE SUBSCRIBER_ID=?";
            ps = conn.prepareStatement(query);
            ps.setString(1, subscriber.getName());
            ps.setInt(2, subscriber.getTenantId());
            ps.setString(3, subscriber.getEmail());
            ps.setTimestamp(4, new Timestamp(subscriber.getSubscribedDate().getTime()));
            ps.setInt(5, subscriber.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
			String msg = "Error in updating subscriber: " + e.getMessage();
			log.error(msg, e);
			throw new APIManagementException(msg, e);
		} finally {
			APIMgtDBUtil.closeAllConnections(ps, conn, rs);
		}
    }
    
    public Subscriber getSubscriber(int subscriberId) throws APIManagementException {
    	Connection conn = null;
        ResultSet rs = null;
        PreparedStatement ps = null;
        try {
        	conn = APIMgtDBUtil.getConnection();
        	String query = "SELECT" +
                    " USER_ID, TENANT_ID, EMAIL_ADDRESS, DATE_SUBSCRIBED FROM AM_SUBSCRIBER" +
                    " WHERE SUBSCRIBER_ID=?";
            ps = conn.prepareStatement(query);
            ps.setInt(1, subscriberId);
            rs = ps.executeQuery();
            if (rs.next()) {
            	Subscriber subscriber = new Subscriber(rs.getString("USER_ID"));
            	subscriber.setId(subscriberId);
            	subscriber.setTenantId(rs.getInt("TENANT_ID"));
            	subscriber.setEmail(rs.getString("EMAIL_ADDRESS"));
            	subscriber.setSubscribedDate(new java.util.Date(
            			rs.getTimestamp("DATE_SUBSCRIBED").getTime()));
            	return subscriber;
            } else {
            	return null;
            }
        } catch (SQLException e) {
			String msg = "Error in retrieving subscriber: " + e.getMessage();
			log.error(msg, e);
			throw new APIManagementException(msg, e);
		} finally {
			APIMgtDBUtil.closeAllConnections(ps, conn, rs);
		}
    }
    
    public int addSubscription(APIIdentifier identifier, String context, int applicationId)
            throws APIManagementException {

        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        int subscriptionId = -1;
        int apiId = -1;
        
        try {
            conn = APIMgtDBUtil.getConnection();
            String getApiQuery = "SELECT API_ID FROM AM_API API WHERE API_PROVIDER = ? AND " +
                    "API_NAME = ? AND API_VERSION = ?";
            ps = conn.prepareStatement(getApiQuery);
            ps.setString(1, identifier.getProviderName());
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                apiId = resultSet.getInt("API_ID");
            }
            resultSet.close();
            ps.close();

            if (apiId == -1) {
                throw new APIManagementException("Unable to get the API ID for: " + identifier);
            }

            //This query to update the AM_SUBSCRIPTION table
            String sqlQuery = "INSERT " +
                    "INTO AM_SUBSCRIPTION (TIER_ID,API_ID,APPLICATION_ID)" +
                    " VALUES (?,?,?)";

            //Adding data to the AM_SUBSCRIPTION table
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, identifier.getTier());
            ps.setInt(2, apiId);
            ps.setInt(3, applicationId);

            ps.executeUpdate();            
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()) {
                subscriptionId = rs.getInt(1);
            }
            ps.close();

            // finally commit transaction
            conn.commit();

        } catch (SQLException e) {
            String msg = "Failed to add subscriber data ";
            log.error(msg, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add subscription ", e);
                }
            }
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
        return subscriptionId;
    }

    public void removeSubscription(APIIdentifier identifier, int applicationId) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;
        int subscriptionId = -1;
        int apiId = -1;

        try {
            conn = APIMgtDBUtil.getConnection();
            String getApiQuery = "SELECT API_ID FROM AM_API API WHERE API_PROVIDER = ? AND " +
                    "API_NAME = ? AND API_VERSION = ?";
            ps = conn.prepareStatement(getApiQuery);
            ps.setString(1, identifier.getProviderName());
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                apiId = resultSet.getInt("API_ID");
            }
            resultSet.close();
            ps.close();

            if (apiId == -1) {
                throw new APIManagementException("Unable to get the API ID for: " + identifier);
            }

            //This query to updates the AM_SUBSCRIPTION table
            String sqlQuery = "DELETE FROM AM_SUBSCRIPTION WHERE API_ID = ? AND APPLICATION_ID = ?";

            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            ps.setInt(2, applicationId);
            ps.executeUpdate();

            // finally commit transaction
            conn.commit();

        } catch (SQLException e) {
            String msg = "Failed to add subscriber data ";
            log.error(msg, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add subscription ", e);
                }
            }
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
    }

    /**
     * This method used tot get Subscriber from subscriberId.
     *
     * @param subscriberName id
     * @return Subscriber
     * @throws APIManagementException if failed to get Subscriber from subscriber id
     */
    public Subscriber getSubscriber(String subscriberName) throws APIManagementException {

        Connection conn = null;
        Subscriber subscriber = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        int tenantId;
        try {
            tenantId = IdentityUtil.getTenantIdOFUser(subscriberName);
        } catch (IdentityException e) {
            String msg = "Failed to get tenant id of user : " + subscriberName;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }

        String sqlQuery = "SELECT " +
                "   SUBSCRIBER_ID, " +
                "   USER_ID, " +
                "   TENANT_ID, " +
                "   EMAIL_ADDRESS, " +
                "   DATE_SUBSCRIBED " +
                "FROM " +
                "   AM_SUBSCRIBER " +
                "WHERE " +
                "   USER_ID = ? " +
                "   AND TENANT_ID = ?";
        try {
            conn = APIMgtDBUtil.getConnection();

            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, subscriberName);
            ps.setInt(2, tenantId);
            result = ps.executeQuery();

            if (result.next()) {
                subscriber = new Subscriber(result.getString(
                        APIConstants.SUBSCRIBER_FIELD_EMAIL_ADDRESS));
                subscriber.setEmail(result.getString("EMAIL_ADDRESS"));
                subscriber.setId(result.getInt("SUBSCRIBER_ID"));
                subscriber.setName(subscriberName);
                subscriber.setSubscribedDate(result.getDate(
                        APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
                subscriber.setTenantId(result.getInt("TENANT_ID"));
            }

        } catch (SQLException e) {
            String msg = "Failed to get Subscriber for :" + subscriberName;
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, result);
        }
        return subscriber;
    }
    
    public Set<APIIdentifier> getAPIByConsumerKey(String accessToken) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String getAPISql = "SELECT" +
                " API.API_PROVIDER," +
                " API.API_NAME," +
                " API.API_VERSION " +
                "FROM" +
                " AM_SUBSCRIPTION SUB," +
                " AM_SUBSCRIPTION_KEY_MAPPING SKM, " +
                " AM_API API " +
                "WHERE" +
                " SKM.ACCESS_TOKEN=?" +
                " AND SKM.SUBSCRIPTION_ID=SUB.SUBSCRIPTION_ID" +
                " AND API.API_ID = SUB.API_ID";

        Set<APIIdentifier> apiList = new HashSet<APIIdentifier>();
        try {
            connection = APIMgtDBUtil.getConnection();
            PreparedStatement nestedPS = connection.prepareStatement(getAPISql);
            nestedPS.setString(1, accessToken);
            ResultSet nestedRS = nestedPS.executeQuery();
            while (nestedRS.next()) {
                apiList.add(new APIIdentifier(nestedRS.getString("API_PROVIDER"),
                        nestedRS.getString("API_NAME"),
                        nestedRS.getString("API_VERSION")));
            }
        } catch (SQLException e) {
            String msg = "Failed to get API ID for token: " + accessToken;
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiList;
    }

    /**
     * This method returns the set of APIs for given subscriber
     *
     * @param subscriber subscriber
     * @return Set<API>
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to get SubscribedAPIs
     */
    public Set<SubscribedAPI> getSubscribedAPIs(Subscriber subscriber) throws APIManagementException {
        Set<SubscribedAPI> subscribedAPIs = new LinkedHashSet<SubscribedAPI> ();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            connection = APIMgtDBUtil.getConnection();

            String sqlQuery = "SELECT " +
                    "   SUBS.SUBSCRIPTION_ID" +
                    "   ,API.API_PROVIDER AS API_PROVIDER" +
                    "   ,API.API_NAME AS API_NAME" +
                    "   ,API.API_VERSION AS API_VERSION" +
                    "   ,SUBS.TIER_ID AS TIER_ID" +
                    "   ,APP.APPLICATION_ID AS APP_ID" +
                    "   ,SUBS.LAST_ACCESSED AS LAST_ACCESSED" +
                    "   ,APP.NAME AS APP_NAME " +
                    "FROM " +
                    "   AM_SUBSCRIBER SUB," +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIPTION SUBS, " +
                    "   AM_API API " +
                    "WHERE " +
                    "   SUB.USER_ID = ? " +
                    "   AND SUB.TENANT_ID = ? " +
                    "   AND SUB.SUBSCRIBER_ID=APP.SUBSCRIBER_ID " +
                    "   AND APP.APPLICATION_ID=SUBS.APPLICATION_ID " +
                    "   AND API.API_ID=SUBS.API_ID";
            
            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, subscriber.getName());
            int tenantId = IdentityUtil.getTenantIdOFUser(subscriber.getName());
            ps.setInt(2, tenantId);
            result = ps.executeQuery();

            if (result == null) {
                return subscribedAPIs;
            }
            
            Map<String,Set<SubscribedAPI>> map = new TreeMap<String, Set<SubscribedAPI>>();
            LRUCache<Integer,Application> applicationCache = new LRUCache<Integer, Application>(100);

            while (result.next()) {
                APIIdentifier apiIdentifier = new APIIdentifier(result.getString("API_PROVIDER"),
                        result.getString("API_NAME"), result.getString("API_VERSION"));

                SubscribedAPI subscribedAPI = new SubscribedAPI(subscriber, apiIdentifier);
                subscribedAPI.setTier(new Tier(
                        result.getString(APIConstants.SUBSCRIPTION_FIELD_TIER_ID)));
                subscribedAPI.setLastAccessed(result.getDate(
                        APIConstants.SUBSCRIPTION_FIELD_LAST_ACCESS));
                //setting NULL for subscriber. If needed, Subscriber object should be constructed &
                // passed in
                int applicationId = result.getInt("APP_ID");
                Application application = applicationCache.get(applicationId);
                if (application == null) {
                    application = new Application(result.getString("APP_NAME"), subscriber);
                    application.setId(result.getInt("APP_ID"));
                    Set<APIKey> keys = getApplicationKeys(applicationId);
                    for (APIKey key : keys) {
                        application.addKey(key);
                    }
                    applicationCache.put(applicationId, application);
                }
                subscribedAPI.setApplication(application);
                
                int subscriptionId = result.getInt(APIConstants.SUBSCRIPTION_FIELD_SUBSCRIPTION_ID);
                Set<APIKey> apiKeys = getAPIKeysBySubscription(subscriptionId);
                for (APIKey key : apiKeys) {
                    subscribedAPI.addKey(key);
                }

                if (!map.containsKey(application.getName())) {
                    map.put(application.getName(), new TreeSet<SubscribedAPI>(new Comparator<SubscribedAPI>() {
                        public int compare(SubscribedAPI o1, SubscribedAPI o2) {
                            int placement = o1.getApiId().getApiName().compareTo(o2.getApiId().getApiName());
                            if (placement == 0) {
                                return new APIVersionComparator().compare(new API(o1.getApiId()),
                                        new API(o2.getApiId()));
                            }
                            return placement;
                        }
                    }));
                }
                map.get(application.getName()).add(subscribedAPI);
            }
            
            for (String application : map.keySet()) {
                Set<SubscribedAPI> apis = map.get(application);
                for (SubscribedAPI api : apis) {
                    subscribedAPIs.add(api);
                }
            }

        } catch (SQLException e) {
            String msg = "Failed to get SubscribedAPI of :" + subscriber.getName();
            throw new APIManagementException(msg, e);
        } catch (IdentityException e) {
            String msg = "Failed get tenant id of user " + subscriber.getName();
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscribedAPIs;
    }
    
    private Set<APIKey> getAPIKeysBySubscription(int subscriptionId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String getKeysSql = "SELECT " +
                " SKM.ACCESS_TOKEN AS ACCESS_TOKEN," +
                " SKM.KEY_TYPE AS TOKEN_TYPE " +
                "FROM" +
                " AM_SUBSCRIPTION_KEY_MAPPING SKM " +
                "WHERE" +
                " SKM.SUBSCRIPTION_ID = ?";

        Set<APIKey> apiKeys = new HashSet<APIKey>();
        try {
            connection = APIMgtDBUtil.getConnection();
            PreparedStatement nestedPS = connection.prepareStatement(getKeysSql);
            nestedPS.setInt(1, subscriptionId);
            ResultSet nestedRS = nestedPS.executeQuery();
            while (nestedRS.next()) {
                APIKey apiKey = new APIKey();
                apiKey.setKey(nestedRS.getString("ACCESS_TOKEN"));
                apiKey.setType(nestedRS.getString("TOKEN_TYPE"));
                apiKeys.add(apiKey);
            }
        } catch (SQLException e) {
            String msg = "Failed to get API keys for subscription: " + subscriptionId;
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiKeys;
    }

    private Set<APIKey> getApplicationKeys(int applicationId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        String getKeysSql = "SELECT " +
                " AKM.ACCESS_TOKEN AS ACCESS_TOKEN," +
                " AKM.KEY_TYPE AS TOKEN_TYPE " +
                "FROM" +
                " AM_APPLICATION_KEY_MAPPING AKM " +
                "WHERE" +
                " AKM.APPLICATION_ID = ?";

        Set<APIKey> apiKeys = new HashSet<APIKey>();
        try {
            connection = APIMgtDBUtil.getConnection();
            PreparedStatement nestedPS = connection.prepareStatement(getKeysSql);
            nestedPS.setInt(1, applicationId);
            ResultSet nestedRS = nestedPS.executeQuery();
            while (nestedRS.next()) {
                APIKey apiKey = new APIKey();
                apiKey.setKey(nestedRS.getString("ACCESS_TOKEN"));
                apiKey.setType(nestedRS.getString("TOKEN_TYPE"));
                apiKeys.add(apiKey);
            }
        } catch (SQLException e) {
            String msg = "Failed to get keys for application: " + applicationId;
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return apiKeys;
    }

    /**
     * This method returns the set of Subscribers for given provider
     *
     * @param providerName name of the provider
     * @return Set<Subscriber>
     * @throws APIManagementException if failed to get subscribers for given provider
     */
    public Set<Subscriber> getSubscribersOfProvider(String providerName)
            throws APIManagementException {

        Set<Subscriber> subscribers = new HashSet<Subscriber>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            connection = APIMgtDBUtil.getConnection();

            String sqlQuery = "SELECT " +
                    "   SUBS.USER_ID AS USER_ID," +
                    "   SUBS.EMAIL_ADDRESS AS EMAIL_ADDRESS, " +
                    "   SUBS.DATE_SUBSCRIBED AS DATE_SUBSCRIBED " +
                    "FROM " +
                    "   AM_SUBSCRIBER  SUBS," +
                    "   AM_APPLICATION  APP, " +
                    "   AM_SUBSCRIPTION SUB, " +
                    "   AM_API API " +
                    "WHERE  " +
                    "   SUB.APPLICATION_ID = APP.APPLICATION_ID " +
                    "   AND SUBS. SUBSCRIBER_ID = APP.SUBSCRIBER_ID " +
                    "   AND API.API_ID = SUB.API_ID " +
                    "   AND API.API_PROVIDER = ?";


            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, providerName);
            result = ps.executeQuery();

            while (result.next()) {
                // Subscription table should have API_VERSION AND API_PROVIDER
                Subscriber subscriber =
                        new Subscriber(result.getString(
                                APIConstants.SUBSCRIBER_FIELD_EMAIL_ADDRESS));
                subscriber.setName(result.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID));
                subscriber.setSubscribedDate(result.getDate(
                        APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
                subscribers.add(subscriber);
            }

        } catch (SQLException e) {
            String msg = "Failed to subscribers for :" + providerName;
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscribers;
    }

    public Set<Subscriber> getSubscribersOfAPI(APIIdentifier identifier)
            throws APIManagementException {

        Set<Subscriber> subscribers = new HashSet<Subscriber>();
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            connection = APIMgtDBUtil.getConnection();


            String sqlQuery = "SELECT " +
                    "SB.USER_ID, SB.DATE_SUBSCRIBED " +
                    "FROM AM_SUBSCRIBER SB, AM_SUBSCRIPTION SP,AM_APPLICATION APP,AM_API API" +
                    " WHERE API.API_PROVIDER=? " +
                    "AND API.API_NAME=? " +
                    "AND API.API_VERSION=? " +
                    "AND SP.APPLICATION_ID=APP.APPLICATION_ID" +
                    " AND APP.SUBSCRIBER_ID=SB.SUBSCRIBER_ID " +
                    " AND API.API_ID = SP.API_ID";

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, identifier.getProviderName());
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            result = ps.executeQuery();
            if (result == null) {
                return subscribers;
            }
            while (result.next()) {
                Subscriber subscriber =
                        new Subscriber(result.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID));
                subscriber.setSubscribedDate(
                        result.getTimestamp(APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
                subscribers.add(subscriber);
            }

        } catch (SQLException e) {
            String msg = "Failed to get subscribers for :" + identifier.getApiName();
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscribers;
    }

    public long getAPISubscriptionCountByAPI(APIIdentifier identifier)
            throws APIManagementException {

        String sqlQuery = "SELECT" +
                " COUNT(SUB.SUBSCRIPTION_ID) AS SUB_ID" +
                " FROM AM_SUBSCRIPTION SUB, AM_API API " +
                " WHERE API.API_PROVIDER=? " +
                " AND API.API_NAME=?" +
                " AND API.API_VERSION=?" +
                " AND API.API_ID=SUB.API_ID";
        long subscriptions = 0;

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            connection = APIMgtDBUtil.getConnection();

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, identifier.getProviderName());
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            result = ps.executeQuery();
            if (result == null) {
                return subscriptions;
            }
            while (result.next()) {
                subscriptions = result.getLong("SUB_ID");
            }
        } catch (SQLException e) {
            String msg = "Failed to get subscription count for API";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscriptions;
    }

    /**
     * This method is used to update the subscriber
     *
     * @param identifier APIIdentifier
     * @param context Context of the API
     * @param applicationId Application id
     * @throws org.wso2.carbon.apimgt.api.APIManagementException
     *          if failed to update subscriber
     */
    public void updateSubscriptions(APIIdentifier identifier, String context, int applicationId)
            throws APIManagementException {
        addSubscription(identifier, context, applicationId);
    }

    /**
     * @param consumerKey ConsumerKey
     * @param applicationName Application name
     * @param userId  User Id
     * @param tenantId Tenant Id of the user
     * @param apiInfoDTO Application Info DTO
     * @param keyType Type (scope) of the key
     * @return accessToken
     * @throws IdentityException if failed to register accessToken
     */
    public String registerAccessToken(String consumerKey, String applicationName, String userId, 
                                      int tenantId, APIInfoDTO apiInfoDTO, String keyType) throws IdentityException {
        // Add Access Token
        String sqlAddAccessToken = "INSERT" +
                " INTO IDENTITY_OAUTH2_ACCESS_TOKEN (ACCESS_TOKEN, CONSUMER_KEY, TOKEN_STATE, TOKEN_SCOPE) " +
                " VALUES (?,?,?,?)";
        
        String getSubscriptionId = "SELECT SUBS.SUBSCRIPTION_ID " +
                "FROM " +
                "  AM_SUBSCRIPTION SUBS, " +
                "  AM_APPLICATION APP, " +
                "  AM_SUBSCRIBER SUB, " +
                "  AM_API API " +
                "WHERE " +
                "  SUB.USER_ID = ?" +
                "  AND SUB.TENANT_ID = ?" +
                "  AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID" +
                "  AND APP.NAME = ?" +
                "  AND API.API_PROVIDER = ?" +
                "  AND API.API_NAME = ?" +
                "  AND API.API_VERSION = ?" +
                "  AND APP.APPLICATION_ID = SUBS.APPLICATION_ID" +
                "  AND API.API_ID = SUBS.API_ID";

        String addSubscriptionKeyMapping = "INSERT " +
                "INTO AM_SUBSCRIPTION_KEY_MAPPING (SUBSCRIPTION_ID, ACCESS_TOKEN, KEY_TYPE) " +
                "VALUES (?,?,?)";

        //String apiId = apiInfoDTO.getProviderId()+"_"+apiInfoDTO.getApiName()+"_"+apiInfoDTO.getVersion();
        String accessToken = OAuthUtil.getRandomNumber();

        Connection connection = null;
        PreparedStatement prepStmt = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            //Add access token
            prepStmt = connection.prepareStatement(sqlAddAccessToken);
            prepStmt.setString(1, accessToken);
            prepStmt.setString(2, consumerKey);
            prepStmt.setString(3, APIConstants.TokenStatus.ACTIVE);
            prepStmt.setString(4, keyType);
            prepStmt.execute();
            prepStmt.close();

            //Update subscription with new key context mapping
            int subscriptionId = -1;
            prepStmt = connection.prepareStatement(getSubscriptionId);
            prepStmt.setString(1, userId);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, applicationName);
            prepStmt.setString(4, apiInfoDTO.getProviderId());
            prepStmt.setString(5, apiInfoDTO.getApiName());
            prepStmt.setString(6, apiInfoDTO.getVersion());
            ResultSet getSubscriptionIdResult = prepStmt.executeQuery();
            while (getSubscriptionIdResult.next()) {
                subscriptionId = getSubscriptionIdResult.getInt(1);
            }
            prepStmt.close();

            prepStmt = connection.prepareStatement(addSubscriptionKeyMapping);
            prepStmt.setInt(1, subscriptionId);
            prepStmt.setString(2, accessToken);
            prepStmt.setString(3, keyType);
            prepStmt.execute();
            prepStmt.close();

            connection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add access token ", e);
                }
            }
          //  throw new IdentityException("Error when storing the access code for consumer key : " + consumerKey);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return accessToken;
    }

    public String registerApplicationAccessToken(String consumerKey, String applicationName, String userId,
                                      int tenantId, String keyType) throws IdentityException {
        // Add Access Token
        String sqlAddAccessToken = "INSERT" +
                " INTO IDENTITY_OAUTH2_ACCESS_TOKEN (ACCESS_TOKEN, CONSUMER_KEY, TOKEN_STATE, TOKEN_SCOPE) " +
                " VALUES (?,?,?,?)";

        String getApplicationId = "SELECT APP.APPLICATION_ID " +
                "FROM " +
                "  AM_APPLICATION APP, " +
                "  AM_SUBSCRIBER SUB " +
                "WHERE " +
                "  SUB.USER_ID = ?" +
                "  AND SUB.TENANT_ID = ?" +
                "  AND APP.NAME = ?" +
                "  AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID";

        String addApplicationKeyMapping = "INSERT " +
                "INTO AM_APPLICATION_KEY_MAPPING (APPLICATION_ID, ACCESS_TOKEN, KEY_TYPE) " +
                "VALUES (?,?,?)";

        String accessToken = OAuthUtil.getRandomNumber();

        Connection connection = null;
        PreparedStatement prepStmt = null;
        try {
            connection = APIMgtDBUtil.getConnection();
            //Add access token
            prepStmt = connection.prepareStatement(sqlAddAccessToken);
            prepStmt.setString(1, accessToken);
            prepStmt.setString(2, consumerKey);
            prepStmt.setString(3, APIConstants.TokenStatus.ACTIVE);
            prepStmt.setString(4, keyType);
            prepStmt.execute();
            prepStmt.close();

            int applicationId = -1;
            prepStmt = connection.prepareStatement(getApplicationId);
            prepStmt.setString(1, userId);
            prepStmt.setInt(2, tenantId);
            prepStmt.setString(3, applicationName);
            ResultSet getApplicationIdResult = prepStmt.executeQuery();
            while (getApplicationIdResult.next()) {
                applicationId = getApplicationIdResult.getInt(1);
            }
            prepStmt.close();

            prepStmt = connection.prepareStatement(addApplicationKeyMapping);
            prepStmt.setInt(1, applicationId);
            prepStmt.setString(2, accessToken);
            prepStmt.setString(3, keyType);
            prepStmt.execute();
            prepStmt.close();

            connection.commit();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add access token ", e);
                }
            }
            //  throw new IdentityException("Error when storing the access code for consumer key : " + consumerKey);
        } finally {
            IdentityDatabaseUtil.closeAllConnections(connection, null, prepStmt);
        }
        return accessToken;
    }


    /**
     * @param apiIdentifier APIIdentifier
     * @param userId  User Id
     * @return true if user subscribed for given APIIdentifier
     * @throws APIManagementException if failed to check subscribed or not
     */
    public boolean isSubscribed(APIIdentifier apiIdentifier, String userId)
            throws APIManagementException {
        boolean isSubscribed = false;
        String apiId = apiIdentifier.getProviderName() + "_" + apiIdentifier.getApiName() + "_" +
                apiIdentifier.getVersion();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        String sqlQuery = "SELECT " +
                "   SUBS.TIER_ID ," +
                "   API.API_PROVIDER ," +
                "   API.API_NAME ," +
                "   API.API_VERSION ," +
                "   SUBS.LAST_ACCESSED ," +
                "   SUBS.APPLICATION_ID " +
                "FROM " +
                "   AM_SUBSCRIPTION SUBS," +
                "   AM_SUBSCRIBER SUB, " +
                "   AM_APPLICATION  APP, " +
                "   AM_API API " +
                "WHERE " +
                "   API.API_PROVIDER  = ?" +
                "   AND API.API_NAME = ?" +
                "   AND API.API_VERSION = ?" +
                "   AND SUB.USER_ID = ?" +
                "   AND SUB.TENANT_ID = ? " +
                "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID" +
                "   AND API.API_ID = SUBS.API_ID";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, apiIdentifier.getProviderName());
            ps.setString(2, apiIdentifier.getApiName());
            ps.setString(3, apiIdentifier.getVersion());
            ps.setString(4, userId);
            int tenantId;
            try {
                tenantId = IdentityUtil.getTenantIdOFUser(userId);
            } catch (IdentityException e) {
                throw new APIManagementException("Failed to get tenant id of user : " + userId, e);
            }
            ps.setInt(5, tenantId);

            rs = ps.executeQuery();

            if (rs.next()) {
                isSubscribed = true;
            }
        } catch (SQLException e) {
            log.error("Error when executing the SQL query : " + sqlQuery, e);
            throw new APIManagementException("Error while checking if user has subscribed to the API ", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, rs);
        }
        return isSubscribed;
    }

    /**
     * @param providerName  Name of the provider
     * @return UserApplicationAPIUsage of given provider
     * @throws org.wso2.carbon.apimgt.api.APIManagementException if failed to get
     * UserApplicationAPIUsage for given provider
     */
    public UserApplicationAPIUsage[] getAllAPIUsageByProvider(String providerName) throws APIManagementException {

        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;

        try {
            connection = APIMgtDBUtil.getConnection();

            String sqlQuery = "SELECT " +
                    "   SUBS.SUBSCRIPTION_ID AS SUBSCRIPTION_ID, " +
                    "   SUBS.TIER_ID AS TIER_ID, " +
                    "   API.API_PROVIDER AS API_PROVIDER, " +
                    "   API.API_NAME AS API_NAME, " +
                    "   API.API_VERSION AS API_VERSION, " +
                    "   SUBS.LAST_ACCESSED AS LAST_ACCESSED, " +
                    "   SUB.USER_ID AS USER_ID, " +
                    "   APP.NAME AS APPNAME " +
                    "FROM " +
                    "   AM_SUBSCRIPTION SUBS, " +
                    "   AM_APPLICATION APP, " +
                    "   AM_SUBSCRIBER SUB, " +
                    "   AM_API API " +
                    "WHERE " +
                    "   SUBS.APPLICATION_ID = APP.APPLICATION_ID " +
                    "   AND APP.SUBSCRIBER_ID = SUB.SUBSCRIBER_ID " +
                    "   AND API.API_PROVIDER = ? " +
                    "   AND API.API_ID = SUBS.API_ID " +
                    "ORDER BY " +
                    "   APP.NAME";

            ps = connection.prepareStatement(sqlQuery);
            ps.setString(1, providerName);
            result = ps.executeQuery();

            Map<String,UserApplicationAPIUsage> userApplicationUsages = new TreeMap<String, UserApplicationAPIUsage>();
            while (result.next()) {
                String userId = result.getString("USER_ID");
                String application = result.getString("APPNAME");
                String key = userId + "::" + application;
                UserApplicationAPIUsage usage = userApplicationUsages.get(key);
                if (usage == null) {
                    usage = new UserApplicationAPIUsage();
                    usage.setUserId(userId);
                    usage.setApplicationName(application);
                    userApplicationUsages.put(key, usage);
                }
                
                usage.addApiIdentifier(new APIIdentifier(result.getString("API_PROVIDER"),
                        result.getString("API_NAME"), result.getString("API_VERSION")));
            }
            return userApplicationUsages.values().toArray(
                    new UserApplicationAPIUsage[userApplicationUsages.size()]);

        } catch (SQLException e) {
            String msg = "Failed to find API Usage for :" + providerName;
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }

    }

    /**
     * return the subscriber for given access token
     * @param accessToken AccessToken
     * @return Subscriber
     * @throws APIManagementException  if failed to get subscriber for given access token
     */
    public Subscriber getSubscriberById(String accessToken) throws APIManagementException {
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet result = null;
        Subscriber subscriber = null;
        String query = " SELECT" +
                " SB.USER_ID, SB.DATE_SUBSCRIBED" +
                " FROM AM_SUBSCRIBER SB , AM_SUBSCRIPTION SP, AM_APPLICATION APP, AM_SUBSCRIPTION_KEY_MAPPING SKM" +
                " WHERE SKM.ACCESS_TOKEN=?" +
                " AND SP.APPLICATION_ID=APP.APPLICATION_ID" +
                " AND APP.SUBSCRIBER_ID=SB.SUBSCRIBER_ID" +
                " AND SP.SUBSCRIPTION_ID=SKM.SUBSCRIPTION_ID";

        try {
            connection = APIMgtDBUtil.getConnection();
            ps = connection.prepareStatement(query);
            ps.setString(1, accessToken);

            result = ps.executeQuery();
            while (result.next()) {
                subscriber = new Subscriber(result.getString(APIConstants.SUBSCRIBER_FIELD_USER_ID));
                subscriber.setSubscribedDate(result.getDate(APIConstants.SUBSCRIBER_FIELD_DATE_SUBSCRIBED));
            }

        } catch (SQLException e) {
            String msg = "Failed to get Subscriber for accessToken";
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, connection, result);
        }
        return subscriber;
    }

    public String[] addOAuthConsumer(String username, int tenantId) throws IdentityOAuthAdminException, APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        String sqlStmt = "INSERT INTO IDENTITY_OAUTH_CONSUMER_APPLICATIONS " +
                "(CONSUMER_KEY, CONSUMER_SECRET, USERNAME, TENANT_ID, OAUTH_VERSION) VALUES (?,?,?,?,?) ";
        String consumerKey;
        String consumerSecret = OAuthUtil.getRandomNumber();

        do {
            consumerKey = OAuthUtil.getRandomNumber();
        }
        while (isDuplicateConsumer(consumerKey));

        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlStmt);
            prepStmt.setString(1, consumerKey);
            prepStmt.setString(2, consumerSecret);
            prepStmt.setString(3, username);
            prepStmt.setInt(4, tenantId);
            // it is assumed that the OAuth version is 1.0a because this is required with OAuth 1.0a
            prepStmt.setString(5, OAuthConstants.OAuthVersions.VERSION_1A);
            prepStmt.execute();

            connection.commit();

        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + sqlStmt);
            throw new APIManagementException("Error when adding a new OAuth consumer.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, null);
        }
        return new String[]{consumerKey, consumerSecret};
    }

    private boolean isDuplicateConsumer(String consumerKey) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rSet = null;
        String sqlQuery = "SELECT * FROM IDENTITY_OAUTH_CONSUMER_APPLICATIONS " +
                "WHERE CONSUMER_KEY=?";

        boolean isDuplicateConsumer = false;

        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, consumerKey);

            rSet = prepStmt.executeQuery();
            if (rSet.next()) {
                isDuplicateConsumer = true;
            }
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + sqlQuery);
            throw new APIManagementException("Error when reading the application information from" +
                    " the persistence store.",e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rSet);
        }
        return isDuplicateConsumer;
    }


    /**
     *
     * @param application Application
     * @param userId  User Id
     * @throws APIManagementException  if failed to add Application
     */
    public void addApplication(Application application,String userId) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {

            conn = APIMgtDBUtil.getConnection();
            int tenantId;
            try {
                tenantId = IdentityUtil.getTenantIdOFUser(userId);
            } catch (IdentityException e) {
                String msg = "Failed to get tenant id of user : " + userId;
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }
            //Get subscriber Id
            Subscriber subscriber = getSubscriber(userId,tenantId);
            if(subscriber == null){
                throw new APIManagementException("Could not load Subscriber record for : "+userId);
            }
            //This query to update the AM_APPLICATION table
            String sqlQuery = "INSERT " +
                    "INTO AM_APPLICATION (NAME, SUBSCRIBER_ID)" +
                    " VALUES (?,?)";
            // Adding data to the AM_APPLICATION  table
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, application.getName());
            ps.setInt(2, subscriber.getId());

            ps.executeUpdate();
            ps.close();
            // finally commit transaction
            conn.commit();

        } catch (SQLException e) {
            String msg = "Failed to add Application";
            log.error(msg, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the add Application ", e);
                }
            }
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }

    }

    public void updateApplication(Application application) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        try {
            conn = APIMgtDBUtil.getConnection();

            //This query to update the AM_APPLICATION table
            String sqlQuery = "UPDATE " +
                    "AM_APPLICATION" +
                    " SET NAME = ? " +
                    "WHERE" +
                    " APPLICATION_ID = ?";
            // Adding data to the AM_APPLICATION  table
            ps = conn.prepareStatement(sqlQuery);
            ps.setString(1, application.getName());
            ps.setInt(2, application.getId());

            ps.executeUpdate();
            ps.close();
            // finally commit transaction
            conn.commit();

        } catch (SQLException e) {
            String msg = "Failed to update Application";
            log.error(msg, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the update Application ", e);
                }
            }
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
    }

    /**
     *
     * @param subscriber Subscriber
     * @return Applications for given subscriber.
     * @throws APIManagementException if failed to get Applications for given subscriber.
     */
    public Application[] getApplications(Subscriber subscriber) throws APIManagementException {
        if (subscriber == null){
            return null;
        }
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Application[] applications;

        String sqlQuery = "SELECT " +
                "   APPLICATION_ID " +
                "   ,NAME" +
                "   ,SUBSCRIBER_ID  " +
                "FROM " +
                "   AM_APPLICATION " +
                "WHERE " +
                "   SUBSCRIBER_ID  = ?";

        try {
            int tenantId;
            try {
                tenantId = IdentityUtil.getTenantIdOFUser(subscriber.getName());
            } catch (IdentityException e) {
                String msg = "Failed to get tenant id of user : " + subscriber.getName();
                log.error(msg, e);
                throw new APIManagementException(msg, e);
            }

            //getSubscriberId
            if(subscriber.getId() == 0){
                Subscriber subs;
                subs = getSubscriber(subscriber.getName(), tenantId);
                if (subs == null) {
                    return null;
                }else{
                    subscriber = subs;
                }
            }

            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setInt(1,subscriber.getId());
            rs = prepStmt.executeQuery();

            ArrayList<Application> applicationsList = new ArrayList<Application>();
            Application application;
            while(rs.next()){
                application = new Application(rs.getString("NAME"),subscriber);
                application.setId(rs.getInt("APPLICATION_ID"));
                applicationsList.add(application);
            }
            applications = applicationsList.toArray(new Application[applicationsList.size()]);
            return applications;

        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + sqlQuery);
            throw new APIManagementException("Error when reading the application information from" +
                    " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
    }
    
    public void deleteApplication(Application application) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        
        String getSubscriptionsQuery = "SELECT" +
                " SUBSCRIPTION_ID " +
                "FROM" +
                " AM_SUBSCRIPTION " +
                "WHERE" +
                " APPLICATION_ID = ?";
        
        String deleteKeyMappingQuery = "DELETE FROM AM_SUBSCRIPTION_KEY_MAPPING WHERE SUBSCRIPTION_ID = ?";
        String deleteSubscriptionsQuery = "DELETE FROM AM_SUBSCRIPTION WHERE APPLICATION_ID = ?";
        String deleteApplicationKeyQuery = "DELETE FROM AM_APPLICATION_KEY_MAPPING WHERE APPLICATION_ID = ?";
        String deleteApplicationQuery = "DELETE FROM AM_APPLICATION WHERE APPLICATION_ID = ?";

        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(getSubscriptionsQuery);
            prepStmt.setInt(1, application.getId());
            rs = prepStmt.executeQuery();
            
            List<Integer> subscriptions = new ArrayList<Integer>();
            while (rs.next()) {
                subscriptions.add(rs.getInt("SUBSCRIPTION_ID"));  
            }
            prepStmt.close();
            rs.close();
            
            prepStmt = connection.prepareStatement(deleteKeyMappingQuery);
            for (Integer subscriptionId : subscriptions) {
                prepStmt.setInt(1, subscriptionId);
                prepStmt.execute();
            }
            prepStmt.close();
            
            prepStmt = connection.prepareStatement(deleteSubscriptionsQuery);
            prepStmt.setInt(1, application.getId());
            prepStmt.execute();
            prepStmt.close();

            prepStmt = connection.prepareStatement(deleteApplicationKeyQuery);
            prepStmt.setInt(1, application.getId());
            prepStmt.execute();
            prepStmt.close();

            prepStmt = connection.prepareStatement(deleteApplicationQuery);
            prepStmt.setInt(1, application.getId());
            prepStmt.execute();
            
            connection.commit();            
        } catch (SQLException e) {
            String msg = "Error while removing application details from the database";
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
    }


    /**
     * returns a subscriber record for given username,tenant Id
     * @param username UserName
     * @param tenantId Tenant Id
     * @return  Subscriber
     * @throws APIManagementException if failed to get subscriber
     */
    private Subscriber getSubscriber(String username, int tenantId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        Subscriber subscriber = null;
        String sqlQuery = "SELECT " +
                "   SUB.SUBSCRIBER_ID AS SUBSCRIBER_ID" +
                "   ,SUB.USER_ID AS USER_ID " +
                "   ,SUB.TENANT_ID AS TENANT_ID" +
                "   ,SUB.EMAIL_ADDRESS AS EMAIL_ADDRESS" +
                "   ,SUB.DATE_SUBSCRIBED AS DATE_SUBSCRIBED " +
                "FROM " +
                "   AM_SUBSCRIBER SUB " +
                "WHERE " +
                "SUB.USER_ID = ? " +
                "AND SUB.TENANT_ID = ?";


        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, username);
            prepStmt.setInt(2,tenantId);
            rs = prepStmt.executeQuery();

            if (rs.next()) {
                subscriber = new Subscriber(rs.getString("USER_ID"));
                subscriber.setEmail(rs.getString("EMAIL_ADDRESS"));
                subscriber.setId(rs.getInt("SUBSCRIBER_ID"));
                subscriber.setSubscribedDate(rs.getDate("DATE_SUBSCRIBED"));
                subscriber.setTenantId(rs.getInt("TENANT_ID"));
                return subscriber;
            }
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + sqlQuery);
            throw new APIManagementException("Error when reading the application information from" +
                    " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return subscriber;
    }

    public void recordAPILifeCycleEvent(APIIdentifier identifier, APIStatus oldStatus,
                                        APIStatus newStatus, String userId) throws APIManagementException {
        Connection conn = null;
        ResultSet resultSet = null;
        PreparedStatement ps = null;

        int tenantId;
        int apiId = -1;
        try {
            tenantId = IdentityUtil.getTenantIdOFUser(userId);
        } catch (IdentityException e) {
            String msg = "Failed to get tenant id of user : " + userId;
            log.error(msg, e);
            throw new APIManagementException(msg, e);
        }
        
        if (oldStatus == null && !newStatus.equals(APIStatus.CREATED)) {
            throw new APIManagementException("Invalid old and new state combination");
        } else if (oldStatus != null && oldStatus.equals(newStatus)) {
            throw new APIManagementException("No measurable differences in API state");
        }

        String getAPIQuery = "SELECT " +
                "API.API_ID FROM AM_API API" +
                " WHERE " +
                "API.API_PROVIDER = ?" +
                "AND API.API_NAME = ?" +
                "AND API.API_VERSION = ?";

        String sqlQuery = "INSERT " +
                "INTO AM_API_LC_EVENT (API_ID, PREVIOUS_STATE, NEW_STATE, USER_ID, TENANT_ID, EVENT_DATE)" +
                " VALUES (?,?,?,?,?,?)";

        try {
            conn = APIMgtDBUtil.getConnection();
            ps = conn.prepareStatement(getAPIQuery);
            ps.setString(1, identifier.getProviderName());
            ps.setString(2, identifier.getApiName());
            ps.setString(3, identifier.getVersion());
            resultSet = ps.executeQuery();
            if (resultSet.next()) {
                apiId = resultSet.getInt("API_ID");
            }
            resultSet.close();
            ps.close();
            if (apiId == -1) {
                throw new APIManagementException("Unable to find the API: " + identifier);
            }
            
            ps = conn.prepareStatement(sqlQuery);
            ps.setInt(1, apiId);
            if (oldStatus != null) {
                ps.setString(2, oldStatus.getStatus());
            } else {
                ps.setNull(2, Types.VARCHAR);
            }
            ps.setString(3, newStatus.getStatus());
            ps.setString(4, userId);
            ps.setInt(5, tenantId);
            ps.setTimestamp(6, new Timestamp(System.currentTimeMillis()));

            ps.executeUpdate();
            ps.close();
            // finally commit transaction
            conn.commit();

        } catch (SQLException e) {
            String msg = "Failed to record API state change";
            log.error(msg, e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    log.error("Failed to rollback the API state change record", e);
                }
            }
            throw new APIManagementException(msg, e);
        } finally {
            APIMgtDBUtil.closeAllConnections(ps, conn, resultSet);
        }
    }
    
    public List<LifeCycleEvent> getLifeCycleEvents(APIIdentifier apiId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String sqlQuery = "SELECT" +
                " LC.API_ID AS API_ID," +
                " LC.PREVIOUS_STATE AS PREVIOUS_STATE," +
                " LC.NEW_STATE AS NEW_STATE," +
                " LC.USER_ID AS USER_ID," +
                " LC.EVENT_DATE AS EVENT_DATE " +                
                "FROM" +
                " AM_API_LC_EVENT LC, " +
                " AM_API API " +
                "WHERE" +
                " API.API_PROVIDER = ?" +
                " AND API.API_NAME = ?" +
                " AND API.API_VERSION = ?" +
                " AND API.API_ID = LC.API_ID";

        List<LifeCycleEvent> events = new ArrayList<LifeCycleEvent>();
        
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(sqlQuery);
            prepStmt.setString(1, apiId.getProviderName());
            prepStmt.setString(2, apiId.getApiName());
            prepStmt.setString(3, apiId.getVersion());
            rs = prepStmt.executeQuery();

            while (rs.next()) {
                LifeCycleEvent event = new LifeCycleEvent();
                event.setApi(apiId);
                String oldState = rs.getString("PREVIOUS_STATE");
                event.setOldStatus(oldState != null ? APIStatus.valueOf(oldState) : null);
                event.setNewStatus(APIStatus.valueOf(rs.getString("NEW_STATE")));
                event.setUserId(rs.getString("USER_ID"));
                event.setDate(rs.getTimestamp("EVENT_DATE"));
                events.add(event);
            }

            Collections.sort(events, new Comparator<LifeCycleEvent>() {
                public int compare(LifeCycleEvent o1, LifeCycleEvent o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
        } catch (SQLException e) {
            log.error("Error when executing the SQL : " + sqlQuery);
            throw new APIManagementException("Error when reading the application information from" +
                    " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
        return events;    
    }

    public void makeKeysForwardCompatible(String provider, String apiName, String oldVersion,
                                          String newVersion, String context) throws APIManagementException {

        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        String getSubscriptionDataQuery = "SELECT" +
                " SUB.SUBSCRIPTION_ID AS SUBSCRIPTION_ID," +
                " SUB.TIER_ID AS TIER_ID," +
                " SUB.APPLICATION_ID AS APPLICATION_ID," +
                " API.CONTEXT AS CONTEXT," +
                " SKM.ACCESS_TOKEN AS ACCESS_TOKEN," +
                " SKM.KEY_TYPE AS KEY_TYPE " +
                "FROM" +
                " AM_SUBSCRIPTION SUB," +
                " AM_SUBSCRIPTION_KEY_MAPPING SKM, " +
                " AM_API API " +
                "WHERE" +
                " API.API_PROVIDER = ?" +
                " AND API.API_NAME = ?" +
                " AND API.API_VERSION = ?" +
                " AND SKM.SUBSCRIPTION_ID = SUB.SUBSCRIPTION_ID" +
                " AND API.API_ID = SUB.API_ID";
        
        String addSubKeyMapping = "INSERT INTO" +
                " AM_SUBSCRIPTION_KEY_MAPPING (SUBSCRIPTION_ID, ACCESS_TOKEN, KEY_TYPE)" +
                " VALUES (?,?,?)";

        String getApplicationDataQuery = "SELECT" +
                " SUB.SUBSCRIPTION_ID AS SUBSCRIPTION_ID," +
                " SUB.TIER_ID AS TIER_ID," +
                " APP.APPLICATION_ID AS APPLICATION_ID," +
                " API.CONTEXT AS CONTEXT " +
                "FROM" +
                " AM_SUBSCRIPTION SUB," +
                " AM_APPLICATION APP," +
                " AM_API API " +
                "WHERE" +
                " API.API_PROVIDER = ?" +
                " AND API.API_NAME = ?" +
                " AND API.API_VERSION = ?" +
                " AND SUB.APPLICATION_ID = APP.APPLICATION_ID" +
                " AND API.API_ID = SUB.API_ID";

        try {
            // Retrieve all the existing subscription for the old version
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(getSubscriptionDataQuery);
            prepStmt.setString(1, provider);
            prepStmt.setString(2, apiName);
            prepStmt.setString(3, oldVersion);
            rs = prepStmt.executeQuery();

            List<SubscriptionInfo> subscriptionData = new ArrayList<SubscriptionInfo>();
            Set<Integer> subscribedApplications = new HashSet<Integer>();
            while (rs.next()) {
                SubscriptionInfo info = new SubscriptionInfo();
                info.subscriptionId = rs.getInt("SUBSCRIPTION_ID");
                info.tierId = rs.getString("TIER_ID");
                info.context = rs.getString("CONTEXT");
                info.applicationId = rs.getInt("APPLICATION_ID");
                info.accessToken = rs.getString("ACCESS_TOKEN");
                info.tokenType = rs.getString("KEY_TYPE");                
                subscriptionData.add(info);
            }
            prepStmt.close();
            rs.close();
            
            Map<Integer,Integer> subscriptionIdMap = new HashMap<Integer, Integer>();
            APIIdentifier apiId = new APIIdentifier(provider, apiName, newVersion);
            for (SubscriptionInfo info : subscriptionData) {
                if (!subscriptionIdMap.containsKey(info.subscriptionId)) {
                    apiId.setTier(info.tierId);
                    int subscriptionId = addSubscription(apiId, context, info.applicationId);
                    if (subscriptionId == -1) {
                        throw new APIManagementException("Unable to add a new subscription for " +
                                "the API: " + apiName + ":v" + newVersion);
                    }
                    subscriptionIdMap.put(info.subscriptionId, subscriptionId);
                }
                
                int subscriptionId = subscriptionIdMap.get(info.subscriptionId);                                                
                prepStmt = connection.prepareStatement(addSubKeyMapping);
                prepStmt.setInt(1, subscriptionId);
                prepStmt.setString(2, info.accessToken);
                prepStmt.setString(3, info.tokenType);
                prepStmt.execute();
                prepStmt.close();
                
                subscribedApplications.add(info.applicationId);
            }

            prepStmt = connection.prepareStatement(getApplicationDataQuery);
            prepStmt.setString(1, provider);
            prepStmt.setString(2, apiName);
            prepStmt.setString(3, oldVersion);
            rs = prepStmt.executeQuery();
            while (rs.next()) {
                int applicationId = rs.getInt("APPLICATION_ID");
                if (!subscribedApplications.contains(applicationId)) {
                    apiId.setTier(rs.getString("TIER_ID"));
                    addSubscription(apiId, rs.getString("CONTEXT"), applicationId);
                }
            }

            connection.commit();
        } catch (SQLException e) {
            log.error("Error when executing the SQL queries");
            throw new APIManagementException("Error when reading the application information from" +
                    " the persistence store.", e);
        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }        
    }
    
    public void addAPI(API api) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        
        String query = "INSERT INTO AM_API (API_PROVIDER, API_NAME, API_VERSION, CONTEXT) " +
                "VALUES (?,?,?,?)";
        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(query);
            prepStmt.setString(1, api.getId().getProviderName());
            prepStmt.setString(2, api.getId().getApiName());
            prepStmt.setString(3, api.getId().getVersion());
            prepStmt.setString(4, api.getContext());
            prepStmt.execute();
            connection.commit();

            recordAPILifeCycleEvent(api.getId(), null, APIStatus.CREATED, api.getId().getProviderName());
        } catch (SQLException e) {
            String msg = "Error while adding the API: " + api.getId() + " to the database";
            log.error(msg, e);
            throw new APIManagementException(msg, e);

        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
    }

    public void deleteAPI(APIIdentifier apiId) throws APIManagementException {
        Connection connection = null;
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        int id = -1;
        String getAPIQuery = "SELECT " +
                             "API.API_ID FROM AM_API API" +
                             " WHERE " +
                             "API.API_PROVIDER = ?" +
                             "AND API.API_NAME = ?" +
                             "AND API.API_VERSION = ?";
        String deleteLCEventQuery = "DELETE FROM AM_API_LC_EVENT WHERE API_ID=? ";
        String deleteSubscriptionQuery = "DELETE FROM AM_SUBSCRIPTION WHERE API_ID=?";
        String deleteAPIQuery = "DELETE FROM AM_API WHERE API_PROVIDER=? AND API_NAME=? AND API_VERSION=? ";

        try {
            connection = APIMgtDBUtil.getConnection();
            prepStmt = connection.prepareStatement(getAPIQuery);
            prepStmt.setString(1, apiId.getProviderName());
            prepStmt.setString(2, apiId.getApiName());
            prepStmt.setString(3, apiId.getVersion());
            rs = prepStmt.executeQuery();
            if (rs.next()) {
                id = rs.getInt("API_ID");
            }
            rs.close();
            prepStmt.close();
            if (id == -1) {
                throw new APIManagementException("Unable to find the API: " + apiId);
            }
            prepStmt = connection.prepareStatement(deleteSubscriptionQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();

            prepStmt = connection.prepareStatement(deleteLCEventQuery);
            prepStmt.setInt(1, id);
            prepStmt.execute();

            prepStmt = connection.prepareStatement(deleteAPIQuery);
            prepStmt.setString(1, apiId.getProviderName());
            prepStmt.setString(2, apiId.getApiName());
            prepStmt.setString(3, apiId.getVersion());
            prepStmt.execute();

            connection.commit();

        } catch (SQLException e) {
            String msg = "Error while removing the API: " + apiId + " from the database";
            log.error(msg, e);
            throw new APIManagementException(msg, e);

        } finally {
            APIMgtDBUtil.closeAllConnections(prepStmt, connection, rs);
        }
    }
    
    private static class SubscriptionInfo {
        private int subscriptionId;
        private String tierId;
        private String context;
        private int applicationId;
        private String accessToken;
        private String tokenType;
    }
}