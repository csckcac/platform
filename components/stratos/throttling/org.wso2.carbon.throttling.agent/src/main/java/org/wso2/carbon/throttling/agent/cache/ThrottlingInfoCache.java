/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.throttling.agent.cache;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In memory cache which keeps throttling information for active tenants(local to a server instance). Cache is updated
 * periodically using information retrieved from registry.
 */
public class ThrottlingInfoCache {

    private static final Log log = LogFactory.getLog(ThrottlingInfoCache.class);

    private Map<Integer, TenantThrottlingInfo> tenantThrottlingInfoMap =
            new ConcurrentHashMap<Integer, TenantThrottlingInfo>();

    public void addTenant(Integer tenantId){
        tenantThrottlingInfoMap.put(tenantId, new TenantThrottlingInfo());
    }

    public void deleteTenant(Integer tenantId){
        tenantThrottlingInfoMap.remove(tenantId);
    }

    public Set<Integer> getActiveTenants(){
        return tenantThrottlingInfoMap.keySet();
    }

    public void updateThrottlingActionInfo(Integer tenantId, String action, ThrottlingActionInfo throttlingActionInfo){
        // throttlingInfo could never be null if the update and lazy loading logic are correct.
        TenantThrottlingInfo throttlingInfo = tenantThrottlingInfoMap.get(tenantId);
        throttlingInfo.updateThrottlingActionInfo(action, throttlingActionInfo);
    }

    public ThrottlingActionInfo getThrottlingActionInfo(Integer tenantId, String action){
        if(tenantThrottlingInfoMap.get(tenantId) != null){
            return tenantThrottlingInfoMap.get(tenantId).getThrottlingActionInfo(action);
        }

        // This could happen if user has never perform this action before or throttling info cache updating task
        // has not executed for this tenant. TODO: Check the validity
        return null;
    }

    public TenantThrottlingInfo getTenantThrottlingInfo(Integer tenantId){
        return tenantThrottlingInfoMap.get(tenantId);
    }
}
