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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.policybuilder.ui.internal.property.TransportPropertyFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 7, 2008
 * Time: 9:40:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class TransportBindingBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(TransportBindingBehavior.class);
	private boolean hasSignature;
	private boolean hasEncryption;

	public TransportBindingBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}


	public TransportBindingBehavior(AbstractSecurityAssertion assertion) {
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
		boolean hasHttps = false;
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {
				if (elementName.equals((String) msgProp.getProperties(TransportPropertyFactory.K_WsaTo))) {
					String text = current.getText().toLowerCase();
					CharSequence s = new String("https://");
					if (text != null && text.contains(s)) {
						hasHttps = true;
						break;
					}
				}
			}
		}
		checkProperties();
		if (hasHttps && !hasEncryption && !hasSignature) {
			this.isBehaviorCompleted = true;
			if (log.isDebugEnabled()) {
				log.debug("Transport Binding");
			}
			//System.out.println("Transport Binding");
		}
		doAssertionLoad(isBehaviorCompleted);
	}


	public boolean isTrasport() {
		return isBehaviorCompleted;
	}


	public void init() {
		this.msgProp = new MessageProperty(new TransportPropertyFactory());
	}

	public void doAssertionLoad(boolean behaviorCompleted) {
	}


	private void checkProperties() {
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
