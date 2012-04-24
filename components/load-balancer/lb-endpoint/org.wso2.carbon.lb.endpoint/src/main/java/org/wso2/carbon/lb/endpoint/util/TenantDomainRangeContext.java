package org.wso2.carbon.lb.endpoint.util;


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
