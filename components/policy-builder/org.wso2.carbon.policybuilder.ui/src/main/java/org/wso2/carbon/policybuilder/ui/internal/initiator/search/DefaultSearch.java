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

import org.wso2.carbon.policybuilder.ui.internal.services.ElementReader;
import org.wso2.carbon.policybuilder.ui.internal.services.OrderedElementReader;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 29, 2009
 * Time: 12:24:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class DefaultSearch extends AbstractSearch {

	private static Log log = LogFactory.getLog(DefaultSearch.class);

	public DefaultSearch(OMElement currentConfig, String docNamespace) {
		this.currentConfig = currentConfig;
		this.docNamespace = docNamespace;
		reader = new OrderedElementReader(currentConfig);
	}

	public boolean search() {
		//To change body of implemented methods use File | Settings | File Templates.
		String elemNameAttrib, elemVisitedAttrib, elemLocal, elemNS, assertionParam;
		OMElement elem;
		boolean isAllVisited = true, hasMainElement = false, elemFound = false;
		if (log.isDebugEnabled()) {
			log.debug(this.docNamespace);
		}
		//System.out.println(this.docNamespace);
		while (reader.next()) {
			elem = reader.getCurrentElement();
			elemNameAttrib = elem.getAttributeValue(new QName("name"));
			elemVisitedAttrib = elem.getAttributeValue(new QName("visited"));
			assertionParam = elem.getAttributeValue(new QName("param"));
			elemLocal = elem.getQName().getLocalPart();
			elemNS = elem.getQName().getNamespaceURI();

			// System.out.println(elem.getQName().getNamespaceURI());
			// System.out.println(elemNameAttrib);
			if (!elemFound && !"true".equals(elemVisitedAttrib) && elemNameAttrib != null && "behavior".equals(elemLocal) && docNamespace.equals(elemNS)) {
				if (elemNameAttrib == null || "".equals(elemNameAttrib)) {
					elem.discard();
				} else {
					this.behaviorElem = elem;
					this.behaviorElem.addAttribute("visited", "true", null);
					hasMainElement = true;
					elemFound = true;
					break;
				}
			}
		}
		if (elemFound) {
			terminateSearch = false;
		} else {
			terminateSearch = true;
		}
		return terminateSearch;
	}
}
