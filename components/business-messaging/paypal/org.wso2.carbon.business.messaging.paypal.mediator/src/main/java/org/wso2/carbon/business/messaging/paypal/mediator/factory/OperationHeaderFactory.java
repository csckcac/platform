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
package org.wso2.carbon.business.messaging.paypal.mediator.factory;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.business.messaging.paypal.mediator.RequestCredential;
import org.wso2.carbon.business.messaging.paypal.mediator.handler.CredentialHandler;

/**
 * This class builds the header elements of the soap request based on the input
 * values extracted from the Message context. These values will be used to
 * authenticate the invocations to the Paypal WS API.
 * 
 * This is a singleton class.
 */
public class OperationHeaderFactory {

	/**
	 * <p>
	 * Holds the log4j based log for the login purposes
	 * </p>
	 */
	private static final Log log = LogFactory
			.getLog(OperationHeaderFactory.class);

	/** Reference of the singleton instance. */
	private static OperationHeaderFactory operationPayloadFactory;

	/** OMFactory to construct the payload for the operation */
	private final OMFactory factory;

	/** Namespace to represent the urn:ebay:api:PayPalAPI */
	private final OMNamespace urnNS;

	/** Namespace to represent the urn:ebay:apis:eBLBaseComponents */
	private final OMNamespace urn1NS;

	/** Handles the inputs of the operation. */
	private final CredentialHandler credentialHandler;

	/** The message context associated with the message. */
	private MessageContext synCtx;

	/**
	 * private constructor to disable the creation of the
	 * OperationPayloadFactory instances
	 */
	private OperationHeaderFactory() {
		factory = OMAbstractFactory.getOMFactory();
		urnNS = factory.createOMNamespace("urn:ebay:api:PayPalAPI", "urn");
		urn1NS = factory.createOMNamespace("urn:ebay:apis:eBLBaseComponents",
				"urn1");

		credentialHandler = new CredentialHandler();
	}

	/**
	 * This method get the singletone instance of the OperationHeaderFactory.
	 * 
	 * @return the singleton instance of this class.
	 */
	public synchronized static OperationHeaderFactory getInstance() {
		if (null == operationPayloadFactory) {
			operationPayloadFactory = new OperationHeaderFactory();
		}
		return operationPayloadFactory;
	}

	/**
	 * Creates an OMElement instance to represent the header information
	 * required to invoke the operation.
	 * 
	 * @param requestCredential
	 *            the request credentials for invoking the operation.
	 * @param synCtx
	 *            the message context.
	 * @param operation
	 *            the operation to be invoked.
	 * 
	 * @return the OMElement payload representing the operation instance.
	 */
	public OMElement buildHeader(RequestCredential requestCredential,
			MessageContext synCtx) {
		this.synCtx = synCtx;
		credentialHandler.handle(requestCredential);
		OMElement payload = buildHeader(requestCredential);

		return payload;
	}

	/**
	 * Creates an OMElement instance to represent the Bill user operation.
	 * 
	 * @param operation
	 *            - the operation.
	 * @return the OMElement payload representing the Bill user operation.
	 */
	private OMElement buildHeader(RequestCredential requestCredential) {

		log.debug("Start building the request credentials");

		OMElement requesterCredentialsElem = factory.createOMElement(
				"RequesterCredentials", urnNS);
		addChild(requesterCredentialsElem, urn1NS, "eBayAuthToken",
				"eBayAuthToken");
		addChild(requesterCredentialsElem, urn1NS, "HardExpirationWarning",
				"HardExpirationWarning");

		OMElement credentialsElem = addChild(requesterCredentialsElem, urn1NS,
				"Credentials", null);
		addChild(credentialsElem, urn1NS, "AppId",
				RequestCredential.APP_ID_ELEM);
		addChild(credentialsElem, urn1NS, "DevId",
				RequestCredential.DEV_ID_ELEM);
		addChild(credentialsElem, urn1NS, "AuthCert",
				RequestCredential.AUTH_CERT_ELEM);
		addChild(credentialsElem, urn1NS, "Username",
				RequestCredential.USERNAME_ELEM);
		addChild(credentialsElem, urn1NS, "Password",
				RequestCredential.PASSWORD_ELEM);
		addChild(credentialsElem, urn1NS, "Signature",
				RequestCredential.SIGNATURE_ELEM);
		addChild(credentialsElem, urn1NS, "Subject",
				RequestCredential.SUBJECT_ELEM);
		addChild(credentialsElem, urn1NS, "AuthToken",
				RequestCredential.EBAY_AUTH_TOKEN_ELEM);

		log.debug("End building the request credentials");
		return requesterCredentialsElem;
	}

	/**
	 * Constructs the portions of the credential elements.
	 * 
	 * @param parent
	 *            the parent element.
	 * @param childElemNS
	 *            the namespace of the child element.
	 * @param childElemName
	 *            the name of the child element.
	 * @param key
	 *            the optional key used to fetch an input.
	 * @return the created child element that represents the part of the header
	 *         information.
	 */
	private OMElement addChild(OMElement parent, OMNamespace childElemNS,
			String childElemName, String key) {

		OMElement child = factory.createOMElement(childElemName, childElemNS);
		if (null != key) {
			child.setText(credentialHandler.lookupValue(synCtx, key));
		}
		parent.addChild(child);

		return child;
	}
}
