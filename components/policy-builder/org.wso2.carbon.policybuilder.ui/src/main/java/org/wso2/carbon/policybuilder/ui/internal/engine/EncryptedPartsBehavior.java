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

import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.ws.secpolicy.model.SignedEncryptedParts;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.property.EncryptedPartsPropertyFactory;

import org.wso2.carbon.policybuilder.ui.internal.services.ElementReader;

import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Dec 9, 2008
 * Time: 1:48:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class EncryptedPartsBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(EncryptedPartsBehavior.class);
	private boolean hasEncryptedParts = false;

	public EncryptedPartsBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}


	public EncryptedPartsBehavior(AbstractSecurityAssertion assertion) {
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
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {
				if (elementName.equals((String) msgProp.getProperties(EncryptedPartsPropertyFactory.K_Body))) {
					ElementReader bodyReader = new ElementReader(current);
					String elName;
					while (bodyReader.next()) {
						elName = bodyReader.getCurrentElementName();
						if (elName.equals((String) msgProp.getProperties(EncryptedPartsPropertyFactory.K_EncData))) {
							this.hasEncryptedParts = true;
							this.isBehaviorCompleted = true;
							if (log.isDebugEnabled()) {
								log.debug("Has Encrypted parts in Body");
							}
							//System.out.println("Has Encrypted parts in Body");
							setContext();
							doAssertionLoad(isBehaviorCompleted);
							return;
						}
					}
				}
			}
		}
		handleSuccessor(this.root);

		//
	}

	public void doAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true && this.assertion != null) {
			if (this.assertion instanceof SignedEncryptedParts) {
				SignedEncryptedParts seParts = (SignedEncryptedParts) this.assertion;
				seParts.setBody(true);
			}
		}
	}


	public boolean hasEncryptedParts() {
		return hasEncryptedParts;
	}


	public void setContext() {
		context.setValue(ContextConstant.hasEncryptedParts, hasEncryptedParts());
	}


	public void init() {
		this.msgProp = new MessageProperty(new EncryptedPartsPropertyFactory());
	}
}
