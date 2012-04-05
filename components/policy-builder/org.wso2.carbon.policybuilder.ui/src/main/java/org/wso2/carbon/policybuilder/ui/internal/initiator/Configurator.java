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
package org.wso2.carbon.policybuilder.ui.internal.initiator;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLFileReader;
import org.wso2.carbon.policybuilder.ui.internal.services.XMLInputStreamReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 30, 2009
 * Time: 2:03:47 AM
 * To change this template use File | Settings | File Templates.
 */
public class Configurator {

	private static Log log = LogFactory.getLog(Configurator.class);

	private final OMElement defaultRoot = new XMLInputStreamReader(this.getClass().getClassLoader().getResourceAsStream("builder.xml")).getDocumentRoot();
	private OMElement currentConfig;


	public Configurator() {
	}

	public boolean setNewConfiguration() {
		discardOldConfig();
		currentConfig = defaultRoot.getFirstChildWithName(new QName(defaultRoot.getQName().getNamespaceURI(), "config"));
		if (currentConfig != null) {
			if (log.isDebugEnabled()) {
				log.debug("New Config Available");
			}
			// System.out.println("\nNew Config Available");
			return true;
		}
		return false;
	}

	public OMElement getCurrentConfiguration() {
		return currentConfig;
	}


	public void discardOldConfig() {
		if (currentConfig != null) {
			currentConfig.discard();
		}
	}

	public String getNamespace() {
		return defaultRoot.getQName().getNamespaceURI();
	}

	public String getSearchType() {
		OMElement temp = currentConfig.getFirstChildWithName(new QName(getNamespace(), "search"));
		if (temp != null)
			return temp.getText();
		else
			return null;
	}

	public void serialize(OutputStream out) {
		try {
			defaultRoot.serialize(out);
		} catch (XMLStreamException e) {
			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
		}
	}
}
