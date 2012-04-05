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

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Nov 4, 2008
 * Time: 10:32:08 AM
 * To change this template use File | Settings | File Templates.
 */


import org.wso2.carbon.policybuilder.ui.internal.engine.*;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.FileReader;
import java.io.Reader;


public class XMLFileReader extends XMLReader {

	private static Log log = LogFactory.getLog(XMLFileReader.class);

	private String FILEPATH = "/home/usw/my";


	public XMLFileReader(String path) {
		if (log.isDebugEnabled()) {
			log.debug("Starting XML Reader...");
		}
		// System.out.println("Startrting reader");
		setFilePath(path);
	}


	private void setFilePath(String path) {
		this.FILEPATH = path;
	}


	public FileReader getFile(String path) {
		try {
			FileReader fr = new FileReader(path);
			return fr;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public FileReader getFile() {
		try {
			FileReader fr = new FileReader(this.FILEPATH);
			return fr;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	public Reader getReader() {
		return getFile();  //To change body of implemented methods use File | Settings | File Templates.
	}


	// for Testing Purposes
	public static void main(String[] args) {

		/*    XMLFileReader xr = new XMLFileReader("/home/usw/my2.xml");
				xr.setFilePath("/home/usw/my.xml");
				OMElement root = xr.getDocumentRoot();

				new SymmetricBindingBehavior().evaluate(root);
				new TokenBehavior(new ProtectionToken(11)).evaluate(root);
				new AlgorithmSuiteBehavior().evaluate(root);
				new EncryptSignOrderBehavior().evaluate(root);
				new InitiatorTokenBehavior().evaluate(root) ;
				new AsymmetricTokenBehavior(new RecipientToken(11)).evaluate(root);
				new TransportBindingBehavior().evaluate(root);
				new SignedPartsBehavior().evaluate(root);
				new EncryptedPartsBehavior().evaluate(root);
				new AsymmetricBindingBehavior().evaluate(root) ;
				new ProtectionToken(11).setToken(new X509Token());
				System.out.println();

				try {

					if (root != null) {
						root.serialize(System.out);


					}
				}
				catch (Exception e) {
					e.printStackTrace();


				}

				*/
		System.out.println("Hi");
	}
}
