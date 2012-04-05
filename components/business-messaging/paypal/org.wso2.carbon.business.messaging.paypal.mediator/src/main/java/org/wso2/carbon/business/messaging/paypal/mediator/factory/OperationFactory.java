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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.wso2.carbon.business.messaging.paypal.mediator.Input;
import org.wso2.carbon.business.messaging.paypal.mediator.Operation;
import org.wso2.carbon.business.messaging.paypal.mediator.handler.ConfigHandler;

public class OperationFactory {

	/**
	 * <p>
	 * Holds the log4j based log for the login purposes
	 * </p>
	 */
	private static final Log log = LogFactory.getLog(OperationFactory.class);

	/** Holds the reference for the type attribute. */
	private static QName ATTR_VERSION = new QName("version");

	/** Holds the reference for the type attribute. */
	private static QName ATTR_CURRENCY = new QName("currency");

	/** Holds the reference for the type attribute. */
	private static QName ATTR_TYPE = new QName("type");

	/** Holds the reference for the name attribute. */
	private static QName ATTR_NAME = new QName("name");

	/** Holds the reference for the inputs element. */
	private static QName ELEM_INPUTS = new QName("inputs");

	/** Holds the reference for the input element. */
	private static QName ELEM_INPUT = new QName("input");

	/** Reference of the singleton instance. */
	private static OperationFactory operationFactory;

	/**
	 * private constructor to disable the creation of the OperationFactory
	 * instances
	 */
	private OperationFactory() {
	}

	/** Returns the singleton instance. */
	public synchronized static OperationFactory getInstance() {
		if (null == operationFactory) {
			operationFactory = new OperationFactory();
		}
		return operationFactory;
	}

	/**
	 * Creates an Operation instance to represent the operation name.
	 * 
	 * @param operationName
	 *            - the operation name.
	 * @return the Operation instance.
	 */
	public Operation create(String operationName) {

		Operation operation = new Operation();

		try {
			OMElement operationOMElement = ConfigHandler.getInstance().parse(
					operationName);
			operation.setVersion(operationOMElement
					.getAttributeValue(ATTR_VERSION));
			operation.setCurrency(operationOMElement
					.getAttributeValue(ATTR_CURRENCY));
			operation.setInputs(createInputs(operationOMElement));
		} catch (Exception e) {
			handleException(
					"An error occured when parsing the configuration file", e);
		}
		return operation;
	}

	/**
	 * Creates collection of Input's.
	 * 
	 * @param inputsElement
	 *            the OMElement representing the collection of inputs.
	 * @return the collection of Input's.
	 */
	@SuppressWarnings("unchecked")
	private List<Input> createInputs(OMElement inputsElement) {

		List<Input> inputs = new ArrayList<Input>();

		if (null != inputsElement.getFirstChildWithName(ELEM_INPUTS)) {

			for (Iterator<OMElement> itr = inputsElement.getFirstChildWithName(
					ELEM_INPUTS).getChildrenWithName(ELEM_INPUT); itr.hasNext();) {
				inputs.add(createInput(itr.next()));
			}
		}
		return inputs;
	}

	/**
	 * Creates an instance of Input.
	 * 
	 * @param inputElement
	 *            the OMElement representing an instance of input.
	 * @return an instance of Input.
	 */
	@SuppressWarnings("unchecked")
	private Input createInput(OMElement inputElement) {

		Input input = new Input();
		OMAttribute typeAttr = inputElement.getAttribute(ATTR_TYPE);
		if (null == typeAttr || null == typeAttr.getAttributeValue()) {
			input.setName(inputElement.getAttribute(ATTR_NAME)
					.getAttributeValue());
		} else {
			input.setType(typeAttr.getAttributeValue());
			for (Iterator<OMElement> itr = inputElement
					.getChildrenWithName(ELEM_INPUT); itr.hasNext();) {
				input.getSubInputs().add(createInput(itr.next()));
			}
		}

		return input;
	}

	/**
	 * Logs the exception and wraps the source message into a
	 * <code>SynapseException</code> exception.
	 * 
	 * @param msg
	 *            the source message
	 * @param e
	 *            the exception
	 */
	private void handleException(String msg, Exception e) {
		log.error(msg, e);
		throw new SynapseException(msg, e);
	}
}
