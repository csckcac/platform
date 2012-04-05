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
package org.wso2.carbon.identity.sts.passive;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultCode;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultSubCode;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPFaultValue;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rahas.TrustException;
import org.wso2.carbon.identity.sts.passive.internal.IdentityPassiveSTSServiceComponent;
import org.wso2.carbon.identity.sts.passive.processors.RequestProcessor;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class PassiveSTSService {
    private static final Log log = LogFactory.getLog(PassiveSTSService.class);
    public static final String CLAIM_DIALECT = "claimDialect";
    public static final String CLAIMS = "claims";
    public static final String REALM_NAME = "realmName";
    private String registryTrustedServicePath = "repository/identity/passiveSTSTrustedRP/";
    private static final String SLASH_REPLACE_CHARACTER = "BACK_SLASH";

    public ResponseToken getResponse(RequestToken request) throws Exception {

        if (IdentityPassiveSTSServiceComponent.getRealm() == null) {
            throw new Exception("User realm not properly set");
        }

        if (request == null || request.getPassword() == null || request.getUserName() == null) {
            throw new Exception("Invalid request token. User credentials not provided");
        }

        RequestProcessor processor = null;
        boolean isAuthenticated = false;
        ResponseToken responseToken = null;
        String soapfault = null;

        isAuthenticated = IdentityPassiveSTSServiceComponent.getRealm().getUserStoreManager().
                authenticate(request.getUserName(), request.getPassword());

        if (!isAuthenticated) {
            return new ResponseToken();
        }

        processor = RequestProcessorFactory.getInstance().getRequestProcessor(request.getAction());

        if (processor != null) {
            try {
                responseToken = processor.process(request);
            } catch (TrustException e) {
                soapfault = genFaultResponse(MessageContext.getCurrentMessageContext(), "Sender",
                                             "InvalidRequest", e.getMessage(), "none").toStringWithConsume();
            }
        } else {
            soapfault = genFaultResponse(MessageContext.getCurrentMessageContext(), "Sender",
                                         "InvalidRequest", "Invalid Request", "none").toStringWithConsume();
        }

        if (responseToken == null) {
            responseToken = new ResponseToken();
        }

        if (soapfault != null) {
            responseToken.setResults(soapfault);
        }

        responseToken.setAuthenticated(true);
        if (request.getReplyTo() != null) {
            responseToken.setReplyTo(request.getReplyTo());
        } else {
            responseToken.setReplyTo(request.getRealm());
        }

        if (responseToken.getReplyTo() == null) {
            throw new Exception("ReplyTo address not found");
        }

        responseToken.setContext(request.getContext());

        return responseToken;
    }

    private SOAPFault genFaultResponse(MessageContext messageCtx, String code, String subCode,
                                       String reason, String detail) {
        SOAPFactory soapFactory = null;
        if (messageCtx.isSOAP11()) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
            SOAPEnvelope message = soapFactory.getDefaultFaultEnvelope();
            SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason();
            soapFaultReason.setText(reason);
            message.getBody().getFault().setReason(soapFaultReason);
            SOAPFaultCode soapFaultCode = soapFactory.createSOAPFaultCode();
            QName qNameSubCode = new QName("http://wso2.org/passivests", subCode, "sts");
            soapFaultCode.setText(qNameSubCode);
            message.getBody().getFault().setCode(soapFaultCode);
            return message.getBody().getFault();
        } else {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
            SOAPEnvelope message = soapFactory.getDefaultFaultEnvelope();
            SOAPFaultDetail soapFaultDetail = soapFactory.createSOAPFaultDetail();
            soapFaultDetail.setText(detail);
            message.getBody().getFault().setDetail(soapFaultDetail);
            SOAPFaultReason soapFaultReason = soapFactory.createSOAPFaultReason();
            SOAPFaultText soapFaultText = soapFactory.createSOAPFaultText();
            soapFaultText.setText(reason);
            soapFaultReason.addSOAPText(soapFaultText);
            message.getBody().getFault().setReason(soapFaultReason);
            SOAPFaultCode soapFaultCode = soapFactory.createSOAPFaultCode();
            SOAPFaultValue soapFaultValue = soapFactory.createSOAPFaultValue(soapFaultCode);
            soapFaultValue.setText(code);
            soapFaultCode.setValue(soapFaultValue);
            SOAPFaultSubCode soapFaultSubCode = soapFactory.createSOAPFaultSubCode(soapFaultCode);
            SOAPFaultValue soapFaultValueSub = soapFactory.createSOAPFaultValue(soapFaultSubCode);
            QName qNameSubCode = new QName("http://wso2.org/passivests", subCode, "sts");
            soapFaultValueSub.setText(qNameSubCode);
            soapFaultSubCode.setValue(soapFaultValueSub);
            soapFaultCode.setSubCode(soapFaultSubCode);
            message.getBody().getFault().setCode(soapFaultCode);
            return message.getBody().getFault();
        }
    }

    /**
     * Add a trusted service to which tokens are issued with given claims.
     *
     * @param realmName - this uniquely represents the trusted service
     * @param claims    - these comma separated default claims are issued when a request is done from the given realm
     */
    public void addTrustedService(String realmName, String claimDialect, String claims)
            throws Exception {
        realmName = replaceSlashWithConstantString(realmName);
        try {
            Registry registry = IdentityPassiveSTSServiceComponent.getConfigSystemRegistry();
            String trustedServicePath = registryTrustedServicePath + realmName;
            // if registry collection does not exists, create
            if (!registry.resourceExists(trustedServicePath)) {
                Resource resource = registry.newResource();
                resource.addProperty(REALM_NAME, realmName);
                resource.addProperty(CLAIMS, claims);
                resource.addProperty(CLAIM_DIALECT, claimDialect);
                registry.put(trustedServicePath, resource);
            } else {
                throw new Exception(realmName + " already added. Please remove first and add again.");
            }
        } catch (RegistryException e) {
            String error = "Error occurred when adding a trusted service due to error in accessing registry.";
            log.error(error, e);
            throw new Exception(error, e);
        }
    }

    /**
     * Remove the given trusted service with realmName
     *
     * @param realmName - the realm of the service
     * @throws Exception
     */
    public void removeTrustedService(String realmName) throws Exception {
        realmName = replaceSlashWithConstantString(realmName);
        try {
            Registry registry = IdentityPassiveSTSServiceComponent.getConfigSystemRegistry();
            String trustedServicePath = registryTrustedServicePath + realmName;
            if (registry.resourceExists(trustedServicePath)) {
                registry.delete(trustedServicePath);
            } else {
                throw new Exception(realmName + " ,No such trusted service exists to delete.");
            }

        } catch (RegistryException e) {
            String error = "Error occurred when removing a trusted service due to error in accessing registry.";
            log.error(error, e);
            throw new Exception(error, e);
        }
    }

    /**
     * Get all trusted services
     *
     * @return
     * @throws Exception
     */
    public ClaimDTO[] getAllTrustedServices() throws Exception {
        try {
            Registry registry = IdentityPassiveSTSServiceComponent.getConfigSystemRegistry();
            List<ClaimDTO> trustedServices = new ArrayList<ClaimDTO>();

            if (!registry.resourceExists(registryTrustedServicePath)) {
                return new ClaimDTO[0];
            }
            Collection trustedServiceCollection = (Collection) registry.get(registryTrustedServicePath);
            for (String resourcePath : trustedServiceCollection.getChildren()) {
                Resource resource = registry.get(resourcePath);
                ClaimDTO claimDTO = new ClaimDTO();
                claimDTO.setRealm(resource.getProperty(REALM_NAME).replace(SLASH_REPLACE_CHARACTER, "/"));

                String claims = resource.getProperty(CLAIMS);
                if (claims.startsWith("[")) {
                    claims = claims.replaceFirst("\\[", "");
                }
                if (claims.endsWith("]")) {
                    claims = claims.substring(0, claims.length() - 2);
                }
                claimDTO.setDefaultClaims(claims.split(","));

                claimDTO.setClaimDialect(resource.getProperty(CLAIM_DIALECT));

                trustedServices.add(claimDTO);
            }

            return trustedServices.toArray(new ClaimDTO[trustedServices.size()]);
        } catch (RegistryException e) {
            String error = "Error occurred when getting all trusted services due to error in accessing registry.";
            log.error(error, e);
            throw new Exception(error, e);
        }
    }

    /**
     * Get all trusted services
     *
     * @return
     * @throws Exception
     */
    public ClaimDTO getTrustedServiceClaims(String realmName) throws Exception {
        realmName = replaceSlashWithConstantString(realmName);
        try {
            Registry registry = IdentityPassiveSTSServiceComponent.getConfigSystemRegistry();
            String trustedServicePath = registryTrustedServicePath + realmName;

            if (!registry.resourceExists(trustedServicePath)) {
                throw new Exception("No trusted service found with name:" + realmName);
            }
            Resource resource = registry.get(trustedServicePath);
            ClaimDTO claimDTO = new ClaimDTO();
            claimDTO.setRealm(realmName.replace(SLASH_REPLACE_CHARACTER, "/"));
            String claims = resource.getProperty(CLAIMS);

            if (claims.startsWith("[")) {
                claims = claims.replaceFirst("\\[", "");
            }
            if (claims.endsWith("]")) {
                // replace ] and , too. handle better way. ugly code
                claims = claims.substring(0, claims.length() - 3);
            }

            claimDTO.setDefaultClaims(claims.split(","));

            claimDTO.setClaimDialect(resource.getProperty(CLAIM_DIALECT));
            return claimDTO;
        } catch (RegistryException e) {
            String error = "Error occurred when getting a trusted service due to error in accessing registry.";
            log.error(error, e);
            throw new Exception(error, e);
        }
    }

    private String replaceSlashWithConstantString(String realmName) throws Exception {
        if (realmName == null || "".equals(realmName)) {
            throw new Exception("realm name can not be empty or null");
        }
        realmName = realmName.trim();
        if (realmName.endsWith("/")) {
            realmName = realmName.substring(0, realmName.length() - 1);
        }
        realmName = realmName.replace("/", SLASH_REPLACE_CHARACTER);
        return realmName;
    }
}
