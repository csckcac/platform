/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.identity.scim.consumer.config;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.wso2.carbon.identity.scim.consumer.utils.IdentitySCIMException;
import org.wso2.carbon.identity.scim.consumer.utils.SCIMConsumerConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This reads scim-consumer-config.xml and initializes and populates SCIMConsumersConfig.
 */
public class SCIMConsumerConfigProcessor {

    private static final String SCIM_CONSUMER_CONFIG = "scim-consumer-config.xml";

    private BundleContext bundleContext;

    private static Log log = LogFactory.getLog(SCIMConsumerConfigProcessor.class);

    /*Set the bundle context to read the config file from resources folder of the bundle*/
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * Build and populate SCIMConsumersConfig reading the scim-consumer-config.xml file
     *
     * @return
     * @throws org.wso2.carbon.identity.scim.consumer.utils.IdentitySCIMException
     *
     */
    public void buildSCIMConsumersConfigFromFile()
            throws IdentitySCIMException {

        try {
            //read the file and get root element
            OMElement configElement = getConfigElement();
            //initializes the SCIMConsumersConfig
            buildSCIMConsumersConfiguration(configElement);
        } catch (XMLStreamException e) {
            String errorMessage = "Error in reading scim-consumer-config.xml";
            log.error(errorMessage);
            throw new IdentitySCIMException(errorMessage);
        } catch (IOException e) {
            String errorMessage = "Error in reading scim-consumer-config.xml file.";
            log.error(errorMessage);
            throw new IdentitySCIMException(errorMessage);
        }
    }

    /**
     * Read and extract SCIM Consumers, reading the root element of scim-consumer-config.xml
     *
     * @param configElement
     * @return
     * @throws IdentitySCIMException
     */
    public void buildSCIMConsumersConfiguration(OMElement configElement)
            throws IdentitySCIMException {
        OMElement scimConsumersElement = configElement.getFirstElement();
        //List to hold all the scim consumer configuration property maps
        List<Map<String,String>> consumerPropertiesList = new ArrayList<Map<String,String>>();

        //read all the individual consumer elements
        Iterator<OMElement> ite = scimConsumersElement.getChildrenWithLocalName(
                SCIMConsumerConstants.ELEMENT_NAME_SCIM_CONSUMER);
        while (ite.hasNext()) {
            OMElement consumerElement = ite.next();
            //read property elements in a consumer element
            Map<String,String> propertiesMap = readChildPropertyElements(consumerElement);
            consumerPropertiesList.add(propertiesMap);
        }
        //hand over the read configs to SCIMConsumersConfig
        SCIMConsumersConfig.setScimConsumers(consumerPropertiesList);
    }

    private Map<String, String> readChildPropertyElements(OMElement consumerElement) {

        Map<String, String> scimConsumerConfigProperties = new HashMap<String, String>();
        Iterator ite = consumerElement.getChildrenWithName(new QName(
                SCIMConsumerConstants.ELEMENT_NAME_PROPERTY));
        while (ite.hasNext()) {
            OMElement propertyElement = (OMElement) ite.next();
            String propertyName = propertyElement.getAttributeValue(new QName(
                    SCIMConsumerConstants.ATTRIBUTE_NAME));
            String propertyValue = propertyElement.getText();
            scimConsumerConfigProperties.put(propertyName, propertyValue);
        }
        return scimConsumerConfigProperties;
    }

    private OMElement getConfigElement() throws IOException, XMLStreamException {
        InputStream inStream = null;
        File scimConsumerConfigXml = new File(CarbonUtils.getCarbonConfigDirPath(), SCIM_CONSUMER_CONFIG);
        if (scimConsumerConfigXml.exists()) {
            inStream = new FileInputStream(scimConsumerConfigXml);
        }

        String warningMessage = "";
        if (inStream == null) {
            URL url;
            if (bundleContext != null) {
                if ((url = bundleContext.getBundle().getResource(SCIM_CONSUMER_CONFIG)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Bundle context could not find resource "
                                     + SCIM_CONSUMER_CONFIG
                                     + " or user does not have sufficient permission to access the resource.";
                }
            } else {
                if ((url = this.getClass().getClassLoader().getResource(SCIM_CONSUMER_CONFIG)) != null) {
                    inStream = url.openStream();
                } else {
                    warningMessage = "Could not find resource "
                                     + SCIM_CONSUMER_CONFIG
                                     + " or user does not have sufficient permission to access the resource.";
                }
            }
        }

        if (inStream == null) {
            String message = "Tenant configuration not found. Cause - " + warningMessage;
            if (log.isDebugEnabled()) {
                log.debug(message);
            }
            throw new FileNotFoundException(message);
        }

        StAXOMBuilder builder = new StAXOMBuilder(inStream);
        OMElement documentElement = builder.getDocumentElement();

        if (inStream != null) {
            inStream.close();
        }

        return documentElement;
    }


}
