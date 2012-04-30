package org.wso2.carbon.apimgt.hostobjects.utils;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConsumerImpl;
import org.wso2.carbon.apimgt.impl.APIManagerImpl;
import org.wso2.carbon.apimgt.impl.APIProviderImpl;

public class APIHostObjectUtil {

    private static APIHostObjectUtil hostObjectUtils;
    private APIManagerImpl apiManager;
    private APIProviderImpl apiProvider;
    private APIConsumerImpl apiConsumer;

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
            apiManager = new APIManagerImpl();
        }
        return apiManager;
    }
    public APIProviderImpl getApiProvider() throws APIManagementException{
        if(apiProvider==null){
            apiProvider = new APIProviderImpl();
        }
        return apiProvider;
    }
    public APIConsumerImpl getApiConsumer() throws APIManagementException{
        if(apiConsumer == null){
            apiConsumer = new APIConsumerImpl();
        }
        return apiConsumer;
    }

    public void cleanup(){
        apiManager.cleanup();
        apiManager = null;
    }
}
