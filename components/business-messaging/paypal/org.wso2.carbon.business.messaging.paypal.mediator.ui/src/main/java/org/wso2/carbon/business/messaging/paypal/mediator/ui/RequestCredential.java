/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.business.messaging.paypal.mediator.ui;

import java.util.HashMap;
import java.util.Map;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.SynapseXPath;

/**
 * <p>
 * Specifies the set of credentials to invoke the APIs for required by Paypal
 * services.
 * </p>
 *
 * @see org.wso2.carbon.business.messaging.paypal.mediator.Operation
 */
public class RequestCredential {

    /**
     * <p>
     * Holds the log4j based log for the login purposes
     * </p>
     */
    private static final Log log = LogFactory.getLog(RequestCredential.class);

    public static final String EBAY_AUTH_TOKEN_ELEM = "eBayAuthToken";
    public static final String HARD_EXP_ELEM = "HardExpirationWarning";
    public static final String APP_ID_ELEM = "AppId";
    public static final String DEV_ID_ELEM = "DevId";
    public static final String AUTH_CERT_ELEM = "AuthCert";
    public static final String USERNAME_ELEM = "Username";
    public static final String PASSWORD_ELEM = "Password";
    public static final String SIGNATURE_ELEM = "Signature";
    public static final String SUBJECT_ELEM = "Subject";
    public static final String AUTH_TOKEN_ELEM = "AuthToken";
    /**
     * <p>
     * XPath describing the auth-token element or the attribute of the message
     * which will be used for PayPal authentication.
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath authTokenXPath;

    private String authTokenValue;
    /**
     * <p>
     * XPath describing the hard-expiration element or the attribute of the
     * message which will be used for PayPal authentication.
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath hardExpirationXPath;

    private String hardExpirationValue;
    /**
     * <p>
     * XPath describing the app-id element or the attribute of the message which
     * will be used for PayPal authentication.
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath appIdXPath;

    private String appIdValue;
    /**
     * <p>
     * XPath describing the dev-id element or the attribute of the message which
     * will be used for PayPal authentication.
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath devIdXPath;

    private String devIdValue;
    /**
     * <p>
     * XPath describing the auth-cert element or the attribute of the message
     * which will be used for PayPal authentication.
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath authCertXPath;

    private String authCertValue;
    /**
     * <p>
     * XPath describing the username element or the attribute of the message
     * which will be used for PayPal authentication.
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath usernameXPath;

    private String usernameValue;
    /**
     * <p>
     * XPath describing the password element or the attribute of the message
     * which will be used for PayPal authentication.
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath passwordXPath;

    private String passwordValue;
    /**
     * <p>
     * XPath describing the signature element or the attribute of the message
     * which will be used for PayPal authentication.
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath signatureXPath;

    private String signatureValue;
    /**
     * <p>
     * XPath describing the subject element or the attribute of the message
     * which will be used for PayPal authentication.
     * </p>
     *
     * @see org.apache.synapse.util.xpath.SynapseXPath
     */
    private SynapseXPath subjectXPath;

    private String subjectValue;
    /**
     * Namespace of the credential element.
     */
    private String namespace;

    /**
     * The prefix of the credential's namespace.
     */
    private String namespacePrefix;

    private Map<String, SynapseXPath> credentialTokensMap = new HashMap<String, SynapseXPath>();

    private Map<String, String> credentialTokenValueMap = new HashMap<String, String>();

    /**
     * Creates an <code>OMElement</code> to represent operation
     *
     * @return
     */
    public OMElement getOMElement() {

        return OMAbstractFactory.getOMFactory().createOMElement(
                "RequesterCredentials",
                OMAbstractFactory.getOMFactory().createOMNamespace(
                        "urn:ebay:api:PayPalAPI", "urn"));
    }

    /**
     * Creates an <code>OMElement</code> to represent sub creadential elements
     *
     * @return
     */
    public OMElement getCredentialsOMElement() {

        return OMAbstractFactory.getOMFactory().createOMElement(
                "Credentials",
                OMAbstractFactory.getOMFactory().createOMNamespace(namespace,
                                                                   namespacePrefix));
    }

    /**
     * This method is called to handle individual <code>SynapseXPath</code>
     * fields to construct the <code>OMElement</code> representing the message
     * header.
     *
     * @param synCtx             the synapse context
     * @param credentialElemName the credential element name.
     * @return the
     */
    public String evaluate(MessageContext synCtx, String credentialElemName) {
        String sourceObject = null;
        // expression is required to perform the match
        if (credentialTokensMap.containsKey(credentialElemName)) {
            SynapseXPath xpath = credentialTokensMap.get(credentialElemName);
            sourceObject = xpath.stringValueOf(synCtx);
            if (null == sourceObject) {
                log.debug(String.format("Source String : %s evaluates to null",
                                        xpath.toString()));
            }
        } else if (credentialTokenValueMap.containsKey(credentialElemName)) {
            sourceObject = credentialTokenValueMap.get(credentialElemName);
        }
        return sourceObject;
    }

    /**
     * This method returns the set of the elements in the order that they
     * appears in the header
     */
    public String[] getCredentialElementSequence() {
        return new String[]{APP_ID_ELEM, DEV_ID_ELEM, AUTH_CERT_ELEM,
                            USERNAME_ELEM, PASSWORD_ELEM, SIGNATURE_ELEM, SUBJECT_ELEM};
    }

    /**
     * @return the authTokenXPath
     */
    public SynapseXPath getAuthTokenXPath() {
        return authTokenXPath;
    }

    /**
     * @param authTokenXPath the authTokenXPath to set
     */
    public void setAuthTokenXPath(SynapseXPath authTokenXPath) {
        this.authTokenXPath = authTokenXPath;

    }

    /**
     * @return the hardExpirationXPath
     */
    public SynapseXPath getHardExpirationXPath() {
        return hardExpirationXPath;
    }

    /**
     * @param hardExpirationXPath the hardExpirationXPath to set
     */
    public void setHardExpirationXPath(SynapseXPath hardExpirationXPath) {
        this.hardExpirationXPath = hardExpirationXPath;
    }

    /**
     * @return the appIdXPath
     */
    public SynapseXPath getAppIdXPath() {
        return appIdXPath;
    }

    /**
     * @param appIdXPath the appIdXPath to set
     */
    public void setAppIdXPath(SynapseXPath appIdXPath) {
        this.appIdXPath = appIdXPath;
        credentialTokensMap.put(APP_ID_ELEM, appIdXPath);
    }

    /**
     * @return the devIdXPath
     */
    public SynapseXPath getDevIdXPath() {
        return devIdXPath;
    }

    /**
     * @param devIdXPath the devIdXPath to set
     */
    public void setDevIdXPath(SynapseXPath devIdXPath) {
        this.devIdXPath = devIdXPath;
        credentialTokensMap.put(DEV_ID_ELEM, devIdXPath);
    }

    /**
     * @return the authCertXPath
     */
    public SynapseXPath getAuthCertXPath() {
        return authCertXPath;
    }

    /**
     * @param authCertXPath the authCertXPath to set
     */
    public void setAuthCertXPath(SynapseXPath authCertXPath) {
        this.authCertXPath = authCertXPath;
        credentialTokensMap.put(AUTH_CERT_ELEM, authCertXPath);
    }

    /**
     * @return the usernameXPath
     */
    public SynapseXPath getUsernameXPath() {
        return usernameXPath;
    }

    /**
     * @param usernameXPath the usernameXPath to set
     */
    public void setUsernameXPath(SynapseXPath usernameXPath) {
        this.usernameXPath = usernameXPath;
        credentialTokensMap.put(USERNAME_ELEM, usernameXPath);
    }

    /**
     * @return the passwordXPath
     */
    public SynapseXPath getPasswordXPath() {
        return passwordXPath;
    }

    /**
     * @param passwordXPath the passwordXPath to set
     */
    public void setPasswordXPath(SynapseXPath passwordXPath) {
        this.passwordXPath = passwordXPath;
        credentialTokensMap.put(PASSWORD_ELEM, passwordXPath);
    }

    /**
     * @return the signatureXPath
     */
    public SynapseXPath getSignatureXPath() {
        return signatureXPath;
    }

    /**
     * @param signatureXPath the signatureXPath to set
     */
    public void setSignatureXPath(SynapseXPath signatureXPath) {
        this.signatureXPath = signatureXPath;
        credentialTokensMap.put(SIGNATURE_ELEM, signatureXPath);
    }

    /**
     * @return the subjectXPath
     */
    public SynapseXPath getSubjectXPath() {
        return subjectXPath;
    }

    /**
     * @param subjectXPath the subjectXPath to set
     */
    public void setSubjectXPath(SynapseXPath subjectXPath) {
        this.subjectXPath = subjectXPath;
        credentialTokensMap.put(SUBJECT_ELEM, subjectXPath);
    }

    /**
     * @return the inputWrapperNameNS
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * @param inputWrapperNameNS the inputWrapperNameNS to set
     */
    public void setNamespace(String inputWrapperNameNS) {
        this.namespace = inputWrapperNameNS;
    }


    /**
     * @return the authTokenValue
     */
    public String getAuthTokenValue() {
        return authTokenValue;
    }

    /**
     * @param authTokenValue the authTokenValue to set
     */
    public void setAuthTokenValue(String authTokenValue) {
        this.authTokenValue = authTokenValue;
        credentialTokenValueMap.put(AUTH_TOKEN_ELEM, authTokenValue);
    }

    /**
     * @return the hardExpirationValue
     */
    public String getHardExpirationValue() {
        return hardExpirationValue;
    }

    /**
     * @param hardExpirationValue the hardExpirationValue to set
     */
    public void setHardExpirationValue(String hardExpirationValue) {
        this.hardExpirationValue = hardExpirationValue;
        credentialTokenValueMap.put(HARD_EXP_ELEM, hardExpirationValue);
    }

    /**
     * @return the appIdValue
     */
    public String getAppIdValue() {
        return appIdValue;
    }

    /**
     * @param appIdValue the appIdValue to set
     */
    public void setAppIdValue(String appIdValue) {
        this.appIdValue = appIdValue;
        credentialTokenValueMap.put(APP_ID_ELEM, appIdValue);
    }

    /**
     * @return the devIdValue
     */
    public String getDevIdValue() {
        return devIdValue;
    }

    /**
     * @param devIdValue the devIdValue to set
     */
    public void setDevIdValue(String devIdValue) {
        this.devIdValue = devIdValue;
        credentialTokenValueMap.put(DEV_ID_ELEM, devIdValue);
    }

    /**
     * @return the authCertValue
     */
    public String getAuthCertValue() {
        return authCertValue;
    }

    /**
     * @param authCertValue the authCertValue to set
     */
    public void setAuthCertValue(String authCertValue) {
        this.authCertValue = authCertValue;
        credentialTokenValueMap.put(AUTH_CERT_ELEM, authCertValue);
    }

    /**
     * @return the usernameValue
     */
    public String getUsernameValue() {
        return usernameValue;
    }

    /**
     * @param usernameValue the usernameValue to set
     */
    public void setUsernameValue(String usernameValue) {
        this.usernameValue = usernameValue;
        credentialTokenValueMap.put(USERNAME_ELEM, usernameValue);
    }

    /**
     * @return the passwordValue
     */
    public String getPasswordValue() {
        return passwordValue;
    }

    /**
     * @param passwordValue the passwordValue to set
     */
    public void setPasswordValue(String passwordValue) {
        this.passwordValue = passwordValue;
        credentialTokenValueMap.put(PASSWORD_ELEM, passwordValue);
    }

    /**
     * @return the signatureValue
     */
    public String getSignatureValue() {
        return signatureValue;
    }

    /**
     * @param signatureValue the signatureValue to set
     */
    public void setSignatureValue(String signatureValue) {
        this.signatureValue = signatureValue;
        credentialTokenValueMap.put(SIGNATURE_ELEM, signatureValue);
    }

    /**
     * @return the subjectValue
     */
    public String getSubjectValue() {
        return subjectValue;
    }

    /**
     * @param subjectValue the subjectValue to set
     */
    public void setSubjectValue(String subjectValue) {
        this.subjectValue = subjectValue;
        credentialTokenValueMap.put(SUBJECT_ELEM, subjectValue);
    }

    /**
     * @return the inputWrapperNameNSPrefix
	 */
	public String getNamespacePrefix() {
		return namespacePrefix;
	}

	/**
     * @param inputWrapperNameNSPrefix the inputWrapperNameNSPrefix to set
     */
	public void setNamespacePrefix(String inputWrapperNameNSPrefix) {
		this.namespacePrefix = inputWrapperNameNSPrefix;
	}
}
