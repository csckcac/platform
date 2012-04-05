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

import org.wso2.carbon.policybuilder.ui.internal.assertions.Consts;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;
import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.apache.axiom.om.OMElement;
import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.ws.secpolicy.model.ProtectionToken;
import org.apache.ws.secpolicy.model.X509Token;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.property.TokenPropertyFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 19, 2008
 * Time: 12:17:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class TokenBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(TokenBehavior.class);
	private boolean isX509Included = false;
	private boolean isX509TokenType = false;
	private boolean isContextTokenIncluded = false;
	private boolean isContextTokentype = false;
	//private boolean hasDerivedKeys=false;
	private boolean hasThumprintReference = false;
	private boolean hasIssuerSerial = false;
	private boolean hasKeyIdentifierRef = false;

	public TokenBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}

	public TokenBehavior(AbstractSecurityAssertion assertion) {
		super();
		this.isBehaviorCompleted = false;
		this.assertion = assertion;
		init();
	}


	public int evaluate(OMElement e) {
		super.evaluate(e);
		doEvaluate(e);
		return 0;
	}

	public void doEvaluate(OMElement e) {
		OMElement current;
		String elementName;
		isX509Included = false;
		isX509TokenType = false;
		isContextTokenIncluded = false;
		isContextTokentype = false;
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {

				//  checkAdditionalProperties(elementName);
				if (elementName.equals((String) msgProp.getProperties(TokenPropertyFactory.K_BinarySecurityToken))) {
					ArrayList valueTypes = (ArrayList) msgProp.getProperties(TokenPropertyFactory.K_ValueType);
					if (valueTypes.contains(current.getAttributeValue(new QName("ValueType")))) {
						isX509TokenType = true;
						isX509Included = true;
						checkAdditionalProperties(current.getAttributeValue(new QName("ValueType")));
					}
				} else
				if (elementName.equals((String) msgProp.getProperties(TokenPropertyFactory.K_SecurityTokenContext))) {
					isContextTokentype = true;
					isContextTokenIncluded = true;
					checkAdditionalProperties(current.getAttributeValue(new QName("ValueType")));
				} else if (elementName.equals((String) msgProp.getProperties(TokenPropertyFactory.K_EncryptKey))) {
					checkProperties(current);
				} else if (elementName.equals((String) msgProp.getProperties(TokenPropertyFactory.K_Signature))) {
					checkProperties(current);
				} else {
				}
			}
		}
		if (!this.isContextTokentype && !this.isX509TokenType) {
			this.isBehaviorCompleted = false;
		} else {
			this.isBehaviorCompleted = true;
		}
		doAssertionLoad(isBehaviorCompleted);
	}

	public boolean hasX509Certificate() {
		return isX509TokenType;
	}

	public boolean hasSecurityContextToken() {
		return isContextTokentype;
	}


	public boolean isX509CertificateIncluded() {
		return isX509Included;
	}

	public boolean isSecurityContextTokenIncluded() {
		return isContextTokenIncluded;
	}


	public boolean isSecurityTokenEnabled() {
		return isBehaviorCompleted;
	}

	public boolean hasThumbPrintRef() {
		return hasThumprintReference;
	}

	public boolean hasIssuerSerial() {
		return hasIssuerSerial;
	}

	public boolean hasKeyIdentifier() {
		return hasKeyIdentifierRef;
	}

	public void checkProperties(OMElement current) {
		Stack elementsENCKey = new Stack();
		elementsENCKey.push(current);
		while (!elementsENCKey.isEmpty()) {
			OMElement tempENC = (OMElement) elementsENCKey.pop();
			String tempENCName = tempENC.getQName().toString();
			checkAdditionalProperties(tempENCName);
			if (tempENCName.equals((String) msgProp.getProperties(TokenPropertyFactory.K_KeyIdentifier))) {
				//checkAdditionalProperties(tempENCName);
				ArrayList valueTypes = (ArrayList) msgProp.getProperties(TokenPropertyFactory.K_X509RefValueType);
				ArrayList valueTypes2 = (ArrayList) msgProp.getProperties(TokenPropertyFactory.K_X509DirectValueType);
				if (valueTypes.contains(tempENC.getAttributeValue(new QName("ValueType")))) {
					isX509TokenType = true;
					isX509Included = false;
					checkAdditionalProperties(tempENC.getAttributeValue(new QName("ValueType")));
				} else if (valueTypes2.contains(tempENC.getAttributeValue(new QName("ValueType")))) {
					isX509TokenType = true;
					isX509Included = true;
					checkAdditionalProperties(tempENC.getAttributeValue(new QName("ValueType")));
				}
			} else if (((ArrayList) msgProp.getProperties(TokenPropertyFactory.K_X509)).contains(tempENCName)) {
				isX509TokenType = true;
				isX509Included = false;
				checkAdditionalProperties(tempENCName);
			}
			Iterator tempENCChildren = tempENC.getChildElements();
			while (tempENCChildren.hasNext()) {
				elementsENCKey.push(tempENCChildren.next());
			}
		}
	}


	public void checkAdditionalProperties(ArrayList values) {
		if (values != null) {
			if (values.contains(TokenPropertyFactory.K_ThumbPrint)) {
				this.hasThumprintReference = true;
			}
			if (values.contains(TokenPropertyFactory.K_KeyIdentifier)) {
				this.hasKeyIdentifierRef = true;
			}
			if (values.contains(TokenPropertyFactory.K_IssuerSerial)) {
				this.hasIssuerSerial = true;
			}
		}
	}

	public void checkAdditionalProperties(String elementName) {
		if (elementName != null) {
			if (elementName.equals(Consts.ThumbPrint_VALUE)) {
				this.hasThumprintReference = true;
			}
			if (elementName.equals(Consts.KeyIdentifier_VALUE)) {
				this.hasKeyIdentifierRef = true;
			}
			if (elementName.equals(Consts.IssuerSerial_VALUE)) {
				this.hasIssuerSerial = true;
			}
		}
	}


	public void doAssertionLoad(boolean behaviorCompleted) {
		if (isBehaviorCompleted) {
			setContext();
		}
		if (behaviorCompleted == true && this.assertion != null) {
			if (log.isDebugEnabled()) {
				log.debug("Security Token enabled...");
			}
			//System.out.println("Security Token enabled...");

			// if(((ProtectionToken)this.assertion ).getClass()==ProtectionToken.class){
			if (this.assertion instanceof ProtectionToken) {
				ProtectionToken tempPToken = (ProtectionToken) this.assertion;
				if (isX509TokenType) {
					X509Token x509Tkn = new X509Token(11);
					tempPToken.setToken(x509Tkn);
					if (log.isDebugEnabled()) {
						log.debug("X509 Token found...");
					}
					//System.out.println("X509 Token found...");
					if (isX509Included) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Token inluded..");
						}
						// System.out.println("X509 Token inluded..");
						x509Tkn.setInclusion(3);
					} else {
						if (log.isDebugEnabled()) {
							log.debug("X509 No Token inluded Never..");
						}
						//System.out.println("X509 No Token inluded Never..");
						x509Tkn.setInclusion(1);
					}
					x509Tkn.setRequireThumbprintReference(hasThumprintReference);
					if (hasThumprintReference) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Thumbprint");
						}
						// System.out.println("X509 Thumbprint");
					}
					x509Tkn.setRequireIssuerSerialReference(hasIssuerSerial);
					if (hasIssuerSerial) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Issuer serial");
						}
						//System.out.println("X509 Issuer serial");
					}
					x509Tkn.setRequireKeyIdentifierReference(hasKeyIdentifierRef);
					if (hasKeyIdentifierRef) {
						if (log.isDebugEnabled()) {
							log.debug("X509 key identifier");
						}
						// System.out.println("X509 key identifier");
					}
				} else if (isContextTokentype) {
					if (log.isDebugEnabled()) {
						log.debug("Context Token found...");
					}
					//System.out.println("Context Token found...");
				}

				/*
								Iterator behaviors = nestedBehaviors.iterator();
								while (behaviors.hasNext()) {
									PolicyBehavior temp = (PolicyBehavior) (behaviors.next());
									temp.evaluate(this.root);

								}
								*/
			}
		}
		handleSuccessor(this.root);
	}

	public void setContext() {
		context.setValue(ContextConstant.isX509Included, isX509CertificateIncluded());
		context.setValue(ContextConstant.isX509TokenType, hasX509Certificate());
		context.setValue(ContextConstant.isContextTokenIncluded, isSecurityContextTokenIncluded());
		context.setValue(ContextConstant.isContextTokentype, hasSecurityContextToken());
		context.setValue(ContextConstant.hasThumprintReference, hasThumbPrintRef());
		context.setValue(ContextConstant.hasKeyIdentifierRef, hasKeyIdentifier());
		context.setValue(ContextConstant.hasIssuerSerial, hasIssuerSerial());
		context.setValue(ContextConstant.isSecurityTokenEnabled, isSecurityTokenEnabled());
	}


	public void init() {
		//To change body of implemented methods use File | Settings | File Templates.
		this.msgProp = new MessageProperty(new TokenPropertyFactory());
	}

	//for Testing Purposes
	public static void main(String[] args) {
		X509Token xp = new X509Token(11);
		XMLStreamWriter writer;
		try {
			writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			xp.setInclusion(3);
			xp.setDerivedKeys(true);
			xp.serialize(writer);
			writer.flush();
		} catch (XMLStreamException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}
