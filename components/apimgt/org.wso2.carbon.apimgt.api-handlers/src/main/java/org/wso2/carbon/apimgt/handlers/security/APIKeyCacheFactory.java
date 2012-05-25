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

package org.wso2.carbon.apimgt.handlers.security;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class APIKeyCacheFactory {
    
    private static final APIKeyCacheFactory instance = new APIKeyCacheFactory();
    
    private Map<String,APIKeyCache> cacheMap = new ConcurrentHashMap<String, APIKeyCache>();
    
    private APIKeyCacheFactory() {
        
    }
    
    public static APIKeyCacheFactory getInstance() {
        return instance;
    }
    
    public APIKeyCache getAPIKeyCache(String context, String version) {
        String identifier = context + ":" + version;
        APIKeyCache cache = cacheMap.get(identifier);
        if (cache == null) {
            synchronized (this) {
                cache = cacheMap.get(identifier);
                if (cache == null) {
                    cache = new APIKeyCache(APISecurityConstants.DEFAULT_MAX_VALID_KEYS, 
                            APISecurityConstants.DEFAULT_MAX_INVALID_KEYS);
                    cacheMap.put(identifier, cache);
                }
            }
        }
        return cache;
    }
    
    public APIKeyCache getExistingAPIKeyCache(String context, String version) {
        String identifier = context + ":" + version;
        return cacheMap.get(identifier);
    }

    void reset() {
        cacheMap.clear();
    }
}
