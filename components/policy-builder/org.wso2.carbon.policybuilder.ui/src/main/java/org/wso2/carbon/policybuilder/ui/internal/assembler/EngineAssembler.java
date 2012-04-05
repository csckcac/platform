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
package org.wso2.carbon.policybuilder.ui.internal.assembler;

import org.apache.ws.secpolicy.model.AbstractSecurityAssertion;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.ByteArrayOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: usw
 * Date: Jan 29, 2009
 * Time: 10:21:10 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class EngineAssembler {

	protected ArrayList assertionsList = new ArrayList();
	protected ArrayList configList = new ArrayList();
	private static Log log = LogFactory.getLog(EngineAssembler.class);

	public abstract String assemble(String input);

	public String print() {
		Iterator assertionSet = assertionsList.iterator();
		String outputString = "";
		ByteArrayOutputStream out = null;
		while (assertionSet.hasNext()) {
			try {
				out = new ByteArrayOutputStream();
				//XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
				XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
				//this.symBind.serialize(writer);
				AbstractSecurityAssertion a = (AbstractSecurityAssertion) assertionSet.next();
				a.serialize(writer);
				writer.flush();
				writer.close();
				/*
								String tempPrefix1="",tempPrefix2="";
								boolean isNewConfig =(Boolean) hasConfigIterator.next();
								if(isNewConfig){
									 tempPrefix1 = "\n<wsp:ExactlyOne>\n  " ;
									 tempPrefix2 = "\n</wsp:ExactlyOne>\n  " ;
								}
								 */
				//System.out.println(out);
				outputString = outputString + "\n <wsp:Policy>\n  " + out.toString() + "\n </wsp:Policy>\n";
				out.flush();
				out.close();
			}
			catch (RuntimeException e) {
				if (log.isInfoEnabled()) {
					log.info(e.getMessage());
				}
				//e.printStackTrace();
			}
			catch (XMLStreamException e) {
				if (log.isInfoEnabled()) {
					log.info(e.getMessage());
				}
				// e.printStackTrace();
			}
			catch (Exception e) {
				if (log.isInfoEnabled()) {
					log.info(e.getMessage());
				}

				// e.printStackTrace();
			}
		}

		// System.out.println(out);
		return outputString;
	}


	private String print(ArrayList current) {
		Iterator assertionSet = current.iterator();
		String outputString = "";
		ByteArrayOutputStream out = null;
		while (assertionSet.hasNext()) {
			try {
				out = new ByteArrayOutputStream();
				//XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
				XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(out);
				//this.symBind.serialize(writer);
				AbstractSecurityAssertion a = (AbstractSecurityAssertion) assertionSet.next();
				a.serialize(writer);
				writer.flush();
				writer.close();
				String tempPrefix1 = "", tempPrefix2 = "";

				//System.out.println(out);
				outputString = outputString + "\n <wsp:Policy>\n  " + out.toString() + "\n </wsp:Policy>\n";
				out.flush();
				out.close();
			}
			catch (RuntimeException e) {
				if (log.isInfoEnabled()) {
					log.info(e.getMessage());
				}
				//e.printStackTrace();
			}
			catch (XMLStreamException e) {
				if (log.isInfoEnabled()) {
					log.info(e.getMessage());
				}
				// e.printStackTrace();
			}
			catch (Exception e) {
				if (log.isInfoEnabled()) {
					log.info(e.getMessage());
				}
				// e.printStackTrace();
			}
		}

		// System.out.println(out);
		return outputString;
	}


	public String printConfigs() {
		String outputString = "";
		String tempPrefix1 = "", tempPrefix2 = "";
		if (configList.size() > 0) {
			Iterator configs = configList.iterator();
			while (configs.hasNext()) {
				ArrayList config = (ArrayList) configs.next();
				if (config != null) {
					tempPrefix1 = "\n<wsp:All>\n  ";
					tempPrefix2 = "\n</wsp:All>\n  ";
					outputString = outputString + tempPrefix1 + print(config) + tempPrefix2;
				}
			}
		}
		return "\n<wsp:ExactlyOne>\n  " + outputString + "\n</wsp:ExactlyOne>\n  ";
	}
}
