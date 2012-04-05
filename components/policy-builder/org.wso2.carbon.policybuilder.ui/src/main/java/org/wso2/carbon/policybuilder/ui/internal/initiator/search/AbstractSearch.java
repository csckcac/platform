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
package org.wso2.carbon.policybuilder.ui.internal.initiator.search;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.policybuilder.ui.internal.services.ElementReader;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 29, 2009
 * Time: 12:23:10 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSearch {

	protected OMElement currentConfig, behaviorElem;
	protected String docNamespace;
	protected ElementReader reader;
	protected boolean terminateSearch;


	public abstract boolean search();

	public OMElement getCurrent() {
		return behaviorElem;
	}

	public String getBehaviorName() {
		return behaviorElem.getAttributeValue(new QName("name"));
	}

	public String getAssertionParam() {
		return behaviorElem.getAttributeValue(new QName("param"));
	}

	public String getAssertionName() {
		return behaviorElem.getAttributeValue(new QName("assert"));
	}


	public String getSkipParam() {
		return behaviorElem.getAttributeValue(new QName("skip"));
	}

	public String getID() {
		return behaviorElem.getAttributeValue(new QName("id"));
	}

	public boolean isSerializable() {
		Iterator children = behaviorElem.getChildElements();
		OMElement temp;
		String mainValue;
		while (children.hasNext()) {
			temp = (OMElement) children.next();
			if (temp != null && "serialize".equals(temp.getQName().getLocalPart()) && docNamespace.equals(temp.getQName().getNamespaceURI())) {
				mainValue = temp.getText();
				if (mainValue != null && "true".equals(mainValue.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean terminateSearching() {
		return terminateSearch;
	}
}
