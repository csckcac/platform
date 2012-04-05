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

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.util.xpath.SynapseXPath;

/**
 * <p>
 * Specifies the properties of a particular output parameter and will be used by
 * the <code>Operation</code>.
 * </p>
 * 
 * 
 * @see org.wso2.carbon.business.messaging.paypal.mediator.Parameter
 * @see org.wso2.carbon.business.messaging.paypal.mediator.Operation
 */
public class Output {

	/**
	 * <p>
	 * Holds the log4j based log for the login purposes
	 * </p>
	 */
	private static final Log log = LogFactory.getLog(Output.class);
	/**
	 * <p>
	 * XPath describing the element or the attribute of the message which will
	 * be matched against the <code>xpath</code> to check the matching from the
	 * response of an operation.
	 * </p>
	 * 
	 * @see org.apache.synapse.util.xpath.SynapseXPath
	 */
	private SynapseXPath sourceXPath;

	/**
	 * <p>
	 * XPath describing the element or the attribute of the message which will
	 * be matched against the <code>xpath</code> to check the matching from the
	 * <code>MessageContext</code>.
	 * </p>
	 * 
	 * @see org.apache.synapse.util.xpath.SynapseXPath
	 */
	private SynapseXPath targetXPath;

	/**
	 * Sets the response as a property to the <code>MessageContext</code> using
	 * this target key.
	 */
	private String targetKey;

	/**
	 * 
	 * @param msgCxt
	 * @return
	 */
	public OMElement evaluate(MessageContext msgCxt) {

		return evaluate(msgCxt, targetXPath);
	}

	/**
	 * @param object
	 * @return
	 */
	public OMElement evaluate(OMElement element) {
		return evaluate(element, sourceXPath);
	}

	/**
	 * @param object
	 * @param xpath
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private OMElement evaluate(Object object, SynapseXPath xpath) {

		Object sourceObject = null;
		OMElement node = null;
		// expression is required to perform the match
		if (null != xpath) {

			try {
				sourceObject = xpath.evaluate(object);

				if (sourceObject != null && sourceObject instanceof OMElement) {

					node = (OMElement) sourceObject;
				} else if (sourceObject != null && sourceObject instanceof List
						&& !((List) sourceObject).isEmpty()) {

					node = (OMElement) ((List) sourceObject).get(0);
				} else {
					handleException("Evaluation of target XPath expression : "
							+ xpath.toString() + " did not yeild an OMNode");
				}
			} catch (Exception e) {
				handleException("Error evaluating XPath expression : "
						+ xpath.toString(), e);
			}

		}
		return node;
	}

	/**
	 * @return the sourceXpath
	 */
	public SynapseXPath getSourceXPath() {
		return sourceXPath;
	}

	/**
	 * @param sourceXpath
	 *            the sourceXpath to set
	 */
	public void setSourceXPath(SynapseXPath sourceXpath) {
		this.sourceXPath = sourceXpath;
	}

	/**
	 * @return the targetXpath
	 */
	public SynapseXPath getTargetXPath() {
		return targetXPath;
	}

	/**
	 * @param targetXpath
	 *            the targetXpath to set
	 */
	public void setTargetXPath(SynapseXPath targetXpath) {
		this.targetXPath = targetXpath;
	}

	/**
	 * @return the targetkey
	 */
	public String getTargetKey() {
		return targetKey;
	}

	/**
	 * @param targetkey
	 *            the targetkey to set
	 */
	public void setTargetKey(String targetkey) {
		this.targetKey = targetkey;
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

	/**
	 * Logs the exception and wraps the source message into a
	 * <code>SynapseException</code> exception.
	 * 
	 * @param msg
	 *            the source message
	 */
	private void handleException(String msg) {
		log.error(msg);
		throw new SynapseException(msg);
	}
}
