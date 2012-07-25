/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.cep.core.internal.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cep.core.Bucket;
import org.wso2.carbon.cep.core.exception.CEPConfigurationException;
import org.wso2.carbon.cep.core.internal.util.CEPConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public class ProviderConfigurationHelper {
    private static final Log log = LogFactory.getLog(ProviderConfigurationHelper.class);

    public static Properties fromOM(OMElement configurationElement) {

        Properties properties = new Properties();

        for (Iterator iter = configurationElement.getChildrenWithName(new QName(CEPConstants.CEP_CONF_NAMESPACE,
                                                                                CEPConstants.CEP_CONF_ELE_PROPERTY)); iter.hasNext(); ) {
            OMElement propertyElement = (OMElement) iter.next();

            if (propertyElement.getAttribute(new QName(CEPConstants.CEP_CONT_ATTR_NAME)) != null) {
                String name = propertyElement.getAttribute(new QName(CEPConstants.CEP_CONT_ATTR_NAME)).getAttributeValue();
                String value = propertyElement.getText();
                properties.setProperty(name, value);
            }
        }
        return properties;
    }

    public static void addProviderConfigurationToRegistry(Properties configProperties,
                                                          Registry registry,
                                                          String parentCollectionPath)
            throws CEPConfigurationException {
        try {
            String providerConfigPath = parentCollectionPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_PROVIDER_CONFIG;
            Collection providerConfig = registry.newCollection();
            for (Map.Entry entry : configProperties.entrySet()) {
                providerConfig.addProperty(entry.getKey().toString(), entry.getValue().toString());
            }
            registry.put(providerConfigPath, providerConfig);
        } catch (RegistryException e) {
            String errorMessage = "Can not add Provider Configuration to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static void loadProviderConfigurationFromRegistry(Registry registry, Bucket bucket,
                                                             String names)
            throws CEPConfigurationException {
        try {
            Collection inputsCollection = (Collection) registry.get(names);
            Properties properties = inputsCollection.getProperties();
            if (properties.size() > 0) {
                bucket.setProviderConfiguration(properties);
            }
        } catch (RegistryException e) {
            String errorMessage = "Can not load inputs from registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static void modifyProviderConfigurationToRegistry(Bucket bucket,
                                                             Registry registry,
                                                             String parentCollectionPath)
            throws CEPConfigurationException {
        try {
            String providerConfigPath = parentCollectionPath + CEPConstants.CEP_REGISTRY_BS + CEPConstants.CEP_REGISTRY_PROVIDER_CONFIG;
            if (registry.resourceExists(providerConfigPath)) {
                registry.delete(providerConfigPath);
                registry.commitTransaction();
            }
            Properties configProperties = bucket.getProviderConfiguration();
            Collection providerConfig = registry.newCollection();
            for (Map.Entry entry : configProperties.entrySet()) {
                providerConfig.addProperty(entry.getKey().toString(), entry.getValue().toString());
            }
            registry.put(providerConfigPath, providerConfig);
        } catch (RegistryException e) {
            String errorMessage = "Can not add Provider Configuration to the registry ";
            log.error(errorMessage, e);
            throw new CEPConfigurationException(errorMessage, e);
        }
    }

    public static OMElement providerConfigurationToOM(Properties properties) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement propertyConfigChild = factory.createOMElement(new QName(
                CEPConstants.CEP_CONF_NAMESPACE,
                CEPConstants.CEP_CONF_ELE_PROVIDER_CONFIG,
                CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
        for (Map.Entry entry : properties.entrySet()) {
            OMElement propertyOmElement = factory.createOMElement(new QName(
                    CEPConstants.CEP_CONF_NAMESPACE,
                    CEPConstants.CEP_CONF_ELE_PROPERTY,
                    CEPConstants.CEP_CONF_CEP_NAME_SPACE_PREFIX));
            propertyOmElement.setText(entry.getValue().toString());
            propertyOmElement.addAttribute(CEPConstants.CEP_CONF_ELE_NAME, entry.getKey().toString(),
                                           null);
            propertyConfigChild.addChild(propertyOmElement);
        }
        return propertyConfigChild;
    }

}
