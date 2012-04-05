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
import org.apache.ws.secpolicy.model.SymmetricBinding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.property.ProtectionTokenPropertyFactory;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 18, 2008
 * Time: 4:35:38 PM
 * To change this template use File | Settings | File Templates.
 */
public class SymmetricProtectionBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(SymmetricProtectionBehavior.class);
	private boolean hasSignature = false;
	private boolean hasDerivedKeys = false;
	private boolean hasEncrption = false;

	private ProtectionToken prtTkn;

	public SymmetricProtectionBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}

	public SymmetricProtectionBehavior(AbstractSecurityAssertion assertion) {
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


	private void doEvaluate(OMElement e) {
		OMElement current;
		String elementName;
		boolean isUsingProtectionToken = false;
		ArrayList listDRK = new ArrayList();
		ArrayList listSGN = new ArrayList();
		ArrayList listDRKId = new ArrayList();
		ArrayList listENC = new ArrayList();
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {
				if (elementName.equals((String) msgProp.getProperties(ProtectionTokenPropertyFactory.K_DerivedKey))) {
					this.hasDerivedKeys = true;
					Stack elementsDRK = new Stack();
					elementsDRK.push(current);
					while (!elementsDRK.isEmpty()) {
						OMElement tempDRK = (OMElement) elementsDRK.pop();
						String tempDRKName = tempDRK.getQName().toString();
						if (tempDRKName.equals((String) msgProp.getProperties(ProtectionTokenPropertyFactory.K_Ref))) {
							listDRK.add(tempDRK.getAttributeValue(new QName("URI")));
						}
						if (tempDRKName.equals((String) msgProp.getProperties(ProtectionTokenPropertyFactory.K_DerivedKey))) {
							listDRKId.add("#" + tempDRK.getAttributeValue(new QName(Consts.WS_UTILITY_NAMESPACE, "Id")));
						}
						Iterator tempDRKChildren = tempDRK.getChildElements();
						while (tempDRKChildren.hasNext()) {
							elementsDRK.push(tempDRKChildren.next());
						}
					}
				} else
				if (elementName.equals((String) msgProp.getProperties(ProtectionTokenPropertyFactory.K_Signature))) {
					this.hasSignature = true;
					Stack elementsSGN = new Stack();
					elementsSGN.push(current);
					while (!elementsSGN.isEmpty()) {
						OMElement tempSGN = (OMElement) elementsSGN.pop();
						String tempSGNName = tempSGN.getQName().toString();
						if (tempSGNName.equals((String) msgProp.getProperties(ProtectionTokenPropertyFactory.K_Ref))) {
							listSGN.add(tempSGN.getAttributeValue(new QName("URI")));
						}
						Iterator tempSGNChildren = tempSGN.getChildElements();
						while (tempSGNChildren.hasNext()) {
							elementsSGN.push(tempSGNChildren.next());
						}
					}
				} else
				if (elementName.equals((String) msgProp.getProperties(ProtectionTokenPropertyFactory.K_EncData))) {
					this.hasEncrption = true;
					Stack elementsENC = new Stack();
					elementsENC.push(current);
					while (!elementsENC.isEmpty()) {
						OMElement tempENC = (OMElement) elementsENC.pop();
						String tempENCName = tempENC.getQName().toString();
						if (tempENCName.equals((String) msgProp.getProperties(ProtectionTokenPropertyFactory.K_Ref))) {
							listENC.add(tempENC.getAttributeValue(new QName("URI")));
						}
						Iterator tempENCChildren = tempENC.getChildElements();
						while (tempENCChildren.hasNext()) {
							elementsENC.push(tempENCChildren.next());
						}
					}
				}
			}
		}
		if (this.assertion != null) {
			if (this.hasEncrption == true && this.hasSignature == true && this.hasDerivedKeys == true) {
				Iterator itrDRKRef = listDRK.iterator();
				String tempRef1 = "-0";
				boolean hasSameRef = false;
				while (itrDRKRef.hasNext()) {
					String tempRef2 = (String) itrDRKRef.next();
					if (tempRef1.equals("-0")) {
						hasSameRef = false;
						tempRef1 = tempRef2;
					} else if (tempRef1.equals(tempRef2)) {
						hasSameRef = true;
						tempRef1 = tempRef2;
					} else {
						hasSameRef = false;
						break;
						//  return;
					}
				}
				if (hasSameRef == true) {
					Iterator itrDRKId = listDRKId.iterator();
					Iterator itrENC = listENC.iterator();
					Iterator itrSGN = listSGN.iterator();
					boolean hasDerivedKeyRef = false;
					;
					while (itrENC.hasNext()) {
						String refENC = (String) itrENC.next();
						hasDerivedKeyRef = false;
						while (itrDRKId.hasNext()) {
							String idDRK = (String) itrDRKId.next();
							if (idDRK.equals(refENC)) {
								hasDerivedKeyRef = true;
							}
						}
						if (hasDerivedKeyRef == false) {
							break;
						}
					}
					while (itrSGN.hasNext() && hasDerivedKeyRef == true) {
						String refSGN = (String) itrSGN.next();
						boolean hasDerivedKeyRef2 = false;
						itrDRKId = listDRKId.iterator();
						while (itrDRKId.hasNext()) {
							String idDRK = (String) itrDRKId.next();
							if (idDRK.equals(refSGN)) {
								hasDerivedKeyRef2 = true;
							}
						}
						if (hasDerivedKeyRef2 == false) {
							hasDerivedKeyRef = false;
							break;
						}
					}
					if (hasDerivedKeyRef == true) {
						this.isBehaviorCompleted = true;
					} else {
						this.isBehaviorCompleted = false;
					}
				}
			} else if (this.hasEncrption == true && this.hasSignature == false) {
				this.isBehaviorCompleted = true;
			} else if (this.hasEncrption == false && this.hasSignature == true) {
				this.isBehaviorCompleted = true;
			}
			this.doAssertionLoad(isBehaviorCompleted);
		}
		this.setContext();
	}


	public void doAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true) {
			this.setContext2();
			if (log.isDebugEnabled()) {
				log.debug("Protection Token enabled...");
			}
			//System.out.println("Protection Token enabled...");
			if (((SymmetricBinding) this.assertion).getClass() == SymmetricBinding.class) {
				this.prtTkn = (ProtectionToken) ((TokenBehavior) getSuccessor()).assertion;
				((SymmetricBinding) this.assertion).setProtectionToken(this.prtTkn);
				handleSuccessor(this.root);
			} else {
				skip(this.root);
			}
		} else {
			skip(this.root);
		}
	}


	public void setContext() {
		context.setValue(ContextConstant.hasEncryption, hasEncryption());
		context.setValue(ContextConstant.hasSignature, hasSignature());
		context.setValue(ContextConstant.hasDerivedKeys, hasDerivedKeys());
	}

	public void setContext2() {
		context.setValue(ContextConstant.isProtectionTokenEnabled, isProtectionTokenEnabled());
	}

	public boolean hasEncryption() {
		return hasEncrption;
	}

	public boolean hasSignature() {
		return hasSignature;
	}


	public boolean hasDerivedKeys() {
		return hasDerivedKeys;
	}

	public boolean isProtectionTokenEnabled() {
		return isBehaviorCompleted;
	}

	public void init() {
		this.msgProp = new MessageProperty(new ProtectionTokenPropertyFactory());
	}
}
