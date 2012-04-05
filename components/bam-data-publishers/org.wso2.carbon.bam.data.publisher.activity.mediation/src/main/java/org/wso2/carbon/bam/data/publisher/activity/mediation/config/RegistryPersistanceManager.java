/*
 * Copyright 2005-2007 WSO2, Inc. (http://wso2.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.wso2.carbon.bam.data.publisher.activity.mediation.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.activity.mediation.ActivityPublisherConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import java.util.ArrayList;

/**
 * Registry persistence manager handles persisting of eventing configuration data to the registry as
 * well as loading the configuration from the registry.
 */
public class RegistryPersistanceManager {
    private static Registry registry;
    private static final String SEPERATOR = "/";

    private static final Log log = LogFactory.getLog(RegistryPersistanceManager.class);

    private static EventingConfigData eventingConfigData = new EventingConfigData();

    public RegistryPersistanceManager() {
      load();
      //loadConfsToMap();
    }

    public static void setRegistry(Registry registryParam) {
        registry = registryParam;

    }

    public String getConfigurationProperty(String propertyName) throws RegistryException {
        String resourcePath = ActivityPublisherConstants.STATISTISTICS_REG_PATH + SEPERATOR + propertyName;
        String value = null;
        if (registry.resourceExists(resourcePath)) {
            Resource resource = registry.get(resourcePath);
            value = resource.getProperty(propertyName);
        }
        return value;
    }

    public String[] getMultiValuedConfigurationProperty(String propertyName)
            throws RegistryException {
        String resourcePath = ActivityPublisherConstants.STATISTISTICS_REG_PATH + SEPERATOR + propertyName;
        ArrayList<String> values = new ArrayList<String>();

        if (registry.resourceExists(resourcePath)) {
            Resource resource = registry.get(resourcePath);

            int counter = 0;
            String propertyValue = resource.getProperty(propertyName + counter);
            while (propertyValue != null) {
                values.add(propertyValue);
                propertyValue = resource.getProperty(propertyName + (++counter));
            }

        }

        return values.toArray(new String[]{});
    }

    public synchronized void updateConfigurationProperty(String propertyName, String value)
            throws RegistryException {
        String resourcePath = ActivityPublisherConstants.STATISTISTICS_REG_PATH + SEPERATOR + propertyName;
        Resource resource;
        if (registry != null) {
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();
                resource.addProperty(propertyName, value);
                registry.put(resourcePath, resource);
            } else {
                resource = registry.get(resourcePath);
                resource.setProperty(propertyName, value);
                registry.put(resourcePath, resource);
            }
        }
        //same time update the map
        //userConfMap.put(propertyName,value);
    }

    public void updateMultivaluedConfigurationProperty(String propertyName, String[] values)
            throws RegistryException {
        String resourcePath = ActivityPublisherConstants.STATISTISTICS_REG_PATH + SEPERATOR + propertyName;
        Resource resource;
        if (registry != null) {
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();

                for (int i = 0; i < values.length; i++) {
                    String property = propertyName + i;
                    resource.addProperty(property, values[i]);
                }
            } else {
                resource = registry.get(resourcePath);
                clearProperties(resource, propertyName);

                for (int i = 0; i < values.length; i++) {
                    String property = propertyName + i;
                    resource.setProperty(property, values[i]);
                }
            }

            registry.put(resourcePath, resource);
        }

    }

    private void clearProperties(Resource resource, String propertyName) {

        int counter = 0;
        String value = resource.getProperty(propertyName + counter);
        while (value != null) {
            resource.removeProperty(propertyName + counter);
            counter = counter + 1;
            value = resource.getProperty(propertyName + counter);
        }
    }

    private void clearXpathProperties(Resource resource) {

        resource.removeProperty(ActivityPublisherConstants.XPATH_PROPERTY);

        int counter = 0;
        String value = resource.getProperty(ActivityPublisherConstants.NAMESAPCE_PROPERTY_PREFIX + counter);
        while (value != null) {
            resource.removeProperty(ActivityPublisherConstants.NAMESAPCE_PROPERTY_PREFIX + counter);
            counter = counter + 1;
            value = resource.getProperty(ActivityPublisherConstants.NAMESAPCE_PROPERTY_PREFIX + counter);
        }

    }

    /**
     * Loads configuration from Registry.
     */
    private void load() {

        // First set it to defaults, but do not persist
        eventingConfigData.setEnableEventing(ActivityPublisherConstants.ENABLE_EVENTING_DEFAULT);
        eventingConfigData.setMessageThreshold(ActivityPublisherConstants.MESSAGE_THRESHOLD_DEFAULT);
        eventingConfigData.setEnableMessageLookup(ActivityPublisherConstants.MESSAGE_LOOKUP_DEFAULT);
        eventingConfigData.setXPathExpressions(new String[]{ActivityPublisherConstants.XPATH_EXPRESSION});
        eventingConfigData.setEnableMessageDumping(ActivityPublisherConstants.MESSAGE_DUMPING_DEFAULT);

        // then load it from registry
        try {
            String eventingStatus = getConfigurationProperty(ActivityPublisherConstants.ENABLE_EVENTING);
            String messageLookupStatus = getConfigurationProperty(ActivityPublisherConstants.ENABLE_MESSAGE_LOOKUP);
            String messageDumpingStatus = getConfigurationProperty(ActivityPublisherConstants.ENABLE_MESSAGE_DUMPING);
            //String msgFlush = getConfigurationProperty(ActivityPublisherConstants.ENABLE_MESSAGE_FLUSH);
            if (eventingStatus != null) { // Registry has eventing config
                eventingConfigData.setEnableEventing(eventingStatus);
                eventingConfigData.setMessageThreshold(Integer
                        .parseInt(getConfigurationProperty(ActivityPublisherConstants.MESSAGE_THRESHOLD)));

            }
            if (messageLookupStatus != null) {
                eventingConfigData.setEnableMessageLookup(messageLookupStatus);
                eventingConfigData
                        .setXPathExpressions(getMultiValuedConfigurationProperty(ActivityPublisherConstants.XPATH_EXPRESSION));
            }
            if (messageDumpingStatus != null) {
                eventingConfigData.setEnableMessageDumping(messageDumpingStatus);
            }
            //eventingConfigData.setMsgFlush(msgFlush);
            update(eventingConfigData);

        } catch (Exception e) {
           log.error("Could not load properties from registry",e);
        }
    }

    /**
     * Updates the Registry with given config data.
     *
     * @param eventingConfigData eventing configuration data
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          thrown when updating the registry properties fails.
     */
    public void update(EventingConfigData eventingConfigData) throws RegistryException {

       //if ("OFF".equals(eventingConfigData.getMsgFlush())) {
            updateConfigurationProperty(ActivityPublisherConstants.ENABLE_EVENTING, eventingConfigData
                .getEnableEventing());
            updateConfigurationProperty(ActivityPublisherConstants.MESSAGE_THRESHOLD, Integer
                .toString(eventingConfigData.getMessageThreshold()));
            updateConfigurationProperty(ActivityPublisherConstants.ENABLE_MESSAGE_LOOKUP, eventingConfigData
                .getEnableMessageLookup());
//            updateMultivaluedConfigurationProperty(ActivityPublisherConstants.XPATH_EXPRESSION, eventingConfigData
//                    .getXPathExpressions());

            updateConfigurationProperty(ActivityPublisherConstants.ENABLE_MESSAGE_DUMPING, eventingConfigData
                .getEnableMessageDumping());
            //updateConfigurationProperty(ActivityPublisherConstants.ENABLE_MESSAGE_FLUSH, eventingConfigData.getMsgFlush());
/*        } else if ("ON".equals(eventingConfigData.getMsgFlush())) {
            updateConfigurationProperty(ActivityPublisherConstants.ENABLE_MESSAGE_FLUSH, eventingConfigData.getMsgFlush());
        }*/
        RegistryPersistanceManager.eventingConfigData = eventingConfigData;
    }

    /**
     * Updates the Registry with given xpath config data.
     *
     * @param xpathConfigData xpath configuration data
     * @throws org.wso2.carbon.registry.core.exceptions.RegistryException
     *          thrown when updating the registry properties fails or when the added xpath key is not unique.
     */
    public void update(XPathConfigData xpathConfigData) throws RegistryException {
        String resourcePath = ActivityPublisherConstants.STATISTISTICS_REG_PATH + SEPERATOR +
                              ActivityPublisherConstants.XPATH_ROOT_PATH + SEPERATOR + xpathConfigData.getKey();
        Resource resource;
        if (registry != null) {
            if (!registry.resourceExists(resourcePath)) {
                resource = registry.newResource();

                resource.addProperty(ActivityPublisherConstants.XPATH_PROPERTY, xpathConfigData.getXpath());

                String[] nameSpaces = xpathConfigData.getNameSpaces();

                if (nameSpaces != null) {
                    for (int i = 0; i < xpathConfigData.getNameSpaces().length; i++) {
                        String property = ActivityPublisherConstants.NAMESAPCE_PROPERTY_PREFIX + i;
                        resource.addProperty(property, nameSpaces[i]);
                    }
                }

            } else {
                if (xpathConfigData.isEditing()) {
                    resource = registry.get(resourcePath);
                    clearXpathProperties(resource);

                    resource.addProperty(ActivityPublisherConstants.XPATH_PROPERTY, xpathConfigData.getXpath());

                    String[] nameSpaces = xpathConfigData.getNameSpaces();

                    if (nameSpaces != null) {
                        for (int i = 0; i < xpathConfigData.getNameSpaces().length; i++) {
                            String property = ActivityPublisherConstants.NAMESAPCE_PROPERTY_PREFIX + i;
                            resource.addProperty(property, nameSpaces[i]);
                        }
                    }
                } else {
                    throw new RegistryException("XPath Expression Key should be unique..");
                }
            }

            registry.put(resourcePath, resource);
        }
    }

    /**
     * Removes the given xpath configuration in registry if exists.
     * @param xpathConfigData
     */
    public void rollback (XPathConfigData xpathConfigData) throws RegistryException {
                String resourcePath = ActivityPublisherConstants.STATISTISTICS_REG_PATH + SEPERATOR +
                              ActivityPublisherConstants.XPATH_ROOT_PATH + SEPERATOR + xpathConfigData.getKey();
        if (registry != null) {

            try {
                if (registry.resourceExists(resourcePath)) {
                    registry.delete(resourcePath);
                }
            } catch (RegistryException e) {
                log.error("Error while performing roll back..", e);
                throw e;
            }
        }
    }

    // load  values to the map
/*    private void loadConfsToMap() {
        userConfMap.put(ActivityPublisherConstants.ENABLE_EVENTING, ActivityPublisherConstants.ENABLE_EVENTING_DEFAULT);
        userConfMap.put(ActivityPublisherConstants.ENABLE_MESSAGE_DUMPING,
                        ActivityPublisherConstants.MESSAGE_DUMPING_DEFAULT);
        userConfMap.put(ActivityPublisherConstants.ENABLE_MESSAGE_LOOKUP,
                        ActivityPublisherConstants.MESSAGE_LOOKUP_DEFAULT);
        userConfMap.put(ActivityPublisherConstants.MESSAGE_THRESHOLD, String
            .valueOf(ActivityPublisherConstants.MESSAGE_THRESHOLD_DEFAULT));

        try {
            String eventingStatus = getConfigurationProperty(ActivityPublisherConstants.ENABLE_EVENTING);
            String messageDumpingStatus = getConfigurationProperty(ActivityPublisherConstants.ENABLE_MESSAGE_DUMPING);
            if (eventingStatus != null) { // Registry has eventing config
                userConfMap.put(ActivityPublisherConstants.ENABLE_EVENTING, eventingStatus);
                userConfMap.put(ActivityPublisherConstants.MESSAGE_THRESHOLD, String
                    .valueOf(getConfigurationProperty(ActivityPublisherConstants.MESSAGE_THRESHOLD)));

            }
            if (messageDumpingStatus != null) {
                userConfMap.put(ActivityPublisherConstants.ENABLE_MESSAGE_DUMPING, messageDumpingStatus);
            }

        } catch (Exception e) {
            log.error("Could not update the map", e);
        }
    }

    public Map<String, String> getUserConfMap() {
        return userConfMap;
    }*/

    public EventingConfigData getEventingConfigData() {
        return eventingConfigData;
    }

    /**
     * Gets xpath config data stored in registry.
     */
    public XPathConfigData[] getXPathConfigData() throws RegistryException {
        String resourcePath = ActivityPublisherConstants.STATISTISTICS_REG_PATH + SEPERATOR +
                              ActivityPublisherConstants.XPATH_ROOT_PATH;

        Resource resource;
        Collection collection;
        ArrayList<XPathConfigData> xpathConfigs = new ArrayList<XPathConfigData>();

        if (registry != null) {
            if (registry.resourceExists(resourcePath)) {
                resource = registry.get(resourcePath);
                if (!(resource instanceof Collection)) {
                    return new XPathConfigData[0];
                }

                collection = (Collection) resource;
                String[] children = collection.getChildren();

                if (children != null) {
                    for (String child : children) {
                        Resource xpath = registry.get(child);

                        XPathConfigData data = new XPathConfigData();
                        data.setEditing(false);
                        data.setKey(RegistryUtils.getResourceName(child));
                        data.setXpath(xpath.getProperty(ActivityPublisherConstants.XPATH_PROPERTY));


                        int counter = 0;
                        String property = ActivityPublisherConstants.NAMESAPCE_PROPERTY_PREFIX + counter;
                        String nameSpace = xpath.getProperty(property);

                        ArrayList<String> nameSpaces = new ArrayList<String>();
                        while (nameSpace != null) {
                            nameSpaces.add(nameSpace);
                            counter = counter + 1;
                            property = ActivityPublisherConstants.NAMESAPCE_PROPERTY_PREFIX + counter;
                            nameSpace = xpath.getProperty(property);
                        }

                        data.setNameSpaces(nameSpaces.toArray(new String[nameSpaces.size()]));

                        xpathConfigs.add(data);
                    }
                }

            }
        }

        return xpathConfigs.toArray(new XPathConfigData[xpathConfigs.size()]);

    }

}