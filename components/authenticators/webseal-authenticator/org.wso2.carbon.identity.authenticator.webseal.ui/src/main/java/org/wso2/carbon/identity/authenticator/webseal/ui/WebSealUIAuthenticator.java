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
package org.wso2.carbon.identity.authenticator.webseal.ui;

import java.rmi.RemoteException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.common.AuthenticationException;
import org.wso2.carbon.core.security.AuthenticatorsConfiguration;
import org.wso2.carbon.identity.authenticator.webseal.stub.client.WebSealAuthenticatorStub;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.ui.DefaultCarbonAuthenticator;

public class WebSealUIAuthenticator extends DefaultCarbonAuthenticator {

    public static final String WEBSEAL_USER = "iv-user";
    protected static final Log log = LogFactory.getLog(WebSealUIAuthenticator.class);
    private static final int DEFAULT_PRIORITY_LEVEL = 10;
    private static final String AUTHENTICATOR_NAME = "WebSealUIAuthenticator";

    public boolean isHandle(Object object) {
        if (!(object instanceof HttpServletRequest)) {
            return false;
        }
        HttpServletRequest request = (HttpServletRequest) object;
        String ivuser = request.getHeader(WEBSEAL_USER);
        if (ivuser != null) {
            return true;
        }
        return false;
    }

    public int getPriority() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);
        if (authenticatorConfig != null && authenticatorConfig.getPriority() > 0) {
            return authenticatorConfig.getPriority();
        }
        return DEFAULT_PRIORITY_LEVEL;
    }

    public String getAuthenticatorName() {
        return AUTHENTICATOR_NAME;
    }

    /**
     * {@inheritDoc}
     */
    public boolean authenticate(Object object) throws AuthenticationException {
        HttpServletRequest request = (HttpServletRequest) object;
        String credentials = request.getHeader("Authorization");
        String websealuser = null;
        String password = null;
        String username;

        username = request.getHeader(WEBSEAL_USER);

        if (credentials != null) {
            credentials = credentials.trim();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Empty Authorization header");
            }
            return false;
        }

        if (credentials != null && credentials.startsWith("Basic ")) {
            credentials = new String(Base64.decode(credentials.substring(6)));
            int i = credentials.indexOf(':');
            if (i == -1) {
                websealuser = credentials;
            } else {
                websealuser = credentials.substring(0, i);
            }

            if (i != -1) {
                password = credentials.substring(i + 1);
                if (password != null && password.equals("")) {
                    password = null;
                }
            }
        }

        try {
            return authenticate(request, websealuser, password, username);
        } catch (RemoteException e) {
            throw new AuthenticationException(e.getMessage(), e);
        }
    }

    private boolean authenticate(HttpServletRequest request, String websealUser, String password,
            String userName) throws RemoteException {
        try {

            if (websealUser == null || password == null) {
                if (log.isDebugEnabled()) {
                    if (websealUser == null) {
                        log.debug("No valid webseal user name provided");
                    }
                    if (password == null) {
                        log.debug("No valid webseal user password provided");
                    }
                    if (password == null) {
                        log.debug("No valid webseal authneticated user name provided");
                    }
                }
                return false;
            }

            ServletContext servletContext = request.getSession().getServletContext();
            ConfigurationContext configContext = (ConfigurationContext) servletContext
                    .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

            if (configContext == null) {
                log.error("Configuration context is null.");
            }
            // Obtain the back-end server URL from the request. If not obtain it
            // from the http
            // session and then from the ServletContext.
            HttpSession session = request.getSession();
            String backendServerURL = request.getParameter("backendURL");
            if (backendServerURL == null) {
                backendServerURL = CarbonUIUtil.getServerURL(servletContext, request.getSession());
            }

            // Back-end server URL is stored in the session, even if it is an
            // incorrect one. This
            // value will be displayed in the server URL text box. Usability
            // improvement.
            session.setAttribute(CarbonConstants.SERVER_URL, backendServerURL);

            boolean isLogged = false;

            String serviceEPR = backendServerURL + "WebSealAuthenticator";
            WebSealAuthenticatorStub stub = new WebSealAuthenticatorStub(configContext, serviceEPR);
            ServiceClient client = stub._getServiceClient();
            Options options = client.getOptions();
            options.setManageSession(true);

            return isLogged;

        } catch (AxisFault axisFault) {
            throw axisFault;
        } catch (Exception e) {
            throw new AxisFault("Exception occured", e);
        }
    }

    public boolean isDisabled() {
        AuthenticatorsConfiguration authenticatorsConfiguration = AuthenticatorsConfiguration.getInstance();
        AuthenticatorsConfiguration.AuthenticatorConfig authenticatorConfig =
                authenticatorsConfiguration.getAuthenticatorConfig(AUTHENTICATOR_NAME);
        if (authenticatorConfig != null) {
            return authenticatorConfig.isDisabled();
        }
        return false;
    }

    public boolean reAuthenticateOnSessionExpire(Object object) throws AuthenticationException {
        return false;
    }
}
