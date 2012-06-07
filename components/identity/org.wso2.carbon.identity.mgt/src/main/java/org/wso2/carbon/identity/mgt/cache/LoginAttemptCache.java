/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.identity.mgt.cache;

import org.wso2.carbon.caching.core.BaseCache;
import org.wso2.carbon.caching.core.identity.IdentityCacheEntry;
import org.wso2.carbon.caching.core.identity.IdentityCacheKey;

/**
 * Login attempt Cache that caches no of failed login attempts of the user
 */
public class LoginAttemptCache extends BaseCache {

    private static LoginAttemptCache loginAttemptCache = null;

    private final static String LOGIN_ATTEMPT_CACHE = "LOGIN_ATTEMPT_CACHE";

    private LoginAttemptCache(String cacheName) {
        super(cacheName);
    }

    public synchronized static LoginAttemptCache getCacheInstance() {
        if (loginAttemptCache == null) {
            loginAttemptCache = new LoginAttemptCache(LOGIN_ATTEMPT_CACHE);
        }
        return loginAttemptCache;
    }

    public void addToCache(String userName, int tenantId) {

        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, userName);
        IdentityCacheEntry cacheEntry = (IdentityCacheEntry) loginAttemptCache.getValueFromCache(cacheKey);

        int i;        
        if(cacheEntry != null){
            i = cacheEntry.getHashEntry() + 1;
        } else {
            i = 1;
        }

        IdentityCacheEntry newCacheEntry = new IdentityCacheEntry(i);
        super.addToCache(cacheKey, newCacheEntry);

    }

    public int getValueFromCache(String userName, int tenantId){
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, userName);
        IdentityCacheEntry cacheEntry = (IdentityCacheEntry) loginAttemptCache.getValueFromCache(cacheKey);
        if(cacheEntry != null){
            return cacheEntry.getHashEntry();
        }

        return 0;
    }

    public void clearCacheEntry(String userName, int tenantId){
        IdentityCacheKey cacheKey = new IdentityCacheKey(tenantId, userName);
        loginAttemptCache.clearCacheEntry(cacheKey);
    }
}
