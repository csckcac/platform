/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.identity.provider.openid.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.association.Association;
import org.wso2.carbon.caching.core.BaseCache;
import org.wso2.carbon.caching.core.identity.IdentityCacheEntry;
import org.wso2.carbon.caching.core.identity.IdentityCacheKey;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;

/**
 *  Association cache
 */
public class AssociationCache extends BaseCache {

    private static AssociationCache associationCache = null;

    private final static String OPENID_ASSOCIATION_CACHE = "OPENID_ASSOCIATION_CACHE";

    private static Log log = LogFactory.getLog(AssociationCache.class);

    private AssociationCache(String cacheName) {
        super(cacheName);
    }

    public synchronized static AssociationCache getCacheInstance() {
        if (associationCache == null) {
            associationCache = new AssociationCache(OPENID_ASSOCIATION_CACHE);
        }
        return associationCache;
    }


    public void addToCache(Association association){

        if(log.isDebugEnabled()){
            log.debug("Trying to add to cache. No of current entries in Cache: " + associationCache.cache.size());
        }
        if(association != null && association.getHandle() != null){
            IdentityCacheKey cacheKey = new IdentityCacheKey(0,association.getHandle());
            IdentityCacheEntry cacheEntry = new IdentityCacheEntry(association.getType(),
                                                association.getMacKey(), association.getExpiry());
            associationCache.addToCache(cacheKey, cacheEntry);
            if(log.isDebugEnabled()){
                log.debug("New entry is added to cache  : " + association.getHandle());
            }
        }
        
    }

    public Association getFromCache(String handle){

        if(log.isDebugEnabled()){
            log.debug("Trying to get from cache. No of current entries in Cache: " + associationCache.cache.size());
        }

        if(handle != null){
            IdentityCacheKey cacheKey = new IdentityCacheKey(0, handle);
            IdentityCacheEntry cacheEntry = (IdentityCacheEntry) associationCache.
                                                                    getValueFromCache(cacheKey);
            if(cacheEntry != null){
                if(log.isDebugEnabled()){
                    log.debug("Cache hit for handle : " + handle);
                }
                Date expiry  = cacheEntry.getDate();
                String type = cacheEntry.getCacheEntry();
                Key secretKey = cacheEntry.getSecretKey();

                if(expiry != null && type != null && secretKey != null){
                    Association association = new Association(type, handle, (SecretKey)secretKey, expiry);
                    if(association.hasExpired()){
                        associationCache.removeCacheEntry(handle);
                        if(log.isDebugEnabled()){
                            log.debug("Expired entry in cache for handle : " + handle);
                        }
                    } else {
                        return association;
                    }
                }
            } else {
                if(log.isDebugEnabled()){
                    log.debug("Cache miss for handle : " + handle);
                }
            }
        }
        
        return null;
    }

    public void removeCacheEntry(String handle){
        if(handle != null){
            IdentityCacheKey cacheKey = new IdentityCacheKey(0, handle);
            associationCache.clearCacheEntry(cacheKey);
        }
    }    
}
