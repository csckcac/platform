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

package org.wso2.carbon.apimgt.keymgt.service;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.keymgt.APIKeyMgtException;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.util.IdentityUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

/**
 * This service class exposes the functionality required by the application developers who will be
 * consuming the APIs published in the API Store.
 */
public class APIKeyMgtSubscriberService extends AbstractAdmin {

    /**
     * Get the access token for a user per given API. Users/developers can use this access token
     * to consume the API by directly passing it as a bearer token as per the OAuth 2.0 specification.
     * @param userId User/Developer name
     * @param apiInfoDTO Information about the API to which the Access token will be issued.
     *                   Provider name, API name and the version should be provided to uniquely identify
     *                   an API.
     * @return  Access Token
     * @throws APIKeyMgtException Error when getting the AccessToken from the underlying token store.
     */
    public String getAccessToken(String userId, APIInfoDTO apiInfoDTO,String applicationName) throws APIKeyMgtException,
            APIManagementException, IdentityException {
        ApiMgtDAO apiMgtDAO = new ApiMgtDAO();
        String accessToken = apiMgtDAO.getAccessKeyForAPI(userId, applicationName, apiInfoDTO);
        if(accessToken == null){
            //get the tenant id for the corresponding domain
            String tenantAwareUserId = MultitenantUtils.getTenantAwareUsername(userId);
            int tenantId = IdentityUtil.getTenantIdOFUser(userId);

            String[] credentials = apiMgtDAO.addOAuthConsumer(tenantAwareUserId, tenantId);

            accessToken = apiMgtDAO.registerAccessToken(credentials[0],applicationName, tenantAwareUserId, tenantId, apiInfoDTO);
        }
        return accessToken;
    }

    /**
     * Get the list of subscribed APIs of a user
     * @param userId User/Developer name
     * @return An array of APIInfoDTO instances, each instance containing information of provider name,
     * api name and version.
     * @throws APIKeyMgtException Error when getting the list of APIs from the persistence store.
     */
    public APIInfoDTO[] getSubscribedAPIsOfUser(String userId) throws APIKeyMgtException,
            APIManagementException, IdentityException {
        ApiMgtDAO ApiMgtDAO = new ApiMgtDAO();
        return ApiMgtDAO.getSubscribedAPIsOfUser(userId);
    }

    public String renewAccessToken(String userId, APIInfoDTO apiInfoDTO) {
        return null;
    }

    public void unsubscribeFromAPI(String userId, APIInfoDTO apiInfoDTO) {

    }

}
