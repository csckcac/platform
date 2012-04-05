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

package org.wso2.carbon.bam.core.util;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;


import org.wso2.carbon.authenticator.proxy.AuthenticationAdminClient;
import org.wso2.carbon.bam.core.clients.AuthenticationAdminClient_2_0_2;
import org.wso2.carbon.bam.core.clients.AuthenticationAdminClient_2_0_3;
import org.wso2.carbon.bam.core.clients.AuthenticationAdminClient_3_1_0;
import org.wso2.carbon.bam.common.dataobjects.service.ServerDO;
import org.wso2.carbon.bam.core.clients.AuthenticationAdminClient_3_2_0;
import org.wso2.carbon.bam.util.BAMException;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;

/**
 * Service client authentication related utilities.
 */
public class ClientAuthHandler {
    private static Log log = LogFactory.getLog(ClientAuthHandler.class);

    private static SessionCache sessionCache = new SessionCache();
    private static ClientAuthHandler handler = null;

    public static ClientAuthHandler getClientAuthHandler() {

        if (handler == null) {
            handler = new ClientAuthHandler();
        }

        return handler;
    }

    public static SessionCache getSessionCache() {
        return sessionCache;
    }

    public String getSessionString(ServerDO server) throws BAMException {
        //this could be the server id
        if (sessionCache.getSessionString(server.getServerURL()) == null) {
            isAuthenticateWithServer(server);
        }

        return sessionCache.getSessionString(server.getServerURL());
    }

    public void authenticateForcefully(ServerDO server) throws BAMException {
        isAuthenticateWithServer(server);
    }

    //Whenever the authentiaction admin WSDL is changed this should be generated from wsdl like in the
    //case of 2_0_X
    private boolean authenticateWithServer_Carbon_3_0_0(ServerDO server) {
        boolean isLoggedIn = false;

        try {
            AuthenticationAdminClient authenticationAdminClient_3_0_0 =
                    new AuthenticationAdminClient(BAMUtil.getConfigurationContextService().getClientConfigContext(),
                            server.getServerURL() + "/services/", null, null, false);

            isLoggedIn = authenticationAdminClient_3_0_0.login(server.getUserName(), server.getPassword(),
                    NetworkUtils.getLocalHostname());

            if (isLoggedIn) {
                getSessionCache().addSessionString(server.getServerURL(),authenticationAdminClient_3_0_0.getAdminCookie());
            }
        } catch (Throwable ignore) {
            log.debug("Carbon 300 authentication failed with server: " + server.toString(), ignore);
        }

        return isLoggedIn;
    }

    private boolean authenticateWithServer_Carbon_2_0_3(ServerDO server) {
        boolean isLoggedIn = false;

        try {
            AuthenticationAdminClient_2_0_3 authenticationAdminClient_2_0_3 =
                    new AuthenticationAdminClient_2_0_3(server.getServerURL());

            isLoggedIn = authenticationAdminClient_2_0_3.authenticate(server.getUserName(), server.getPassword());

            if (isLoggedIn) {
                String sessionCookie = authenticationAdminClient_2_0_3.getSessionCookie();
                getSessionCache().addSessionString(server.getServerURL(), sessionCookie);
            }
        } catch (Throwable ignore) {
            log.debug("Carbon 203 authentication failed with server: " + server.toString(), ignore);
        }

        return isLoggedIn;
    }

    private boolean authenticateWithServer_Carbon_2_0_2(ServerDO server) {
        boolean isLoggedIn = false;

        try {
            AuthenticationAdminClient_2_0_2 authenticationAdminClient_2_0_2 =
                    new AuthenticationAdminClient_2_0_2(server.getServerURL());

            isLoggedIn = authenticationAdminClient_2_0_2.authenticate(server.getUserName(), server.getPassword());

            if (isLoggedIn) {
                String sessionCookie = authenticationAdminClient_2_0_2.getSessionCookie();
                getSessionCache().addSessionString(server.getServerURL(), sessionCookie);
            }
        } catch (Throwable ignore) {
            log.debug("Carbon 202 authentication failed with server: " + server.toString(), ignore);
        }

        return isLoggedIn;
    }

    private boolean authenticateWithServer_Carbon_3_1_0(ServerDO server) {
        boolean isLoggedIn = false;

        try {
            AuthenticationAdminClient_3_1_0 authenticationAdminClient_3_1_0 =
                    new AuthenticationAdminClient_3_1_0(server.getServerURL());

            isLoggedIn = authenticationAdminClient_3_1_0.authenticate(server.getUserName(), server.getPassword());

            if (isLoggedIn) {
                String sessionCookie = authenticationAdminClient_3_1_0.getSessionCookie();
                getSessionCache().addSessionString(server.getServerURL(), sessionCookie);
            }
        } catch (Throwable ignore) {
            if (log.isDebugEnabled()) {
                log.debug("Authentication failed with server: " + server.toString(), ignore);
            }
        }

        return isLoggedIn;
    }

    private boolean authenticateWithServer_Carbon_CurrentVersion(ServerDO server) {
        boolean isLoggedIn = false;

        try {
            AuthenticationAdminClient_3_2_0 authenticationAdminClient = new AuthenticationAdminClient_3_2_0(server.getServerURL());

            isLoggedIn = authenticationAdminClient.authenticate(server.getUserName(), server.getPassword());

            if (isLoggedIn) {
                String sessionCookie = authenticationAdminClient.getSessionCookie();
                getSessionCache().addSessionString(server.getServerURL(), sessionCookie);
            }
        } catch (Throwable ignore) {
            if (log.isDebugEnabled()) {
                log.debug("Authentication failed with server: " + server.toString(), ignore);
            }
        }

        return isLoggedIn;
    }


    public boolean isAuthenticateWithServer(ServerDO server) throws BAMException {
        boolean logInState;

        logInState = authenticateWithServer_Carbon_CurrentVersion(server);
        if (!logInState) {
            logInState = authenticateWithServer_Carbon_3_1_0(server);
            if (!logInState) {
                logInState = authenticateWithServer_Carbon_3_0_0(server);
                if (!logInState) {
                    logInState = authenticateWithServer_Carbon_2_0_3(server);
                    if (!logInState) {
                        logInState = authenticateWithServer_Carbon_2_0_2(server);
                        if (!logInState) {
                            log.info("Could not authenticate with server : " + server.getServerURL());
                        }
                    }

                }
            }
        }
        return logInState;
    }


    public static boolean checkAuthException(AxisFault axisFault) {

        if (axisFault != null) {
            Throwable cause = axisFault.getCause();
            QName name = axisFault.getFaultCode();
            if (name != null && name.getLocalPart() != null
                    && (name.getLocalPart().equals(ServerConstants.AUTHENTICATION_FAULT_CODE)
                    || name.getLocalPart().equals(CarbonConstants.AUTHZ_FAULT_CODE))) {
                return true;
            }
            if (axisFault.getMessage().toLowerCase().indexOf("session timed out") != -1) {
                return true;
            }
            if ((cause != null) && (cause instanceof AxisFault)) {
                axisFault = (AxisFault) cause;
                name = axisFault.getFaultCode();
                if (name != null && name.getLocalPart() != null
                        && (name.getLocalPart().equals(ServerConstants.AUTHENTICATION_FAULT_CODE)
                        || name.getLocalPart().equals(CarbonConstants.AUTHZ_FAULT_CODE))) {
                    return true;
                }
            }
        }

        return false;
    }

}
