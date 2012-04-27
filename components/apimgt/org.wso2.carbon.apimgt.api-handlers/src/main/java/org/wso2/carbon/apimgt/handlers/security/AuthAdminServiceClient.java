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
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.utils.CarbonUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class AuthAdminServiceClient {

    private AuthenticationAdminStub authenticationAdminStub;
    String username;
    String password;
    private String hostname;

    public AuthAdminServiceClient() throws AxisFault {
        setSystemProperties();
        String serviceURL = CarbonUtils.getServerURL(ServerConfiguration.getInstance(),
                ServiceReferenceHolder.getInstance().getServerConfigurationContext());
        serviceURL += "AuthenticationAdmin";
        try {
            URL url = new URL(serviceURL);
            hostname = url.getHost();
        } catch (MalformedURLException e) {
            throw new AxisFault("Malformed admin service URL: " + serviceURL, e);
        }

        APIManagerConfiguration config = APIManagerConfiguration.getInstance();
        username = config.getFirstProperty(APISecurityConstants.API_SECURITY_AUTH_USERNAME);
        password = config.getFirstProperty(APISecurityConstants.API_SECURITY_AUTH_PASSWORD);
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

    private void setSystemProperties() throws AxisFault {
        ServerConfiguration config = ServerConfiguration.getInstance();
        String keyStorePath = config.getFirstProperty("Security.TrustStore.Location");
        String keyStorePassword = config.getFirstProperty("Security.TrustStore.Password");
        String keyStoreType = config.getFirstProperty("Security.TrustStore.Type");
        if (keyStorePath == null || keyStorePassword == null || keyStoreType == null) {
            throw new AxisFault("Required truststore parameters missing for authentication" +
                    " admin stub");
        }
        System.setProperty("javax.net.ssl.trustStore", keyStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);
        System.setProperty("javax.net.ssl.trustStoreType", keyStoreType);
    }
}
