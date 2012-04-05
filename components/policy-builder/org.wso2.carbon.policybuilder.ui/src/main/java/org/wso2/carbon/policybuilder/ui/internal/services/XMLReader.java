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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLInputFactory;
import java.io.FileReader;
import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 13, 2009
 * Time: 5:35:24 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class XMLReader {


	public abstract Reader getReader();

	public OMElement getDocumentRoot() {
		OMElement documentElement;
		try {
			XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(getReader());
			StAXOMBuilder builder = new StAXOMBuilder(parser);
			documentElement = builder.getDocumentElement();
			return documentElement;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
