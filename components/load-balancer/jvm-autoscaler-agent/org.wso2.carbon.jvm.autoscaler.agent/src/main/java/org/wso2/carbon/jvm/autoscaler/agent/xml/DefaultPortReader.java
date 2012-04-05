/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.jvm.autoscaler.agent.xml;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Reads the default port values mentioned in Carbon XML
 *
 */
public class DefaultPortReader {

	private static final Log log = LogFactory.getLog(DefaultPortReader.class);
	
	private static final String RELATIVE_PATH_TO_CARBON_XML = "/repository/conf/carbon.xml";
	
	/**
	 * Will be used when we could not read the carbon XML
	 */
	private static final int DEFAULT_HTTPS_PORT = 9443;
	private static final int DEFAULT_HTTP_PORT = 9763;
	
	private String absolutePathToCarbonXML;
	
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private Document dom;
	
	
	public DefaultPortReader(String carbonHome){
		absolutePathToCarbonXML = carbonHome+ RELATIVE_PATH_TO_CARBON_XML;
	}
	
	/**
	 * Read the HTTPS port
	 * @return value that is read or default value
	 */
	public int getHttpsPort() {
		/**
		 * Parse the configuration file.
		 */
		try {
			//Using factory, get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(absolutePathToCarbonXML);
			

		}catch(Exception ex) {
			String msg = "Error occurred when reading HTTPS port from the " 
										+ absolutePathToCarbonXML+".";
			log.error(msg, ex);
			
			//Hence return the default value
			return DEFAULT_HTTPS_PORT;
		}
		
		/**
		 * Read document elements.
		 */
		//get the root element
		Element docEle = dom.getDocumentElement();

		//get ServletTransports
		NodeList nl = docEle.getElementsByTagName("ServletTransports");
		
		//check whether there are elements
		if(nl != null && nl.getLength() > 0 ) {
			//read the https port value
			Element el = (Element)nl.item(0);
			nl = el.getElementsByTagName("HTTPS");
			
			if(nl != null && nl.getLength() > 0 ) {
				return Integer.parseInt(nl.item(0).getTextContent());
			}
						
		}
		
		return DEFAULT_HTTPS_PORT;
    }
	
	/**
	 * Read the HTTP port
	 * @return value that is read or default value
	 */
	public int getHttpPort() {
		/**
		 * Parse the configuration file.
		 */
		try {
			//Using factory, get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(absolutePathToCarbonXML);
			

		}catch(Exception ex) {
			String msg = "Error occurred when reading HTTP port from the " 
										+ absolutePathToCarbonXML+".";
			log.error(msg, ex);
			
			//Hence return the default value
			return DEFAULT_HTTP_PORT;
		}
		
		/**
		 * Read document elements.
		 */
		//get the root element
		Element docEle = dom.getDocumentElement();

		//get ServletTransports
		NodeList nl = docEle.getElementsByTagName("ServletTransports");
		
		//check whether there are elements
		if(nl != null && nl.getLength() > 0 ) {
			//read the https port value
			Element el = (Element)nl.item(0);
			nl = el.getElementsByTagName("HTTP");
			
			if(nl != null && nl.getLength() > 0 ) {
				return Integer.parseInt(nl.item(0).getTextContent());
			}
						
		}
		
		return DEFAULT_HTTP_PORT;
    }

}
