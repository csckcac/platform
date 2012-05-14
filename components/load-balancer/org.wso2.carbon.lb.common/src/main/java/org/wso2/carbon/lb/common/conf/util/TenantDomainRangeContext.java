/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.lb.common.conf.util;

import java.util.HashMap;

import java.util.Map;

public class TenantDomainRangeContext {

    private Map<Integer, String> tenantIDClusterDomainMap = new HashMap<Integer, String>();

    public Map<String, String> getTenantDomainRangeContextMap() {
        return tenantDomainRangeContextMap;
    }

    private Map<String, String> tenantDomainRangeContextMap = new HashMap<String, String>();

    public TenantDomainRangeContext() {

    }

    public void addTenantDomain(String domain, String tenantRange) {
        tenantDomainRangeContextMap.put(domain, tenantRange);
        String[] parsedLine = tenantRange.trim().split("-");
        if (parsedLine[0].equalsIgnoreCase("*")) {
            tenantIDClusterDomainMap.put(0, domain);
        } else {
            int startIndex = Integer.parseInt(parsedLine[0]);
            int endIndex = Integer.parseInt(parsedLine[1]);
            for (int tenantId = startIndex; tenantId < endIndex; tenantId++) {
                tenantIDClusterDomainMap.put(tenantId, domain);
            }
        }
    }

    public Map<Integer, String> getTenantIDClusterDomainMap() {
        return this.tenantIDClusterDomainMap;
    }

    public String getClusterDomainFormTenantId(int id) {
        String clusterDomain = this.tenantIDClusterDomainMap.get(id);
        if (clusterDomain == null) {
            return this.tenantIDClusterDomainMap.get(0);
        } else {
            return clusterDomain;
        }
    }
}
