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

import org.apache.ws.secpolicy.model.*;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.AsymmetricTokenPropertyFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;
import org.wso2.carbon.policybuilder.ui.internal.services.ElementReader;

import javax.xml.namespace.QName;
import java.util.ArrayList;

import org.wso2.carbon.policybuilder.ui.internal.assertions.Consts;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Dec 2, 2008
 * Time: 4:22:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class AsymmetricTokenBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(AsymmetricTokenBehavior.class);
	private int tokenType;
	private boolean hasSignature = false;
	private boolean hasEncryption = false;
	private boolean hasEncryptedSignature = false;
	private boolean initiatorX509Type = false;
	private boolean initiatorTknIncluded = false;
	private boolean recipientX509Type = false;
	private boolean recipientSecurityContextType = false;
	private boolean recipientTknIncluded = false;

	private boolean hasInitiatorIssuerSerial = false;
	private boolean hasInitiatorThumbPrintRef = false;
	private boolean hasInitiatorKeyIdentifier = false;

	private boolean hasRecipientIssuerSerial = false;
	private boolean hasRecipientThumbPrintRef = false;
	private boolean hasRecipientKeyIdentifier = false;

	public AsymmetricTokenBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}


	public AsymmetricTokenBehavior(AbstractSecurityAssertion assertion) {
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
		checkTokenType();
		checkSignature();
		checkEncryptSignature();
		if (tokenType == AsymmetricTokenPropertyFactory.INITIATOR_TOKEN) {
			doEvaluateInitiatorToken(e);
		} else if (tokenType == AsymmetricTokenPropertyFactory.RECIPIENT_TOKEN) {
			doEvaluateRecipientToken(e);
		} else {
			this.isBehaviorCompleted = false;
		}
	}

	private void checkTokenType() {
		if (this.assertion instanceof InitiatorToken) {
			this.tokenType = AsymmetricTokenPropertyFactory.INITIATOR_TOKEN;
		} else if (this.assertion instanceof RecipientToken) {
			this.tokenType = AsymmetricTokenPropertyFactory.RECIPIENT_TOKEN;
		}
	}


	public void init() {
		this.msgProp = new MessageProperty(new AsymmetricTokenPropertyFactory());
		//To change body of implemented methods use File | Settings | File Templates.
	}

	public void doEvaluateInitiatorToken(OMElement e) {
		OMElement current;
		String elementName;
		boolean hasBinaryST = false;
		OMElement tempBST = null;
		OMElement tempSig = null;
		String sigRefURI = "-0";
		ArrayList binarySTURI = new ArrayList();
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {
				if (elementName.equals((String) msgProp.getProperties(AsymmetricTokenPropertyFactory.K_Signature))) {
					tempSig = current;
					ElementReader signatureReader = new ElementReader(current);
					while (signatureReader.next()) {
						OMElement tempSigElement = signatureReader.getCurrentElement();
						String elName = signatureReader.getCurrentElementName();
						if (elName.equals((String) msgProp.getProperties(AsymmetricTokenPropertyFactory.K_Ref))) {
							sigRefURI = tempSigElement.getAttributeValue(new QName("URI"));
						}
					}
				}
				if (((ArrayList) msgProp.getProperties(AsymmetricTokenPropertyFactory.K_SecurityToken)).contains(elementName)) {
					hasBinaryST = true;
					if (log.isDebugEnabled()) {
						log.debug("a Security Token Found");
					}
					//System.out.println("a Security Token Found");
					tempBST = current;
					binarySTURI.add("#" + current.getAttributeValue(new QName(Consts.WS_UTILITY_NAMESPACE, "Id")));
				}
			}
		}
		if (hasSignature) {
			this.isBehaviorCompleted = true;
			if (hasBinaryST) {
				if (binarySTURI.contains(sigRefURI)) {
					initiatorTknIncluded = true;
					if (log.isDebugEnabled()) {
						log.debug("Initiator Security Token included");
					}
					//System.out.println("Initiator Security Token included");
					TokenBehavior tknBehavior = new TokenBehavior();
					tknBehavior.evaluate(tempBST);
					initiatorX509Type = tknBehavior.hasX509Certificate();
					hasInitiatorIssuerSerial = tknBehavior.hasIssuerSerial();
					hasInitiatorKeyIdentifier = tknBehavior.hasKeyIdentifier();
					hasInitiatorThumbPrintRef = tknBehavior.hasThumbPrintRef();
				} else {
					TokenBehavior tknBehavior = new TokenBehavior();
					tknBehavior.evaluate(tempSig);
					initiatorX509Type = tknBehavior.hasX509Certificate();
					hasInitiatorIssuerSerial = tknBehavior.hasIssuerSerial();
					hasInitiatorKeyIdentifier = tknBehavior.hasKeyIdentifier();
					hasInitiatorThumbPrintRef = tknBehavior.hasThumbPrintRef();
				}
			} else {
				TokenBehavior tknBehavior = new TokenBehavior();
				tknBehavior.evaluate(tempSig);
				initiatorX509Type = tknBehavior.hasX509Certificate();
				hasInitiatorIssuerSerial = tknBehavior.hasIssuerSerial();
				hasInitiatorKeyIdentifier = tknBehavior.hasKeyIdentifier();
				hasInitiatorThumbPrintRef = tknBehavior.hasThumbPrintRef();
			}
		} else if (hasEncryptedSignature) {
			this.isBehaviorCompleted = true;
			if (hasBinaryST) {
				initiatorX509Type = true;
				initiatorTknIncluded = true;
				hasInitiatorIssuerSerial = false;
				hasInitiatorKeyIdentifier = false;
				hasInitiatorThumbPrintRef = false;
			} else {
				initiatorX509Type = true;
				initiatorTknIncluded = false;
				hasInitiatorIssuerSerial = false;
				hasInitiatorKeyIdentifier = true;
				hasInitiatorThumbPrintRef = true;
			}
		} else {
			//default behavior
			if (log.isDebugEnabled()) {
				log.debug("Default Initiator Token enabled...");
			}
			//System.out.println("Default Initiator Token enabled...");
			this.isBehaviorCompleted = true;
			initiatorX509Type = true;
			initiatorTknIncluded = false;
		}
		doInitiatorAssertionLoad(isBehaviorCompleted);
	}


	public void doEvaluateRecipientToken(OMElement e) {
		OMElement current;
		String elementName;
		boolean hasBinaryST = false;
		OMElement tempBST = null;
		OMElement tempEnc = null;
		String encRefURI = "-0";
		ArrayList binarySTURI = new ArrayList();
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {
				if (elementName.equals((String) msgProp.getProperties(AsymmetricTokenPropertyFactory.K_EncryptKey))) {
					tempEnc = current;
					ElementReader encryptReader = new ElementReader(current);
					while (encryptReader.next()) {
						OMElement tempEncElement = encryptReader.getCurrentElement();
						String elName = encryptReader.getCurrentElementName();
						if (elName.equals((String) msgProp.getProperties(AsymmetricTokenPropertyFactory.K_Ref))) {
							encRefURI = tempEncElement.getAttributeValue(new QName("URI"));
						}
					}
				}
				if (((ArrayList) msgProp.getProperties(AsymmetricTokenPropertyFactory.K_SecurityToken)).contains(elementName)) {
					hasBinaryST = true;
					tempBST = current;
					binarySTURI.add("#" + current.getAttributeValue(new QName(Consts.WS_UTILITY_NAMESPACE, "Id")));
				}
			}
		}
		if (hasEncryption) {
			this.isBehaviorCompleted = true;
			if (hasBinaryST) {
				if (binarySTURI.contains(encRefURI)) {
					recipientTknIncluded = true;
					if (log.isDebugEnabled()) {
						log.debug("Recipient Security Token included");
					}
					//System.out.println("Recipient Security Token included");
					TokenBehavior tknBehavior = new TokenBehavior();
					tknBehavior.evaluate(tempBST);
					recipientX509Type = tknBehavior.hasX509Certificate();
					recipientSecurityContextType = tknBehavior.hasSecurityContextToken();
					hasRecipientIssuerSerial = tknBehavior.hasIssuerSerial();
					hasRecipientKeyIdentifier = tknBehavior.hasKeyIdentifier();
					hasRecipientThumbPrintRef = tknBehavior.hasThumbPrintRef();
				} else {
					TokenBehavior tknBehavior = new TokenBehavior();
					if (log.isDebugEnabled()) {
						log.debug("Recipient Security not included Never");
					}
					//System.out.println("Recipient Security not included Never");
					tknBehavior.evaluate(tempEnc);
					recipientX509Type = tknBehavior.hasX509Certificate();
					recipientSecurityContextType = tknBehavior.hasSecurityContextToken();
					hasRecipientIssuerSerial = tknBehavior.hasIssuerSerial();
					hasRecipientKeyIdentifier = tknBehavior.hasKeyIdentifier();
					hasRecipientThumbPrintRef = tknBehavior.hasThumbPrintRef();
				}
			} else {
				TokenBehavior tknBehavior = new TokenBehavior();
				if (log.isDebugEnabled()) {
					log.debug("Recipient Security not included Never");
				}
				//System.out.println("Recipient Security not included Never");
				tknBehavior.evaluate(tempEnc);
				recipientX509Type = tknBehavior.hasX509Certificate();
				recipientSecurityContextType = tknBehavior.hasSecurityContextToken();
				hasRecipientIssuerSerial = tknBehavior.hasIssuerSerial();
				hasRecipientKeyIdentifier = tknBehavior.hasKeyIdentifier();
				hasRecipientThumbPrintRef = tknBehavior.hasThumbPrintRef();
			}
		} else {
			//default behavior
			if (log.isDebugEnabled()) {
				log.debug("Default Recipient Token enabled...");
			}
			//System.out.println("Default Recipient Token enabled...");
			this.isBehaviorCompleted = true;
			recipientX509Type = true;
			recipientTknIncluded = false;
		}
		doRecipientAssertionLoad(isBehaviorCompleted);
	}

	public void doInitiatorAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true && this.assertion != null) {
			if (log.isDebugEnabled()) {
				log.debug("Security Initiator Token enabled...");
			}
			//System.out.println("Security Initiator Token enabled...");

			// if(((ProtectionToken)this.assertion ).getClass()==ProtectionToken.class){
			if (this.assertion instanceof InitiatorToken) {
				InitiatorToken tempPToken = (InitiatorToken) this.assertion;
				if (initiatorX509Type) {
					X509Token x509Tkn = new X509Token(11);
					tempPToken.setToken(x509Tkn);
					if (log.isDebugEnabled()) {
						log.debug("X509 Initiator Token found...");
					}
					// System.out.println("X509 Initiator Token found...");
					if (initiatorTknIncluded) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Initiator Token inluded..");
						}
						// System.out.println("X509 Initiator Token inluded..");
						x509Tkn.setInclusion(3);
					} else {
						if (log.isDebugEnabled()) {
							log.debug("X509 Initiator Token inluded Never..");
						}
						//  System.out.println("X509 Initiator Token inluded Never..");
						x509Tkn.setInclusion(1);
					}
					x509Tkn.setRequireThumbprintReference(hasInitiatorThumbPrintRef);
					if (hasInitiatorThumbPrintRef) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Initiator Thumbprint");
						}
						//  System.out.println("X509 Initiator Thumbprint");
					}
					x509Tkn.setRequireIssuerSerialReference(hasInitiatorIssuerSerial);
					if (hasInitiatorIssuerSerial) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Initiator Issuer serial");
						}
						// System.out.println("X509 Initiator Issuer serial");
					}
					x509Tkn.setRequireKeyIdentifierReference(hasInitiatorKeyIdentifier);
					if (hasInitiatorKeyIdentifier) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Initiator key identifier");
						}
						// System.out.println("X509 Initiator key identifier");
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Context Token found...");
					}
					// System.out.println("Context Token found...");
				}
			}
		}
		handleSuccessor(this.root);
	}


	public void doRecipientAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true && this.assertion != null) {
			if (log.isDebugEnabled()) {
				log.debug("Security Recipient Token enabled...");
			}
			//System.out.println("Security Recipient Token enabled...");

			// if(((ProtectionToken)this.assertion ).getClass()==ProtectionToken.class){
			if (this.assertion instanceof RecipientToken) {
				RecipientToken tempPToken = (RecipientToken) this.assertion;
				if (recipientX509Type) {
					X509Token x509Tkn = new X509Token(11);
					tempPToken.setToken(x509Tkn);
					if (log.isDebugEnabled()) {
						log.debug("X509 Recipient Token found...");
					}
					// System.out.println("X509 Recipient Token found...");
					if (recipientTknIncluded) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Recipient Token inluded..");
						}
						// System.out.println("X509 Recipient Token inluded..");
						x509Tkn.setInclusion(3);
					} else {
						if (log.isDebugEnabled()) {
							log.debug("X509 Recipient Token inluded Never...");
						}
						//  System.out.println("X509 Recipient Token inluded Never..");
						x509Tkn.setInclusion(1);
					}
					x509Tkn.setRequireThumbprintReference(hasRecipientThumbPrintRef);
					if (hasRecipientThumbPrintRef) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Recipient Thumbprint");
						}
						// System.out.println("X509 Recipient Thumbprint");
					}
					x509Tkn.setRequireIssuerSerialReference(hasRecipientIssuerSerial);
					if (hasRecipientIssuerSerial) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Recipient Issuer serial");
						}
						//System.out.println("X509 Recipient Issuer serial");
					}
					x509Tkn.setRequireKeyIdentifierReference(hasRecipientKeyIdentifier);
					if (hasRecipientKeyIdentifier) {
						if (log.isDebugEnabled()) {
							log.debug("X509 Recipient key identifier");
						}
						//  System.out.println("X509 Recipient key identifier");
					}
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Context Token found...");
					}
					// System.out.println("Context Token found...");
				}
			}
		}
		handleSuccessor(this.root);
	}


	public void checkEncryptSignature() {
		Boolean temp1 = context.getValue(ContextConstant.hasEncryptSignature);
		if (temp1 != null) {
			this.hasEncryptedSignature = temp1.booleanValue();
		} else {
			EncryptSignOrderBehavior encSigOrdBehavior = new EncryptSignOrderBehavior();
			encSigOrdBehavior.evaluate(this.root);
			this.hasEncryptedSignature = encSigOrdBehavior.hasSignatureEncryption();
		}
	}


	public void checkSignature() {
		Boolean temp1 = context.getValue(ContextConstant.hasSignature);
		Boolean temp2 = context.getValue(ContextConstant.hasEncryption);
		if (temp1 != null) {
			this.hasSignature = temp1.booleanValue();
			this.hasEncryption = temp2.booleanValue();
		} else {
			SymmetricProtectionBehavior symProtBehavior = new SymmetricProtectionBehavior();
			symProtBehavior.evaluate(this.root);
			this.hasSignature = symProtBehavior.hasSignature();
			this.hasEncryption = symProtBehavior.hasEncryption();
		}
	}
}
