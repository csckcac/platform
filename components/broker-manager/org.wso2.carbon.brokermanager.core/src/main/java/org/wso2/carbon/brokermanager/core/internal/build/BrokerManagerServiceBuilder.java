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

package org.wso2.carbon.brokermanager.core.internal.build;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.brokermanager.core.BrokerManagerService;
import org.wso2.carbon.brokermanager.core.exception.BMConfigurationException;
import org.wso2.carbon.brokermanager.core.internal.CarbonBrokerManagerService;
import org.wso2.carbon.brokermanager.core.internal.config.BrokerConfigurationHelper;
import org.wso2.carbon.brokermanager.core.internal.util.BMConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

/**
 * this class creates the broker manager service by reading the values from the carbon.xml file
 *
 */
public class BrokerManagerServiceBuilder {

    private static final Log log = LogFactory.getLog(BrokerManagerServiceBuilder.class);
    private static CarbonBrokerManagerService carbonBrokerManagerService;

    /**
     * creates the service by reading the values.
     * @return - broker configuration service.
     * @throws BMConfigurationException
     */
    public static BrokerManagerService createBrokerManagerService() throws BMConfigurationException {

        carbonBrokerManagerService = new CarbonBrokerManagerService();
        carbonBrokerManagerService.loadConfigurations(MultitenantConstants.SUPER_TENANT_ID);
        OMElement brokerManagerConfig = loadConfigXML();

        if (brokerManagerConfig != null) {
            if (!brokerManagerConfig.getQName().equals(new QName(BMConstants.BM_CONF_NS,
                    BMConstants.BM_ELE_ROOT_ELEMENT))) {
                throw new BMConfigurationException("Invalid root element "
                        + brokerManagerConfig.getQName());
            }
            Iterator brokersIter = brokerManagerConfig.getChildrenWithName(
                    new QName(BMConstants.BM_CONF_NS, BMConstants.BM_ELE_BROKER_CONFIGURATION));
            for (; brokersIter.hasNext();) {
                OMElement brokerOMElement = (OMElement) brokersIter.next();
                carbonBrokerManagerService.addBrokerConfigurationForSuperTenant(
                        BrokerConfigurationHelper.fromOM(brokerOMElement));
            }
        }

        return carbonBrokerManagerService;
    }

    /**
     * Helper method to load the event config
     *
     * @return OMElement representation of the event config
     */
    private static OMElement loadConfigXML() throws BMConfigurationException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + BMConstants.BM_CONF;

        File configFile = new File(path);

        if (!configFile.exists()){
           log.info("The " + BMConstants.BM_CONF + " can not found ");
           return null;
        }

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(configFile));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            throw new BMConfigurationException(" carbon.xml "
                    + "cannot be found in the path : " + path, e);
        } catch (XMLStreamException e) {
            throw new BMConfigurationException("Invalid XML for " + "carbon.xml"
                    + " located in the path : " + path, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ingored) {
                throw new BMConfigurationException("Can not close the input stream");
            }
        }
    }

    public static void loadConfigurationsFromRegistry() {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        try {
            carbonBrokerManagerService.loadConfigurations(tenantId);
        } catch (BMConfigurationException e) {
          log.error("Error in loading configurations for the tenant :" +tenantId, e);
        }
    }

}
