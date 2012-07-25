package org.wso2.carbon.hosting.mgt.internal;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *   Class to store all the maps regarding tenant, public ip private ip
 */
public class Store {
    /**
     * Key is public ip Value is tenant
     */
    public static ConcurrentMap<String, Integer> privateIpToTenantMap = new ConcurrentHashMap<String, Integer>();
    /**
     * Key is tenant Value is public ip
     */
    public static ConcurrentMap<Integer, String> tenantToPublicIpMap = new ConcurrentHashMap<Integer, String>();
    /**
     * Key is tenant Value is private ip
     */
    public static ConcurrentMap<Integer, String> tenantToPrivateIpMap = new ConcurrentHashMap<Integer, String>();
}
