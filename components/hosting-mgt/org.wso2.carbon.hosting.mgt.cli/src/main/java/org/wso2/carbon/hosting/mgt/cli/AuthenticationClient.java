/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.hosting.mgt.cli;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGException;
import org.wso2.carbon.cloud.csg.common.CSGUtils;


import java.io.File;
import java.net.SocketException;
import java.rmi.RemoteException;

public class AuthenticationClient {
    private static final String AXIS2_XML_FILE = "axis2_client.xml";
    /**
     * Returns the session cookie for subsequent invocations
     *
     * @param serverUrl  the url of the server to authenticate
     * @param userName   username
     * @param passWord   password
     * @param hostName   the host name of the remote server
     * @param domainName domain name of the tenant
     * @return the session cookie
     * @throws LoginAuthenticationExceptionException
     *                                  throws in case of an auth error
     * @throws java.rmi.RemoteException throws in case of a connection error
     * @throws java.net.SocketException throws in case of a socket error
     */
    public String getSessionCookie(String serverUrl,
                                   String userName,
                                   String passWord,
                                   String hostName,
                                   String domainName)
            throws RemoteException, SocketException, LoginAuthenticationExceptionException {
        try {
            AuthenticationAdminStub authenticationAdminStub =
                    getLoggedAuthAdminStub(serverUrl, userName, passWord, hostName, domainName);
            ServiceContext serivceContext = authenticationAdminStub._getServiceClient().
                    getLastOperationContext().getServiceContext();

            return (String) serivceContext.getProperty(HTTPConstants.COOKIE_STRING);

        } catch (CSGException ex) {
            throw new AxisFault(ex.getMessage(), ex);
        }
    }


    /**
     * Get the authentication stub for a logged user
     *
     * @param serverUrl  the remote login server url
     * @param userName   user name of the server
     * @param passWord   password of the server
     * @param hostName   host name of the remote login server
     * @param domainName tenant domain name
     * @return authentication stub of the logged user
     * @throws CSGException
     */
    public AuthenticationAdminStub getLoggedAuthAdminStub(String serverUrl,
                                                          String userName,
                                                          String passWord,
                                                          String hostName,
                                                          String domainName) throws CSGException {
        AuthenticationAdminStub authenticationAdminStub;
        boolean isLoggedIn;
        String loggingName = CSGUtils.getFullUserName(userName, domainName);
        System.out.println("logging Name  " + loggingName);
        System.out.println("server url  " + serverUrl);
        try {
            if (isClientAxis2XMLExists()) {

                ConfigurationContext configurationContext =
                        ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                                null, AXIS2_XML_FILE);


                authenticationAdminStub =
                        new AuthenticationAdminStub(configurationContext, serverUrl);
            } else {

                authenticationAdminStub = new AuthenticationAdminStub(serverUrl);
            }
            isLoggedIn = authenticationAdminStub.login(loggingName, passWord, hostName);
        } catch (Exception e) {
            throw new CSGException(e);
        }

        if (!isLoggedIn) {
            System.out.println("Login failed !");
            throw new CSGException("User '" + loggingName + "' cloud not logged into server '" +
                    hostName + "', using server URL '" + serverUrl + "'");
        }
        return authenticationAdminStub;
    }

    /**
     * Check if we have a client axis2.xml ( for example in ESB)
     *
     * @return true if we have client axis2.xml, false otherwise
     */
    public static boolean isClientAxis2XMLExists() {
        File f = new File(AXIS2_XML_FILE);
        return f.exists();
    }

}