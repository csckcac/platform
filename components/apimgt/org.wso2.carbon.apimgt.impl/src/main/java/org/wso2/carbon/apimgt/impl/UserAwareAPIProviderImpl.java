/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.DuplicateAPIException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * User aware APIProvider implementation which ensures that the invoking user has the
 * necessary privileges to execute the operations.
 */
public class UserAwareAPIProviderImpl extends APIProviderImpl {
    
    private String username;
    private AuthorizationManager authorizationManager;
    
    public UserAwareAPIProviderImpl(String username) throws APIManagementException {
        super();
        this.username = username;
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        RealmService realmService = ServiceReferenceHolder.getInstance().getRealmService();
        try {
            UserRealm realm = AnonymousSessionUtil.getRealmByUserName(registryService,
                    realmService, username);
            authorizationManager = realm.getAuthorizationManager();
        } catch (CarbonException e) {
            handleException("Error while loading user realm for user: " + username, e);
        } catch (UserStoreException e) {
            handleException("Error while loading the authorization manager", e);
        }
    }

    @Override
    public void addAPI(API api) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_CREATE);
        super.addAPI(api);
    }

    @Override
    public void createNewAPIVersion(API api, String newVersion) throws DuplicateAPIException,
            APIManagementException {
        checkPermission(APIConstants.Permissions.API_CREATE);
        super.createNewAPIVersion(api, newVersion);
    }

    @Override
    public void updateAPI(API api) throws APIManagementException {
        API oldApi = getAPI(api.getId());
        String oldStatus = oldApi.getStatus().getStatus();
        String newStatus = api.getStatus().getStatus();
        if ("CREATED".equals(oldStatus) && "PUBLISHED".equals(newStatus)) {
            checkPermission(APIConstants.Permissions.API_PUBLISH);
        }
        super.updateAPI(api);
    }

    private void checkPermission(String permission) throws APIManagementException {
        boolean authorized;
        try {
            authorized = authorizationManager.isUserAuthorized(username, permission,
                    CarbonConstants.UI_PERMISSION_ACTION);
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while checking user authorization", e);
        }

        if (!authorized) {
            throw new APIManagementException("User: " + username + " does not have the " +
                    "required permission: " + permission);
        }
    }
}
