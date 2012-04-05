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
import org.wso2.carbon.business.messaging.paypal.mediator.Operation;
import org.wso2.carbon.business.messaging.paypal.mediator.handler.InputHandler;

/***
 * This is a singleton class that builds the payload of the operation to be
 * invoked in the Paypal WS API.
 */
public class OperationPayloadFactory {

	/**
	 * <p>
	 * Holds the log4j based log for the login purposes
	 * </p>
	 */
	private static final Log log = LogFactory
			.getLog(OperationPayloadFactory.class);

	/** Reference of the singleton instance. */
	private static OperationPayloadFactory operationPayloadFactory;

	/** OMFactory to construct the payload for the operation */
	private final OMFactory factory;

	/** Namespace to represent the urn:ebay:api:PayPalAPI */
	private final OMNamespace urnNS;

	/** Namespace to represent the urn:ebay:apis:eBLBaseComponents */
	private final OMNamespace urn1NS;

	/** Version of the PayPal WS API. */
	private final String version;

	/** Currency Id used for the operation. */
	private String currencyId;

	/** Handles the inputs of the operation. */
	private final InputHandler inputHandler;

	/** The message context associated with the message. */
	private MessageContext synCtx;

	/**
	 * private constructor to disable the creation of the
	 * OperationPayloadFactory instances
	 */
	private OperationPayloadFactory() {
		factory = OMAbstractFactory.getOMFactory();
		urnNS = factory.createOMNamespace("urn:ebay:api:PayPalAPI", "urn");
		urn1NS = factory.createOMNamespace("urn:ebay:apis:eBLBaseComponents",
				"urn1");
		version = "61.0";

		inputHandler = new InputHandler();
	}

	/**
	 * Returns the singleton instance. This method returns the singleton
	 * instance of the OperationPayloadFactory.
	 * 
	 * @return the singleton instance of this class.
	 */
	public synchronized static OperationPayloadFactory getInstance() {
		if (null == operationPayloadFactory) {
			operationPayloadFactory = new OperationPayloadFactory();
		}
		return operationPayloadFactory;
	}

	/**
	 * Creates an OMElement instance to represent the operation.
	 * 
	 * @param operation
	 *            - the operation.
	 * @return the OMElement payload representing the operation instance.
	 */
	public OMElement buildPayload(Operation operation, MessageContext synCtx) {

		this.synCtx = synCtx;
		currencyId = operation.getCurrency();
		inputHandler.handle(operation.getInputs());
		OMElement payload = null;
		if ("BillUser".equals(operation.getName())) {
			payload = buildBillUser(operation);
		} else if ("GetBalance".equals(operation.getName())) {
			payload = buildGetBalance(operation);
		} else if ("GetPalDetails".equals(operation.getName())) {
			payload = buildGetPalDetails(operation);
		}
		return payload;
	}

	/**
	 * Creates an OMElement instance to represent the Bill user operation.
	 * 
	 * @param operation
	 *            - the operation.
	 * @return the OMElement payload representing the Bill user operation.
	 */
	public OMElement buildBillUser(Operation operation) {

		log.debug("Start building payload for BillUser operation");
		OMElement billUserReqElem = factory.createOMElement("BillUserReq",
				urnNS);
		OMElement billUserRequestElem = addChild(billUserReqElem, urnNS,
				"BillUserRequest", (String[]) null);
		addChild(billUserRequestElem, urn1NS, "DetailLevel", "DetailLevel");
		addChild(billUserRequestElem, urn1NS, "ErrorLanguage", "ErrorLanguage");
		addChild(billUserRequestElem, urn1NS, "Version", "Version");

		OMElement merchantPullPaymentDetailsElem = addChild(
				billUserRequestElem, urn1NS, "MerchantPullPaymentDetails",
				(String[]) null);
		OMElement amountElem = addChild(merchantPullPaymentDetailsElem, urn1NS,
				"Amount", "MerchantPullPaymentDetails", "Amount");
		amountElem.addAttribute("currencyID", currencyId, null);

		addChild(merchantPullPaymentDetailsElem, urn1NS, "MpID",
				"MerchantPullPaymentDetails", "MpID");
		addChild(merchantPullPaymentDetailsElem, urn1NS, "PaymentType",
				"MerchantPullPaymentDetails", "PaymentType");
		addChild(merchantPullPaymentDetailsElem, urn1NS, "Memo",
				"MerchantPullPaymentDetails", "Memo");
		addChild(merchantPullPaymentDetailsElem, urn1NS, "EmailSubject",
				"MerchantPullPaymentDetails", "EmailSubject");

		OMElement taxElem = addChild(merchantPullPaymentDetailsElem, urn1NS,
				"Tax", "MerchantPullPaymentDetails", "Tax");
		taxElem.addAttribute("currencyID", currencyId, null);

		OMElement shippingElem = addChild(merchantPullPaymentDetailsElem,
				urn1NS, "Shipping", "MerchantPullPaymentDetails", "Shipping");
		shippingElem.addAttribute("currencyID", currencyId, null);

		OMElement handlingElem = addChild(merchantPullPaymentDetailsElem,
				urn1NS, "Handling", "MerchantPullPaymentDetails", "Handling");
		handlingElem.addAttribute("currencyID", currencyId, null);

		addChild(merchantPullPaymentDetailsElem, urn1NS, "ItemName",
				"MerchantPullPaymentDetails", "ItemName");
		addChild(merchantPullPaymentDetailsElem, urn1NS, "ItemNumber",
				"MerchantPullPaymentDetails", "ItemNumber");
		addChild(merchantPullPaymentDetailsElem, urn1NS, "Invoice",
				"MerchantPullPaymentDetails", "Invoice");
		addChild(merchantPullPaymentDetailsElem, urn1NS, "Custom",
				"MerchantPullPaymentDetails", "Custom");
		addChild(merchantPullPaymentDetailsElem, urn1NS, "ButtonSource",
				"MerchantPullPaymentDetails", "ButtonSource");
		addChild(merchantPullPaymentDetailsElem, urn1NS, "SoftDescriptor",
				"MerchantPullPaymentDetails", "SoftDescriptor");

		addChild(billUserRequestElem, urn1NS, "ReturnFMFDetails",
				"ReturnFMFDetails");

		log.debug("End building payload for BillUser operation");

		return billUserReqElem;
	}

	/**
	 * Creates an OMElement instance to represent the GetBalance operation.
	 * 
	 * @param operation
	 *            - the operation.
	 * @return the OMElement payload representing the GetBalance operation.
	 */
	public OMElement buildGetBalance(Operation operation) {

		log.debug("Start building payload for GetBalance operation");

		OMElement getBalanceReqElem = factory.createOMElement("GetBalanceReq",
				urnNS);
		OMElement getBalanceRequestElem = addChild(getBalanceReqElem, urnNS,
				"GetBalanceRequest", (String[]) null);
		addChild(getBalanceRequestElem, urn1NS, "DetailLevel", "DetailLevel");
		addChild(getBalanceRequestElem, urn1NS, "ErrorLanguage",
				"ErrorLanguage");
		addChild(getBalanceRequestElem, urn1NS, "Version", "Version");
		addChild(getBalanceRequestElem, urnNS, "ReturnAllCurrencies",
				"ReturnAllCurrencies");

		log.debug("End building payload for GetBalance operation");
		return getBalanceReqElem;
	}

	/**
	 * Creates an OMElement instance to represent the GetBalance operation.
	 * 
	 * @param operation
	 *            - the operation.
	 * @return the OMElement payload representing the GetBalance operation.
	 */
	public OMElement buildGetPalDetails(Operation operation) {

		log.debug("Start building payload for GetPalDetails operation");

		OMElement getPalDetailsReqElem = factory.createOMElement(
				"GetPalDetailsReq", urnNS);
		OMElement getPalDetailsRequestElem = addChild(getPalDetailsReqElem,
				urnNS, "GetPalDetailsRequest", (String[]) null);
		addChild(getPalDetailsRequestElem, urn1NS, "DetailLevel", "DetailLevel");
		addChild(getPalDetailsRequestElem, urn1NS, "ErrorLanguage",
				"ErrorLanguage");
		addChild(getPalDetailsRequestElem, urn1NS, "Version", "Version");

		log.debug("End building payload for GetPalDetails operation");

		return getPalDetailsReqElem;
	}

	/**
	 * Creates an OMElement instance to represent the GetBalance operation.
	 * 
	 * @param operation
	 *            - the operation.
	 * @return the OMElement payload representing the GetBalance operation.
	 */
	public OMElement buildAddressVerify(Operation operation) {

		log.debug("Start building payload for AddressVerify operation");

		OMElement addressVerifyReqElem = factory.createOMElement(
				"AddressVerifyReq", urnNS);
		OMElement addressVerifyRequestElem = addChild(addressVerifyReqElem,
				urnNS, "AddressVerifyRequest", (String[]) null);
		addChild(addressVerifyRequestElem, urn1NS, "DetailLevel", "DetailLevel");
		addChild(addressVerifyRequestElem, urn1NS, "ErrorLanguage",
				"ErrorLanguage");
		addChild(addressVerifyRequestElem, urn1NS, "Version", "Version");
		addChild(addressVerifyRequestElem, urnNS, "Email", "Email");
		addChild(addressVerifyRequestElem, urnNS, "Street", "Street");
		addChild(addressVerifyRequestElem, urnNS, "Zip", "Zip");

		log.debug("End building payload for AddressVerify operation");

		return addressVerifyReqElem;
	}

	/**
	 * @param parent
	 *            the parent element.
	 * @param childElemNS
	 *            the namespace of the child element.
	 * @param childElemName
	 *            the name of the child element.
	 * @param elemKey
	 *            the optional key used to fetch an input.
	 * @return the created child element.
	 */
	private OMElement addChild(OMElement parent, OMNamespace childElemNS,
			String childElemName, String... elemKeys) {

		OMElement child = factory.createOMElement(childElemName, childElemNS);

		if (null != elemKeys) {
			if ("Version".equals(elemKeys[0])) {
				child.setText(version);
			} else {
				child.setText(inputHandler.lookupValue(synCtx, elemKeys));
			}
		}
		parent.addChild(child);

		return child;
	}
}
