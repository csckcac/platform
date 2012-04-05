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

import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;
import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.apache.axiom.om.OMElement;
import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.ws.secpolicy.model.AsymmetricBinding;
import org.apache.ws.secpolicy.model.SymmetricBinding;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.property.TimeStampPropertyFactory;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 13, 2008
 * Time: 10:12:09 AM
 * To change this template use File | Settings | File Templates.
 */
public class TimeStampBehavior extends PolicyBehavior {

	private static Log log = LogFactory.getLog(TimeStampBehavior.class);
	private boolean isTimeStampAvailable = false;

	public TimeStampBehavior() {
		this.isBehaviorCompleted = false;
		init();
	}

	public TimeStampBehavior(AbstractSecurityAssertion assertion) {
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
		while (!isEmptyList()) {
			current = next();
			elementName = current.getQName().toString();
			if (this.msgProp.contains(elementName)) {
				isTimeStampAvailable = true;
				break;
			}
		}
		if (isTimeStampAvailable == true) {
			setContext();
			this.isBehaviorCompleted = true;
			if (this.assertion != null) {
				if ((this.assertion instanceof SymmetricBinding)) {
					((SymmetricBinding) this.assertion).setIncludeTimestamp(true);
					if (log.isDebugEnabled()) {
						log.debug("TimeStamp included for Symmetric binding");
					}
					//System.out.println("TimeStamp included for Symmetric binding");
				} else if ((this.assertion instanceof AsymmetricBinding)) {
					((AsymmetricBinding) this.assertion).setIncludeTimestamp(true);
					if (log.isDebugEnabled()) {
						log.debug("TimeStamp included for Asymmetric binding");
					}
					//System.out.println("TimeStamp included for Asymmetric binding");
				} else {
					if (log.isDebugEnabled()) {
						log.debug("No Asymmetric or Symmetric Binding present");
					}
					//System.out.println("No Asymmetric or Symmetric Binding present");
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("No Assertion Present");
				}
				//System.out.println("No assertion");
			}
		}
		handleSuccessor(this.root);
	}

	public void setContext() {
		context.setValue(ContextConstant.hasTimeStamp, hasTimeStamp());
	}


	public boolean hasTimeStamp() {
		return isTimeStampAvailable;
	}

	public void init() {
		this.msgProp = new MessageProperty(new TimeStampPropertyFactory());
	}
}
