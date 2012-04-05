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
package org.wso2.carbon.api.handler.throttle.rolebase.impl.basic;


import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.stub.types.axis2.APIKeyValidationServiceAPIKeyMgtException;
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceStub;


import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthInfoContext {

    private static volatile AuthInfoContext infoHolderSingleton;
    private static final int TIMEOUT_IN_MILLISECS = 15 * 60 * 1000;

    private String authCookieString = null;

    /**
     * Map containing all sessesion inforamtion for APIKeys
     */
    private Map<String,APIKeyValidationInfoDTO> keyValidationInfo = null;

    private AuthInfoContext(){
        keyValidationInfo = new ConcurrentHashMap<String,APIKeyValidationInfoDTO>();
    }

    public static AuthInfoContext getInstance(){
        if(infoHolderSingleton == null){
            synchronized (AuthInfoContext.class){
                if(infoHolderSingleton == null){
                    infoHolderSingleton = new AuthInfoContext();
                }
            }
        }
        return infoHolderSingleton;
    }

    public synchronized String getAuthSessionForAdminServices() throws Exception {
        if(authCookieString == null){
            return new AuthAdminServiceClient().login(AuthAdminServiceClient.HOST_NAME,
                                                         AuthAdminServiceClient.USER_NAME,
                                                         AuthAdminServiceClient.PASSWORD);
        }
        return authCookieString;
    }

    public synchronized void resetSessionCookie(){
        authCookieString = null;
    }

    public APIKeyValidationInfoDTO getValidatedKeyInfo(String context, String apiKey, String apiVersion) throws Exception {
        if(keyValidationInfo.get(apiKey) == null){
            APIKeyValidationServiceStub validator = new APIKeyValidationServiceStub(null, AuthAdminServiceClient.SERVICE_URL + "APIKeyValidationService");

            ServiceClient client = validator._getServiceClient();
            Options options = client.getOptions();
            options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLISECS);
            options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLISECS);
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLISECS);
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, getAuthSessionForAdminServices());
            //TODO : Get the version
            APIKeyValidationInfoDTO keyValidationInfoDTO = validator.validateKey(context, apiVersion, apiKey);
            //store key validation results
            if (keyValidationInfoDTO != null && keyValidationInfoDTO.getAuthorized()) {
                keyValidationInfo.put(apiKey, keyValidationInfoDTO);
                return keyValidationInfoDTO;
            }
        }
        return  keyValidationInfo.get(apiKey);
    }
}
