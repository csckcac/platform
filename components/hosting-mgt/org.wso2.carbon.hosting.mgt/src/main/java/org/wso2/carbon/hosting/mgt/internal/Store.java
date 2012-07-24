package org.wso2.carbon.hosting.mgt.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class Store {
    public static ConcurrentMap<String, Integer> publicIpToTenantMap = new ConcurrentHashMap<String, Integer>();
    public static ConcurrentMap<Integer, String> tenantToPublicIpMap = new ConcurrentHashMap<Integer, String>();
}
