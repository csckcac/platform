/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.handlers.security;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceStub;

public class APIKeyValidator {

    private static final APIKeyValidator instance = new APIKeyValidator();

    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    private APIKeyCache infoCache;

    private APIKeyValidator() {
        infoCache = new APIKeyCache(1000, 250);
    }

    public static APIKeyValidator getInstance(){
        return instance;
    }

    private String getAuthSessionForAdminServices() throws Exception {
        return new AuthAdminServiceClient().login();
    }

    /**
     * Get the API key validated against the specified API
     *
     * @param context API context
     * @param apiKey API key to be validated
     * @param apiVersion API version number
     * @return An APIKeyValidationInfoDTO object
     * @throws APISecurityException If an error occurs while accessing backend services
     */
    public APIKeyValidationInfoDTO getKeyValidationInfo(String context, String apiKey,
                                                       String apiVersion) throws APISecurityException {
        // We use a combination of API context, API version and API key as the
        // cache key, because API keys are issued per API, per version.
        String cacheKey = "{" + context + ":" + apiVersion + ":" + apiKey + "}";
        APIKeyValidationInfoDTO info = infoCache.getInfo(cacheKey);
        if (info != null) {
            return info;
        }

        synchronized (cacheKey.intern()) {
            // We synchronize on the API key here to allow concurrent processing
            // of different API keys - However when a burst of requests with the
            // same key is encountered, only one will be allowed to execute the logic,
            // and the rest will pick the value from the cache.
            info = infoCache.getInfo(cacheKey);
            if (info != null) {
                return info;
            }

            try {
                String serviceURL = APIManagerConfiguration.getInstance().getFirstProperty(
                        APISecurityConstants.API_SECURITY_AUTH_ADMIN_URL);
                APIKeyValidationServiceStub validator = new APIKeyValidationServiceStub(null,
                        serviceURL + "APIKeyValidationService");

                ServiceClient client = validator._getServiceClient();
                Options options = client.getOptions();
                options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
                options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
                options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
                options.setManageSession(true);
                options.setProperty(HTTPConstants.COOKIE_STRING,
                        getAuthSessionForAdminServices());
                info = validator.validateKey(context, apiVersion, apiKey);
            } catch (Exception e) {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        "Error while accessing backend services for API key validation", e);
            }

            if (info != null) {
                if (info.getAuthorized()) {
                    infoCache.addValidKey(cacheKey, info);
                } else {
                    infoCache.addInvalidKey(cacheKey, info);
                }
                return info;
            } else {
                throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                        "API key validator returned null");
            }
        }
    }
}
