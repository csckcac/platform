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
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * User aware APIProvider implementation which ensures that the invoking user has the
 * necessary privileges to execute the operations. Users can use this class as an
 * entry point to accessing the core API provider functionality. In order to ensure
 * proper initialization and cleanup of these objects, the constructors of the class
 * has been hidden. Users should use the APIManagerFactory class to obtain an instance
 * of this class. This implementation also allows anonymous access to some of the
 * available operations. However if the user attempts to execute a privileged operation
 * when the object had been created in the anonymous mode, an exception will be thrown.
 */
public class UserAwareAPIProvider extends APIProviderImpl {
    
    private String username;
    private AuthorizationManager authorizationManager;

    UserAwareAPIProvider() throws APIManagementException {
        super();
    }
    
    UserAwareAPIProvider(String username) throws APIManagementException {
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
        checkPermission(APIConstants.Permissions.API_CREATE);
        super.updateAPI(api);
    }

    @Override
    public void changeAPIStatus(API api, APIStatus status,
                                boolean updateGatewayConfig) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_PUBLISH);
        super.changeAPIStatus(api, status, updateGatewayConfig);
    }

    @Override
    public void addDocumentation(APIIdentifier apiId,
                                 Documentation documentation) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_CREATE);
        super.addDocumentation(apiId, documentation);
    }

    @Override
    public void removeDocumentation(APIIdentifier apiId, String docName,
                                    String docType) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_CREATE);
        super.removeDocumentation(apiId, docName, docType);
    }

    @Override
    public void updateDocumentation(APIIdentifier apiId,
                                    Documentation documentation) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_CREATE);
        super.updateDocumentation(apiId, documentation);
    }

    @Override
    public void addDocumentationContent(APIIdentifier identifier, String documentationName,
                                        String text) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_CREATE);
        super.addDocumentationContent(identifier, documentationName, text);
    }

    @Override
    public void copyAllDocumentation(APIIdentifier apiId, String toVersion) throws APIManagementException {
        checkPermission(APIConstants.Permissions.API_CREATE);
        super.copyAllDocumentation(apiId, toVersion);
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
            throw new APIManagementException("User: " + username + " does not have the " +
                    "required permission: " + permission);
        }
    }
}
