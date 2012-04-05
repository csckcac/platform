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
package org.wso2.carbon.autoscaler.agent.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Responsible for reading the agent configuration file.
 *
 */
public class AgentConfigFileReader {
	
	private static final Log log = LogFactory.getLog(AgentConfigFileReader.class);

	//get the factory
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private Document dom;

    /**
     * Path to configuration file, which specifies the path to services available
     * and their domain names.
     */
    private static final String INSTANCE_CONFIG_XML_FILE = CarbonUtils.getCarbonConfigDirPath() +
        File.separator + "autoscaler-agent-config.xml";
	
	
	public AgentConfigFileReader() throws Exception{
		
        /**
         * Parse the configuration file.
         */
        try {
            // Using factory, get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            // parse using builder to get DOM representation of the XML file
            dom = db.parse(INSTANCE_CONFIG_XML_FILE);

        } catch (Exception ex) {
            String msg = "Error occurred when parsing the " + INSTANCE_CONFIG_XML_FILE + ".";
            log.error(msg, ex);
            throw new Exception(msg, ex);
        }
	}

	/**
	 * Method reads the configuration file and returns a list where key is the domain name
	 * and value is the path to the image of the instance.
	 * @return Map- key: domain name 		value: path to the image instance.
	 * @throws Exception 
	 */
	public Map<String, String> getAvailableInstanceImages() {
		
        Map<String, String> instanceMap = new HashMap<String, String>();

        // get the root element
        Element docEle = dom.getDocumentElement();

        // get list of instance elements
        NodeList nl = docEle.getElementsByTagName("image");

        // check whether there are elements
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {

                // for each instance element, read its attributes and put into the map.
                Element el = (Element) nl.item(i);
                String domain, path;
                // be safe
                if ((domain = el.getAttribute("domain")) != null &&
                    (path = el.getAttribute("path")) != null) {

                    instanceMap.put(domain, path);
                }
            }
        }

        // finally returns the map
        return instanceMap;
		
    }
	
	/**
	 * Read url of the dependent services.
	 * @return URL of the dependent services.
	 * @throws Exception when can not obtain url from the config file.
	 */
	public String getHostUrlOfDependentServices() throws Exception {
	    
        // get the root element
        Element docEle = dom.getDocumentElement();

        // get list of instance elements
        NodeList nl = docEle.getElementsByTagName("autoscalerHost");

        // check whether there are elements
        if (nl != null && nl.getLength() > 0) {

            // only reads the first element
            Element el = (Element) nl.item(0);

            String url;

            if ((url = el.getAttribute("url")) != null) {

                return url;
            } else {
                String msg =
                    "Hosted URL of the dependent services is null in " + INSTANCE_CONFIG_XML_FILE +
                        ".";
                log.error(msg);
                throw new Exception(msg);
            }

        } else {
            String msg =
                "autoscalerHost element cannot be found at " + INSTANCE_CONFIG_XML_FILE + " .";
            log.error(msg);
            throw new Exception(msg);
        }
    }
	
	
	/**
     * Read maxMemoryPerInstance element.
     * @return maxMemoryPerInstance element's value
     * @throws Exception when can not obtain maxMemoryPerInstance from the config file.
     */
    public long getMaxMemoryPerInstance() throws Exception {

        // get the root element
        Element docEle = dom.getDocumentElement();

        // get list of instance elements
        NodeList nl = docEle.getElementsByTagName("maxMemoryPerInstance");

        // check whether there are elements
        if (nl != null && nl.getLength() > 0) {

            // only reads the first element
            Element el = (Element) nl.item(0);

            String maxMemoryPerInstance = el.getTextContent();
            if (maxMemoryPerInstance != null) {
                return Long.parseLong(maxMemoryPerInstance);
            }

            String msg =
                "maxMemoryPerInstance's value is null in " + INSTANCE_CONFIG_XML_FILE + ".";
            log.error(msg);
            throw new Exception(msg);
        }

        String msg =
            "maxMemoryPerInstance element cannot be found at " + INSTANCE_CONFIG_XML_FILE + " .";
        log.error(msg);
        throw new Exception(msg);

    }
	
}
