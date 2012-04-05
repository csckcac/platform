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

import org.apache.axiom.om.OMElement;
import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.ws.secpolicy.model.AsymmetricBinding;
import org.apache.ws.secpolicy.model.SymmetricBinding;
import org.apache.ws.secpolicy.SPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.EncryptSignOrderPropertyFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.services.ElementReader;

import java.util.Stack;

import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 26, 2008
 * Time: 4:50:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class EncryptSignOrderBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(EncryptSignOrderBehavior.class);
	private boolean hasSignature = false;
	private boolean hasEncryption = false;
	private boolean hasEncryptSignature = false;
	private boolean hasEncryptBeforeSign = false;
	private boolean hasEndorsingSignature = false;

	public EncryptSignOrderBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}


	public EncryptSignOrderBehavior(AbstractSecurityAssertion assertion) {
		super();
		this.isBehaviorCompleted = false;
		this.assertion = assertion;
		init();
	}


	public void init() {
		//To change body of implemented methods use File | Settings | File Templates.
		this.msgProp = new MessageProperty(new EncryptSignOrderPropertyFactory());
	}


	public int evaluate(OMElement e) {
		super.evaluate(e);
		doEvaluate(e);
		return 0;  //To change body of implemented methods use File | Settings | File Templates.
	}


	public void doEvaluate(OMElement e) {
		OMElement current;
		String elementName;
		Stack orderStack = new Stack();
		;
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {
				if (elementName.equals((String) msgProp.getProperties(EncryptSignOrderPropertyFactory.K_RefList))) {
					orderStack.push(current);
					if (log.isDebugEnabled()) {
						log.debug(elementName);
					}
					//System.out.println(elementName);
				} else
				if (elementName.equals((String) msgProp.getProperties(EncryptSignOrderPropertyFactory.K_EncData))) {
					this.hasEncryption = true;
				} else
				if (elementName.equals((String) msgProp.getProperties(EncryptSignOrderPropertyFactory.K_Signature))) {
					orderStack.push(current);
					this.hasSignature = true;
					if (log.isDebugEnabled()) {
						log.debug(elementName);
					}
					//System.out.println(elementName);
				}
			}
		}
		if (orderStack != null && !orderStack.isEmpty()) {
			if (hasSignature && hasEncryption && !hasEndorsingSignature) {
				String elName = ((OMElement) orderStack.pop()).getQName().toString();
				if (elName.equals((String) msgProp.getProperties(EncryptSignOrderPropertyFactory.K_RefList))) {
					this.isBehaviorCompleted = true;
					this.hasEncryptBeforeSign = false;
					if (log.isDebugEnabled()) {
						log.debug("Sign b4 Encrypt");
					}
					//System.out.println("Sign b4 Encrypt ");
				} else if (elName.equals((String) msgProp.getProperties(EncryptSignOrderPropertyFactory.K_Signature))) {
					this.isBehaviorCompleted = true;
					this.hasEncryptBeforeSign = true;
					if (log.isDebugEnabled()) {
						log.debug("Encrypt b4 Sign ");
					}
					//   System.out.println("Encrypt b4 Sign ");
				}
			} else if (hasEncryption) {
				OMElement currentElement;
				String elName;
				int noRefListElements = 0;
				while (!orderStack.isEmpty()) {
					currentElement = (OMElement) orderStack.pop();
					elName = currentElement.getQName().toString();
					if (elName.equals((String) msgProp.getProperties(EncryptSignOrderPropertyFactory.K_RefList))) {
						noRefListElements++;
						ElementReader refListReader = new ElementReader(currentElement);
						int noDataRefElements = 0;
						while (refListReader.next()) {
							String eName = refListReader.getCurrentElementName();

							//OMElement element = refListReader.getCurrentElement();
							if (eName.equals((String) msgProp.getProperties(EncryptSignOrderPropertyFactory.K_DataRef))) {
								noDataRefElements++;
							}
							if (noDataRefElements >= 2) {
								hasEncryptBeforeSign = false;
								hasEncryptSignature = true;
								this.isBehaviorCompleted = true;
								if (log.isDebugEnabled()) {
									log.debug("Sign b4 Encrypt : Encrypt Signature");
								}
								// System.out.println("Sign b4 Encrypt : Encrypt Signature");
								break;
							}
						}
						if (noRefListElements >= 2) {
							hasEncryptBeforeSign = true;
							hasEncryptSignature = true;
							this.isBehaviorCompleted = true;
							if (log.isDebugEnabled()) {
								log.debug("Encrypt b4 Sign : Encrypt Signature");
							}
							// System.out.println("Encrypt b4 Sign : Encrypt Signature");
						}
						if (isBehaviorCompleted == true) {
							break;
						}
					}
				}
			}
		}
		setContext();
		doAssertionLoad(isBehaviorCompleted);
	}


	public boolean hasEncryptBeforeSign() {
		return hasEncryptBeforeSign;
	}

	public boolean hasSignBeforeEncrypt() {
		return !hasEncryptBeforeSign;
	}

	public boolean isEncryptSignOrderBehaviorCompleted() {
		return isBehaviorCompleted;
	}

	public boolean hasSignatureEncryption() {
		return hasEncryptSignature;
	}

	public void doAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true && this.assertion != null) {
			if ((this.assertion instanceof SymmetricBinding)) {
				if (hasEncryptBeforeSign) {
					((SymmetricBinding) this.assertion).setProtectionOrder(SPConstants.ENCRYPT_BEFORE_SIGNING);
				} else {
					((SymmetricBinding) this.assertion).setProtectionOrder(SPConstants.SIGN_BEFORE_ENCRYPTING);
				}
				if (hasEncryptSignature) {
					((SymmetricBinding) this.assertion).setSignatureProtection(hasEncryptSignature);
				}
			} else if ((this.assertion instanceof AsymmetricBinding)) {
				if (hasEncryptBeforeSign) {
					((AsymmetricBinding) this.assertion).setProtectionOrder(SPConstants.ENCRYPT_BEFORE_SIGNING);
				} else {
					((AsymmetricBinding) this.assertion).setProtectionOrder(SPConstants.SIGN_BEFORE_ENCRYPTING);
				}
				if (hasEncryptSignature) {
					((AsymmetricBinding) this.assertion).setSignatureProtection(hasEncryptSignature);
				}
			}
		}
		handleSuccessor(this.root);
	}


	public void setContext() {
		context.setValue(ContextConstant.hasEncryptBeforeSign, hasEncryptBeforeSign());
		context.setValue(ContextConstant.hasEncryptSignature, hasSignatureEncryption());
	}
}
