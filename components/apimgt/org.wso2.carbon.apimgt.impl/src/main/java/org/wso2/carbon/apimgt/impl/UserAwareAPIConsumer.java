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

import org.wso2.carbon.CarbonException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;

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
}
