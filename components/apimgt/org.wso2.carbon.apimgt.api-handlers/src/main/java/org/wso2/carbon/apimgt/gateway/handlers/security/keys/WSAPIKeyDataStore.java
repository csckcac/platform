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

package org.wso2.carbon.apimgt.gateway.handlers.security.keys;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityConstants;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

/**
 * Provides a web service interface for the API key data store. This implementation
 * acts as a client stub for the APIKeyValidationService in the API key manager. Using
 * this stub, one may query the key manager to authenticate and authorize API keys.
 * All service invocations are secured using BasicAuth over TLS. Therefore this class
 * may incur a significant overhead on the key validation process.
 */
public class WSAPIKeyDataStore implements APIKeyDataStore {

    private ObjectPool clientPool;

    public WSAPIKeyDataStore() throws APISecurityException {
        clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                return new APIKeyValidatorClient();
            }
        });
    }

    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey) throws APISecurityException {
        APIKeyValidatorClient client = null;
        try {
            client = (APIKeyValidatorClient) clientPool.borrowObject();
            return client.getAPIKeyData(context, apiVersion, apiKey);

        } catch (Exception e) {
            throw new APISecurityException(APISecurityConstants.API_AUTH_GENERAL_ERROR,
                    "Error while accessing backend services for API key validation", e);
        } finally {
            try {
                if (client != null) {
                    clientPool.returnObject(client);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public void cleanup() {
        try {
            clientPool.close();
        } catch (Exception ignored) {

        }
    }
}
