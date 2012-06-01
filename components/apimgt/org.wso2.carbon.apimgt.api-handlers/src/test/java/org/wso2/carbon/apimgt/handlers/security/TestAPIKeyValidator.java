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

import org.wso2.carbon.apimgt.impl.dto.APIKeyValidationInfoDTO;

import java.util.HashMap;
import java.util.Map;

public class TestAPIKeyValidator extends APIKeyValidator {
    
    private int counter = 0;
    private Map<String,APIKeyValidationInfoDTO> userInfo = new HashMap<String, APIKeyValidationInfoDTO>();

    @Override
    protected APIKeyValidationInfoDTO doGetKeyValidationInfo(String context, String apiVersion, 
                                                             String apiKey) throws APISecurityException {
        counter++;
        String key = getKey(context, apiVersion, apiKey);
        if (userInfo.containsKey(key)) {
            return userInfo.get(key);
        }
        APIKeyValidationInfoDTO info = new APIKeyValidationInfoDTO();
        info.setAuthorized(false);
        return info;
    }

    public void addUserInfo(String context, String apiVersion, 
                            String apiKey, APIKeyValidationInfoDTO info) {
        String key = getKey(context, apiVersion, apiKey);
        userInfo.put(key, info);
    }

    private String getKey(String context, String apiVersion, String apiKey) {
        return "{" + context + ":" + apiVersion + ":" + apiKey + "}";
    }

    public int getCounter() {
        return counter;
    }
}
