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
package org.wso2.carbon.identity.provider.openid.ui.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.message.Parameter;
import org.openid4java.message.ParameterList;
import org.wso2.carbon.identity.provider.openid.stub.OpenIDProviderServiceStub;
import org.wso2.carbon.identity.provider.openid.stub.dto.*;
import org.wso2.carbon.identity.provider.openid.ui.handlers.OpenIDHandler;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenIDAdminClient {

	public final static String OPENID_ADMIN_COOKIE = "OPENID_ADMIN_COOKIE";
	private OpenIDProviderServiceStub stub;
	private String newCookieValue;
    private boolean isUserApprovalBypassEnabled;
	private static Log log = LogFactory.getLog(OpenIDHandler.class);

	public String getNewCookieValue() {
		return newCookieValue;
	}

	public void setNewCookieValue(String newCookieValue) {
		this.newCookieValue = newCookieValue;
	}

	public OpenIDAdminClient(ConfigurationContext context, String backendServerURL, String cookie)
                                                                              throws AxisFault {

		if (backendServerURL.indexOf("${carbon.context}") != -1) {
			String contextPath = null;
			contextPath = context.getContextRoot();
			if ("/".equals(contextPath)) {
				contextPath = "";
			}
			backendServerURL = backendServerURL.replace("${carbon.context}", contextPath);
		}

		String serviceURL = backendServerURL + "OpenIDProviderService";
		stub = new OpenIDProviderServiceStub(context, serviceURL);
		stub._getServiceClient().getOptions().setTimeOutInMilliSeconds(120 * 1000);
		if (cookie != null) {
			stub._getServiceClient().getOptions()
			    .setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        }
        try {
            isUserApprovalBypassEnabled = stub.isOpenIDUserApprovalBypassEnabled();
        } catch (RemoteException ignore) {
            isUserApprovalBypassEnabled  = false;
        }
	}

	public OpenIDProviderInfoDTO getOpenIDProviderInfo(String userName, String openID)
	                                                                                  throws Exception {
		return stub.getOpenIDProviderInfo(userName, openID);
	}

	public boolean authenticateWithOpenID(String openId, String password, HttpSession session,
	                                      HttpServletRequest request, HttpServletResponse response,
	                                      boolean useRememberMe) {
		String cookie = null;
		boolean isAuthenticated = false;
		OpenIDRememberMeDTO dto = null;
		try {
			// Check whether the remember me option is set
			Cookie[] cookies = request.getCookies();
			String token = null;

			if (cookies != null) {
				Cookie curCookie = null;
				for (int x = 0; x < cookies.length; x++) {
					curCookie = cookies[x];
					if (curCookie.getName().equalsIgnoreCase("openidtoken")) {
						token = curCookie.getValue();
						break;
					}
				}
			}

			if ((token != null && !"null".equals(token)) || useRememberMe) {
				dto =
				      stub.authenticateWithOpenIDRememberMe(openId.trim(), password,
				                                            request.getRemoteAddr(), token);
				if (dto != null && dto.getAuthenticated()) {
					newCookieValue = dto.getNewCookieValue();
				}
				isAuthenticated = dto.getAuthenticated();
			} else {
				isAuthenticated = stub.authenticateWithOpenID(openId.trim(), password);
			}

			cookie =
			         (String) stub._getServiceClient()
			                      .getServiceContext()
			                      .getProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING);
			session.setAttribute(OPENID_ADMIN_COOKIE, cookie);
		} catch (Exception e) {
			return false;
		}
		return isAuthenticated;
	}

	public Map<String, OpenIDClaimDTO> getClaimValues(String openId, String profileId,
	                                                  ParameterList requredClaims) throws Exception {
		Map<String, OpenIDClaimDTO> map = null;
		map = new HashMap<String, OpenIDClaimDTO>();
		OpenIDClaimDTO[] claims = null;
		OpenIDParameterDTO[] params = null;
		List list = null;

		try {
			list = requredClaims.getParameters();
			params = new OpenIDParameterDTO[list.size()];
			int i = 0;
			for (Object object : list) {
				Parameter param = (Parameter) object;
				OpenIDParameterDTO openIDParameterDTO = new OpenIDParameterDTO();
				openIDParameterDTO.setName(param.getKey());
				openIDParameterDTO.setValue(param.getValue());
				params[i++] = openIDParameterDTO;
			}
			claims = stub.getClaimValues(openId.trim(), profileId, params);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (claims != null) {
			for (int i = 0; i < claims.length; i++) {
				if (claims[i] != null) {
					map.put(claims[i].getClaimUri(), claims[i]);
				}
			}
		}

		return map;
	}

	public OpenIDAuthResponseDTO getOpenIDAuthResponse(OpenIDAuthRequestDTO authRequest)
	                                                                                    throws Exception {
		return stub.getOpenIDAuthResponse(authRequest);
	}

	public String getOpenIDAssociationResponse(OpenIDParameterDTO[] params) throws Exception {
		return stub.getOpenIDAssociationResponse(params);
	}

	public String verify(OpenIDParameterDTO[] params) throws Exception {
		return stub.verify(params);
	}

	public InfoCardSignInDTO signInWithInfoCard(InfoCardDTO inforCard) throws Exception {
		return stub.signInWithInfoCard(inforCard);
	}

	public String getCookie() {
		return (String) stub._getServiceClient()
		                    .getServiceContext()
		                    .getProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING);
	}

	/**
	 * Get PAPE Info for a particular user
	 * 
	 * @param reqDTO
	 * @return
	 * @throws Exception
	 */
	public PapeInfoResponseDTO getPapeInfo(PapeInfoRequestDTO reqDTO) throws Exception {
		return stub.retrievePapeInfo(reqDTO);
	}

	/**
	 * Do multi-factor authentication for an user
	 * 
	 * @param userId
	 * @return
	 * @throws Exception
	 */
	public boolean doxmppBasedMultiFactorAuthForInfoCards(String userId) throws Exception {
		return stub.doXMPPBasedMultiFactorAuthForInfocard(userId);
	}

	/**
	 * 
	 * @param openid
	 * @return
	 * @throws Exception
	 */
	public OpenIDUserProfileDTO[] getUserProfiles(String openid, ParameterList requredClaims)
	                                                                                         throws Exception {
		OpenIDParameterDTO[] params = null;
		List list = null;
		list = requredClaims.getParameters();
		params = new OpenIDParameterDTO[list.size()];
		int i = 0;
		for (Object object : list) {
			Parameter param = (Parameter) object;
			OpenIDParameterDTO openIDParameterDTO = new OpenIDParameterDTO();
			openIDParameterDTO.setName(param.getKey());
			openIDParameterDTO.setValue(param.getValue());
			params[i++] = openIDParameterDTO;
		}
		return stub.getUserProfiles(openid, params);
	}

	/**
	 * Allow this relying party to retrieve user attributes without user
	 * permission next time
	 * 
	 * @param rpUrl
	 * @param isTrustedAlways
	 * @param defaultProfileName
	 * @param openID
	 * @throws Exception
	 */
	public void updateOpenIDUserRPInfo(String rpUrl, boolean isTrustedAlways,
	                                   String defaultProfileName, String openID) throws Exception {

        if(isUserApprovalBypassEnabled){
            return;
        }
        
		if (log.isDebugEnabled()) {
			log.debug("Updating RP " + rpUrl + "info for " + openID);
		}

		OpenIDUserRPDTO rpdto = new OpenIDUserRPDTO();
		rpdto.setRpUrl(rpUrl);
		rpdto.setTrustedAlways(isTrustedAlways);
		rpdto.setDefaultProfileName(defaultProfileName);
		rpdto.setOpenID(openID);

		stub.updateOpenIDUserRPInfo(rpdto);
	}

	/**
	 * Returns RP DTOs for the given OpenID
	 * 
	 * @param openID
	 * @return openIDUserRPDTOs
	 * @throws Exception
	 */
	public OpenIDUserRPDTO[] getOpenIDUserRPs(String openID) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Getting OpenID User RP DTOs for " + openID);
		}

		return stub.getOpenIDUserRPs(openID);
	}

	/**
	 * Return the RPDTO object for a given OpenID and RP URL
	 * 
	 * @param openID
	 * @param rpUrl
	 * @return openIDUserRPDTO
	 * @throws Exception
	 */
	public OpenIDUserRPDTO getOpenIDUserRPDTO(String openID, String rpUrl) throws Exception {

		if (log.isDebugEnabled()) {
			log.debug("Getting OpenID User RP DTO for " + openID + "for RP " + rpUrl);
		}

		return stub.getOpenIDUserRPInfo(openID, rpUrl);
	}

	/**
	 * Return a string array of RP info. The first values of the array contains
	 * isAlwaysSpecified boolean value
	 * 
	 * @param openID
	 * @param rpUrl
	 * @return rpInfo[]to.getDefaultProfileName())
	 * @throws Exception
	 */
	public String[] getOpenIDUserRPInfo(String openID, String rpUrl) throws Exception {

        OpenIDUserRPDTO rpdto = null;
		String[] rpInfo = new String[7];
        
        if(!isUserApprovalBypassEnabled){
		    rpdto = stub.getOpenIDUserRPInfo(openID, rpUrl);
        }

		if (rpdto != null) {
			// do not change the order
			rpInfo[0] = new Boolean(rpdto.getTrustedAlways()).toString();
			rpInfo[1] = rpdto.getDefaultProfileName();
			rpInfo[2] = rpdto.getOpenID();
			rpInfo[3] = rpdto.getRpUrl();
			rpInfo[4] = rpdto.getUserName();
			rpInfo[5] = Integer.toString(rpdto.getVisitCount());
			if (rpdto.getLastVisit() == null) {
				rpdto.setLastVisit(new Date());
			}
			rpInfo[6] = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(rpdto.getLastVisit());

		} else {
			rpInfo[0] = "false";
            rpInfo[1] = "default";
		}

		return rpInfo;
	}

	/**
	 * Check if the user approval bypass setting has made in the identity.xml
	 * 
	 * @return
	 * @throws Exception
	 */
	public boolean isOpenIDUserApprovalBypassEnabled() throws Exception {
        return isUserApprovalBypassEnabled;
	}
}
