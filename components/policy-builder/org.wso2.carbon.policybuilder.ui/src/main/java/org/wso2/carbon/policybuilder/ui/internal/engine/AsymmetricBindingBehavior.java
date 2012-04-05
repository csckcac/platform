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
import org.apache.ws.secpolicy.model.InitiatorToken;
import org.apache.ws.secpolicy.model.RecipientToken;
import org.apache.ws.secpolicy.model.AsymmetricBinding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;

import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.property.AsymetricPropertyFactory;


/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 7, 2008
 * Time: 9:38:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class AsymmetricBindingBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(AsymmetricBindingBehavior.class);
	private boolean isSymmetric = false;
	private boolean hasEncryption = false;
	private boolean hasSignature = false;
	private boolean isAsymmetric = false;


	private TimeStampBehavior tsBehavior;


	public AsymmetricBindingBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}


	public AsymmetricBindingBehavior(AbstractSecurityAssertion assertion) {
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

	public void init() {
		this.msgProp = new MessageProperty(new AsymetricPropertyFactory());
	}

	public void doEvaluate(OMElement e) {
		this.checkSymmetric();
		this.checkProperties();
		if (!this.isSymmetric) {
			if (this.hasEncryption || this.hasSignature) {
				this.isBehaviorCompleted = true;
				this.isAsymmetric = true;
				if (log.isDebugEnabled()) {
					log.debug("Assymetric Binding Detected");
				}
				//System.out.println("Assymetric Binding Detected");
			}
		}
		setContext();
		doAssertionLoad(isBehaviorCompleted);
	}

	private void checkSymmetric() {
		Boolean temp = context.getValue(ContextConstant.isSymmetric);
		if (temp != null) {
			this.isSymmetric = temp.booleanValue();
		} else {
			SymmetricBindingBehavior symBehavior = new SymmetricBindingBehavior();
			symBehavior.evaluate(this.root);
			this.isSymmetric = symBehavior.isSymmetric();
		}
	}

	public void checkProperties() {
		Boolean temp1 = context.getValue(ContextConstant.hasEncryption);
		Boolean temp2 = context.getValue(ContextConstant.hasSignature);
		if (temp1 != null && temp2 != null) {
			this.hasSignature = temp2.booleanValue();
			this.hasEncryption = temp1.booleanValue();
		} else {
			SymmetricProtectionBehavior symProtBehavior = new SymmetricProtectionBehavior();
			symProtBehavior.evaluate(this.root);
			this.hasEncryption = symProtBehavior.hasEncryption();
			this.hasSignature = symProtBehavior.hasSignature();
		}
	}

	public void doAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true && this.assertion != null) {
			this.isBehaviorCompleted = true;
			if (log.isDebugEnabled()) {
				log.debug("Processing Asymmetric Binding process.. ");
			}
			// System.out.println("Asymmetric");
			handleSuccessor(this.root);
		} else {
			skip(this.root);
		}
	}


	public boolean isAssymmetric() {
		return isAsymmetric;
	}


	public void setContext() {
		context.setValue(ContextConstant.isAsymmetric, isAssymmetric());
	}
}
