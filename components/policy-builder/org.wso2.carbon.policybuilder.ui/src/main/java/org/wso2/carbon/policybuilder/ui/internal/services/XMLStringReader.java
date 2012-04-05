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

import java.io.FileReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 13, 2009
 * Time: 5:52:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class XMLStringReader extends XMLReader {

	private String xmlText = "";


	public XMLStringReader(String text) {
		setXMLText(text);
	}


	private void setXMLText(String text) {
		this.xmlText = text;
	}

	public Reader getString() {
		try {
			ByteArrayInputStream stream = new ByteArrayInputStream(xmlText.getBytes());
			Reader reader = new InputStreamReader(stream);
			return reader;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	public Reader getReader() {
		return getString();  //To change body of implemented methods use File | Settings | File Templates.
	}
}
