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
package org.wso2.carbon.api.handler.throttle.rolebase.factory;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.api.handler.throttle.RestAPIThrottleHandler;
import org.wso2.carbon.api.handler.throttle.rolebase.AuthenticatorFactory;
import org.wso2.carbon.api.handler.throttle.rolebase.UserPrivilegesHandler;
import org.wso2.carbon.api.handler.throttle.rolebase.impl.basic.BasicAPIOAuthHandler;
import org.wso2.carbon.api.handler.throttle.rolebase.impl.basic.BasicOAuthAuthenticator;

import java.util.Map;

public class BasicOAuthAuthenticatorFactory implements AuthenticatorFactory{

    static Log log = LogFactory.getLog(BasicOAuthAuthenticatorFactory.class);
    public UserPrivilegesHandler createAuthenticationHandler(Map settings) {
        String oAuthHeader = (String) settings.get(RestAPIThrottleHandler.O_AUTH_HEADER);
        String apiVersion = (String) settings.get(RESTConstants.SYNAPSE_REST_API_VERSION);
        String context = (String) settings.get(RESTConstants.REST_API_CONTEXT);
        BasicOAuthAuthenticator authFuture = new BasicOAuthAuthenticator(oAuthHeader, context, apiVersion);
        try {
            return new BasicAPIOAuthHandler(authFuture);
        } catch (AxisFault axisFault) {
            log.error("Error creating BasicAPIOAuthHandler", axisFault);
            return null;
        }
    }
}
