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

/**
 * Contains some context information related to an authenticated request. This can be used
 * to access API keys and tier information related to already authenticated requests.
 */
public class AuthenticationContext {

    private boolean authenticated;
    private String tier;
    private String apiKey;
    private String keyType;

    public boolean isAuthenticated() {
        return authenticated;
    }

    public String getTier() {
        return tier;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
    
    public void setTier(String tier) {
        this.tier = tier;
    }

    public String getKeyType() {
        return keyType;
    }

    public void setKeyType(String keyType) {
        this.keyType = keyType;
    }
}
