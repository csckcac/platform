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
import java.util.Map.Entry;

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
import org.wso2.carbon.autoscaler.service.util.ServiceTemplate;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Responsible for reading the JClouds configuration file.
 * Following is a sample XML.
 *
 * &lt;jcloudConfig&gt;
 *      &lt;iaasProviders&gt;
 *          &lt;iaasProvider name="ec2"&gt;
 *              &lt;provider&gt;aws-ec2&lt;/provider&gt;
 *              &lt;identity&gt;aaa&lt;/identity&gt;
 *              &lt;credential&gt;aaaa&lt;/credential&gt;
 *              &lt;scaleUpOrder&gt;1&lt;/scaleUpOrder&gt;
 *              &lt;scaleDownOrder&gt;2&lt;/scaleDownOrder&gt;
 *              &lt;property name="A" value="a"/&gt;
 *              &lt;property name="B" value="b"/&gt;
 *              &lt;template&gt;temp1&lt;/template&gt;
 *          &lt;/iaasProvider&gt;
 *          
 *          &lt;iaasProvider name="lxc"&gt;
 *              &lt;provider&gt;aws-ec2&lt;/provider&gt;
 *              &lt;identity&gt;aaa&lt;/identity&gt;
 *              &lt;credential&gt;aaaa&lt;/credential&gt;
 *              &lt;scaleUpOrder&gt;2&lt;/scaleUpOrder&gt;
 *              &lt;scaleDownOrder&gt;1&lt;/scaleDownOrder&gt;
 *              &lt;property name="X" value="x"/&gt;
 *              &lt;property name="Y" value="y"/&gt;
 *              &lt;template&gt;temp2&lt;/template&gt;
 *          &lt;/iaasProvider&gt;
 *      &lt;/iaasProviders&gt;
 *      $lt;services&gt;
 *          $lt;default&gt;
 *              $lt;property name="availabilityZone" value="us-east-1c"/&gt;
 *              $lt;property name="securityGroups" value="manager,cep,mb,default"/&gt;
 *              $lt;property name="instanceType" value="m1.large"/&gt;
 *              $lt;property name="keyPair" value="aa"/&gt;
 *          $lt;/default&gt;
 *          $lt;service domain="wso2.as.domain"&gt;
 *              $lt;property name="securityGroups" value="manager,default"/&gt;
 *              $lt;property name="payload" value="resources/as.zip"/&gt;
 *          $lt;/service&gt;
 *      $lt;/services&gt;
 *  &lt;/jcloudConfig&gt;
 */
public class AutoscalerConfigFileReader {
	
	private static final Log log = LogFactory.getLog(AutoscalerConfigFileReader.class);

	//get the factory
	private DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	private Document dom;

	/**
	 * Path to Autoscaler config XML file, which specifies the Iaas specific details.
	 */
	private String autoscalerConfigXMLFile;
	
	
	public AutoscalerConfigFileReader(){
	    
	    autoscalerConfigXMLFile = 
	            CarbonUtils.getCarbonConfigDirPath() + File.separator + "autoscaler-config.xml";
	    
		/**
		 * Parse the configuration file.
		 */
		try {
			//Using factory, get an instance of document builder
			DocumentBuilder db = dbf.newDocumentBuilder();

			//parse using builder to get DOM representation of the XML file
			dom = db.parse(autoscalerConfigXMLFile);
			

		}catch(Exception ex) {
			String msg = "Error occurred while parsing the "+autoscalerConfigXMLFile+".";
			log.error(msg, ex);
			throw new MalformedConfigurationFileException(msg, ex);
		}
	}
	
	public AutoscalerConfigFileReader(String file) {
        
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
            throw new MalformedConfigurationFileException(msg, ex);
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
	        String msg = "Essential 'iaasProvider' element cannot be found in "+autoscalerConfigXMLFile;
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
            
            iaas.setProperties(loadProperties(iaasElt));
            loadTemplate(iaas, iaasElt);
            loadScalingOrders(iaas, iaasElt);
            loadProvider(iaas, iaasElt);
            loadIdentity(iaas, iaasElt);
            loadCredentials(iaas, iaasElt);
        }
        
        
        return iaas;
    }
    
    
    public List<ServiceTemplate> getTemplates() {
        
        List<ServiceTemplate> templates = new ArrayList<ServiceTemplate>();
        
        // build default template object
        ServiceTemplate template = new ServiceTemplate();
        
        Element docEle = dom.getDocumentElement();
        NodeList nl = docEle.getElementsByTagName("default");
        
        if (nl != null && nl.getLength() > 0) {

            Node item = nl.item(0);

            if (item.getNodeType() == Node.ELEMENT_NODE) {
                Element defaultElt = (Element) item;

                template.setProperties(loadProperties(defaultElt));
            }

        }
        
        // append / overwrite the default template object with values in each domain
        nl = docEle.getElementsByTagName("service");
        
        if (nl != null && nl.getLength() > 0) {

            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);

                // clone the default template to an independent object
                try {
                    ServiceTemplate temp = (ServiceTemplate) template.clone();

                    if (item.getNodeType() == Node.ELEMENT_NODE) {
                        Element imageElt = (Element) item;

                        if ("".equals(imageElt.getAttribute("domain"))) {
                            String msg =
                                "Essential 'domain' attribute of 'image' element" +
                                    " cannot be found in " + autoscalerConfigXMLFile;
                            log.error(msg);
                            throw new MalformedConfigurationFileException(msg);
                        }

                        // set domain name
                        temp.setDomainName(imageElt.getAttribute("domain"));
                        
                        // load custom properties
                        Map<String, String> customProperties = loadProperties(imageElt);
                        
                        // add custom properties (overwrite default properties where necessary)
                        for (Entry<String, String> pair : customProperties.entrySet()) {
                            temp.setProperty(pair.getKey(), pair.getValue());
                        }

                    }
                    
                    // add each domain specific template to list
                    templates.add(temp);

                } catch (CloneNotSupportedException e) {
                    String msg = "This is extraordinary!! ";
                    log.error(msg, e);
                    throw new RuntimeException(msg, e);
                }
            }
        }
        
        return templates;
    }
    
    public Map<String, String> getDomainToTemplateMap() {
        
        Map<String, String> domainToTemplateMap = new HashMap<String, String>();
        
        Element docEle = dom.getDocumentElement();
        
        // load images to the map
        NodeList nl = docEle.getElementsByTagName("image");
        
        if (nl != null && nl.getLength() > 0) {
            
            for(int i=0; i< nl.getLength() ; i++){
                Node item = nl.item(i);
                
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    Element imageElt = (Element) item;
                    
                    if (!"".equals(imageElt.getAttribute("domain"))) {
                        domainToTemplateMap.put(imageElt.getAttribute("domain"),
                                                imageElt.getAttribute("payload"));
                    }
                }
            }
            
        }
        
        // load default template to the map
        nl = docEle.getElementsByTagName("default");
        
        if (nl != null && nl.getLength() > 0) {
            
            Node item = nl.item(0);
            
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                Element imageElt = (Element) item;
                
                if (!"".equals(imageElt.getAttribute("payload"))) {
                    // default template's domain is denoted by a '*' 
                    domainToTemplateMap.put("*",
                                            imageElt.getAttribute("payload"));
                }
            }
            
        }
        
        // if no images specified, the config file is a malformed one.
        if(domainToTemplateMap.size()==0){
            String msg = "Essential 'images' element cannot be found in "+autoscalerConfigXMLFile;
            log.error(msg);
            throw new MalformedConfigurationFileException(msg);
        }
        
        return domainToTemplateMap;
    }

    private void loadCredentials(IaaSProvider iaas, Element iaasElt) {

        NodeList nl = iaasElt.getElementsByTagName("credential");

        // there should be only one credential element, we neglect all the others
        if (nl != null && nl.getLength() > 0) {
            
            if (nl.getLength() > 1){
                log.warn(autoscalerConfigXMLFile +" contains more than one credential elements!" +
                        " Elements other than the first will be neglected.");
            }
            
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                Element prop = (Element) nl.item(0);

                iaas.setCredential(prop.getTextContent());

            }
        }
        else{
            String msg = "Essential 'credential' element has not specified in "+autoscalerConfigXMLFile;
            log.error(msg);
            throw new MalformedConfigurationFileException(msg);
        }
    }

    private void loadIdentity(IaaSProvider iaas, Element iaasElt) {

        NodeList nl = iaasElt.getElementsByTagName("identity");

        // there should be only one identity element, we neglect all the others
        if (nl != null && nl.getLength() > 0) {
            
            if (nl.getLength() > 1){
                log.warn(autoscalerConfigXMLFile +" contains more than one identity elements!" +
                        " Elements other than the first will be neglected.");
            }
            
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                Element prop = (Element) nl.item(0);

                iaas.setIdentity(prop.getTextContent());

            }
        }
        else{
            String msg = "Essential 'identity' element has not specified in "+autoscalerConfigXMLFile;
            log.error(msg);
            throw new MalformedConfigurationFileException(msg);
        }
    }

    private void loadProvider(IaaSProvider iaas, Element iaasElt) {

        NodeList nl = iaasElt.getElementsByTagName("provider");

        // there should be only one provider element, we neglect all the others
        if (nl != null && nl.getLength() > 0) {
            
            if (nl.getLength() > 1){
                log.warn(autoscalerConfigXMLFile +" contains more than one provider elements!" +
                        " Elements other than the first will be neglected.");
            }
            
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                Element prop = (Element) nl.item(0);

                iaas.setProvider(prop.getTextContent());

            }
        }
        else{
            String msg = "Essential 'provider' element has not specified in "+autoscalerConfigXMLFile;
            log.error(msg);
            throw new MalformedConfigurationFileException(msg);
        }
    }

    private void loadScalingOrders(IaaSProvider iaas, Element iaasElt) {

        NodeList nl = iaasElt.getElementsByTagName("scaleUpOrder");

        // there should be only one scaleUpOrder element, we neglect all the others
        if (nl != null && nl.getLength() > 0) {
            
            if (nl.getLength() > 1){
                log.warn(autoscalerConfigXMLFile +" contains more than one scaleUpOrder elements!" +
                        " Elements other than the first will be neglected.");
            }
            
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                Element prop = (Element) nl.item(0);

                try {
                    iaas.setScaleUpOrder(Integer.parseInt(prop.getTextContent()));
                }catch (NumberFormatException e) {
                    String msg = "scaleUpOrder element contained in "+autoscalerConfigXMLFile +"" +
                    		" has a value which is not an Integer value.";
                    log.error(msg, e);
                    throw new MalformedConfigurationFileException(msg, e);
                }

            }
        }
        else{
            String msg = "Essential 'scaleUpOrder' element has not specified in "+autoscalerConfigXMLFile;
            log.error(msg);
            throw new MalformedConfigurationFileException(msg);
        }
        
        
        nl = iaasElt.getElementsByTagName("scaleDownOrder");

        // there should be only one scaleUpOrder element, we neglect all the others
        if (nl != null && nl.getLength() > 0) {
            
            if (nl.getLength() > 1){
                log.warn(autoscalerConfigXMLFile +" contains more than one scaleDownOrder elements!" +
                        " Elements other than the first will be neglected.");
            }
            
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                Element prop = (Element) nl.item(0);

                try {
                    iaas.setScaleDownOrder(Integer.parseInt(prop.getTextContent()));
                }catch (NumberFormatException e) {
                    String msg = "scaleDownOrder element contained in "+autoscalerConfigXMLFile +"" +
                            " has a value which is not an Integer value.";
                    log.error(msg, e);
                    throw new MalformedConfigurationFileException(msg, e);
                }

            }
        }
        else{
            String msg = "Essential 'scaleDownOrder' element has not specified in "+autoscalerConfigXMLFile;
            log.error(msg);
            throw new MalformedConfigurationFileException(msg);
        }
    }

    private void loadTemplate(IaaSProvider iaas, Element iaasElt) {

        NodeList nl = iaasElt.getElementsByTagName("template");

        // there should be only one template element, we neglect all the others
        if (nl != null && nl.getLength() > 0) {
            
            if (nl.getLength() > 1){
                log.warn(autoscalerConfigXMLFile +" contains more than one template elements!" +
                		" Elements other than the first will be neglected.");
            }
            
            if (nl.item(0).getNodeType() == Node.ELEMENT_NODE) {
                Element prop = (Element) nl.item(0);

                iaas.setTemplate(prop.getTextContent());

            }
        }
        else{
            String msg = "Essential 'template' element has not specified in "+autoscalerConfigXMLFile;
            log.error(msg);
            throw new MalformedConfigurationFileException(msg);
        }
    }

    private Map<String, String> loadProperties(Element iaasElt) {

        Map<String, String> propertyMap = new HashMap<String, String>();
        
        NodeList nl = iaasElt.getElementsByTagName("property");
        
        if (nl != null && nl.getLength() > 0) {
            for(int i=0; i< nl.getLength() ; i++){
                
                if (nl.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element prop = (Element) nl.item(i);
                    
                    if("".equals(prop.getAttribute("name")) || "".equals(prop.getAttribute("value"))){
                        String msg ="Property element's, name and value attributes should be specified " +
                        		"in "+autoscalerConfigXMLFile;
                        log.error(msg);
                        throw new MalformedConfigurationFileException(msg);
                    }
                    propertyMap.put(prop.getAttribute("name"), prop.getAttribute("value"));
                    
                }
            }
        }
        
        return propertyMap;
    }
	
    
}
