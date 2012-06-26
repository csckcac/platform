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

package org.wso2.carbon.apimgt.impl.utils;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.util.Map;

public class RemoteAuthorizationManager {

    private static final RemoteAuthorizationManager instance = new RemoteAuthorizationManager();

    private ObjectPool clientPool;
    private Map<String,Boolean> authorizationDataCache = new LRUCache<String, Boolean>(1000);

    private RemoteAuthorizationManager() {

    }

    public static RemoteAuthorizationManager getInstance() {
        return instance;
    }

    public void init() {
        clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                return new RemoteAuthorizationManagerClient();
            }
        });
    }

    public void destroy() {
        try {
            clientPool.close();
        } catch (Exception ignored) {
        }
    }
    
    public boolean isUserAuthorized(String user, String permission) throws APIManagementException {
        String key = user + ":" + permission;
        Boolean authorized = authorizationDataCache.get(key);
        if (authorized == null) {
            synchronized (this) {
                authorized = authorizationDataCache.get(key);
                if (authorized == null) {
                    authorized = queryRemoteAuthorizationManager(user, permission);
                    authorizationDataCache.put(key, authorized);
                }
            }
        }
        return authorized;
    }
    
    private boolean queryRemoteAuthorizationManager(String user, String permission) throws APIManagementException {
        RemoteAuthorizationManagerClient client = null;
        try {
            client = (RemoteAuthorizationManagerClient) clientPool.borrowObject();
            return client.isUserAuthorized(user, permission);

        } catch (Exception e) {
            throw new APIManagementException("Error while accessing backend services for API key validation", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.returnObject(client);
                }
            } catch (Exception ignored) {
            }
        }
    }

}
