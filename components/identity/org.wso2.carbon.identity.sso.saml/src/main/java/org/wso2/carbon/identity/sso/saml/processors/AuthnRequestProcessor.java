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
package org.wso2.carbon.identity.sso.saml.processors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.Response;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.core.util.AnonymousSessionUtil;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.core.persistence.IdentityPersistenceManager;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.SSOServiceProviderConfigManager;
import org.wso2.carbon.identity.sso.saml.builders.ErrorResponseBuilder;
import org.wso2.carbon.identity.sso.saml.builders.ResponseBuilder;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOAuthnReqDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOReqValidationResponseDTO;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSORespDTO;
import org.wso2.carbon.identity.sso.saml.session.SSOSessionPersistenceManager;
import org.wso2.carbon.identity.sso.saml.session.SessionInfoData;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.user.api.TenantManager;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class AuthnRequestProcessor {

    private static Log log = LogFactory.getLog(AuthnRequestProcessor.class);

    public SAMLSSORespDTO process(SAMLSSOAuthnReqDTO authnReqDTO, String sessionId,
                                  boolean isAuthencated, String authMode)
            throws Exception {
        try {

            //validate issuer info.
            if (!validateIssuer(authnReqDTO)) {
                String msg = "Issuer details are not valid. " +
                             "Issuer details should be registered in advance";
                log.warn(msg);
                return buildErrorResponse(authnReqDTO.getId(),
                                          SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR, msg);
            }

            // validate the signature
            if (authnReqDTO.getCertAlias() != null) {
                boolean isSignatureValid =
                        SAMLSSOUtil.validateAssertionSignature(
                                authnReqDTO.getAssertionString(), authnReqDTO.getCertAlias(),
                                MultitenantUtils.getTenantDomain(authnReqDTO.getUsername()));
                if (!isSignatureValid) {
                    String msg = "Signature Validation Failed for the SAML Assertion.";
                    log.warn(msg);
                    return buildErrorResponse(authnReqDTO.getId(),
                                              SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR, msg);
                }
            }

            //if subject is specified in Auth.Request only that user should be allowed to logged-in
            if (authnReqDTO.getSubject() != null && authnReqDTO.getUsername() != null) {
                if (!authnReqDTO.getUsername().equals(authnReqDTO.getSubject())) {
                    String msg = "Provided username does not match with the requested subject";
                    log.warn(msg);
                    return buildErrorResponse(authnReqDTO.getId(),
                                              SAMLSSOConstants.StatusCodes.AUTHN_FAILURE, msg);
                }
            }

            //persist the session
            SSOSessionPersistenceManager sessionPersistenceManager = SSOSessionPersistenceManager
                    .getPersistenceManager();

            //authenticate the user, if required
            if (!isAuthencated && authMode.equals(SAMLSSOConstants.AuthnModes.USERNAME_PASSWORD)) {
                RealmService realmService = SAMLSSOUtil.getRealmService();
                TenantManager tenantManager = realmService.getTenantManager();
                String tenantDomain = MultitenantUtils.getTenantDomain(authnReqDTO.getUsername());
                int tenantId = tenantManager.getTenantId(tenantDomain);
                if (tenantId > 0) {
                    boolean isTenantActive = tenantManager.isTenantActive(tenantId);
                    if (!isTenantActive) {
                        log.warn("Unsuccessful login attempt from the tenant : " + tenantDomain);
                        String errorMsg = "login.fail.inactive.tenant";
                        SAMLSSORespDTO errorResp =
                                buildErrorResponse(authnReqDTO.getId(),
                                                   SAMLSSOConstants.StatusCodes.AUTHN_FAILURE,
                                                                      errorMsg);
                        errorResp.setErrorMsg(errorMsg);
                        errorResp.setLoginPageURL(authnReqDTO.getLoginPageURL());
                        return errorResp;
                    }
                }
                if (!authenticate(authnReqDTO.getUsername(), authnReqDTO.getPassword())) {
                    log.warn("Authentication Failure, invalid username or password.");
                    String errorMsg = "login.fail.message";
                    SAMLSSORespDTO errorResp =
                            buildErrorResponse(authnReqDTO.getId(),
                                               SAMLSSOConstants.StatusCodes.AUTHN_FAILURE,
                                               errorMsg);
                    errorResp.setErrorMsg(errorMsg);
                    errorResp.setLoginPageURL(authnReqDTO.getLoginPageURL());
                    return errorResp;
                }
                SAMLSSOServiceProviderDO spDO = new SAMLSSOServiceProviderDO();
                spDO.setIssuer(authnReqDTO.getIssuer());
                spDO.setAssertionConsumerUrl(authnReqDTO.getAssertionConsumerURL());
                spDO.setCertAlias(authnReqDTO.getCertAlias());
                spDO.setLogoutURL(authnReqDTO.getLogoutURL());
                sessionPersistenceManager.persistSession(sessionId, authnReqDTO.getUsername(),
                                                         spDO, authnReqDTO.getRpSessionId());
            }

            if (isAuthencated && authMode.equals(SAMLSSOConstants.AuthnModes.USERNAME_PASSWORD)) {
                SessionInfoData sessionInfo = sessionPersistenceManager.getSessionInfo(sessionId);
                authnReqDTO.setUsername(sessionInfo.getSubject());
                sessionPersistenceManager.
                        persistSession(sessionId, authnReqDTO.getIssuer(),
                                       authnReqDTO.getAssertionConsumerURL(),
                                       authnReqDTO.getRpSessionId());
            }

            if (isAuthencated && authMode.equals(SAMLSSOConstants.AuthnModes.OPENID)) {
                SAMLSSOServiceProviderDO spDO = new SAMLSSOServiceProviderDO();
                spDO.setIssuer(authnReqDTO.getIssuer());
                spDO.setAssertionConsumerUrl(authnReqDTO.getAssertionConsumerURL());
                spDO.setCertAlias(authnReqDTO.getCertAlias());
                spDO.setLogoutURL(authnReqDTO.getLogoutURL());
                sessionPersistenceManager.persistSession(sessionId, authnReqDTO.getUsername(),
                                                         spDO, authnReqDTO.getRpSessionId());
            }

            //Build the response for the successful scenario
            ResponseBuilder respBuilder = new ResponseBuilder();
            Response response = respBuilder.buildResponse(authnReqDTO, sessionId);
            SAMLSSORespDTO samlssoRespDTO = new SAMLSSORespDTO();
            samlssoRespDTO.setRespString(SAMLSSOUtil.encode(SAMLSSOUtil.marshall(response)));
            samlssoRespDTO.setSessionEstablished(true);
            samlssoRespDTO.setAssertionConsumerURL(authnReqDTO.getAssertionConsumerURL());
            samlssoRespDTO.setLoginPageURL(authnReqDTO.getLoginPageURL());
            return samlssoRespDTO;

        } catch (Exception e) {
            log.error("Error processing the authentication request", e);
            SAMLSSORespDTO errorResp =
                    buildErrorResponse(authnReqDTO.getId(),
                                       SAMLSSOConstants.StatusCodes.AUTHN_FAILURE,
                                       "Authentication Failure, invalid username or password.");
            errorResp.setLoginPageURL(authnReqDTO.getLoginPageURL());
            return errorResp;
        }
    }

    public SAMLSSOReqValidationResponseDTO process(SAMLSSOReqValidationResponseDTO valiationDTO,
                                                   String sessionId, String rpSessionId,
                                                   String authMode) throws Exception {
        SAMLSSOAuthnReqDTO authReqDTO = new SAMLSSOAuthnReqDTO();
        authReqDTO.setIssuer(valiationDTO.getIssuer());
        authReqDTO.setAssertionConsumerURL(valiationDTO.getAssertionConsumerURL());
        authReqDTO.setSubject(valiationDTO.getSubject());
        authReqDTO.setId(valiationDTO.getId());
        authReqDTO.setRpSessionId(rpSessionId);
        authReqDTO.setAssertionString(valiationDTO.getAssertionString());

        if (authMode.equals(SAMLSSOConstants.AuthnModes.USERNAME_PASSWORD)) {
            //Set the username in the SAMLSSOAuthnReqDTO
            SSOSessionPersistenceManager sessionPersistenceManager = SSOSessionPersistenceManager
                    .getPersistenceManager();
            SessionInfoData sessionInfo = sessionPersistenceManager.getSessionInfo(sessionId);
            authReqDTO.setUsername(sessionInfo.getSubject());
        } else {
            authReqDTO.setUsername(valiationDTO.getSubject());
        }

        SAMLSSOReqValidationResponseDTO responseDTO = new SAMLSSOReqValidationResponseDTO();
        SAMLSSORespDTO respDTO = process(authReqDTO, sessionId, true, authMode);
        responseDTO.setValid(true);
        responseDTO.setResponse(respDTO.getRespString());
        responseDTO.setAssertionConsumerURL(respDTO.getAssertionConsumerURL());
        responseDTO.setLoginPageURL(respDTO.getLoginPageURL());
        return responseDTO;
    }

    private SAMLSSORespDTO buildErrorResponse(String id, String status,
                                              String statMsg) throws Exception {
        SAMLSSORespDTO samlSSORespDTO = new SAMLSSORespDTO();
        ErrorResponseBuilder errRespBuilder = new ErrorResponseBuilder();
        Response resp = errRespBuilder.buildResponse(id, status, statMsg);
        samlSSORespDTO.setRespString(SAMLSSOUtil.encode(SAMLSSOUtil.marshall(resp)));
        samlSSORespDTO.setSessionEstablished(false);
        return samlSSORespDTO;
    }

    private boolean authenticate(String username, String password) throws IdentityException {
        boolean isAuthenticated = false;
        try {
            UserRealm realm = AnonymousSessionUtil.getRealmByUserName(
                    SAMLSSOUtil.getRegistryService(), SAMLSSOUtil.getRealmService(), username);

            if (realm == null) {
                log.warn("Realm creation failed. Tenant may be inactive or invalid.");
                return false;
            }

            UserStoreManager userStoreManager = realm.getUserStoreManager();

//            //update the permission tree before authentication
//            String tenantDomain = MultitenantUtils.getTenantDomain(username);
//            int tenantId = SAMLSSOUtil.getRealmService().getTenantManager().getTenantId(tenantDomain);
//            PermissionUpdateUtil.updatePermissionTree(tenantId);

            // Check the authentication
            isAuthenticated = userStoreManager.authenticate(
                    MultitenantUtils.getTenantAwareUsername(username), password);
            if (!isAuthenticated) {
                if (log.isDebugEnabled()) {
                    log.debug("user authentication failed due to invalid credentials.");
                }
                return false;
            }

            // Check the authorization
            boolean isAuthorized = realm.getAuthorizationManager().
                    isUserAuthorized(MultitenantUtils.getTenantAwareUsername(username),
                                     "/permission/admin/login",
                                     CarbonConstants.UI_PERMISSION_ACTION);
            if (!isAuthorized) {
                if (log.isDebugEnabled()) {
                    log.debug("Authorization Failure when performing log-in action");
                }
                return false;
            }
            if (log.isDebugEnabled()) {
                log.debug("User is successfully authenticated.");
            }
            return true;
        } catch (Exception e) {
            String msg = "Error obtaining user realm for authenticating the user";
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }

    private boolean validateIssuer(SAMLSSOAuthnReqDTO authReqDTO) throws IdentityException {

        try {
            SSOServiceProviderConfigManager spConfigManager =
                    SSOServiceProviderConfigManager.getInstance();
            SAMLSSOServiceProviderDO spDO =
                    spConfigManager.getServiceProvider(authReqDTO.getIssuer());
            if (spDO == null) {
                IdentityPersistenceManager persistenceManager =
                        IdentityPersistenceManager.getPersistanceManager();
                spDO = persistenceManager.getServiceProvider(
                        AnonymousSessionUtil.getSystemRegistryByUserName(
                                SAMLSSOUtil.getRegistryService(), SAMLSSOUtil.getRealmService(),
                                authReqDTO.getUsername()), authReqDTO.getIssuer());
            }

            //Invalid issuer
            if (spDO == null) {
                return false;
            }

            //give more priority to the Assrtn Consumer URL if specified in the Auth. Req
            if (authReqDTO.getAssertionConsumerURL() == null) {
                authReqDTO.setAssertionConsumerURL(spDO.getAssertionConsumerUrl());
            }

            authReqDTO.setLoginPageURL(spDO.getLoginPageURL());
            authReqDTO.setCertAlias(spDO.getCertAlias());
            authReqDTO.setUseFullyQualifiedUsernameAsSubject(spDO.isUseFullyQualifiedUsername());
            authReqDTO.setDoSingleLogout(spDO.isDoSingleLogout());
            authReqDTO.setLogoutURL(spDO.getLogoutURL());
            authReqDTO.setDoSignAssertions(spDO.isDoSignAssertions());
            return true;

        } catch (Exception e) {
            String msg = "Error validating the issuer";
            log.error(msg, e);
            throw new IdentityException(msg, e);
        }
    }
}
