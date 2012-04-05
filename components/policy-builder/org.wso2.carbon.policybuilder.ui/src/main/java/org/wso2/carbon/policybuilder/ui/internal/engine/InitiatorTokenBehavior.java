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
import org.apache.ws.secpolicy.model.SymmetricBinding;
import org.apache.ws.secpolicy.model.AsymmetricBinding;
import org.apache.ws.secpolicy.model.InitiatorToken;
import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.property.InitiatorTokenPropertyFactory;

import java.util.ArrayList;
import java.util.Iterator;

import org.wso2.carbon.policybuilder.ui.internal.services.ElementReader;
import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Dec 2, 2008
 * Time: 10:19:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class InitiatorTokenBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(InitiatorTokenBehavior.class);
	private boolean isAsymmetric = false;
	private InitiatorToken initTkn;


	public InitiatorTokenBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}


	public InitiatorTokenBehavior(AbstractSecurityAssertion assertion) {
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
		checkProperties();
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {
				if (elementName.equals((String) msgProp.getProperties(InitiatorTokenPropertyFactory.K_Signature))) {
					ElementReader signatureReader = new ElementReader(current);
					ArrayList list = (ArrayList) msgProp.getProperties(InitiatorTokenPropertyFactory.K_STR);
					String eName;
					while (signatureReader.next() && isAsymmetric) {
						eName = signatureReader.getCurrentElementName();
						if (list.contains(eName)) {
							if (log.isDebugEnabled()) {
								log.debug("Intitiator token found");
							}
							//System.out.println("Intitiator token found");
							this.isBehaviorCompleted = true;
							break;
						}
					}
					if (isBehaviorCompleted) {
						break;
					}
				}
			}
		}
		if (isAsymmetric) {
			if (log.isDebugEnabled()) {
				log.debug("Encrypted Signature ; Intitiator token assumed");
			}
			//System.out.println("Encrypted Signature ; Intitiator token assumed");
			this.isBehaviorCompleted = true;
		}
		doAssertionLoad(isBehaviorCompleted);
	}


	public boolean hasInitiatorToken() {
		return isBehaviorCompleted;
	}

	public void checkProperties() {
		Boolean temp1 = context.getValue(ContextConstant.isAsymmetric);
		if (temp1 != null) {
			this.isAsymmetric = temp1.booleanValue();
		} else {
			AsymmetricBindingBehavior asyBehavior = new AsymmetricBindingBehavior();
			asyBehavior.evaluate(this.root);
			this.isAsymmetric = asyBehavior.isAssymmetric();
		}
	}

	public void doAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true && this.assertion != null) {
			if ((this.assertion instanceof AsymmetricBinding)) {
				this.initTkn = (InitiatorToken) ((getSuccessor()).assertion);
				((AsymmetricBinding) this.assertion).setInitiatorToken(initTkn);
				handleSuccessor(this.root);
			}
		} else {
			skip(this.root);
		}
	}


	public void setContext() {
	}

	public void init() {
		this.msgProp = new MessageProperty(new InitiatorTokenPropertyFactory());
	}
}
