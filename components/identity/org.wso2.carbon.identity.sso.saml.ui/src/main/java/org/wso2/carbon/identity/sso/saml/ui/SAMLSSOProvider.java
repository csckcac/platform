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
package org.wso2.carbon.identity.sso.saml.ui;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.sso.saml.stub.IdentityException;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOAuthnReqDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSOReqValidationResponseDTO;
import org.wso2.carbon.identity.sso.saml.stub.types.SAMLSSORespDTO;
import org.wso2.carbon.identity.sso.saml.ui.client.SAMLSSOServiceClient;
import org.wso2.carbon.identity.sso.saml.ui.logout.LogoutRequestSender;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * This is the entry point for authentication process in an SSO scenario. This servlet is registered
 * with the URL pattern /samlsso and act as the control servlet. The message flow of an SSO scenario
 * is as follows.
 * <ol>
 * <li> SP sends a SAML Request via HTTP POST to the https://<ip>:<port>/samlsso endpoint. </li>
 * <li> IdP validates the SAML Request and checks whether this user is already authenticated.</li>
 * <li> If the user is authenticated, it will generate a SAML Response and send it back the SP via the
 * redirect_ajaxprocessor.jsp. </li>
 * <li> If the user is not authenticated, it will send him to the login page and prompts user to enter
 * his credentials. </li>
 * <li> If these credentials are valid, then the user will be redirected back the SP with a valid SAML
 * Assertion. If not, he will be prompted again for credentials. </li>
 * </ol>
 */
public class SAMLSSOProvider extends HttpServlet {

    private static Log log = LogFactory.getLog(SAMLSSOProvider.class);
    /**
     * session timeout happens in 10 hours
     */
    private static final int SSO_SESSION_EXPIRE = 36000;

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        doPost(httpServletRequest, httpServletResponse);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String username = req.getParameter(SAMLSSOProviderConstants.USERNAME);
        String password = req.getParameter(SAMLSSOProviderConstants.PASSWORD);
        HttpSession session = req.getSession();

        // Use sessionID as the tokenID, if cookie is not set.
        String ssoTokenID = session.getId();
        Cookie tokenCookie = getSSOTokenCookie(req);
        if (tokenCookie != null) {
            ssoTokenID = tokenCookie.getValue();
        }
        // Handle the request.
        try {
            if (username == null && password == null) { // First request without credentials. Should redirect  to login page
                String samlRequest = req.getParameter("SAMLRequest");
                String authMode = SAMLSSOProviderConstants.AuthnModes.USERNAME_PASSWORD;
                if (req.getParameter("authMode") != null &&
                    SAMLSSOProviderConstants.AuthnModes.OPENID.equals(req.getParameter("authMode"))) {
                    authMode = SAMLSSOProviderConstants.AuthnModes.OPENID;
                }

                // Get the relay state. If it is not there, redirect users to the error page.
                String relayState = req.getParameter(SAMLSSOProviderConstants.RELAY_STATE);
                if(relayState == null){
                    log.warn("RelayState is not present in the request.");
                    req.setAttribute(SAMLSSOProviderConstants.STATUS,
                            "RealyState is not present in the request.");
                    req.setAttribute(SAMLSSOProviderConstants.STATUS_MSG,
                            "This request will not be processed further.");
                    RequestDispatcher reqDispatcher = getServletContext().getRequestDispatcher(
                            "/carbon/sso-saml/notification_ajaxprocessor.jsp");
                    reqDispatcher.forward(req, resp);
                    return;
                }

                if (samlRequest != null) { // this is a login request
                    handleSAMLRequest(req, resp, ssoTokenID, samlRequest, relayState,
                                      authMode);
                } else { // Non-SAML request are assumed to be logout  requests
                    handleLogout(req, resp);
                }
            } else {  // Request coming from login page with username and password
                handleRequestFromLoginPage(req, resp, ssoTokenID);
            }
        } catch (IdentityException e) { // in case of an error, redirect them to notifications page with an error msg.
            log.error("Error when processing the authentication request!", e);
            req.setAttribute(SAMLSSOProviderConstants.STATUS,
                             "Error when processing the authentication request!");
            req.setAttribute(SAMLSSOProviderConstants.STATUS_MSG,
                             "Please try login again.");
            RequestDispatcher reqDispatcher = getServletContext().getRequestDispatcher(
                    "/carbon/sso-saml/notification_ajaxprocessor.jsp");
            reqDispatcher.forward(req, resp);
        }
    }

    private void handleRequestFromLoginPage(HttpServletRequest req,
                                            HttpServletResponse resp,
                                            String ssoTokenID)
            throws IdentityException, IOException, ServletException {
        String relayState = req.getParameter(SAMLSSOProviderConstants.RELAY_STATE);
        HttpSession session = req.getSession();

        // instantiate the service client
        String serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) session.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        SAMLSSOServiceClient ssoServiceClient = new SAMLSSOServiceClient(serverURL, configContext);

        // Create SAMLSSOAuthnReqDTO using the request Parameters
        SAMLSSOAuthnReqDTO authnReqDTO = new SAMLSSOAuthnReqDTO();
        populateAuthnReqDTO(req, authnReqDTO);

        // authenticate the user
        SAMLSSORespDTO authRespDTO = ssoServiceClient.authenticate(authnReqDTO, ssoTokenID);

        if (authRespDTO.getSessionEstablished()) {  // authentication is SUCCESSFUL
            // Store the cookie
            storeSSOTokenCookie(ssoTokenID, req, resp);

            // set the relay state, assertion and ACS URL as req. attributes
            req.setAttribute(SAMLSSOProviderConstants.RELAY_STATE, relayState);
            req.setAttribute(SAMLSSOProviderConstants.ASSERTION_STR, authRespDTO.getRespString());
            req.setAttribute(SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL, authRespDTO.getAssertionConsumerURL());

            // forward the request to redirect_ajaxprocessor.jsp
            RequestDispatcher reqDispatcher = getServletContext().getRequestDispatcher(
                    "/carbon/sso-saml/redirect_ajaxprocessor.jsp");
            reqDispatcher.forward(req, resp);
        } else {    // authentication FAILURE
            req.setAttribute(SAMLSSOProviderConstants.AUTH_FAILURE, Boolean.parseBoolean("true"));
            req.setAttribute(SAMLSSOProviderConstants.AUTH_FAILURE_MSG, authRespDTO.getErrorMsg());
            // repopulate the HTTP req. with the Auth. req. parameters.
            populateReAuthenticationRequest(req);

            // send back to the login.page for the next authentication attempt.
            String forwardingPath = getLoginPage(authRespDTO.getLoginPageURL());
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(forwardingPath);
            dispatcher.forward(req, resp);
        }
    }

    private void populateAuthnReqDTO(HttpServletRequest req, SAMLSSOAuthnReqDTO authnReqDTO) {
        authnReqDTO.setAssertionConsumerURL(getRequestParameter(req, SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL));
        authnReqDTO.setId(getRequestParameter(req, SAMLSSOProviderConstants.REQ_ID));
        authnReqDTO.setIssuer(getRequestParameter(req, SAMLSSOProviderConstants.ISSUER));
        authnReqDTO.setUsername(getRequestParameter(req, SAMLSSOProviderConstants.USERNAME));
        authnReqDTO.setPassword(getRequestParameter(req, SAMLSSOProviderConstants.PASSWORD));
        authnReqDTO.setSubject(getRequestParameter(req, SAMLSSOProviderConstants.SUBJECT));
        authnReqDTO.setRpSessionId(getRequestParameter(req, SAMLSSOProviderConstants.RP_SESSION_ID));
        authnReqDTO.setAssertionString(getRequestParameter(req, SAMLSSOProviderConstants.ASSERTION_STR));
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp)
            throws IdentityException, IOException, ServletException {
        req.setAttribute(SAMLSSOProviderConstants.STATUS,
                         "You have been successfully signed out.");
        req.setAttribute(SAMLSSOProviderConstants.STATUS_MSG,
                         "All the other authenticated sessions are terminated.");
        RequestDispatcher reqDispatcher = getServletContext().getRequestDispatcher(
                "/carbon/sso-saml/notification_ajaxprocessor.jsp");
        reqDispatcher.forward(req, resp);
    }

    private void handleSAMLRequest(HttpServletRequest req,
                                   HttpServletResponse resp,
                                   String ssoTokenID, String samlRequest,
                                   String relayState, String authMode)
            throws IdentityException, IOException, ServletException {
        String rpSessionId = req.getParameter(MultitenantConstants.SSO_AUTH_SESSION_ID);
        // Instantiate the service client.
        HttpSession session = req.getSession();
        String serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
        ConfigurationContext configContext = (ConfigurationContext) session.getServletContext()
                .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        SAMLSSOServiceClient ssoServiceClient = new SAMLSSOServiceClient(serverURL, configContext);
        SAMLSSOReqValidationResponseDTO signInRespDTO = ssoServiceClient.validate(samlRequest, ssoTokenID,
                                                                                  rpSessionId, authMode);
        // If it is a login request.
        if (!signInRespDTO.getLogOutReq()) {
            //  an authentication context has not been already established, redirect user to a login page.
            if (signInRespDTO.getValid() && signInRespDTO.getResponse() == null) {
                populateLoginPageRequest(req, signInRespDTO);
                req.setAttribute(SAMLSSOProviderConstants.RELAY_STATE, relayState);
                String forwardingPath = getLoginPage(signInRespDTO.getLoginPageURL());
                RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(forwardingPath);
                dispatcher.forward(req, resp);

                // an auth. context has been already established. So redirect users back to ACS.
            } else if (signInRespDTO.getResponse() != null) {
                // add relay state, assertion string and ACS URL as request parameters.
                req.setAttribute(SAMLSSOProviderConstants.RELAY_STATE, relayState);
                req.setAttribute(SAMLSSOProviderConstants.ASSERTION_STR, signInRespDTO.getResponse());
                req.setAttribute(SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL, signInRespDTO.getAssertionConsumerURL());
                if (SAMLSSOProviderConstants.AuthnModes.OPENID.equals(authMode)) {
                    storeSSOTokenCookie(ssoTokenID, req, resp);
                }
                // forward to the redirect_ajaxprocessor.jsp
                RequestDispatcher reqDispatcher = getServletContext().getRequestDispatcher(
                        "/carbon/sso-saml/redirect_ajaxprocessor.jsp");
                reqDispatcher.forward(req, resp);
            }
        } else {     // in case of a logout request
            // trigger sending asynchronous logout requests to the other session participants.
            LogoutRequestSender.getInstance().sendLogoutRequests(signInRespDTO.getLogoutRespDTO());
            // add relay state, assertion string and ACS URL as request parameters.
            req.setAttribute(SAMLSSOProviderConstants.RELAY_STATE, relayState);
            req.setAttribute(SAMLSSOProviderConstants.ASSERTION_STR, signInRespDTO.getLogoutResponse());
            req.setAttribute(SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL, signInRespDTO.getAssertionConsumerURL());
            // forward to the redirect_ajaxprocessor.jsp
            RequestDispatcher reqDispatcher = getServletContext().getRequestDispatcher("/carbon/sso-saml/redirect_ajaxprocessor.jsp");
            reqDispatcher.forward(req, resp);
        }
    }

    private void populateLoginPageRequest(HttpServletRequest req,
                                          SAMLSSOReqValidationResponseDTO signInRespDTO) {
        req.setAttribute(SAMLSSOProviderConstants.ISSUER, signInRespDTO.getIssuer());
        req.setAttribute(SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL, signInRespDTO.getAssertionConsumerURL());
        req.setAttribute(SAMLSSOProviderConstants.REQ_ID, signInRespDTO.getId());
        req.setAttribute(SAMLSSOProviderConstants.SUBJECT, signInRespDTO.getSubject());
        req.setAttribute(SAMLSSOProviderConstants.RP_SESSION_ID, signInRespDTO.getRpSessionId());
        req.setAttribute(SAMLSSOProviderConstants.ASSERTION_STR, signInRespDTO.getAssertionString());
    }

    private void populateReAuthenticationRequest(HttpServletRequest req) {
        req.setAttribute(SAMLSSOProviderConstants.ISSUER, req.getParameter(SAMLSSOProviderConstants.ISSUER));
        req.setAttribute(SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL, req.getParameter(SAMLSSOProviderConstants.ASSRTN_CONSUMER_URL));
        req.setAttribute(SAMLSSOProviderConstants.REQ_ID, req.getParameter(SAMLSSOProviderConstants.REQ_ID));
        req.setAttribute(SAMLSSOProviderConstants.SUBJECT, req.getParameter(SAMLSSOProviderConstants.SUBJECT));
        req.setAttribute(SAMLSSOProviderConstants.RP_SESSION_ID, req.getParameter(SAMLSSOProviderConstants.RP_SESSION_ID));
        req.setAttribute(SAMLSSOProviderConstants.ASSERTION_STR, req.getParameter(SAMLSSOProviderConstants.ASSERTION_STR));
    }

    private String getAdminConsoleURL(HttpServletRequest request) {
        String url = CarbonUIUtil.getAdminConsoleURL(request);
        if (url.contains("/samlsso")) {
            url = url.replace("/samlsso", "");
        }
        return url;
    }

    private Cookie getSSOTokenCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(SAMLSSOProviderConstants.SSO_TOKEN_ID)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private void storeSSOTokenCookie(String ssoTokenID, HttpServletRequest req,
                                     HttpServletResponse resp) {
        Cookie ssoTokenCookie = getSSOTokenCookie(req);
        if (ssoTokenCookie == null) {
            ssoTokenCookie = new Cookie(SAMLSSOProviderConstants.SSO_TOKEN_ID, ssoTokenID);
        }
        ssoTokenCookie.setMaxAge(SSO_SESSION_EXPIRE);
        resp.addCookie(ssoTokenCookie);
    }

    private String getLoginPage(String customLoginPage) {
        if (customLoginPage != null) {
            return "/carbon/" + customLoginPage.trim();
        } else {
            return "/carbon/" + "sso-saml/login_ajaxprocessor.jsp";
        }
    }

    private String getRequestParameter(HttpServletRequest req, String paramName) {
        // This is to handle "null" values coming as the parameter values from the JSP.
        if (req.getParameter(paramName) != null && req.getParameter(paramName).equals("null")) {
            return null;
        }
        return req.getParameter(paramName);
    }

}
