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
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceStub;

public class AuthInfoContext {

    private static final AuthInfoContext infoHolderSingleton = new AuthInfoContext();

    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    private String authCookieString = null;

    private APIKeyValidationInfoCache infoCache;

    private AuthInfoContext(){
        infoCache = new APIKeyValidationInfoCache(1000, 250);
    }

    public static AuthInfoContext getInstance(){
        return infoHolderSingleton;
    }

    public synchronized String getAuthSessionForAdminServices() throws Exception {
        if (authCookieString == null) {
            return new AuthAdminServiceClient().login(AuthAdminServiceClient.HOST_NAME,
                                                         AuthAdminServiceClient.USER_NAME,
                                                         AuthAdminServiceClient.PASSWORD);
        }
        return authCookieString;
    }

    public synchronized void resetSessionCookie() {
        authCookieString = null;
    }

    /**
     * Get the API key validated against the specified API
     *
     * @param context API context
     * @param apiKey API key to be validated
     * @param apiVersion API version number
     * @return An APIKeyValidationInfoDTO object
     * @throws Exception If an error occurs while accessing backend services
     */
    public APIKeyValidationInfoDTO getValidatedKeyInfo(String context, String apiKey, 
                                                       String apiVersion) throws Exception {
        
        APIKeyValidationInfoDTO info = infoCache.getInfo(apiKey);
        if (info != null) {
            return info;
        }

        synchronized (apiKey.intern()) {
            // We synchronize on the API key here to allow concurrent processing
            // of different API keys - However when a burst of requests with the
            // same key is encountered, only one will be allowed to execute the logic,
            // and the rest will pick the value from the cache.
            info = infoCache.getInfo(apiKey);
            if (info != null) {
                return info;
            }

            APIKeyValidationServiceStub validator = new APIKeyValidationServiceStub(null,
                    AuthAdminServiceClient.SERVICE_URL + "APIKeyValidationService");

            ServiceClient client = validator._getServiceClient();
            Options options = client.getOptions();
            options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
            options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
            options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                    getAuthSessionForAdminServices());
            info = validator.validateKey(context, apiVersion, apiKey);
            if (info != null) {
                if (info.getAuthorized()) {
                    infoCache.addValidKey(apiKey, info);
                } else {
                    infoCache.addInvalidKey(apiKey, info);
                }
                return info;
            } else {
                throw new AxisFault("API key validator returned null");
            }
        }
    }
}
