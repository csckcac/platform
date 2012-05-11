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

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.apimgt.handlers.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Service client used to login to backend AuthenticationAdmin service.
 */
public class AuthAdminServiceClient {

    private AuthenticationAdminStub authenticationAdminStub;
    String username;
    String password;
    private String hostname;

    public AuthAdminServiceClient() throws AxisFault {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfiguration();
        String serviceURL = config.getFirstProperty(APIConstants.API_KEY_MANAGER_URL);;
        serviceURL += "AuthenticationAdmin";
        try {
            URL url = new URL(serviceURL);
            hostname = url.getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("Malformed admin service URL: " + serviceURL, e);
        }

        username = config.getFirstProperty(APIConstants.API_KEY_MANAGER_USERNAME);
        password = config.getFirstProperty(APIConstants.API_KEY_MANAGER_PASSWORD);
        if (username == null || password == null) {
            throw new AxisFault("Credentials for AuthenticationAdmin not provided");
        }
        
        authenticationAdminStub = new AuthenticationAdminStub(null, serviceURL);
        ServiceClient client = authenticationAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
    }

    public String login() throws Exception {
        authenticationAdminStub.login(username, password, hostname);
        ServiceContext serviceContext = authenticationAdminStub.
                _getServiceClient().getLastOperationContext().getServiceContext();
        String sessionCookie = (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
        return sessionCookie;
    }
}
