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
package org.wso2.carbon.policybuilder.ui.internal.services;

import org.apache.axiom.om.OMElement;

import java.util.Iterator;
import java.util.Stack;
import java.util.Queue;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 20, 2009
 * Time: 3:28:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrderedElementReader extends ElementReader {

	private Stack orderingStack;

	public OrderedElementReader(OMElement om) {
		super(om);
		this.orderingStack = new Stack();
	}

	public boolean next() {
		if (!container.isEmpty()) {
			OMElement temp = (OMElement) ((Stack) container).pop();
			this.element = temp;
			Iterator children = temp.getChildElements();
			while (children.hasNext()) {
				this.orderingStack.push(children.next());
			}
			while (!this.orderingStack.isEmpty()) {
				((Stack) this.container).push(orderingStack.pop());
			}
			return true;
		}
		return false;
	}
}
