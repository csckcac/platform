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
import org.apache.ws.secpolicy.model.InitiatorToken;
import org.apache.ws.secpolicy.model.RecipientToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;

import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Dec 2, 2008
 * Time: 3:59:55 PM
 * To change this template use File | Settings | File Templates.
 */
public class RecipientTokenBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(RecipientTokenBehavior.class);
	private boolean isAsymmetric = false;
	private RecipientToken recptTkn;


	public RecipientTokenBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}


	public RecipientTokenBehavior(AbstractSecurityAssertion assertion) {
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
		checkProperties();
		if (isAsymmetric) {
			if (log.isDebugEnabled()) {
				log.debug("Recipient token assumed");
			}
			// System.out.println("recipient token assumed");
			this.isBehaviorCompleted = true;
		}
		doAssertionLoad(isBehaviorCompleted);
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


	public void init() {

		//To change body of implemented methods use File | Settings | File Templates.
	}


	public void doAssertionLoad(boolean behaviorCompleted) {
		if (behaviorCompleted == true && this.assertion != null) {
			if ((this.assertion instanceof AsymmetricBinding)) {
				this.recptTkn = (RecipientToken) ((getSuccessor()).assertion);
				((AsymmetricBinding) this.assertion).setRecipientToken(recptTkn);
				handleSuccessor(this.root);
			}
		} else {
			skip(this.root);
		}
	}
}
