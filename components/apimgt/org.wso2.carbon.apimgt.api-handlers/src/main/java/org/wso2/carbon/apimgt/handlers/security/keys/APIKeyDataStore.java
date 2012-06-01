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

import org.wso2.carbon.apimgt.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

/**
 * Represents the interface used by the APIKeyValidator to interact with the API
 * key manager. Different implementations of this interface may employ different
 * techniques/protocols to communicate with the actual key management server.
 */
public interface APIKeyDataStore {

    /**
     * Validate the given API key for the specified API context and version.
     *
     * @param context Context of an API
     * @param apiVersion A valid version of the API
     * @param apiKey An API key string - Not necessarily a valid key
     * @return an APIKeyValidationInfoDTO instance containing key validation data
     * @throws APISecurityException on error
     */
    public APIKeyValidationInfoDTO getAPIKeyData(String context, String apiVersion,
                                                 String apiKey) throws APISecurityException;

}
