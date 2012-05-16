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
package org.wso2.carbon.autoscaler.service.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.wso2.carbon.autoscaler.service.util.Policy;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Responsible for reading the adapters.xml configuration file.
 * Following is a sample XML.
 *
 * &lt;adapters&gt;
 *  &lt;adapter className="org.wso2.carbon.autoscaler.service.adapters.LXCAdapter" /&gt;
 * &lt;/adapters&gt;
 */
public class AdaptersFileReader {
	
	private static final Log log = LogFactory.getLog(AdaptersFileReader.class);

	//get the factory
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private Document dom;
	private List<String> adapterClassList = new ArrayList<String>();

	/**
	 * Path to adapters XML file, which specifies all the adapters' classes to be used.
	 */
	private String adaptersXMLFile;
	
	
	public AdaptersFileReader() throws Exception{
	    
	    adaptersXMLFile = 
	            CarbonUtils.getCarbonConfigDirPath() + File.separator + "adapters.xml";
		
		/**
		 * Parse the configuration file.
		 */
		try {
			//Using factory, get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(adaptersXMLFile);
			

		}catch(Exception ex) {
			String msg = "Error occurred when parsing the "+adaptersXMLFile+".";
			log.error(msg, ex);
			throw new Exception(msg, ex);
		}
	}
	
	public AdaptersFileReader(String file) throws Exception{
        
        /**
         * Parse the configuration file.
         */
        try {
            //Using factory, get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            //parse using builder to get DOM representation of the XML file
            dom = db.parse(file);
            

        }catch(Exception ex) {
            String msg = "Error occurred when parsing the "+file+".";
            log.error(msg, ex);
            throw new Exception(msg, ex);
        }
    }
	
    public List<String> adapterClassList() {

        // get list of adapter elements
        NodeList nl = dom.getElementsByTagName("adapter");

        // check whether there are elements
        if (nl != null && nl.getLength() > 0) {

            for (int i = 0; i < nl.getLength(); i++) {

                // read the adapter element
                Element el = (Element) nl.item(i);

                adapterClassList.add(el.getAttribute("className"));
            }
            

        }

        return adapterClassList;
    }
	

}
