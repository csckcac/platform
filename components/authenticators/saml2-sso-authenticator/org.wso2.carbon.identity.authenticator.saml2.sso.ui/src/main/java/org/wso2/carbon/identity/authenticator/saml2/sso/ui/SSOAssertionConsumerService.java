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
package org.wso2.carbon.identity.authenticator.saml2.sso.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.*;
import org.opensaml.xml.XMLObject;
import org.wso2.carbon.identity.authenticator.saml2.sso.ui.internal.SAML2SSOAuthFEDataHolder;
import org.wso2.carbon.ui.CarbonSSOSessionManager;
import org.wso2.carbon.ui.CarbonUIUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 
 */
public class SSOAssertionConsumerService extends HttpServlet {

    public static final Log log = LogFactory.getLog(SSOAssertionConsumerService.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String samlRespString = req.getParameter(
                SAML2SSOAuthenticatorConstants.HTTP_POST_PARAM_SAML2_RESP);
        String relayState = req.getParameter(
                SAML2SSOAuthenticatorConstants.HTTP_POST_PARAM_RELAY_STATE);
        
        if(log.isDebugEnabled()){
            log.debug("SAML Response Received. : " + samlRespString);
        }

        // Handle single logout requests
        if (req.getParameter(SAML2SSOAuthenticatorConstants.HTTP_POST_PARAM_SAML2_AUTH_REQ) != null) {
            handleSingleLogoutRequest(req, resp);
            return;
        }

        // If SAML Response is not present in the redirected req, send the user to an error page.
        if (samlRespString == null) {
            log.error("SAML Response is not present in the request.");
            handleMalformedResponses(req, resp,
                                     SAML2SSOAuthenticatorConstants.ErrorMessageConstants.RESPONSE_NOT_PRESENT);
            return;
        }

//        // If RELAY-STATE is invalid, redirect the users to an error page.
//        if (!SSOSessionManager.isValidResponse(relayState)) {
//            handleMalformedResponses(req, resp,
//                                     SAML2SSOAuthenticatorConstants.ErrorMessageConstants.RESPONSE_INVALID);
//            return;
//        }

        // Handle valid messages, either SAML Responses or LogoutRequests
        try {
            XMLObject samlObject = Util.unmarshall(samlRespString);
            if (samlObject instanceof LogoutResponse) {   // if it is a logout response, redirect it to login page.
                resp.sendRedirect(getAdminConsoleURL(req) + "admin/logout_action.jsp?logoutcomplete=true");
            } else if (samlObject instanceof Response) {    // if it is a SAML Response
                handleSAMLResponses(req, resp, samlObject);
            }
        } catch (SAML2SSOUIAuthenticatorException e) {
            log.error("Error when processing the SAML Assertion in the request.", e);
            handleMalformedResponses(req, resp, SAML2SSOAuthenticatorConstants.ErrorMessageConstants.RESPONSE_MALFORMED);
        }
    }

    /**
     * Handle SAML Responses and authenticate.
     *
     * @param req   HttpServletRequest
     * @param resp  HttpServletResponse
     * @param samlObject    SAML Response object
     * @throws ServletException Error when redirecting
     * @throws IOException  Error when redirecting
     */
    private void handleSAMLResponses(HttpServletRequest req, HttpServletResponse resp,
                                     XMLObject samlObject)
            throws ServletException, IOException, SAML2SSOUIAuthenticatorException {
        Response samlResponse;
        samlResponse = (Response) samlObject;
        List<Assertion> assertions = samlResponse.getAssertions();
        Assertion assertion = null;
        if (assertions != null && assertions.size() > 0) {
            assertion = assertions.get(0);
        }

        if(assertion == null){
            log.error("SAMLResponse does not contain Assertions.");
            throw new SAML2SSOUIAuthenticatorException("SAMLResponse does not contain Assertions.");
        }

        // Get the subject name from the Response Object and forward it to login_action.jsp
        String username = null;
        if(assertion.getSubject() != null && assertion.getSubject().getNameID() != null){
            username = assertion.getSubject().getNameID().getValue();
        }
        
        if(log.isDebugEnabled()){
            log.debug("A username is extracted from the response. : " + username);
        }

        if(username == null){
            log.error("SAMLResponse does not contain the name of the subject");
            throw new SAML2SSOUIAuthenticatorException("SAMLResponse does not contain the name of the subject");
        }

        // Set the SAML2 Response as a HTTP Attribute, so it is not required to build the assertion again.
        req.setAttribute(SAML2SSOAuthenticatorConstants.HTTP_ATTR_SAML2_RESP_TOKEN, samlResponse);

        String url = req.getRequestURI();
        url = url.replace("acs", "carbon/admin/login_action.jsp?username=" + URLEncoder.encode(username));
        RequestDispatcher reqDispatcher = req.getRequestDispatcher(url);
        reqDispatcher.forward(req, resp);
    }

    /**
     * Handle malformed Responses.
     *
     * @param req   HttpServletRequest
     * @param resp  HttpServletResponse
     * @param errorMsg  Error message to be displayed in HttpServletResponse.jsp
     * @throws IOException  Error when redirecting
     */
    private void handleMalformedResponses(HttpServletRequest req, HttpServletResponse resp,
                                          String errorMsg) throws IOException {
        HttpSession session = req.getSession();
        session.setAttribute(SAML2SSOAuthenticatorConstants.NOTIFICATIONS_ERROR_MSG, errorMsg);
        resp.sendRedirect(getAdminConsoleURL(req) + "sso-acs/notifications.jsp");
        return;
    }


    /**
     * This method is used to handle the single logout requests sent by the Identity Provider
     *
     * @param req  Corresponding HttpServletRequest
     * @param resp Corresponding HttpServletResponse
     */
    private void handleSingleLogoutRequest(HttpServletRequest req, HttpServletResponse resp) {
        String logoutReqStr = decodeHTMLCharacters(req.getParameter(
                SAML2SSOAuthenticatorConstants.HTTP_POST_PARAM_SAML2_AUTH_REQ));
        CarbonSSOSessionManager ssoSessionManager = null;
        XMLObject samlObject = null;

        try {
            ssoSessionManager = SAML2SSOAuthFEDataHolder.getInstance().getCarbonSSOSessionManager();
            samlObject = Util.unmarshall(logoutReqStr);
        } catch (SAML2SSOUIAuthenticatorException e) {
            log.error("Error handling the single logout request", e);
        }

        if (samlObject instanceof LogoutRequest) {
            LogoutRequest logoutRequest = (LogoutRequest) samlObject;
            //  There can be only one session index entry.
            List<SessionIndex> sessionIndexList = logoutRequest.getSessionIndexes();
            if (sessionIndexList.size() > 0) {
                // mark this session as invalid.
                ssoSessionManager.makeSessionInvalid(sessionIndexList.get(0).getSessionIndex());
            }
        }
    }

    /**
     * Get the admin console url from the request.
     *
     * @param request httpServletReq that hits the ACS Servlet
     * @return Admin Console URL       https://10.100.1.221:8443/acs/carbon/
     */
    private String getAdminConsoleURL(HttpServletRequest request) {
        String url = CarbonUIUtil.getAdminConsoleURL(request);
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        if (url.indexOf("/acs") != -1) {
            url = url.replace("/acs", "");
        }
        return url;
    }

    /**
     * A utility method to decode an HTML encoded string
     *
     * @param encodedStr encoded String
     * @return decoded String
     */
    private String decodeHTMLCharacters(String encodedStr) {
        return encodedStr.replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">")
                .replaceAll("&quot;", "\"").replaceAll("&apos;", "'");

    }


}
