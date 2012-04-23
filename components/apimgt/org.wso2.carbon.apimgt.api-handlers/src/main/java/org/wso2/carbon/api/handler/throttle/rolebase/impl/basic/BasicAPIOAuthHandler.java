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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.api.handler.throttle.rolebase.AuthenticationFuture;
import org.wso2.carbon.api.handler.throttle.rolebase.UserPrivilegesHandler;
import org.wso2.carbon.apimgt.impl.dto.xsd.APIKeyValidationInfoDTO;

import java.util.ArrayList;
import java.util.List;

public class BasicAPIOAuthHandler implements UserPrivilegesHandler {

    private static final Log log = LogFactory.getLog(BasicAPIOAuthHandler.class);

    private AuthenticationFuture callback = null ;
    private AuthInfoContext context;

    public BasicAPIOAuthHandler(AuthenticationFuture callback) throws AxisFault {
        this.callback = callback;
        context = AuthInfoContext.getInstance();
    }

    public boolean authenticateUser() {
        APIKeyValidationInfoDTO result;
        try {
            result = context.getValidatedKeyInfo(callback.getURI(),
                    callback.getAPIKey(), callback.getAPIVersion());
        } catch (Exception e) {
            log.error("Unexpected error while validating API key: " + callback.getAPIKey(), e);
            return false;
        }

        callback.setAuthenticated(result.getAuthorized());
        List<String> roles = new ArrayList<String>();
        roles.add(result.getTier());
        callback.setAuthorizedRoles(roles);
        return callback.isAuthenticated();
    }

    public AuthenticationFuture getAuthenticator() {
        return callback;
    }
}
