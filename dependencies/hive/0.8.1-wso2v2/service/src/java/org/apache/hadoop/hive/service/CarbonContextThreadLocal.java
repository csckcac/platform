package org.apache.hadoop.hive.service;

public class CarbonContextThreadLocal {

    private static final ThreadLocal<Integer> carbonContextThreadLocal = new ThreadLocal<Integer>();

    public static void setTenantId(int tenantId){
        carbonContextThreadLocal.set(tenantId);
    }

    public static int getTenantId(){
        return carbonContextThreadLocal.get();
    }

    public static void unsetTenantId() {
        carbonContextThreadLocal.remove();
    }
}