/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.policybuilder.ui.internal.engine;

import org.apache.ws.secpolicy.model.SignedEncryptedParts;
import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;

import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.property.SignedPartsPropertyFactory;

import java.util.ArrayList;
import java.util.Iterator;

import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;
import org.wso2.carbon.policybuilder.ui.internal.services.ElementReader;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLFileReader;
import org.wso2.carbon.policybuilder.ui.internal.assertions.Consts;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Dec 4, 2008
 * Time: 2:12:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class SignedPartsBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(SignedPartsBehavior.class);
	private boolean hasEncryptSignature = false;
	private boolean hasSignedBody = false;

	public SignedPartsBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}


	public SignedPartsBehavior(AbstractSecurityAssertion assertion) {
		super();
		this.isBehaviorCompleted = false;
		this.assertion = assertion;
		init();
	}


	public int evaluate(OMElement e) {
		super.evaluate(e);
		doEvaluate(e);
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}


	public void doEvaluate(OMElement e) {
		OMElement current;
		String elementName;
		ArrayList uriList = new ArrayList();
		ArrayList refList = new ArrayList();
		checkEncryptSignature();
		if (!hasEncryptSignature) {
			while (!isEmptyList()) {
				current = next();
				elementName = current.getQName().toString();
				if (this.msgProp.contains(elementName)) {
					if (elementName.equals((String) msgProp.getProperties(SignedPartsPropertyFactory.K_Body))) {
						ElementReader bodyReader = new ElementReader(current);
						String wsuId;
						while (bodyReader.next()) {
							wsuId = bodyReader.getCurrentElement().getAttributeValue(new QName(Consts.WS_UTILITY_NAMESPACE, "Id"));
							if (wsuId != null) {
								uriList.add("#" + wsuId);
								wsuId = null;
							}
						}
					} else
					if (elementName.equals((String) msgProp.getProperties(SignedPartsPropertyFactory.K_Signature))) {
						ElementReader signatureReader = new ElementReader(current);
						String elName, ref;
						while (signatureReader.next()) {
							elName = signatureReader.getCurrentElementName();
							if (elName.equals((String) msgProp.getProperties(SignedPartsPropertyFactory.K_SignRef))) {
								ref = signatureReader.getCurrentElement().getAttributeValue(new QName("URI"));
								if (ref != null) {
									refList.add(ref);
									ref = null;
								}
							}
						}
					}
				}
			}
			this.hasSignedBody = checkEncryptBody(uriList, refList);
			this.isBehaviorCompleted = this.hasSignedBody;
		} else {
			//assume body has been signed
			this.hasSignedBody = true;
			this.isBehaviorCompleted = this.hasSignedBody;
			if (log.isDebugEnabled()) {
				log.debug("Soap Body signed assumed");
			}
			//System.out.println("Soap Body signed assumed");
		}
		setContext();
		doAssertionLoad(isBehaviorCompleted);
	}


	public void checkEncryptSignature() {
		Boolean temp = context.getValue(ContextConstant.hasEncryptSignature);
		if (temp != null) {
			this.hasEncryptSignature = temp.booleanValue();
		} else {
			EncryptSignOrderBehavior orderBehavior = new EncryptSignOrderBehavior();
			orderBehavior.evaluate(this.root);
			this.hasEncryptSignature = orderBehavior.hasSignatureEncryption();
		}
	}

	private boolean checkEncryptBody(ArrayList uriList, ArrayList refList) {
		if (!refList.isEmpty() && !uriList.isEmpty()) {
			Iterator refListIterator = refList.iterator();
			while (refListIterator.hasNext()) {
				String ref = (String) refListIterator.next();
				if (uriList.contains(ref)) {
					if (log.isDebugEnabled()) {
						log.debug("Has Soap Body signed");
					}
					// System.out.println("Has Soap Body signed");
					return true;
				}
			}
		}
		return false;
	}


	public void init() {
		//To change body of implemented methods use File | Settings | File Templates.
		this.msgProp = new MessageProperty(new SignedPartsPropertyFactory());
	}

	public boolean hasSignedParts() {
		return hasSignedBody;
	}


	public void setContext() {
		context.setValue(ContextConstant.hasSignedParts, hasSignedParts());
	}


	public void doAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true && this.assertion != null) {
			if (this.assertion instanceof SignedEncryptedParts) {
				SignedEncryptedParts seParts = (SignedEncryptedParts) this.assertion;
				seParts.setBody(true);
			}
		}
		handleSuccessor(this.root);
	}

	//for Testing Purposes
	public static void main(String[] args) {
		try {
			XMLFileReader xr = new XMLFileReader("/home/usw/my.xml");
			// xr.setFilePath("/home/usw/my.xml");
			OMElement root = xr.getDocumentRoot();
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			//SignedEncryptedParts sp = new SignedEncryptedParts(true,11);
			SignedEncryptedParts sp = new SignedEncryptedParts(true, 11);
			new SignedPartsBehavior(sp).evaluate(root);
			sp.serialize(writer);
			writer.flush();
			writer.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}
