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

import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.state.Replicator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.LogoutRequest;
import org.opensaml.saml2.core.LogoutResponse;
import org.opensaml.saml2.core.NameID;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.builders.SingleLogoutMessageBuilder;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOReqValidationResponseDTO;
import org.wso2.carbon.identity.sso.saml.dto.SingleLogoutRequestDTO;
import org.wso2.carbon.identity.sso.saml.session.SSOSessionCommand;
import org.wso2.carbon.identity.sso.saml.session.SSOSessionPersistenceManager;
import org.wso2.carbon.identity.sso.saml.session.SessionInfoData;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;

public class LogoutRequestProcessor {

    private static Log log = LogFactory.getLog(LogoutRequestProcessor.class);


    public SAMLSSOReqValidationResponseDTO process(LogoutRequest logoutRequest, String sessionId) throws IdentityException {

        try {
            SAMLSSOReqValidationResponseDTO reqValidationResponseDTO = new SAMLSSOReqValidationResponseDTO();
            reqValidationResponseDTO.setLogOutReq(true);

            String subject = null;

            //Only if the logout request is received.
            if (logoutRequest != null) {
                if (logoutRequest.getIssuer() == null) {
                    String message = "Issuer should be mentioned in the Logout Request";
                    log.error(message);
                    return buildErrorResponse(logoutRequest.getID(), SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR, message);
                }

                // TODO : Check for BaseID and EncryptedID as well.
                if (logoutRequest.getNameID() != null) {
                    NameID nameID = logoutRequest.getNameID();
                    subject = nameID.getValue();
                } else {
                    String message = "Subject Name should be specified in the Logout Request";
                    log.error(message);
                    return buildErrorResponse(logoutRequest.getID(), SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR, message);
                }

                if (logoutRequest.getSessionIndexes() == null) {
                    String message = "At least one Session Index should be present in the Logout Request";
                    log.error(message);
                    return buildErrorResponse(logoutRequest.getID(), SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR, message);
                }
            }

            //Get the sessions from the SessionPersistenceManager and prepare the logout responses
            SSOSessionPersistenceManager ssoSessionPersistenceManager = SSOSessionPersistenceManager.getPersistenceManager();
            SessionInfoData sessionInfoData = ssoSessionPersistenceManager.getSessionInfo(sessionId);

            if (sessionInfoData == null) {
                String message = "No Established Sessions corresponding to Session Indexes provided.";
                log.error(message);
                return buildErrorResponse(logoutRequest.getID(), SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR,
                        message);
            }
            subject = sessionInfoData.getSubject();
            String issuer = logoutRequest.getIssuer().getValue();
            Map<String, SAMLSSOServiceProviderDO> sessionsList = sessionInfoData.getServiceProviderList();
            SAMLSSOServiceProviderDO logoutReqIssuer = sessionsList.get(issuer);

            // validate the signature, if it is set.
            if(logoutReqIssuer.getCertAlias() != null){
                boolean isSignatureValid = SAMLSSOUtil.validateAssertionSignature(logoutRequest, logoutReqIssuer.getCertAlias(),
                                                       MultitenantUtils.getTenantDomain(subject));
                if (!isSignatureValid) {
                    String message = "The signature contained in the Assertion is not valid.";
                    log.error(message);
                    return buildErrorResponse(logoutRequest.getID(), SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR,
                            message);
                }
            }

            SingleLogoutMessageBuilder logoutMsgBuilder = new SingleLogoutMessageBuilder();
            Map<String, String> rpSessionsList = sessionInfoData.getRPSessionsList();
            SingleLogoutRequestDTO[] singleLogoutReqDTOs = new SingleLogoutRequestDTO[sessionsList.size()-1];
            LogoutRequest logoutReq = logoutMsgBuilder.buildLogoutRequest(subject, sessionId,
                    SAMLSSOConstants.SingleLogoutCodes.LOGOUT_USER);
            String logoutReqString = SAMLSSOUtil.encode(SAMLSSOUtil.marshall(logoutReq));
            int index = 0;
            for (String key : sessionsList.keySet()) {
                if (!key.equals(issuer)) {
                    SingleLogoutRequestDTO logoutReqDTO = new SingleLogoutRequestDTO();
                    logoutReqDTO.setAssertionConsumerURL(sessionsList.get(key).getLogoutURL());
                    if (sessionsList.get(key).getLogoutURL() == null ||
                        sessionsList.get(key).getLogoutURL().length() == 0) {
                        logoutReqDTO.setAssertionConsumerURL(sessionsList.get(key).getAssertionConsumerUrl());
                    }
                    logoutReqDTO.setLogoutResponse(logoutReqString);
                    logoutReqDTO.setRpSessionId(rpSessionsList.get(key));
                    singleLogoutReqDTOs[index] = logoutReqDTO;
                    index ++;
                }
                else {
                    reqValidationResponseDTO.setIssuer(sessionsList.get(key).getIssuer());
                    reqValidationResponseDTO.setAssertionConsumerURL(sessionsList.get(key).getAssertionConsumerUrl());
                    if(sessionsList.get(key).getLogoutURL() != null && sessionsList.get(key).getLogoutURL().length() > 0){
                        reqValidationResponseDTO.setAssertionConsumerURL(sessionsList.get(key).getLogoutURL());
                    }
                }
            }
            reqValidationResponseDTO.setLogoutRespDTO(singleLogoutReqDTOs);

            if (logoutRequest != null) {
                LogoutResponse logoutResponse = logoutMsgBuilder.buildLogoutResponse(logoutRequest.getID(),
                        SAMLSSOConstants.StatusCodes.SUCCESS_CODE, null);
                reqValidationResponseDTO.setLogoutResponse(SAMLSSOUtil.encode(SAMLSSOUtil.marshall(logoutResponse)));
                reqValidationResponseDTO.setValid(true);
            }

            ssoSessionPersistenceManager.removeSession(sessionId, issuer);
            replicateSessionInfo(sessionId, issuer);
            return reqValidationResponseDTO;
        } catch (Exception e) {
            log.error("Error Processing the Logout Request", e);
            throw new IdentityException("Error Processing the Logout Request", e);
        }
    }

    private SAMLSSOReqValidationResponseDTO buildErrorResponse(String id, String status, String statMsg) throws Exception {
        SAMLSSOReqValidationResponseDTO reqValidationResponseDTO = new SAMLSSOReqValidationResponseDTO();
        LogoutResponse logoutResp = new SingleLogoutMessageBuilder().buildLogoutResponse(id, status, statMsg);
        reqValidationResponseDTO.setLogOutReq(true);
        reqValidationResponseDTO.setValid(false);
        reqValidationResponseDTO.setResponse(SAMLSSOUtil.encode(SAMLSSOUtil.marshall(logoutResp)));
        return reqValidationResponseDTO;
    }

    private void replicateSessionInfo(String sessionID, String issuer) {
        SSOSessionCommand sessionCommand = new SSOSessionCommand();
        sessionCommand.setSsoTokenID(sessionID);
        sessionCommand.setIssuer(issuer);
        sessionCommand.setSignOut(true);

        try {
            if (log.isDebugEnabled()) {
                log.info("Starting to replicate sign-out session Info for TokenID : " + sessionID);
            }
            Replicator.replicateState(sessionCommand,
                                      SAMLSSOUtil.getConfigCtxService().getServerConfigContext().getAxisConfiguration());
            if (log.isDebugEnabled()) {
                log.info("Completed replicating sign-out Session Info for TokenID : " + sessionID);
            }
        } catch (AxisFault axisFault) {
            log.error("Error when replicating the sign-out session info within the cluster", axisFault);
        }
    }

    }
