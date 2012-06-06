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

import net.sf.jsr107cache.Cache;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.handlers.security.keys.APIKeyDataStore;
import org.wso2.carbon.apimgt.handlers.security.keys.WSAPIKeyDataStore;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;

/**
 * This class is used to validate a given API key against a given API context and a version.
 * Actual validation operations are carried out by invoking back-end authentication and
 * key validation services. In order to minimize the network overhead, this implementation
 * caches some API key authentication information in memory. This implementation and the
 * underlying caching implementation are thread-safe. An instance of this class must not be
 * shared among multiple APIs, API handlers or authenticators.
 */
public class APIKeyValidator {
    
    private static final Log log = LogFactory.getLog(APIKeyValidator.class);

    private Cache infoCache;
    private volatile APIKeyDataStore dataStore;
    private AxisConfiguration axisConfig;

    public APIKeyValidator(AxisConfiguration axisConfig) {
        this.axisConfig = axisConfig;
        this.infoCache = initCache();
    }

    protected Cache initCache() {
        return SuperTenantCarbonContext.getCurrentContext(axisConfig).getCache();
    }

    /**
     * Get the API key validated against the specified API
     *
     * @param context API context
     * @param apiKey API key to be validated
     * @param apiVersion API version number
     * @return An APIKeyValidationInfoDTO object
     * @throws APISecurityException If an error occurs while accessing backend services
     */
    public APIKeyValidationInfoDTO getKeyValidationInfo(String context, String apiKey,
                                                       String apiVersion) throws APISecurityException {
        String cacheKey = apiKey + ":" + context + ":" + apiVersion;
        APIKeyValidationInfoDTO info = (APIKeyValidationInfoDTO) infoCache.get(cacheKey);
        if (info != null) {
            return info;
        }

        synchronized (apiKey.intern()) {
            // We synchronize on the API key here to allow concurrent processing
            // of different API keys - However when a burst of requests with the
            // same key is encountered, only one will be allowed to execute the logic,
            // and the rest will pick the value from the cache.
            info = (APIKeyValidationInfoDTO) infoCache.get(cacheKey);
            if (info != null) {
                return info;
            }

            info = doGetKeyValidationInfo(context, apiVersion, apiKey);
            if (info != null) {
                infoCache.put(cacheKey, info);
                return info;
            } else {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        "API key validator returned null");
            }
        }
    }
    
    protected APIKeyValidationInfoDTO doGetKeyValidationInfo(String context, String apiVersion, 
                                                             String apiKey) throws APISecurityException {

        if (dataStore == null) {
            synchronized (this) {
                if (dataStore == null) {
                    initDataStore();
                }
            }
        }
        return dataStore.getAPIKeyData(context, apiVersion, apiKey);
    }

    private void initDataStore() throws APISecurityException {
        log.debug("Initializing WS API key data store");
        dataStore = new WSAPIKeyDataStore();
    }
}
