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

package org.wso2.carbon.identity.oauth.ui;

import org.apache.amber.oauth2.as.request.OAuthAuthzRequest;
import org.apache.amber.oauth2.as.response.OAuthASResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.OAuthResponse;
import org.apache.amber.oauth2.common.message.types.ResponseType;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.oauth.common.OAuth2ErrorCodes;
import org.wso2.carbon.identity.oauth.ui.client.OAuth2ServiceClient;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2AuthorizeReqDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2AuthorizeRespDTO;
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
public class OAuth2Servlet extends HttpServlet {

    private static final Log log = LogFactory.getLog(OAuth2Servlet.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        service(req, resp);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            // requests coming from the login page.
            if (req.getParameter("oauth_user_name") != null && req.getParameter("oauth_user_password") != null) {
                String redirectURL = handleResourceOwnerAuthorization(req, resp);
                resp.sendRedirect(redirectURL);
                return;
            }
            // requests coming for authorization.
            if (req.getRequestURI().endsWith(OAuthConstants.AUTHORIZE_TOKEN_URL)) {
                String redirectURL = handleOAuthAuthorizationRequest(req);
                resp.sendRedirect(redirectURL);
                return;
            }

            else {
                HttpSession session = req.getSession();
                session.setAttribute(OAuthConstants.OAUTH_ERROR_CODE, OAuth2ErrorCodes.INVALID_OAUTH_URL);
                session.setAttribute(OAuthConstants.OAUTH_ERROR_MESSAGE, "Invalid OAuth request URL.");
                String errorPageURL = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth-error.jsp";
                errorPageURL = errorPageURL.replace("/oauth2/carbon/oauth/", "/carbon/oauth/");
                resp.sendRedirect(errorPageURL);
                return;
            }
        } catch (OAuthSystemException e) {
            log.error("Error when processing the authorization request.", e);
            HttpSession session = req.getSession();
            session.setAttribute(OAuthConstants.OAUTH_ERROR_CODE, OAuth2ErrorCodes.SERVER_ERROR);
            session.setAttribute(OAuthConstants.OAUTH_ERROR_MESSAGE, "Error when processing the authorization request.");
            String errorPageURL = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth-error.jsp";
            errorPageURL = errorPageURL.replace("/oauth2/carbon/oauth/", "/carbon/oauth/");
            resp.sendRedirect(errorPageURL);
            return;
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
                errorPageURL = errorPageURL.replace("/oauth2/carbon/oauth/", "/carbon/oauth/");
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
                errorPageURL = errorPageURL.replace("/oauth2/carbon/oauth/", "/carbon/oauth/");
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
            String loginPage = CarbonUIUtil.getAdminConsoleURL(req) + "oauth/oauth2-login.jsp";
            loginPage = loginPage.replace("/oauth2/carbon/oauth/", "/carbon/oauth/");
            return loginPage;

        } catch (OAuthProblemException e) {
            log.error(e.getError(), e.getCause());
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                    .error(e).location(clientValidationResponseDTO.getCallbackURL()).buildQueryMessage().getLocationUri();
        }
    }

    private String handleResourceOwnerAuthorization(HttpServletRequest req, HttpServletResponse resp) throws IOException, OAuthSystemException {

        OAuth2Parameters oauth2Params = (OAuth2Parameters) req.getSession().getAttribute(
                OAuthConstants.OAUTH2_PARAMS);

        if(log.isDebugEnabled()){
            log.debug("Request from the oauth2-login.jsp for the user : " + req.getParameter(
                    "oauth_user_name"));
        }

        try {
            OAuth2AuthorizeRespDTO authzRespDTO = authorize(req, oauth2Params);

            OAuthASResponse.OAuthAuthorizationResponseBuilder builder = OAuthASResponse
                    .authorizationResponse(req, HttpServletResponse.SC_FOUND);

            OAuthResponse response;

            // user is authorized.
            if (authzRespDTO.getAuthorized()) {
                if (ResponseType.CODE.toString().equals(oauth2Params.getResponseType())) {
                    builder.setCode(authzRespDTO.getAuthorizationCode());
                } else if (ResponseType.TOKEN.toString().equals(oauth2Params.getResponseType())) {
                    builder.setAccessToken(authzRespDTO.getAccessToken());
                    builder.setExpiresIn(String.valueOf(60 * 60));
                }
                builder.setParam("state", oauth2Params.getState());
                String redirectURL = authzRespDTO.getCallbackURI();
                response = builder.location(redirectURL).buildQueryMessage();
            } else {
                OAuthProblemException oauthException = OAuthProblemException.error(
                        authzRespDTO.getErrorCode(), authzRespDTO.getErrorMsg());
                response = OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                        .error(oauthException)
                        .location(authzRespDTO.getCallbackURI()).buildQueryMessage();
            }
            resp.setStatus(HttpServletResponse.SC_FOUND);
            return response.getLocationUri();
        } catch (OAuthProblemException e) {
            log.error(e.getError(), e.getCause());
            return OAuthASResponse.errorResponse(HttpServletResponse.SC_FOUND)
                    .error(e)
                    .location(oauth2Params.getRedirectURI()).buildQueryMessage().getLocationUri();
        }
    }

    private OAuth2AuthorizeRespDTO authorize(HttpServletRequest req, OAuth2Parameters oauth2Params)
            throws OAuthProblemException {
        try {
            // authenticate and issue the authorization code
            String backendServerURL = CarbonUIUtil.getServerURL(req.getSession()
                    .getServletContext(), req.getSession());
            ConfigurationContext configContext = (ConfigurationContext) req.getSession()
                    .getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
            OAuth2ServiceClient oauth2ServiceClient = new OAuth2ServiceClient(backendServerURL, configContext);

            OAuth2AuthorizeReqDTO authzReqDTO = new OAuth2AuthorizeReqDTO();
            authzReqDTO.setCallbackUrl(oauth2Params.getRedirectURI());
            authzReqDTO.setConsumerKey(oauth2Params.getClientId());
            authzReqDTO.setResponseType(oauth2Params.getResponseType());
            authzReqDTO.setScopes(oauth2Params.getScopes().toArray(new String[oauth2Params.getScopes().size()]));
            authzReqDTO.setUsername(req.getParameter("oauth_user_name"));
            authzReqDTO.setPassword(req.getParameter("oauth_user_password"));

            return oauth2ServiceClient.authorize(authzReqDTO);
        } catch (RemoteException e) {
            log.error("Error when invoking the OAuth2Service to perform authorization.", e);
            throw OAuthProblemException.error(OAuth2ErrorCodes.SERVER_ERROR,
                    "Error when invoking the OAuth2Service to perform authorization.");
        }
    }

    private OAuth2ClientValidationResponseDTO validateClient(HttpServletRequest req,
                                                               String clientId,
                                                               String callbackURL) throws OAuthSystemException {
        // authenticate and issue the authorization code
        String backendServerURL = CarbonUIUtil.getServerURL(req.getSession()
                .getServletContext(), req.getSession());
        ConfigurationContext configContext = (ConfigurationContext) req.getSession()
                .getServletContext().getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        try {
            OAuth2ServiceClient oauth2ServiceClient = new OAuth2ServiceClient(backendServerURL, configContext);
            return oauth2ServiceClient.validateClient(clientId, callbackURL);
        } catch (RemoteException e) {
            log.error("Error when invoking the OAuth2Service for client validation.");
            throw new OAuthSystemException(e.getMessage(), e);
        }
    }
}
