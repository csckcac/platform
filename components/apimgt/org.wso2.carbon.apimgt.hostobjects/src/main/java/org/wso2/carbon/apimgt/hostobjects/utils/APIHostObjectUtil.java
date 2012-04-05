package org.wso2.carbon.apimgt.hostobjects.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerImpl;

public class APIHostObjectUtil {

    private static APIHostObjectUtil hostObjectUtils;
    private APIManagerImpl apiManager;

    private APIHostObjectUtil(){
    }

    public static APIHostObjectUtil getApiHostObjectUtils() {
        if(hostObjectUtils == null){
            hostObjectUtils = new APIHostObjectUtil();
        }
        return hostObjectUtils;
    }

    public APIManagerImpl getApiManager() throws APIManagementException {
        if(apiManager == null){
            apiManager = new APIManagerImpl("admin", "admin", "https://localhost:9443/services/");
        }
        return apiManager;
    }

    public void cleanup(){
        apiManager.cleanup();
        apiManager = null;
    }
}
