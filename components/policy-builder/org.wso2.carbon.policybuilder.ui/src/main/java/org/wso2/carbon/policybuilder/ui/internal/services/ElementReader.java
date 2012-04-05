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

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Stack;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 25, 2008
 * Time: 1:57:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class ElementReader {

	protected OMElement element;
	protected Collection container;

	public ElementReader(OMElement om) {
		this.element = om;
		this.container = new Stack();
		((Stack) this.container).push(om);
	}

	public boolean next() {
		if (!container.isEmpty()) {
			OMElement temp = (OMElement) ((Stack) container).pop();
			this.element = temp;
			Iterator children = temp.getChildElements();
			while (children.hasNext()) {
				((Stack) this.container).push(children.next());
			}
			return true;
		}
		return false;
	}


	public OMElement getCurrentElement() {
		return element;
	}

	public String getCurrentElementName() {
		return element.getQName().toString();
	}

	public QName getCurrentElementQName() {
		return element.getQName();
	}
}
