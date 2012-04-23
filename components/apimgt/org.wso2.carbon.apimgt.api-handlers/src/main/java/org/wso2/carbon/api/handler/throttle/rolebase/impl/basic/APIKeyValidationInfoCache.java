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

package org.wso2.carbon.api.handler.throttle.rolebase.impl.basic;

import org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A simple in-memory cache for API keys and validation information related to API keys.
 * In order to conserve resource, this implementation imposes hard upper bounds on the
 * number of valid and invalid keys kept in the cache. When the cache is full, a simple
 * LRU algorithm is used to replace existing cache entries. This cache is not thread safe.
 * Any operation that may structurally modify the cache (eg: put, delete) should be
 * externally synchronized.
 */
public class APIKeyValidationInfoCache {
    
    private Map<String,APIKeyValidationInfoDTO> validKeys;    
    private Map<String,APIKeyValidationInfoDTO> invalidKeys;

    public APIKeyValidationInfoCache(int maxValidKeys, int maxInvalidKeys) {
        validKeys = new LRUCache<String, APIKeyValidationInfoDTO>(maxValidKeys);
        invalidKeys = new LRUCache<String, APIKeyValidationInfoDTO>(maxInvalidKeys);
    }

    public void addValidKey(String key, APIKeyValidationInfoDTO info) {
        validKeys.put(key, info);
    }
    
    public void addInvalidKey(String key, APIKeyValidationInfoDTO info) {
        invalidKeys.put(key, info);
    }

    public APIKeyValidationInfoDTO getInfo(String key) {
        APIKeyValidationInfoDTO info = validKeys.get(key);
        if (info == null) {
            info = invalidKeys.get(key);
        }
        return info;
    }

    private static class LRUCache<K,V> extends LinkedHashMap<K,V> {

        private int maxEntries;

        public LRUCache(int maxEntries) {
            super(maxEntries + 1, 1, false);
            this.maxEntries = maxEntries;
        }
        
        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxEntries;
        }
    }
}
