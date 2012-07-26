/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.identity.oauth.ui.endpoints.authz;

import org.apache.amber.oauth2.as.request.OAuthAuthzRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.ui.OAuth2Parameters;
import org.wso2.carbon.identity.oauth.ui.OAuthConstants;
import org.wso2.carbon.identity.oauth.ui.client.OAuth2ServiceClient;
import org.wso2.carbon.identity.oauth.ui.internal.OAuthUIServiceComponentHolder;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientValidationResponseDTO;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.rmi.RemoteException;

/**
 * This servlet handles the authorization endpoint and token endpoint.
 */
public class OAuth2AuthzEndpoint extends HttpServlet {

    private static final Log log = LogFactory.getLog(OAuth2AuthzEndpoint.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {

            // requests coming for authorization.
            if (req.getRequestURI().endsWith("authorize")) {
                String redirectURL = handleOAuthAuthorizationRequest(req);
                resp.sendRedirect(redirectURL);
            }

            else {
                HttpSession session = req.getSession();
                session.setAttribute(OAuthConstants.OAUTH_ERROR_CODE,
                        OAuth2ErrorCodes.INVALID_OAUTH_URL);
                session.setAttribute(OAuthConstants.OAUTH_ERROR_MESSAGE,
                        "Invalid OAuth request URL.");
                String errorPageURL = CarbonUIUtil.getAdminConsoleURL(req) +
                        "oauth/oauth-error.jsp";
                errorPageURL = errorPageURL.replace("/oauth2/authorize", "");
                resp.sendRedirect(errorPageURL);
            }
        } catch (OAuthSystemException e) {
            log.error("Error when processing the authorization request.", e);
            HttpSession session = req.getSession();
            session.setAttribute(OAuthConstants.OAUTH_ERROR_CODE, OAuth2ErrorCodes.SERVER_ERROR);
            session.setAttribute(OAuthConstants.OAUTH_ERROR_MESSAGE, "Error when processing the authorization request.");
            String errorPageURL = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth-error.jsp";
            errorPageURL = errorPageURL.replace("/oauth2/authorize", "");
            resp.sendRedirect(errorPageURL);
        }
    }

    private String handleOAuthAuthorizationRequest(HttpServletRequest req) throws IOException, OAuthSystemException {
        OAuth2ClientValidationResponseDTO clientValidationResponseDTO = null;
        try {
            // Extract the client_id and callback url from the request, because constructing an Amber
            // Authz request can cause an OAuthProblemException exception. In that case, that error
            // needs to be passed back to client. Before that we need to validate the client_id and callback URL
            String clientId = req.getParameter("client_id");
            String callbackURL = req.getParameter("redirect_uri");

            if (clientId != null) {
                clientValidationResponseDTO = validateClient(req, clientId, callbackURL);
            } else { // Client Id is not present in the request.
                log.warn("Client Id is not present in the authorization request.");
                HttpSession session = req.getSession();
                session.setAttribute(OAuthConstants.OAUTH_ERROR_CODE,
                        OAuth2ErrorCodes.INVALID_REQUEST);
                session.setAttribute(OAuthConstants.OAUTH_ERROR_MESSAGE,
                        "Invalid Request. Client Id is not present in the request");
                String errorPageURL = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth-error.jsp";
                errorPageURL = errorPageURL.replace("/oauth2/authorize", "");
                return errorPageURL;
            }
            // Client is not valid. Do not send this error back to client, send to an error page instead.
            if (!clientValidationResponseDTO.getValidClient()) {
                HttpSession session = req.getSession();
                session.setAttribute(OAuthConstants.OAUTH_ERROR_CODE,
                        clientValidationResponseDTO.getErrorCode());
                session.setAttribute(OAuthConstants.OAUTH_ERROR_MESSAGE,
                        clientValidationResponseDTO.getErrorMsg());
                String errorPageURL = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth-error.jsp";
                errorPageURL = errorPageURL.replace("/oauth2/authorize", "");
                return errorPageURL;
            }

            // Now the client is valid, redirect him for authorization page.
            OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(req);
            OAuth2Parameters params = new OAuth2Parameters();
            params.setApplicationName(clientValidationResponseDTO.getApplicationName());
            params.setRedirectURI(clientValidationResponseDTO.getCallbackURL());
            params.setResponseType(oauthRequest.getResponseType());
            params.setScopes(oauthRequest.getScopes());
            params.setState(oauthRequest.getState());
            params.setClientId(clientId);

            HttpSession session = req.getSession();
            session.setAttribute(OAuthConstants.OAUTH2_PARAMS, params);
            String loginPage = CarbonUIUtil.getAdminConsoleURL(req) +
                    "oauth/oauth2_authn_ajaxprocessor.jsp";
            loginPage = loginPage.replace("/oauth2/authorize", "");
            return loginPage;

        } catch (OAuthProblemException e) {
            log.error(e.getError(), e.getCause());
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                    .error(e).location(clientValidationResponseDTO.getCallbackURL())
                    .buildQueryMessage().getLocationUri();
        }
    }

    private OAuth2ClientValidationResponseDTO validateClient(HttpServletRequest req,
                                                               String clientId,
                                                               String callbackURL)
            throws OAuthSystemException {
        // authenticate and issue the authorization code
        String backendServerURL = CarbonUIUtil.getServerURL(
                OAuthUIServiceComponentHolder.getInstance().getServerConfigurationService());
        ConfigurationContext configContext = (ConfigurationContext) req.getSession()
                .getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        try {
            OAuth2ServiceClient oauth2ServiceClient = new OAuth2ServiceClient(backendServerURL,
                    configContext);
            return oauth2ServiceClient.validateClient(clientId, callbackURL);
        } catch (RemoteException e) {
            log.error("Error when invoking the OAuth2Service for client validation.");
            throw new OAuthSystemException(e.getMessage(), e);
        }
    }

}
