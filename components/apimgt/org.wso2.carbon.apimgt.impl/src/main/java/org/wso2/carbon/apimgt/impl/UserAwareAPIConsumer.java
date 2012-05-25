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
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Application;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * User aware APIConsumer implementation which ensures that the invoking user has the
 * necessary privileges to execute the operations. Users can use this class as an
 * entry point to accessing the core API provider functionality. In order to ensure
 * proper initialization and cleanup of these objects, the constructors of the class
 * has been hidden. Users should use the APIManagerFactory class to obtain an instance
 * of this class. This implementation also allows anonymous access to some of the
 * available operations. However if the user attempts to execute a privileged operation
 * when the object had been created in the anonymous mode, an exception will be thrown.
 */
public class UserAwareAPIConsumer extends APIConsumerImpl {

    private String username;
    private AuthorizationManager authorizationManager;

    UserAwareAPIConsumer() throws APIManagementException {
        super();
    }

    UserAwareAPIConsumer(String username) throws APIManagementException {
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
    public void addSubscription(APIIdentifier identifier,
                                String userId, int applicationId) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_SUBSCRIBE);
        super.addSubscription(identifier, userId, applicationId);
    }

    @Override
    public void addApplication(Application application, String userId) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_SUBSCRIBE);
        super.addApplication(application, userId);
    }

    @Override
    public void updateApplication(Application application) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_SUBSCRIBE);
        super.updateApplication(application);
    }

    @Override
    public void removeApplication(Application application) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_SUBSCRIBE);
        super.removeApplication(application);
    }

    @Override
    public void addComment(APIIdentifier identifier, String s, String user) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_SUBSCRIBE);
        super.addComment(identifier, s, user);
    }

    private void checkPermission(String permission) throws APIManagementException {
        if (username == null) {
            throw new APIManagementException("Attempt to execute privileged operation as" +
                    " the anonymous user");
        }

        boolean authorized;
        try {
            authorized = authorizationManager.isUserAuthorized(username, permission,
                    CarbonConstants.UI_PERMISSION_ACTION);
        } catch (UserStoreException e) {
            throw new APIManagementException("Error while checking user authorization", e);
        }

        if (!authorized) {
            throw new APIManagementException("User '" + username + "' does not have the " +
                    "required permission: " + permission);
        }
    }
}
