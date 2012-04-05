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

import org.wso2.carbon.policybuilder.ui.internal.context.PolicyContext;
import org.wso2.carbon.policybuilder.ui.internal.context.Context;
import org.wso2.carbon.policybuilder.ui.internal.context.ContextConstant;
import org.apache.axiom.om.OMElement;
import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.wso2.carbon.policybuilder.ui.internal.property.MessageProperty;
import org.wso2.carbon.policybuilder.ui.internal.initiator.Initiator;


import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 7, 2008
 * Time: 9:29:02 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PolicyBehavior {

	protected OMElement root;
	protected Map assertionsMap;
	protected Stack list;
	protected AbstractSecurityAssertion assertion;

	public ArrayList nestedBehaviors;
	protected MessageProperty msgProp;
	protected boolean isBehaviorCompleted;
	protected Context context = new PolicyContext();
	private Initiator parentInitiator;
	private PolicyBehavior next;
	private String skipTag = "", ID;

	public PolicyBehavior() {
		this.nestedBehaviors = new ArrayList();
	}

	public void injectInitiator(Initiator init) {
		this.parentInitiator = init;
	}

	public void setID(String ID) {
		this.ID = ID;
	}

	public void setSkipTag(String tag) {
		this.skipTag = tag;
	}

	private String getSkipTag() {
		return this.skipTag;
	}

	private String getID() {
		return this.ID;
	}

	public void injectContext(Context context) {
		this.context = context;
	}

	public void setSuccessor(PolicyBehavior next) {
		this.next = next;
	}

	protected PolicyBehavior getSuccessor() {
		return this.next;
	}


	public void handleSuccessor(OMElement el) {
		if (next != null) {
			next.evaluate(el);
		}
	}

	public void skip(OMElement el) {
		PolicyBehavior temp = next;
		PolicyBehavior skipBehavior = null;
		while (temp != null) {
			if (skipTag != null && skipTag.equals(temp.getID())) {
				skipBehavior = temp;
				break;
			} else if (skipTag == null) {
				skipBehavior = next;
				break;
			}
			temp = temp.getSuccessor();
		}
		if (skipBehavior != null) {
			skipBehavior.evaluate(el);
		} else if (next != null) {
			next.evaluate(el);
		}
	}

	public int evaluate(OMElement e) {
		this.root = e;
		list = new Stack();
		list.push(root);
		return 0;
	}


	public abstract void init();

	public void reset() {
		if (root != null) {
			list = new Stack();
			list.push(root);
		}
	}

	public OMElement next() {
		if (root != null && !(list.isEmpty())) {
			OMElement temp = (OMElement) list.pop();
			Iterator children = temp.getChildElements();
			while (children.hasNext()) {
				list.push(children.next());
			}
			return temp;
		}
		return null;
	}

	public boolean isEmptyList() {
		if (list != null && list.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public void addBehavior(PolicyBehavior pb) {
		if (pb != null) {
			nestedBehaviors.add(pb);
		}
	}

	public void removeBehavior(PolicyBehavior pb) {
		nestedBehaviors.remove(pb);
	}
}
