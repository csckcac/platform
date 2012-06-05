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
package org.wso2.carbon.identity.sso.saml.builders;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.security.signature.XMLSignature;
import org.joda.time.DateTime;
import org.opensaml.common.SAMLVersion;
import org.opensaml.saml1.core.NameIdentifier;
import org.opensaml.saml2.core.Assertion;
import org.opensaml.saml2.core.Audience;
import org.opensaml.saml2.core.AudienceRestriction;
import org.opensaml.saml2.core.AuthnContext;
import org.opensaml.saml2.core.AuthnContextClassRef;
import org.opensaml.saml2.core.AuthnStatement;
import org.opensaml.saml2.core.Conditions;
import org.opensaml.saml2.core.NameID;
import org.opensaml.saml2.core.Response;
import org.opensaml.saml2.core.Status;
import org.opensaml.saml2.core.StatusCode;
import org.opensaml.saml2.core.StatusMessage;
import org.opensaml.saml2.core.Subject;
import org.opensaml.saml2.core.SubjectConfirmation;
import org.opensaml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml2.core.impl.AssertionBuilder;
import org.opensaml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml2.core.impl.AuthnContextBuilder;
import org.opensaml.saml2.core.impl.AuthnContextClassRefBuilder;
import org.opensaml.saml2.core.impl.AuthnStatementBuilder;
import org.opensaml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.saml2.core.impl.NameIDBuilder;
import org.opensaml.saml2.core.impl.StatusBuilder;
import org.opensaml.saml2.core.impl.StatusCodeBuilder;
import org.opensaml.saml2.core.impl.StatusMessageBuilder;
import org.opensaml.saml2.core.impl.SubjectBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationBuilder;
import org.opensaml.saml2.core.impl.SubjectConfirmationDataBuilder;
import org.wso2.carbon.identity.base.IdentityException;
import org.wso2.carbon.identity.sso.saml.SAMLSSOConstants;
import org.wso2.carbon.identity.sso.saml.dto.SAMLSSOAuthnReqDTO;
import org.wso2.carbon.identity.sso.saml.util.SAMLSSOUtil;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

public class ResponseBuilder {

    private static Log log = LogFactory.getLog(ResponseBuilder.class);

    static {
        SAMLSSOUtil.doBootstrap();
    }

    public Response buildResponse(SAMLSSOAuthnReqDTO authReqDTO, String sessionId) throws IdentityException {
        Response response = new org.opensaml.saml2.core.impl.ResponseBuilder().buildObject();
        response.setID(SAMLSSOUtil.createID());
        response.setStatus(buildStatus(SAMLSSOConstants.StatusCodes.SUCCESS_CODE, null));
        response.setVersion(SAMLVersion.VERSION_20);
        DateTime issueInstant = new DateTime();
        DateTime notOnOrAfter = new DateTime(issueInstant.getMillis() + 5 * 60 * 1000);
        response.setIssueInstant(issueInstant);
        response.getAssertions().add(buildSAMLAssertion(authReqDTO, notOnOrAfter, sessionId));
        if (authReqDTO.getDoSignAssertions()) {
            SAMLSSOUtil.setSignature(response, XMLSignature.ALGO_ID_SIGNATURE_RSA,
                    new SignKeyDataHolder(authReqDTO.getUsername()));
        }
        return response;
    }

    private Assertion buildSAMLAssertion(SAMLSSOAuthnReqDTO authReqDTO, DateTime notOnOrAfter, String sessionId) throws IdentityException {
        try {
            DateTime currentTime = new DateTime();
            Assertion samlAssertion = new AssertionBuilder().buildObject();
            samlAssertion.setID(SAMLSSOUtil.createID());
            samlAssertion.setVersion(SAMLVersion.VERSION_20);
            samlAssertion.setIssuer(SAMLSSOUtil.getIssuer());
            samlAssertion.setIssueInstant(currentTime);
            Subject subject = new SubjectBuilder().buildObject();

            NameID nameId = new NameIDBuilder().buildObject();
            if (authReqDTO.getUseFullyQualifiedUsernameAsSubject()) {
                nameId.setValue(authReqDTO.getUsername());
                nameId.setFormat(NameIdentifier.EMAIL);
            } else {
                nameId.setValue(MultitenantUtils.getTenantAwareUsername(authReqDTO.getUsername()));
                nameId.setFormat(authReqDTO.getNameIDFormat());
            }

            subject.setNameID(nameId);

            SubjectConfirmation subjectConfirmation = new SubjectConfirmationBuilder().buildObject();
            subjectConfirmation.setMethod(SAMLSSOConstants.SUBJECT_CONFIRM_BEARER);

            SubjectConfirmationData scData = new SubjectConfirmationDataBuilder().buildObject();
            scData.setRecipient(authReqDTO.getAssertionConsumerURL());
            scData.setNotOnOrAfter(notOnOrAfter);
            scData.setInResponseTo(authReqDTO.getId());
            subjectConfirmation.setSubjectConfirmationData(scData);

            subject.getSubjectConfirmations().add(subjectConfirmation);

            samlAssertion.setSubject(subject);

            AuthnStatement authStmt = new AuthnStatementBuilder().buildObject();
            authStmt.setAuthnInstant(new DateTime());

            AuthnContext authContext = new AuthnContextBuilder().buildObject();
            AuthnContextClassRef authCtxClassRef = new AuthnContextClassRefBuilder().buildObject();
            authCtxClassRef.setAuthnContextClassRef(AuthnContext.PASSWORD_AUTHN_CTX);
            authContext.setAuthnContextClassRef(authCtxClassRef);
            authStmt.setAuthnContext(authContext);
            if(authReqDTO.isDoSingleLogout()){
                authStmt.setSessionIndex(sessionId);
            }
            samlAssertion.getAuthnStatements().add(authStmt);

            Audience audience = new AudienceBuilder().buildObject();
            audience.setAudienceURI(authReqDTO.getIssuer());
            AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder().buildObject();
            audienceRestriction.getAudiences().add(audience);
            Conditions conditions = new ConditionsBuilder().buildObject();
            conditions.setNotBefore(currentTime);
            conditions.setNotOnOrAfter(notOnOrAfter);
            conditions.getAudienceRestrictions().add(audienceRestriction);
            samlAssertion.setConditions(conditions);

            return samlAssertion;
        } catch (Exception e) {
            log.error("Error when reading claim values for generating SAML Response");
            throw new IdentityException("Error when reading claim values for generating SAML Response", e);
        }
    }

    private Status buildStatus(String status, String statMsg) {

        Status stat = new StatusBuilder().buildObject();

        //Set the status code
        StatusCode statCode = new StatusCodeBuilder().buildObject();
        statCode.setValue(status);
        stat.setStatusCode(statCode);

        //Set the status Message
        if (statMsg != null) {
            StatusMessage statMesssage = new StatusMessageBuilder().buildObject();
            statMesssage.setMessage(statMsg);
            stat.setStatusMessage(statMesssage);
        }

        return stat;
    }

}
