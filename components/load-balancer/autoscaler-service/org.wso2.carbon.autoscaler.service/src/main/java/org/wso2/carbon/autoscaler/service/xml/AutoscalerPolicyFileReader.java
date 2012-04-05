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
 * Responsible for reading the policy configuration file.
 * Following is a sample XML.
 *
 * <autoscalePolicy useDefault="false">
 *
 *   <!-- specify the order of adapters when scaling up
 *        if not specified, default will be used.-->
 *   <scaleUpOrder>
 *       <adapter name="jvm" />
 *       <adapter name="ec2" />
 *   </scaleUpOrder>
 *
 *   <!-- specify the order of adapters when scaling down
 *        if not specified, default will be used.-->
 *   <scaleDownOrder>
 *       <adapter name="jvm" minInstanceCount="1" />
 *       <adapter name="ec2" minInstanceCount="0" />
 *   </scaleDownOrder>
 *
 * </autoscalePolicy>
 */
public class AutoscalerPolicyFileReader {
	
	private static final Log log = LogFactory.getLog(AutoscalerPolicyFileReader.class);

	//get the factory
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private Document dom;
	private List<String> scaleUpOrder, scaleDownOrder;
	private Map<Integer, Integer> scaleDownMap;

	/**
	 * Path to policy file, which specifies the policies to use when scaling up
	 * and down.
	 */
	private static final String AUTOSCALER_POLICY_XML_FILE = 
            CarbonUtils.getCarbonConfigDirPath() + File.separator + "autoscaler-policy.xml";
	
	
	public AutoscalerPolicyFileReader() throws Exception{
		
		/**
		 * Parse the configuration file.
		 */
		try {
			//Using factory, get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(AUTOSCALER_POLICY_XML_FILE);
			

		}catch(Exception ex) {
			String msg = "Error occurred when parsing the "+AUTOSCALER_POLICY_XML_FILE+".";
			log.error(msg, ex);
			throw new Exception(msg, ex);
		}
	}
	
	/**
	 * This will return the policy object instance that autoscaler service should follow.
	 * @return Policy object instance
	 */
	public Policy getPolicy() {
	    
        // create a policy object having default policy.
        Policy policyObj = new Policy();
        
	    /**
         * Read document elements.
         */
        //get the root element
        Element docEle = dom.getDocumentElement();

        //get list of instance elements
        NodeList nl = docEle.getElementsByTagName("autoscalePolicy");
        
        //check whether there are elements
        if (nl != null && nl.getLength() > 0) {

            // read the autoscalePolicy element
            Element el = (Element) nl.item(0);
            // read the useDefault attribute, if attribute is not specified this will get false
            boolean useDefault = Boolean.parseBoolean(el.getAttribute("useDefault"));
            
            //if useDefault set to false
            if(!useDefault){
                
                //read custom policy
                loadScaleUpOrder(el);
                loadScaleDownOrder(el);
               
                if( scaleUpOrder != null && scaleUpOrder.size() > 0){
                    policyObj.setScaleUpOrderList(scaleUpOrder);
                }
                
                if( scaleDownOrder != null && scaleDownOrder.size() > 0){
                    policyObj.setScaleDownOrderList(scaleDownOrder);
                    policyObj.setScaleDownOrderIdToMinInstanceCountMap(scaleDownMap);
                }
                
                                
            }
        }

       return policyObj;
    }
	
	private void loadScaleUpOrder(Element docEle){

        // get list of instance elements
        NodeList nl = docEle.getElementsByTagName("scaleUpOrder");

        // check whether there are elements
        if (nl != null && nl.getLength() > 0) {

            // read the scaleUpOrder element
            Element el = (Element) nl.item(0);
            
            populateAdapterScaleUpPolicy(el);
           
        }
        
	}
	
	private void loadScaleDownOrder(Element docEle){

        // get list of instance elements
        NodeList nl = docEle.getElementsByTagName("scaleDownOrder");

        // check whether there are elements
        if (nl != null && nl.getLength() > 0) {

            // read the scaleUpOrder element
            Element el = (Element) nl.item(0);
            
            populateScaleDownOrderPolicy(el);
            
        }
        
    }
	

	/**
	 * This will read adapter elements under provided element and put the name attributes of them
	 * into a list. 
	 * @param el 
	 * @return
	 */
	private void populateAdapterScaleUpPolicy(Element el) {

        // get list of instance elements
        NodeList nl = el.getChildNodes();

        // check whether there are child nodes
        if (nl != null && nl.getLength() > 0) {
            
            scaleUpOrder = new ArrayList<String>();

            for (int i = 0; i < nl.getLength(); i++) {
                // read the adapter element
                Element e = (Element) nl.item(i);
                if (e.getNodeName().trim().equals("adapter")) {
                    scaleUpOrder.add(e.getAttribute("name"));
                }
            }
            
        }

    }
	
	
	/**
     * This will read adapter elements under provided element and put the name attributes of them
     * into {@link #scaleDownOrder}. Also will populate the {@link #scaleDownMap}.
     * @param el 
     * @return
     */
    private void populateScaleDownOrderPolicy(Element el) {

        scaleDownMap = new HashMap<Integer, Integer>();
        
        // get list of instance elements
        NodeList nl = el.getChildNodes();

        // check whether there are child nodes
        if (nl != null && nl.getLength() > 0) {
            
            scaleDownOrder = new ArrayList<String>();

            for (int i = 0; i < nl.getLength(); i++) {
                // read the adapter element
                Element e = (Element) nl.item(i);
                if (e.getNodeName().trim().equals("adapter")) {
                    scaleDownOrder.add(e.getAttribute("name"));
                    int minInstanceCount = Integer.parseInt(e.getAttribute("minInstanceCount"));
                    scaleDownMap.put(i, minInstanceCount);
                }
            }
            
        }

    }

    
}
