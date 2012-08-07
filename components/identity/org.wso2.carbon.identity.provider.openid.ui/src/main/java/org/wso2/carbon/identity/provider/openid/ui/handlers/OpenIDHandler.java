/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.identity.provider.openid.ui.handlers;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.DirectError;
import org.openid4java.message.ParameterList;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.identity.base.IdentityConstants;
import org.wso2.carbon.identity.base.IdentityConstants.OpenId;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDAuthRequestDTO;
import org.wso2.carbon.identity.provider.openid.stub.dto.OpenIDAuthResponseDTO;
import org.wso2.carbon.identity.provider.openid.ui.client.OpenIDAdminClient;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.TenantUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles functionality related OpenID association,
 * authentication,checkid_immediate checkid_setup. check_authentication [POST] :
 * Ask an Identity Provider if a message is valid. For dumb, state-less
 * Consumers or when verifying an invalidate_handle response. checkid_setup
 * [GET] : Ask an Identity Provider if a End User owns the Claimed Identifier,
 * but be willing to wait for the reply. The Consumer will pass the User-Agent
 * to the Identity Provider for a short period of time which will return either
 * a "yes" or "cancel" answer. checkid_immediate [GET] : Ask an Identity
 * Provider if a End User owns the Claimed Identifier, getting back an immediate
 * "yes" or "can't say" answer. associate [POST] : Establish a shared secret
 * between Consumer and Identity Provider
 */
public class OpenIDHandler {

	// Instantiate a ServerManager object.
	// private ServerManager manager = new ServerManager();

	private String frontEndUrl;
	private String opAddress;

	// Guaranteed to be thread safe
	private static OpenIDHandler provider;
	private static Log log = LogFactory.getLog(OpenIDHandler.class);

	/**
	 * Configure the OpenID Provider's end-point URL
	 */
	private OpenIDHandler(String serverUrl) {
		// This is the OpenID provider server URL
		opAddress = serverUrl;
	}

	// Return an instance of the OpenIDProvider
	public static OpenIDHandler getInstance(String serverUrl) {
		if (provider == null) {
			provider = new OpenIDHandler(serverUrl);
		}
		return provider;
	}

	/**
	 * This is the page the user will be redirected for authentication.
	 * 
	 * @param frontEndUrl Authentication page
	 */
	public void setFrontEndUrl(String frontEndUrl) {
		// Should be always on HTTPS
		this.frontEndUrl = frontEndUrl;

		if (log.isDebugEnabled()) {
			log.debug("Authentication page set to :" + this.frontEndUrl);
		}
	}

	/**
	 * @return OpenID Provider server URL.
	 */
	public String getOpAddress() {
		return opAddress;
	}

	/**
	 * @param request
	 * @param response
	 * @return
	 * @throws IdentityException
	 */
	public String processRequest(HttpServletRequest request, HttpServletResponse response)
	                                                                                      throws IdentityException {
		// for single logout
        if (request.getParameter("logoutUrl") != null) {
			
			log.info("OpenID Single Logout for " +
			         request.getSession().getAttribute("authenticatedOpenID"));
			// removing the authenticated user from the session
			request.getSession().setAttribute("authenticatedOpenID", null);
			// removing the remember-me cookie
			Cookie[] cookies = request.getCookies();
			String token = null;
			if (cookies != null) {
				Cookie curCookie = null;
				for (int x = 0; x < cookies.length; x++) {
					curCookie = cookies[x];
					if (curCookie.getName().equalsIgnoreCase("openidtoken")) {
						curCookie.setMaxAge(0);// removing the cookie
						response.addCookie(curCookie);
						break;
					}
				}
			}
			return (String) request.getParameter("logoutUrl");
		}

		ParameterList paramList = null;
		String responseText = null;
		HttpSession session = null;
		OpenIDAdminClient client = null;
		String cookie = null;
		ConfigurationContext configContext = null;
		String serverURL = null;

		if (request == null || response == null) {
			throw new IdentityException("Required attributes missing");
		}

		try {
			session = request.getSession();
			serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
			configContext =
			                (ConfigurationContext) session.getServletContext()
			                                              .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
			cookie = (String) session.getAttribute(OpenIDAdminClient.OPENID_ADMIN_COOKIE);
			client = new OpenIDAdminClient(configContext, serverURL, cookie);

			if (OpenId.COMPLETE.equals(session.getAttribute(OpenId.ACTION)) ||
			    OpenId.CANCEL.equals(session.getAttribute(OpenId.ACTION))) {
				// Ready for authentication.
				paramList = (ParameterList) session.getAttribute(OpenId.PARAM_LIST);
			} else {
				// Extract the parameters from the request. Authentication not
				// completed.
				paramList = new ParameterList(request.getParameterMap());
			}

			if (paramList == null) {
				responseText = getErrorResponseText("Invalid OpenID authentication request");
				if (log.isDebugEnabled()) {
					log.debug("Invalid OpenID authentication request :" + responseText);
				}
				directResponse(response, responseText);
				return null;
			}

			String mode =
			              paramList.hasParameter(OpenId.ATTR_MODE)
			                                                      ? paramList.getParameterValue(OpenId.ATTR_MODE)
			                                                      : null;

			if (log.isDebugEnabled()) {
				log.debug("OpenID authentication mode :" + mode);
			}

			if (OpenId.ASSOCIATE.equals(mode)) {
				responseText =
				               client.getOpenIDAssociationResponse(OpenIDUtil.getOpenIDAuthRequest(request));
				if (log.isDebugEnabled()) {
					log.debug("Association created successfully");
				}
			} else if (OpenId.CHECKID_SETUP.equals(mode) || OpenId.CHECKID_IMMEDIATE.equals(mode)) {
				return checkSetupOrImmediate(request, paramList);
			} else if (OpenId.CHECK_AUTHENTICATION.equals(mode)) {
				responseText = client.verify(OpenIDUtil.getOpenIDAuthRequest(request));
				if (log.isDebugEnabled()) {
					log.debug("Authentication verified successfully");
				}
			} else {
				// Error response - oops..!!! we did not get a valid OpenID
				// mode.
				responseText =
				               getErrorResponseText("No valid OpenID found in the authentication request");
				if (log.isDebugEnabled()) {
					log.debug("No valid OpenID found in the authentication request");
				}
			}
		} catch (Exception e) {
			responseText = getErrorResponseText(e.getMessage());
		}

		try {
			// Return the result to the user.
			directResponse(response, responseText);
		} catch (IOException e) {
			log.error(e.getMessage());
			throw new IdentityException("OpenID redirect reponse failed");
		}

		return null;
	}

	/**
	 * checkid_immediate : Ask an Identity Provider if an End User owns the
	 * Claimed Identifier, getting back an immediate "yes" or "can't say"
	 * answer. checkid_setup Description: Ask an Identity Provider if a End User
	 * owns the Claimed Identifier, but be willing to wait for the reply. The
	 * Consumer will pass the User-Agent to the Identity Provider for a short
	 * period of time which will return either a "yes" or "cancel" answer.
	 */
	private String checkSetupOrImmediate(HttpServletRequest request, ParameterList params)
	                                                                                      throws Exception {

		boolean authenticated = false;
		String userSelectedClaimedId = null;
		String openId = null;
		HttpSession session = null;
		String profileName = null;

		session = request.getSession();

		openId =
		         params.hasParameter(IdentityConstants.OpenId.ATTR_IDENTITY)
		                                                                    ? params.getParameterValue(IdentityConstants.OpenId.ATTR_IDENTITY)
		                                                                    : null;

		if (openId != null && openId.endsWith("/openid/")) {
			String openIdInSession = (String) session.getAttribute("openId");
			if (openIdInSession != null && !"".equals(openIdInSession.trim())) {
				openId = openIdInSession;
			}
		}

		if (log.isDebugEnabled()) {
			log.debug("Authentication check for OpenID " + openId);
		}

		if (openId == null) {
			throw new IdentityException("Required attributes missing");
		}

		if (log.isDebugEnabled()) {
			log.debug("Authentication check for user " + openId);
		}

		boolean completed =
		                    IdentityConstants.OpenId.COMPLETE.equals(session.getAttribute(IdentityConstants.OpenId.ACTION));
		boolean approved = "true".equals(session.getAttribute(IdentityConstants.USER_APPROVED));

		String serverURL = CarbonUIUtil.getServerURL(session.getServletContext(), session);
		ConfigurationContext configContext =
		                                     (ConfigurationContext) session.getServletContext()
		                                                                   .getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);

		OpenIDAdminClient openIDAdmin =
		                                new OpenIDAdminClient(
		                                                      configContext,
		                                                      serverURL,
		                                                      (String) session.getAttribute(OpenIDAdminClient.OPENID_ADMIN_COOKIE));

		if (completed && approved) {
			session.removeAttribute(IdentityConstants.USER_APPROVED);
			session.removeAttribute(IdentityConstants.OpenId.ACTION);
			profileName = (String) session.getAttribute("profile");
			session.removeAttribute("profile");
			// Done - authenticated.
			authenticated = true;
			if (log.isDebugEnabled()) {
				log.debug("Authenticated and user confirmed :" + openId);
			}
			// tracking down RP informations
			profileName = (String) session.getAttribute("selectedProfile");
			if (profileName == null) {
				profileName = "default";
			}
			boolean alwaysApprovedRp;
			if (Boolean.parseBoolean((String) session.getAttribute("userApprovedAlways"))) {
				alwaysApprovedRp = true;
			} else {
				alwaysApprovedRp =
				                   Boolean.parseBoolean((String) request.getParameter("hasApprovedAlways"));
			}
            
			openIDAdmin.updateOpenIDUserRPInfo(params.getParameterValue(IdentityConstants.OpenId.ATTR_RETURN_TO),
			                                   alwaysApprovedRp, profileName, openId);
		}

		if (IdentityConstants.OpenId.CANCEL.equals(session.getAttribute(IdentityConstants.OpenId.ACTION))) {
			if (log.isDebugEnabled()) {
				log.debug("User cancelled :" + openId);
			}
			authenticated = false;
		} else if (!authenticated) {
			// Not authenticated, redirect to the authentication page.
			session.setAttribute(IdentityConstants.OpenId.PARAM_LIST, params);
			if (log.isDebugEnabled()) {
				log.debug("User not authenticated. Redirecting to the authentication page :" +
				          openId);
			}

//			PapeInfoRequestDTO papeInfoRequestDTO = new PapeInfoRequestDTO();
//			papeInfoRequestDTO.setParamList(OpenIDUtil.getOpenIDAuthRequest(params));
//			papeInfoRequestDTO.setOpenID(openId);
//
//			PapeInfoResponseDTO papeInfoResponseDTO = openIDAdmin.getPapeInfo(papeInfoRequestDTO);
//			OpenIDParameterDTO[] parameterSet = papeInfoResponseDTO.getPolicies();
			String tenant = TenantUtils.getDomainNameFromOpenId(openId);
			String returnUrl = null;

			/*if (parameterSet[0].getValue().equals("true") ||
			    parameterSet[1].getValue().equals("true")) {
				returnUrl =
				            CarbonUIUtil.getAdminConsoleURL(request) +
				                    "openid-provider/PAPE_info.jsp";
				session.setAttribute("papePhishingResistance", parameterSet[0].getValue());
				session.setAttribute("multiFactorAuth", parameterSet[1].getValue());
				session.setAttribute("infoCardBasedMultiFacotrAuth", parameterSet[2].getValue());
				session.setAttribute("xmppBasedMultiFacotrAuth", parameterSet[3].getValue());
				if (tenant != null && tenant.trim().length() > 0) {
					return returnUrl.replace("/carbon/", "/t/" + tenant + "/carbon/");
				}
				return returnUrl;
			}*/

			returnUrl = frontEndUrl;

			Cookie[] cookies = request.getCookies();
			String token = null;

			if (cookies != null) {
				Cookie curCookie = null;
				for (Cookie cookie : cookies) {
					curCookie = cookie;
					params.getParameterValue(IdentityConstants.OpenId.ATTR_RETURN_TO);
					if (curCookie.getName().equalsIgnoreCase("openidtoken")) {
						token = curCookie.getValue();
						break;
					}
				}
			}

			if ((token != null && !"null".equals(token)) ||
			    (session.getAttribute("authenticatedOpenID") != null && (session.getAttribute("authenticatedOpenID").equals(openId)))) {
				session.setAttribute("openId", openId);
				returnUrl =
				            returnUrl.replace("openid-provider/openid_auth.jsp",
				                              "openid-provider/openid_auth_submit.jsp");
			}

			if (tenant != null && tenant.trim().length() > 0) {
				return returnUrl.replace("/carbon/", "/t/" + tenant + "/carbon/");
			}

			return returnUrl;

		}

		session.removeAttribute(IdentityConstants.OpenId.PARAM_LIST);
		String opLocalId = null;

		OpenIDAuthRequestDTO openIDAuthRequest = null;
		OpenIDAuthResponseDTO openIDAuthResponse = null;

		openIDAuthRequest = new OpenIDAuthRequestDTO();

		if (IdentityConstants.TRUE.equals(session.getAttribute(IdentityConstants.PHISHING_RESISTANCE))) {
			openIDAuthRequest.setPhishiingResistanceAuthRequest(true);
			// Clear the session.
			session.removeAttribute(IdentityConstants.PHISHING_RESISTANCE);
		}

		if (IdentityConstants.TRUE.equals(session.getAttribute(IdentityConstants.MULTI_FACTOR_AUTH))) {
			openIDAuthRequest.setMultiFactorAuthRequested(true);
			// Clear the cache.
			session.removeAttribute(IdentityConstants.MULTI_FACTOR_AUTH);
		}

		openIDAuthRequest.setParams(OpenIDUtil.getOpenIDAuthRequest(params));
		openIDAuthRequest.setOpLocalId(opLocalId);
		openIDAuthRequest.setUserSelectedClaimedId(userSelectedClaimedId);
		openIDAuthRequest.setAuthenticated(authenticated);
		openIDAuthRequest.setOpenID(openId);
		openIDAuthRequest.setProfileName(profileName);
		openIDAuthResponse = openIDAdmin.getOpenIDAuthResponse(openIDAuthRequest);

		if (openIDAuthResponse != null) {
			return openIDAuthResponse.getDestinationUrl();
		}

		return null;
	}

	/**
	 * Return the error response message based on the given message
	 * 
	 * @param message
	 *            Error message
	 * @return Direct error
	 */
	private String getErrorResponseText(String message) {
		log.error(message);
		// Error response.
		return DirectError.createDirectError(message).keyValueFormEncoding();
	}

	/**
	 * Send a direct response to the RP.
	 * 
	 * @param httpResp
	 *            HttpServletResponse
	 * @param response
	 *            Response message
	 * @return
	 * @throws IOException
	 */
	private void directResponse(HttpServletResponse httpResp, String response) throws IOException {
		ServletOutputStream stream = null;
		try {
			stream = httpResp.getOutputStream();
			stream.write(response.getBytes());
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
	}

}
