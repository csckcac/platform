package org.wso2.carbon.brokermanager.admin.internal;


/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.wso2.carbon.broker.core.BrokerTypeDto;
import org.wso2.carbon.broker.core.Property;
import org.wso2.carbon.broker.core.exception.BrokerEventProcessingException;
import org.wso2.carbon.brokermanager.admin.internal.exception.BrokerManagerAdminServiceException;
import org.wso2.carbon.brokermanager.admin.internal.util.BrokerHolder;
import org.wso2.carbon.brokermanager.admin.internal.util.BrokerManagerHolder;
import org.wso2.carbon.brokermanager.core.BrokerConfiguration;
import org.wso2.carbon.brokermanager.core.exception.BMConfigurationException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

public class BrokerManagerAdminService extends AbstractAdmin {

    /**
     * @return Array of Broker names
     * @throws BrokerManagerAdminServiceException
     *          if broker names are empty
     */
    public String[] getBrokerNames() throws BrokerManagerAdminServiceException {
        BrokerHolder brokerHolder = BrokerHolder.getInstance();
        List<BrokerTypeDto> brokerTypeDtoList = brokerHolder.getBrokerService().getBrokerTypes();
        if (brokerTypeDtoList != null) {
            String[] brokerNames = new String[brokerTypeDtoList.size()];
            for (int index = 0; index < brokerNames.length; index++) {
                brokerNames[index] = brokerTypeDtoList.get(index).getName();
            }
            return brokerNames;
        }
        throw new BrokerManagerAdminServiceException("No Broker Names are received.");
    }

    /**
     * Get broker properties with property parameters such as isRequired,isSecured
     *
     * @param brokerName - get broker properties of this broker
     * @return broker properties
     * @throws BrokerManagerAdminServiceException
     *          if broker properties not found
     */
    public BrokerProperty[] getBrokerProperties(String brokerName)
            throws BrokerManagerAdminServiceException {
        BrokerHolder brokerHolder = BrokerHolder.getInstance();
        List<BrokerTypeDto> brokerTypeDtoList = brokerHolder.getBrokerService().getBrokerTypes();
        for (BrokerTypeDto brokerTypeDto : brokerTypeDtoList) {
            // check for broker with broker name
            if (brokerTypeDto.getName().equals(brokerName)) {
                // get broker properties
                List<Property> propertyList = brokerTypeDto.getPropertyList();
                BrokerProperty[] brokerPropertyArray = new BrokerProperty[propertyList.size()];
                for (int index = 0; index < brokerPropertyArray.length; index++) {
                    Property property = propertyList.get(index);
                    // set broker property parameters
                    brokerPropertyArray[index] = new BrokerProperty(property.getPropertyName(), "");
                    brokerPropertyArray[index].setRequired(property.isRequired());
                    brokerPropertyArray[index].setSecured(property.isSecured());
                    brokerPropertyArray[index].setDisplayName(property.getDisplayName());
                }
                return brokerPropertyArray;
            }
        }
        throw new BrokerManagerAdminServiceException("No Broker Properties are received.");
    }

    /**
     * Add Broker Configuration
     *
     * @param brokerName -name of the broker to be added
     * @param brokerType - broker type; jms,ws-event
     * @param properties - properties with values
     */
    public void addBrokerConfiguration(String brokerName, String brokerType,
                                       BrokerProperty[] properties)
            throws BrokerManagerAdminServiceException {
        BrokerManagerHolder brokerManager = BrokerManagerHolder.getInstance();
        BrokerConfiguration brokerConfiguration = new BrokerConfiguration();
        brokerConfiguration.setName(brokerName);
        brokerConfiguration.setType(brokerType);
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        // add broker properties
        for (BrokerProperty brokerProperty : properties) {
            brokerConfiguration.addProperty(brokerProperty.getKey(), brokerProperty.getValue());
        }
        // add broker configuration
        try {
            brokerManager.getBrokerManagerService().addBrokerConfiguration(brokerConfiguration, tenantId);
            testBrokerConfiguration(brokerName);
        } catch (BMConfigurationException e) {
            throw new BrokerManagerAdminServiceException("Error in adding broker Configuration", e);
        }
    }

    /**
     * Remove given broker configuration
     *
     * @param brokerName broker to be removed
     */
    public void removeBrokerConfiguration(String brokerName)
            throws BrokerManagerAdminServiceException {
        BrokerManagerHolder brokerManager = BrokerManagerHolder.getInstance();
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        try {
            brokerManager.getBrokerManagerService().removeBrokerConfiguration(brokerName, tenantId);
        } catch (BMConfigurationException e) {
            throw new BrokerManagerAdminServiceException("Error in removing broker configurations" + e);
        }
    }

    /**
     * Get broker configurations and convert to BrokerConfigurationDetails
     *
     * @return Array of BrokerConfigurationDetails
     * @throws BrokerManagerAdminServiceException
     *
     */
    public BrokerConfigurationDetails[] getAllBrokerConfigurationNamesAndTypes()
            throws BrokerManagerAdminServiceException {
        BrokerManagerHolder brokerManager = BrokerManagerHolder.getInstance();

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        // get broker configurations
        List<BrokerConfiguration> brokerConfigurationList = brokerManager.getBrokerManagerService().
                getAllBrokerConfigurations(tenantId);
        if (brokerConfigurationList != null) {
            // create broker configuration details array
            BrokerConfigurationDetails[] brokerConfigurationDetailsArray = new
                    BrokerConfigurationDetails[brokerConfigurationList.size()];
            for (int index = 0; index < brokerConfigurationDetailsArray.length; index++) {
                BrokerConfiguration brokerConfiguration = brokerConfigurationList.get(index);
                String brokerName = brokerConfiguration.getName();
                String brokerType = brokerConfiguration.getType();
                Map<String, String> propertiesMap = brokerConfiguration.getProperties();

                // create broker configuration details with broker name and type
                brokerConfigurationDetailsArray[index] = new BrokerConfigurationDetails(
                        brokerName, brokerType, propertiesMap.size());
                // add broker properties
                for (Map.Entry entry : propertiesMap.entrySet()) {
                    brokerConfigurationDetailsArray[index].addBrokerProperty(entry.getKey().toString(),
                                                                             entry.getValue().toString());
                }

            }
            return brokerConfigurationDetailsArray;
        } else {
            throw new BrokerManagerAdminServiceException("No Broker Configurations received.");
        }

    }

    /**
     * Get broker details
     *
     * @param brokerName
     * @return Array of Broker properties including parameters such as isSecured, isRequired
     * @throws BrokerManagerAdminServiceException
     *          if broker configuration not found
     */
    public BrokerProperty[] getBrokerConfiguration(String brokerName)
            throws BrokerManagerAdminServiceException {
        BrokerManagerHolder brokerManager = BrokerManagerHolder.getInstance();
        // get broker to get broker properties with parameters isSecured, isRequired
        BrokerHolder brokerHolder = BrokerHolder.getInstance();
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        BrokerConfiguration brokerConfiguration = brokerManager.getBrokerManagerService().
                getBrokerConfiguration(brokerName, tenantId);
        if (brokerConfiguration != null) {
            // get broker type
            String brokerType = brokerConfiguration.getType();
            // get broker properties
            List<Property> propertyList = brokerHolder.getBrokerService().getBrokerProperties(
                    brokerType);
            Map<String, String> brokerProperties = brokerConfiguration.getProperties();
            BrokerProperty[] brokerPropertyArray = new BrokerProperty[brokerProperties.size()];
            int index = 0;
            for (Property property : propertyList) {
                // create broker property
                brokerPropertyArray[index] = new BrokerProperty(property.getPropertyName(),
                                                                brokerProperties.get(property.
                                                                        getPropertyName()));
                // set broker property parameters
                brokerPropertyArray[index].setSecured(property.isSecured());
                brokerPropertyArray[index].setRequired(property.isRequired());
                brokerPropertyArray[index].setDisplayName(property.getDisplayName());
                index++;
            }
            return brokerPropertyArray;
        } else {
            throw new BrokerManagerAdminServiceException("No such broker exists.");
        }
    }

    private void testBrokerConfiguration(String brokerName)
            throws BrokerManagerAdminServiceException {
        BrokerHolder brokerHolder = BrokerHolder.getInstance();
        BrokerManagerHolder brokerManager = BrokerManagerHolder.getInstance();

        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        BrokerConfiguration brokerConfiguration =
                brokerManager.getBrokerManagerService().getBrokerConfiguration(brokerName, tenantId);
        org.wso2.carbon.broker.core.BrokerConfiguration configuration =
                new org.wso2.carbon.broker.core.BrokerConfiguration();
        configuration.setName(brokerConfiguration.getName());
        configuration.setType(brokerConfiguration.getType());
        configuration.setProperties(brokerConfiguration.getProperties());
        XMLStreamReader reader1 = null;
        String testMessage = " <brokerConfigurationTest>\n" +
                             "   <message>This is a test message.</message>\n" +
                             "   </brokerConfigurationTest>";
        try {
            reader1 = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(testMessage.getBytes()));
            StAXOMBuilder builder1 = new StAXOMBuilder(reader1);
            brokerHolder.getBrokerService().publish(configuration, "test", builder1.getDocumentElement());
        } catch (XMLStreamException e) {
            removeBrokerConfiguration(brokerName);
            throw new BrokerManagerAdminServiceException("Failed to prepare test message to " +
                                                         " publish to broker:" + brokerName, e);
        } catch (BrokerEventProcessingException e) {
            removeBrokerConfiguration(brokerName);
            throw new BrokerManagerAdminServiceException("Error at testing broker configuration with name"
                                                         + brokerName + ". " + e.getMessage(), e);
        }
    }

}