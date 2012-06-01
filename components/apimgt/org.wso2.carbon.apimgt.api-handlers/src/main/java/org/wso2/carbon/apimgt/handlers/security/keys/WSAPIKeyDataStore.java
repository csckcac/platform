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

package org.wso2.carbon.apimgt.handlers.security.keys;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.handlers.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;
import org.wso2.carbon.apimgt.keymgt.stub.validator.APIKeyValidationServiceStub;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Provides a web service interface for the API key data store. This implementation
 * acts as a client stub for the APIKeyValidationService in the API key manager. Using
 * this stub, one may query the key manager to authenticate and authorize API keys.
 * All service invocations are secured using BasicAuth over TLS. Therefore this class
 * may incur a significant overhead on the key validation process.
 */
public class WSAPIKeyDataStore implements APIKeyDataStore {

    private static final int TIMEOUT_IN_MILLIS = 15 * 60 * 1000;

    private volatile String cookie;
    private ObjectPool clientPool;

    private String username;
    private String password;

    public WSAPIKeyDataStore() throws APISecurityException {
        final APIManagerConfiguration config = ServiceReferenceHolder.getInstance().
                getAPIManagerConfiguration();
        username = config.getFirstProperty(APIConstants.API_KEY_MANAGER_USERNAME);
        password = config.getFirstProperty(APIConstants.API_KEY_MANAGER_PASSWORD);
        String serviceURL = config.getFirstProperty(APIConstants.API_KEY_MANAGER_URL);
        // Just check for the service URL parameter at this point - Don't hold on to it.
        // Chances are the listener manager is not properly initialized yet and therefore
        // we should defer reading the service URL for later.
        if (serviceURL == null || username == null || password == null) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Required connection details for the key management server not provided");
        }

        clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                String serviceURL = config.getFirstProperty(APIConstants.API_KEY_MANAGER_URL);
                APIKeyValidationServiceStub clientStub = new APIKeyValidationServiceStub(null,
                        serviceURL + "APIKeyValidationService");

                ServiceClient client = clientStub._getServiceClient();
                Options options = client.getOptions();
                options.setTimeOutInMilliSeconds(TIMEOUT_IN_MILLIS);
                options.setProperty(HTTPConstants.SO_TIMEOUT, TIMEOUT_IN_MILLIS);
                options.setProperty(HTTPConstants.CONNECTION_TIMEOUT, TIMEOUT_IN_MILLIS);
                options.setCallTransportCleanup(true);
                options.setManageSession(true);
                return clientStub;
            }
        });
    }

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey) throws APISecurityException {
        APIKeyValidationServiceStub stub = null;
        try {
            stub = (APIKeyValidationServiceStub) clientPool.borrowObject();
            // Add the Authorization header to all requests
            // If the cookie we send is invalid, this will renew the cookie automatically
            CarbonUtils.setBasicAccessSecurityHeaders(username, password, true, stub._getServiceClient());

            if (cookie == null) {
                synchronized (this) {
                    if (cookie == null) {
                        // We are using this validator for the first time
                        // Need to invoke the service and obtain the cookie
                        org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO dto =
                                stub.validateKey(context, apiVersion, apiKey);
                        ServiceContext serviceContext = stub.
                                _getServiceClient().getLastOperationContext().getServiceContext();
                        cookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
                        return toDTO(dto);
                    }
                }
            }

            stub._getServiceClient().getOptions().setProperty(HTTPConstants.COOKIE_STRING, cookie);
            return toDTO(stub.validateKey(context, apiVersion, apiKey));

        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        } finally {
            try {
                if (stub != null) {
                    clientPool.returnObject(stub);
                }
            } catch (Exception ignored) {
            }
        }
    }

    private APIKeyValidationInfoDTO toDTO(
            org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO generatedDto) {
        APIKeyValidationInfoDTO dto = new APIKeyValidationInfoDTO();
        dto.setUsername(generatedDto.getUsername());
        dto.setAuthorized(generatedDto.getAuthorized());
        dto.setTier(generatedDto.getTier());
        dto.setType(generatedDto.getType());
        return dto;
    }
}
