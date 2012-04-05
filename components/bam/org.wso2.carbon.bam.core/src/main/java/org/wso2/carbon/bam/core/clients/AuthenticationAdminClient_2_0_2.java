/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.bam.core.clients;

import org.apache.axis2.AxisFault;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.services.stub.authenticationadminservice202.AuthenticationAdminServiceStub;
import org.wso2.carbon.bam.services.stub.authenticationadminservice202.AuthenticationExceptionException;
import org.wso2.carbon.bam.core.util.BAMUtil;
import org.wso2.carbon.utils.NetworkUtils;

import java.net.SocketException;
import java.rmi.RemoteException;

import static org.wso2.carbon.bam.core.BAMConstants.AUTH_ADMIN_SERVICE_2_0_X;
import static org.wso2.carbon.bam.core.BAMConstants.SERVICES_SUFFIX;

public class AuthenticationAdminClient_2_0_2 extends AbstractAdminClient<AuthenticationAdminServiceStub> {
    private static Log log = LogFactory.getLog(AuthenticationAdminClient_2_0_2.class);
    private String sessionCookie;

    public AuthenticationAdminClient_2_0_2(String serverURL) throws AxisFault {
        String serviceURL;
        serviceURL = generateURL(new String[]{serverURL, SERVICES_SUFFIX, AUTH_ADMIN_SERVICE_2_0_X});
        stub = new AuthenticationAdminServiceStub(BAMUtil.getConfigurationContextService().getClientConfigContext(), serviceURL);
        stub._getServiceClient().getOptions().setManageSession(true);
    }

    public boolean authenticate(String username, String password) throws RemoteException, SocketException,
            AuthenticationExceptionException {
        boolean loginResponse;

        loginResponse = stub.login(username, password, NetworkUtils.getLocalHostname());

        this.sessionCookie = (String) stub._getServiceClient().getLastOperationContext().getServiceContext()
                .getProperty(HTTPConstants.COOKIE_STRING);

        return loginResponse;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }
}
