/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.impl.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.utils.ServerConstants;

import javax.naming.AuthenticationException;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;

public class AuthenticationClient {

    private AuthenticationAdminStub stub;
    private String remoteAddress;
    private HttpSession  httpSession;

    public AuthenticationClient(String endPoint) throws AxisFault {
        remoteAddress = endPoint;
        String authenticationServiceURL = endPoint + "AuthenticationAdmin";
        stub = new AuthenticationAdminStub(authenticationServiceURL);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

    }

    public boolean login(String userName, String password) throws AuthenticationException {
        boolean result = false;
        try {
            result = stub.login(userName, password, remoteAddress);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (LoginAuthenticationExceptionException e) {
            e.printStackTrace();
        }
        if (result) {
            ServiceContext serviceContext = stub.
                    _getServiceClient().getLastOperationContext().getServiceContext();
            String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
            httpSession.setAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN, sessionCookie);
        } else {
            throw new AuthenticationException("Failed to authenticate user");
        }

        return result;
    }

    public void logout() throws AuthenticationException {
        try {
            stub.logout();
            httpSession.removeAttribute(ServerConstants.ADMIN_SERVICE_AUTH_TOKEN);
        } catch (java.lang.Exception e) {
            String msg = "Error occurred while logging out";
            throw new AuthenticationException(msg);
        }
    }

}
