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
package org.wso2.carbon.identity.sso.saml.validators;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opensaml.saml2.core.AuthnRequest;
import org.opensaml.saml2.core.Issuer;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.Response;
import org.opensaml.common.SAMLVersion;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.core.model.SAMLSSOServiceProviderDO;
import org.wso2.carbon.identity.sso.saml.SSOServiceProviderConfigManager;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOReqValidationResponseDTO;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.identity.sso.saml.builders.ErrorResponseBuilder;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;


public class AuthnRequestValidator {

    private static Log log = LogFactory.getLog(AuthnRequestValidator.class);
    AuthnRequest authnReq;


    public AuthnRequestValidator(AuthnRequest authnReq) throws IdentityException {
        this.authnReq = authnReq;
    }

    /**
     * Validates the authentication request according to SAML SSO Web Browser Specification
     * @return SAMLSSOSignInResponseDTO
     * @throws IdentityException
     */
    public SAMLSSOReqValidationResponseDTO validate() throws IdentityException {

        try {
            SAMLSSOReqValidationResponseDTO validationResponse = new SAMLSSOReqValidationResponseDTO();
            Issuer issuer = authnReq.getIssuer();
            Subject subject = authnReq.getSubject();

            //Validate the version
            if (!(authnReq.getVersion().equals(SAMLVersion.VERSION_20))) {
                String errorResp = buildErrorResponse(SAMLSSOConstants.StatusCodes.VERSION_MISMATCH,
                        "Invalid SAML Version in Authentication Request. SAML Version should be equal to 2.0");
                validationResponse.setResponse(errorResp);
                validationResponse.setValid(false);
                return validationResponse;
            }

            //validate the issuer
            if (issuer.getValue() != null) {
                validationResponse.setIssuer(issuer.getValue());
            } else if (issuer.getSPProvidedID() != null) {
                validationResponse.setIssuer(issuer.getSPProvidedID());
            } else {
                validationResponse.setValid(false);
                String errorResp = buildErrorResponse(SAMLSSOConstants.StatusCodes.REQUESTOR_ERROR,
                        "Issuer/ProviderName should not be empty in the Authentication Request.");
                validationResponse.setResponse(errorResp);
                validationResponse.setValid(false);
                return validationResponse;
            }

            // set the custom login page URL, if available
            SSOServiceProviderConfigManager spConfigManager = SSOServiceProviderConfigManager.getInstance();
            SAMLSSOServiceProviderDO spDO = spConfigManager.getServiceProvider(issuer.getValue());
            if(spDO != null){
                validationResponse.setLoginPageURL(spDO.getLoginPageURL());
            }

            //TODO : Validate the NameID Format
            if (subject != null) {
                if (subject.getNameID() != null) {
                    validationResponse.setSubject(subject.getNameID().getValue());
                }
            }
            //TODO : validate the signature
            validationResponse.setId(authnReq.getID());
            validationResponse.setAssertionConsumerURL(authnReq.getAssertionConsumerServiceURL());
            validationResponse.setValid(true);

            if (log.isDebugEnabled()) {
                log.debug("Authentication Request Validation is successfull..");
            }
            return validationResponse;
        } catch (Exception e) {
            throw new IdentityException("Error validating the authentication request", e);
        }
    }

    /**
     * build the error response
     * @param status
     * @param message
     * @return decoded response
     * @throws IdentityException
     */
    private String buildErrorResponse(String status, String message) throws Exception {
        ErrorResponseBuilder respBuilder = new ErrorResponseBuilder();
        Response response = respBuilder.buildResponse(authnReq.getID(), status, message);
        return SAMLSSOUtil.encode(SAMLSSOUtil.marshall(response));
    }
}
