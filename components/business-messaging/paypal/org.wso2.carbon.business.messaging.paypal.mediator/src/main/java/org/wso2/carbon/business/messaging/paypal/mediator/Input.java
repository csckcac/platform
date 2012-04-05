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

package org.wso2.carbon.business.messaging.paypal.mediator;

import java.util.ArrayList;
import java.util.List;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.SynapseXPath;

/**
 * <p>
 * Specifies the properties of a particular input parameter and will be used by
 * the <code>Operation</code>.
 * </p>
 * 
 * @see org.wso2.carbon.business.messaging.paypal.mediator.Operation
 */
public class Input {

	/**
	 * <p>
	 * Holds the log4j based log for the login purposes
	 * </p>
	 */
	private static final Log log = LogFactory.getLog(Input.class);

	/**
	 * <p>
	 * Specifies the name of the parameter.
	 * </p>
	 */
	private String name;

	/**
	 * <p>
	 * Specifies the source value of the input parameter.
	 * </p>
	 */
	private String sourceValue;

	/**
	 * <p>
	 * Specifies the input's type.
	 * </p>
	 */
	private String type;

	/**
	 * <p>
	 * XPath describing the element or the attribute of the message which will
	 * be matched against the <code>source-xpath</code> to check the matching.
	 * If there is no <code>source-xpath</code> then the presence of this
	 * expression will be taken as the matching
	 * </p>
	 * 
	 * @see org.apache.synapse.util.xpath.SynapseXPath
	 */
	private SynapseXPath sourceXPath;

	/**
	 * <p>
	 * The namespace of the input element.
	 * </p>
	 */
	private String namespace;

	/**
	 * <p>
	 * The prefix of the namespace of the input element.
	 * </p>
	 */
	private String NSPrefix;

	/**
	 * Contains the set of inputs representing the type attribute value.
	 */
	private List<Input> subInputs = new ArrayList<Input>();

	/**
	 * <p>
	 * If both the <code>sourceXpath</code> and <code>type</code>='string' is
	 * provided then the evaluated string value of the <code>xpath</code> over
	 * the message will be returned.
	 * </p>
	 * 
	 * <p>
	 * If both the <code>sourceXpath</code> and <code>type</code>='xml' is
	 * provided then the evaluated xml value of the <code>xpath</code> over the
	 * message will be returned.
	 * </p>
	 * 
	 * <p>
	 * If the <code>value</code> is provided then then that string value will be
	 * returned.
	 * </p>
	 * 
	 * @param synCtx
	 *            message to be evaluated.
	 * @return the evaluated value from the <code>MessageContext</code>
	 */
	public String evaluate(MessageContext synCtx) {
		
		String sourceObjectValue = null;

		// expression is required to perform the match
		if (null != sourceXPath) {

			sourceObjectValue = sourceXPath.stringValueOf(synCtx);

			if (null == sourceObjectValue) {
				log.debug(String.format("Source String : %s evaluates to null",
						sourceXPath.toString()));
			}
		} else if (null != sourceValue) {

			sourceObjectValue = sourceValue;
		}
		return sourceObjectValue;
	}

	/**
	 * Creates an <code>OMElement</code> to represent operation
	 * 
	 * @return
	 */
	public OMElement getOMElement() {
		OMNamespace ns = null;
		if (null != namespace) {
			ns = OMAbstractFactory.getOMFactory().createOMNamespace(namespace,
					NSPrefix);
		}

		return OMAbstractFactory.getOMFactory().createOMElement(name, ns);
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the expression
	 */
	public SynapseXPath getSourceXPath() {
		return sourceXPath;
	}

	/**
	 * @param xpath
	 *            the expression to set
	 */
	public void setSourceXPath(SynapseXPath xpath) {
		this.sourceXPath = xpath;
	}

	/**
	 * @return the source value
	 */
	public String getSourceValue() {
		return sourceValue;
	}

	/**
	 * @param value
	 *            the source value to set
	 */
	public void setSourceValue(String value) {
		this.sourceValue = value;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the namespace
	 */
	public String getNamespace() {
		return namespace;
	}

	/**
	 * @param namespace
	 *            the namespace to set
	 */
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	/**
	 * @return the nSPrefix
	 */
	public String getNSPrefix() {
		return NSPrefix;
	}

	/**
	 * @param prefix
	 *            the nSPrefix to set
	 */
	public void setNSPrefix(String prefix) {
		NSPrefix = prefix;
	}

	/**
	 * @return the subInputs
	 */
	public List<Input> getSubInputs() {
		return subInputs;
	}

	/**
	 * @param subInputs
	 *            the subInputs to set
	 */
	public void setSubInputs(List<Input> subInputs) {
		this.subInputs = subInputs;
	}

	//
	// /**
	// * Logs the exception and wraps the source message into a
	// * <code>SynapseException</code> exception.
	// *
	// * @param msg
	// * the source message
	// * @param msgContext
	// * the message context
	// */
	// private void handleException(String msg, Exception e,
	// MessageContext msgContext) {
	// log.error(msg, e);
	// if (msgContext.getServiceLog() != null) {
	// msgContext.getServiceLog().error(msg, e);
	// }
	// throw new SynapseException(msg, e);
	// }
}
