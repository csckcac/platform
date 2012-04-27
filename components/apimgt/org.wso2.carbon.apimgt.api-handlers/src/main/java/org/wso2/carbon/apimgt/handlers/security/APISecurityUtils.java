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

import org.apache.synapse.MessageContext;

public class APISecurityUtils {
    
    private static final String API_AUTH_CONTEXT = "__API_AUTH_CONTEXT";

    /**
     * Add AuthenticationContext information into a validated request. This method does not
     * allow overriding existing AuthenticationContext information on the request. Therefore
     * this should only be used with newly validated requests. It shouldn't be used to modify
     * already validated requests.
     *
     * @param synCtx A newly authenticated request
     * @param authContext AuthenticationContext information to be added
     */
    public static void setAuthenticationContext(MessageContext synCtx,
                                                AuthenticationContext authContext) {
        if (synCtx.getProperty(API_AUTH_CONTEXT) != null) {
            throw new IllegalStateException("Attempting to override existing AuthenticationContext");
        }
        synCtx.setProperty(API_AUTH_CONTEXT, authContext);
    }

    /**
     * Retrieve the AuthenticationContext information from the request. If the request hasn't
     * been validated yet, this method will return null.
     *
     * @param synCtx Current message
     * @return An AuthenticationContext instance or null
     */
    public static AuthenticationContext getAuthenticationContext(MessageContext synCtx) {
        return (AuthenticationContext) synCtx.getProperty(API_AUTH_CONTEXT);
    }
    
}
