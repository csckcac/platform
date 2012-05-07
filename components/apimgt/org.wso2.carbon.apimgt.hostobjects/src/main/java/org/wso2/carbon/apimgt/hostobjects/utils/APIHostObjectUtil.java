/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
            //apiManager = new APIManagerImpl();
        }
        return apiManager;
    }
    public APIProviderImpl getApiProvider() throws APIManagementException{
        if(apiProvider==null){
            //apiProvider = new APIProviderImpl();
        }
        return apiProvider;
    }
    public APIConsumerImpl getApiConsumer() throws APIManagementException{
        if(apiConsumer == null){
            //apiConsumer = new APIConsumerImpl();
        }
        return apiConsumer;
    }

    public void cleanup(){
        apiManager.cleanup();
        apiManager = null;
    }
}
