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
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.autoscaler.service.exception.MalformedConfigurationFileException;
import org.wso2.carbon.autoscaler.service.util.IaaSProvider;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Responsible for reading the JClouds configuration file.
 * Following is a sample XML.
 *
 * &lt;jcloudConfig&gt;
 *      &lt;iaasProviders&gt;
 *          &lt;iaasProvider name="ec2"&gt;
 *              &lt;scaleUpOrder&gt;1&lt;/scaleUpOrder&gt;
 *              &lt;scaleDownOrder&gt;2&lt;/scaleDownOrder&gt;
 *              &lt;property name="A" value="a"/&gt;
 *              &lt;property name="B" value="b"/&gt;
 *              &lt;template&gt;temp1&lt;/template&gt;
 *          &lt;/iaasProvider&gt;
 *          
 *          &lt;iaasProvider name="lxc"&gt;
 *              &lt;scaleUpOrder&gt;2&lt;/scaleUpOrder&gt;
 *              &lt;scaleDownOrder&gt;1&lt;/scaleDownOrder&gt;
 *              &lt;property name="X" value="x"/&gt;
 *              &lt;property name="Y" value="y"/&gt;
 *              &lt;template&gt;temp2&lt;/template&gt;
 *          &lt;/iaasProvider&gt;
 *      &lt;/iaasProviders&gt;
 *  &lt;/jcloudConfig&gt;
 */
public class JCloudsConfigFileReader {
	
	private static final Log log = LogFactory.getLog(JCloudsConfigFileReader.class);

	//get the factory
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private Document dom;

	/**
	 * Path to JClouds config XML file, which specifies the JClouds specific details.
	 */
	private String jcloudsConfigXMLFile;
	
	
	public JCloudsConfigFileReader() throws Exception{
	    
	    jcloudsConfigXMLFile = 
	            CarbonUtils.getCarbonConfigDirPath() + File.separator + "jclouds-config.xml";
	    
		/**
		 * Parse the configuration file.
		 */
		try {
			//Using factory, get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(jcloudsConfigXMLFile);
			

		}catch(Exception ex) {
			String msg = "Error occurred while parsing the "+jcloudsConfigXMLFile+".";
			log.error(msg, ex);
			throw new Exception(msg, ex);
		}
	}
	
	public JCloudsConfigFileReader(String file) throws Exception{
        
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
	
	/**
	 * Load all IaasProviders from the configuration file and returns a list.
	 * @return a list of IaasProvider instances.
	 */
	public List<IaaSProvider> getIaasProvidersList() {
	    List<IaaSProvider> iaasProviders = new ArrayList<IaaSProvider>();
	    
	    Element docEle = dom.getDocumentElement();
	    NodeList nl = docEle.getElementsByTagName("iaasProvider");
	    
	    if (nl != null && nl.getLength() > 0) {
	        
	        for(int i=0; i< nl.getLength() ; i++){
	            iaasProviders.add(getIaasProvider(nl.item(i)));
	        }
	        
	    }
	    else{
	        String msg = "Essential 'iaasProvider' element cannot be found in "+jcloudsConfigXMLFile;
	        log.error(msg);
	        throw new MalformedConfigurationFileException(msg);
	    }
	    
	    return iaasProviders;
	    
    }

    private IaaSProvider getIaasProvider(Node item) {

        IaaSProvider iaas = new IaaSProvider();
        
        if (item.getNodeType() == Node.ELEMENT_NODE) {
            Element iaasElt = (Element) item;
            iaas.setName(iaasElt.getAttribute("name"));
            
            if("".equals(iaas.getName())){
                String msg = "'iaasProvider' element's 'name' attribute should be specified!";
                log.error(msg);
                throw new MalformedConfigurationFileException(msg);
            }
            
            loadProperties(iaas, iaasElt);
            loadTemplate(iaas, iaasElt);
            loadScalingOrders(iaas, iaasElt);
        }
        
        
        return iaas;
    }

    private void loadScalingOrders(IaaSProvider iaas, Element iaasElt) {

        NodeList nl = iaasElt.getElementsByTagName("scaleUpOrder");

        // there should be only one scaleUpOrder element, we neglect all the others
        if (nl != null && nl.getLength() > 0) {
            
            if (nl.getLength() > 1){
                log.warn(jcloudsConfigXMLFile +" contains more than one scaleUpOrder elements!" +
                        " Elements other than the first will be neglected.");
            }
            
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                Element prop = (Element) nl.item(0);

                try {
                    iaas.setScaleUpOrder(Integer.parseInt(prop.getTextContent()));
                }catch (NumberFormatException e) {
                    String msg = "scaleUpOrder element contained in "+jcloudsConfigXMLFile +"" +
                    		" has a value which is not an Integer value.";
                    log.error(msg, e);
                    throw new MalformedConfigurationFileException(msg, e);
                }

            }
        }
        
        
        nl = iaasElt.getElementsByTagName("scaleDownOrder");

        // there should be only one scaleUpOrder element, we neglect all the others
        if (nl != null && nl.getLength() > 0) {
            
            if (nl.getLength() > 1){
                log.warn(jcloudsConfigXMLFile +" contains more than one scaleDownOrder elements!" +
                        " Elements other than the first will be neglected.");
            }
            
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                Element prop = (Element) nl.item(0);

                try {
                    iaas.setScaleDownOrder(Integer.parseInt(prop.getTextContent()));
                }catch (NumberFormatException e) {
                    String msg = "scaleDownOrder element contained in "+jcloudsConfigXMLFile +"" +
                            " has a value which is not an Integer value.";
                    log.error(msg, e);
                    throw new MalformedConfigurationFileException(msg, e);
                }

            }
        }
    }

    private void loadTemplate(IaaSProvider iaas, Element iaasElt) {

        NodeList nl = iaasElt.getElementsByTagName("template");

        // there should be only one template element, we neglect all the others
        if (nl != null && nl.getLength() > 0) {
            
            if (nl.getLength() > 1){
                log.warn(jcloudsConfigXMLFile +" contains more than one template elements!" +
                		" Elements other than the first will be neglected.");
            }
            
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                Element prop = (Element) nl.item(0);

                iaas.setTemplate(prop.getTextContent());

            }
        }
        else{
            String msg = "Essential 'template' element has not specified in "+jcloudsConfigXMLFile;
            log.error(msg);
            throw new MalformedConfigurationFileException(msg);
        }
    }

    private void loadProperties(IaaSProvider iaas, Element iaasElt) {

        NodeList nl = iaasElt.getElementsByTagName("property");
        
        if (nl != null && nl.getLength() > 0) {
            for(int i=0; i< nl.getLength() ; i++){
                
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element prop = (Element) nl.item(i);
                    
                    if("".equals(prop.getAttribute("name")) || "".equals(prop.getAttribute("value"))){
                        String msg ="Property element's name and value attributes should be specified " +
                        		"in "+jcloudsConfigXMLFile;
                        log.error(msg);
                        throw new MalformedConfigurationFileException(msg);
                    }
                    iaas.setProperty(prop.getAttribute("name"), prop.getAttribute("value"));
                    
                }
            }
        }
    }
	
    
}
