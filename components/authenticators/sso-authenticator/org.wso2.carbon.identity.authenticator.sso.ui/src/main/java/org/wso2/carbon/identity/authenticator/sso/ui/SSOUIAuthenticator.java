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
package org.wso2.carbon.identity.authenticator.sso.ui;

import java.rmi.RemoteException;

import javax.servlet.http.HttpServletRequest;

import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.ui.DefaultCarbonAuthenticator;

public class SSOUIAuthenticator extends DefaultCarbonAuthenticator {

    public static final String SSO_SESSION_ID = "ssoSessionId";
    private static final int DEFAULT_PRIORITY_LEVEL = 15;
    private static final String AUTHENTICATOR_NAME = "Authenticators.SSOUIAuthenticator";
    private static final String AUTHENTICATOR_PRIOROTY = AUTHENTICATOR_NAME + ".Priority";
    private static final String AUTHENTICATOR_DISABLED = AUTHENTICATOR_NAME + ".Disabled";


    public boolean authenticate(Object object) throws AuthenticationException{
        String userName = null;
        String ssoSessionId = null;
        HttpServletRequest request = (HttpServletRequest) object;
        userName = request.getParameter("username");
        ssoSessionId = request.getParameter(SSO_SESSION_ID);
        try {
            return super.authenticate(request, userName, ssoSessionId, false);
        } catch (RemoteException e) {
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    public boolean isHandle(Object object) {
        if (!(object instanceof HttpServletRequest)) {
            return false;
        }
        HttpServletRequest request = (HttpServletRequest) object;
        String password = request.getParameter("password");
        String ssoSessionId = request.getParameter(SSO_SESSION_ID);
        if (ssoSessionId != null && password == null) {
            return true;
        }
        return false;
    }

    public int getPriority() {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String priority = serverConfig.getFirstProperty(AUTHENTICATOR_PRIOROTY);
        if (priority != null && priority.length() > 0) {
            return Integer.parseInt(priority);
        }
        return DEFAULT_PRIORITY_LEVEL;
    }

    public String getAuthenticatorName() {
        return AUTHENTICATOR_NAME;
    }
    
    public boolean isDisabled() {
        ServerConfiguration serverConfig = ServerConfiguration.getInstance();
        String isDisabled = serverConfig.getFirstProperty(AUTHENTICATOR_DISABLED);
        if (isDisabled != null && isDisabled.length() > 0) {
            return Boolean.getBoolean(isDisabled);
        }
        return false;
    }

}
