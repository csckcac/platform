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

package org.wso2.carbon.apimgt.handlers.security.service;

import net.sf.jsr107cache.Cache;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;

public class APIAuthenticationService extends AbstractServiceBusAdmin {

    public void invalidateKeys(APIKeyMapping[] mappings) {
        Cache cache = SuperTenantCarbonContext.getCurrentContext(getAxisConfig()).getCache();
        for (APIKeyMapping mapping : mappings) {
            String cacheKey = mapping.getKey() + ":" + mapping.getContext() + ":" + mapping.getApiVersion();
            cache.remove(cacheKey);
        }
    }

}
