/*
 * Copyright (c) 2007, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.registry.extensions.aspects;

import org.wso2.carbon.registry.core.Aspect;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.core.jdbc.handlers.filters.Filter;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.utils.NetworkUtils;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.net.MalformedURLException;
import java.util.*;
import java.net.URL;

public class DistributedLifeCycle extends Aspect {
    public static final String PROMOTE = "promote";
    public static final String DEMOTE = "demote";
    public static final String RESOURCE_TYPE = "resource";
    public static final String LITERAL_TYPE = "literal";
    public static final String FILE_TYPE = "file";
    public static final String TYPE_TAG = "type";
    public static final String REGISTRY_TAG = "registry";
    public static final String LOCATION_TAG = "location";
    public static final String URL_TAG = "registry-url";
    public static final String PASSWORD_TAG = "registry-password";
    public static final String USERNAME_TAG = "registry-username";
    public static final String NAME_TAG = "name";
    public static final String DISTRIBUTED_CONFIGURATION_TAG = "distributed-configuration";
    
    public enum ConditionEnum { isNull, equals, contains, lessThan, greaterThan }
    public enum ConfigurationFromEnum { LOCAL_RESOURCE, REMOTE_RESOURCE, FILE, LITERAL } 

    static class Condition {
        public String property;
        public ConditionEnum condition;
        public String value;

        Condition(String property, String condition, String value) {
            this.property = property;
            this.condition = ConditionEnum.valueOf(condition);
            this.value = value;
        }

        public boolean isTrue(Resource resource) {
            String propVal = resource.getProperty(property);
            if (propVal == null) {
                return condition == ConditionEnum.isNull;
            }

            switch (condition) {
                case equals:
                    return propVal.equals(value);
                case contains:
                    return propVal.indexOf(value) > -1;
                case lessThan:
                    return Integer.parseInt(propVal) < Integer.parseInt(value);
                case greaterThan:
                    return Integer.parseInt(propVal) > Integer.parseInt(value);
                default:
                    return false;
            }
        }

        public String getDescription() {
            StringBuffer ret = new StringBuffer();
            ret.append("Property '");
            ret.append(property);
            ret.append("' ");
            switch (condition) {
                case isNull:
                    ret.append("must be null");
                    break;
                case equals:
                    ret.append("must equal '");
                    ret.append(value);
                    ret.append("'");
                    break;
                case contains:
                    ret.append("must contain '");
                    ret.append(value);
                    ret.append("'");
                    break;
                case lessThan:
                    ret.append("must be less than ");
                    ret.append(value);
                    break;
                case greaterThan:
                    ret.append("must be greater than ");
                    ret.append(value);
                    break;
            }
            return ret.toString();
        }
    }

    private List<String> states = new ArrayList<String>();
    private Map<String, List<Condition>> transitions = new HashMap<String, List<Condition>>();
    private String stateProperty = "registry.lifecycle.Checklist.state";
    //boolean isConfigurationFromResource = false;
    //boolean configurationFromResourceExtracted = false;
    //String configurationResourcePath = "";

    String aspectConfigurationLocation = "/checklists/products";
    String aspectConfigurationRegistryURL = "https://10.100.1.121:9443/carbon";
    String aspectConfigurationRegistryUsername = "admin";
    String aspectConfigurationRegistryPassword = "password";

    ConfigurationFromEnum configurationFrom = ConfigurationFromEnum.LOCAL_RESOURCE;

    public DistributedLifeCycle() {
        // Lifecycle with no configuration gets the default set of states, with no conditions.
        states.add("Created");
        states.add("Tested");
        states.add("Deployed");
        states.add("Deprecated");
    }

    public DistributedLifeCycle(OMElement config) throws RegistryException {
        String myName = config.getAttributeValue(new QName(NAME_TAG));
        myName = myName.replaceAll("\\s", "");
        stateProperty = "registry.lifecycle." + myName + ".state";
        
        Iterator configurationElements = config.getChildElements();
        while (configurationElements.hasNext()) {
            OMElement configurationElement = (OMElement)configurationElements.next();

            /* expected format @ registry.xml
            <aspect name="Checklist" class="org.wso2.carbon.registry.extensions.aspects.ChecklistLifeCycle">
		        <configuration type="resource">/checklists/products</configuration>
	        </aspect>
	        <aspect name="DistributedLifeCycle" class="org.wso2.carbon.governance.registry.extensions.aspects.DistributedLifeCycle">
                <configuration type="resource">        available options resource/literal/file
                    <location>/checklists/products</location>
                    <registry-url>https://10.100.1.1</registry-url>    local or https://10.100.1.1:9445/
                    <registry-username>admin</registry-username>
                    <registry-password>admin</registry-password>
                </configuration>
            </aspect>
             */

            if (configurationElement.getAttribute(new QName(TYPE_TAG)) != null) {
                String type = configurationElement.getAttributeValue(new QName(TYPE_TAG));
                if (type.equalsIgnoreCase(RESOURCE_TYPE)) {                   
                    states.clear();
                    transitions.clear();

                    Iterator configIterator = configurationElement.getChildrenWithName(new QName(LOCATION_TAG));
                    if (configIterator.hasNext()) {
                        OMElement itemElement = (OMElement)configIterator.next();
                        aspectConfigurationLocation = itemElement.getText();
                    }

                    configIterator = configurationElement.getChildrenWithName(new QName(URL_TAG));
                    if (configIterator.hasNext()) {
                        OMElement itemElement = (OMElement)configIterator.next();
                        aspectConfigurationRegistryURL = itemElement.getText();

                        //TODO: check with registry URL and set the ConfigurationFrom enum
                        //NetworkUtils.getLocalHostname();

                         configurationFrom = ConfigurationFromEnum.REMOTE_RESOURCE;
                    }

                    configIterator = configurationElement.getChildrenWithName(new QName(USERNAME_TAG));
                    if (configIterator.hasNext()) {
                        OMElement itemElement = (OMElement)configIterator.next();
                        aspectConfigurationRegistryUsername = itemElement.getText();
                    }

                    configIterator = configurationElement.getChildrenWithName(new QName(PASSWORD_TAG));
                    if (configIterator.hasNext()) {
                        OMElement itemElement = (OMElement)configIterator.next();
                        aspectConfigurationRegistryPassword = itemElement.getText();
                    }
                    
                    break;
                }
                else if (type.equalsIgnoreCase(LITERAL_TYPE)) {
                    //lifecycle stages are specified inside the configuration itself
                    //TODO: implement/check/bugfix this
                    String name = configurationElement.getAttributeValue(new QName(NAME_TAG));
                    if (name == null) {
                        throw new IllegalArgumentException("Must have a name attribute for each state");
                    }

                    states.add(name);
                    List<Condition> conditions = null;
                    Iterator conditionIterator = configurationElement.getChildElements();
                    while (conditionIterator.hasNext()) {
                        OMElement conditionEl = (OMElement)conditionIterator.next();
                        if (conditionEl.getQName().equals(new QName("condition"))) {
                            String property = conditionEl.getAttributeValue(new QName("property"));
                            String condition = conditionEl.getAttributeValue(new QName("condition"));
                            String value = conditionEl.getAttributeValue(new QName("value"));
                            Condition c = new Condition(property, condition, value);
                            if (conditions == null) conditions = new ArrayList<Condition>();
                            conditions.add(c);
                        }
                    }
                    if (conditions != null) {
                        transitions.put(name, conditions);
                    }
                    
                    break;
                }
                else if (type.equalsIgnoreCase(FILE_TYPE)) {
                    //lifecycle stages are specified in a file
                    //TODO: implement this
                    break;
                }
            }
        }
    }

    public void associate(Resource resource, Registry registry) throws RegistryException {
        try {
            if (configurationFrom == ConfigurationFromEnum.REMOTE_RESOURCE) {
                System.setProperty("javax.net.ssl.trustStore", "CARBON_HOME/resources/security/client-truststore.jks");
                System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
                System.setProperty("javax.net.ssl.trustStoreType","JKS");

                RemoteRegistry remoteConfigurationRegisitry = new RemoteRegistry(new URL(aspectConfigurationRegistryURL),
                    aspectConfigurationRegistryUsername,
                    aspectConfigurationRegistryPassword);

                Resource configurationResource = remoteConfigurationRegisitry.get(aspectConfigurationLocation);
                String xmlContent = new String((byte[])configurationResource.getContent());   
                OMElement checklistConfiguration = AXIOMUtil.stringToOM(xmlContent);
                Iterator stateElements = checklistConfiguration.getChildElements();
                                                          /*
                <lifecycle-configuration>
                    <state name="Created">
                        <distributed-configuration>
                            <registry-url>https://10.100.1.1</registry-url>
                            <registry-username>admin</registry-username>
                            <registry-password>admin</registry-password>
                            <location>/workspace/development</location>
                        </distributed-configuration>
                        <vote-configuration>
                            <minimum-votes>4</minimum-votes>
                            <description></description>
                            <voted>
                                <user></user>
                                <user></user>
                            </voted>
                        </vote-configuration>
                        <checklist>
                            <item>Load Test Completed</item>
                            <item>Documentation Checked</item>
                            <item>Binary Packs Checked</item>
                            <item>UI Issues Checked</item>
                        </checklist>
                        <permissions>
                            <roles>
                                <promote>
                                    <role>everyone</role>
                                </promote>
                                <demote>
                                    <role>administrator</role>
                                    <role>tester</role>
                                </demote>
                                <vote/>
                                <view/>
                                <checklist-update/>
                            </roles>
                            <users>
                                <promote>
                                    <user>annonymous</user>
                                </promote>
                                <demote>
                                    <user>john</user>
                                    <user>jane</user>
                                </demote>
                                <vote/>
                                <view/>
                                <checklist-update/>
                            </users>
                        </permissions>
                    </state>
                 */
                int propertyOrder = 0;
                while (stateElements.hasNext()) {
                    OMElement stateElement = (OMElement)stateElements.next();
                    String stateName = stateElement.getAttributeValue(new QName(NAME_TAG));
                    if (stateName == null) {
                        throw new IllegalArgumentException("Must have a name attribute for each state");
                    }

                    states.add(stateName);

                    Iterator distConfigIterator = stateElement.getChildrenWithName
                            (new QName(DISTRIBUTED_CONFIGURATION_TAG));
                    if (distConfigIterator.hasNext()) {
                        OMElement distConfigElement = (OMElement)distConfigIterator.next();
                        Iterator configIterator = distConfigElement.getChildrenWithName(new QName(URL_TAG));
                        if (configIterator.hasNext()) {
                            
                        }
                        configIterator = distConfigElement.getChildrenWithName(new QName(PASSWORD_TAG));
                        if (configIterator.hasNext()) {

                        }
                        configIterator = distConfigElement.getChildrenWithName(new QName(USERNAME_TAG));
                        if (configIterator.hasNext()) {

                        }
                        configIterator = distConfigElement.getChildrenWithName(new QName(LOCATION_TAG));
                        if (configIterator.hasNext()) {

                        }
                    }

                    Iterator checkListIterator = stateElement.getChildElements();
                    int checklistItemOrder = 0;
                    while (checkListIterator.hasNext()) {
                        OMElement itemEl = (OMElement)checkListIterator.next();
                        if (itemEl.getQName().equals(new QName("checkitem"))) {
                            List<String> items = new ArrayList<String>();
                            String itemName = itemEl.getText();
                            if (itemName == null)
                                throw new RegistryException("Checklist items should have a name!");
                            items.add("status:" + stateName);
                            items.add("name:" + itemName);
                            items.add("value:false");

                            if (itemEl.getAttribute(new QName("order")) != null) {
                                items.add("order:" + itemEl.getAttributeValue(new QName("order")));
                            }
                            else {
                                items.add("order:" + checklistItemOrder);
                            }

                            String resourcePropertyNameForItem
                                    = "registry.custom_lifecycle.checklist.option" + propertyOrder + ".item";

                            resource.setProperty(resourcePropertyNameForItem, items);
                            checklistItemOrder++;
                            propertyOrder++;
                        }
                    }
                }
            }
        } catch (XMLStreamException e) {
            throw new RegistryException("Resource does not contain valid XML configuration: " + e.toString());
        }  catch (MalformedURLException e) {
            throw new RegistryException("Invalid URL in aspect configuration: " + e.toString());
        }

        resource.setProperty(stateProperty, states.get(0));
    }

    public void invoke(RequestContext context, String action) throws RegistryException {
        Resource resource = context.getResource();
        String currentState = resource.getProperty(stateProperty);
        int stateIndex = states.indexOf(currentState);
        if (stateIndex == -1) {
            throw new RegistryException("State '" + currentState + "' is not valid!");
        }

        String newState;
        if (PROMOTE.equals(action)) {
            if (stateIndex == states.size() - 1) {
                throw new RegistryException("Can't promote beyond end of configured lifecycle!");
            }

            // Make sure all conditions are met
            List<Condition> conditions = transitions.get(currentState);
            if (conditions != null) {
                for (Condition condition : conditions) {
                    if (!condition.isTrue(resource)) {
                        throw new RegistryException(
                                "Condition failed - " + condition.getDescription());
                    }
                }
            }
            newState = states.get(stateIndex + 1);
        } else if (DEMOTE.equals(action)) {
            if (stateIndex == 0) {
                throw new RegistryException("Can't demote beyond start of configured lifecycle!");
            }
            newState = states.get(stateIndex - 1);
        } else {
            throw new RegistryException("Invalid action '" + action + "'");
        }

        resource.setProperty(stateProperty, newState);
        context.getRepository().put(resource.getPath(), resource);
    }

    public String[] getAvailableActions(RequestContext context) {
        ArrayList<String> actions = new ArrayList<String>();
        Resource resource = context.getResource();
        String currentState = resource.getProperty(stateProperty);

        Properties props = resource.getProperties();
        boolean allItemsAreChecked = true;
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            if (((String) e.getKey()).startsWith("registry.custom_lifecycle.checklist.")) {
                List<String> propValues = (List<String>) e.getValue();
                String[] propertyValues = propValues.toArray(new String[propValues.size()]);
                String itemLifeCycleState = null;
                String itemValue = null;

                if (propertyValues != null) {
                      for (int index = 0; index < propertyValues.length; index++) {
                        String item = propertyValues[index];
                        if ((itemLifeCycleState == null) && (item.startsWith("status:"))) {
                            itemLifeCycleState = item.substring(7);                           
                        }
                        if ((itemValue == null) && (item.startsWith("value:"))) {
                            itemValue = item.substring(6);
                        }
                    }
                }

                if ((itemLifeCycleState != null) && (itemValue != null)) {
                    if (itemLifeCycleState.equalsIgnoreCase(currentState)) {
                        if (itemValue.equalsIgnoreCase("false")) {
                            allItemsAreChecked = false;
                            break;
                        }
                    }
                }
            }
        }

        int stateIndex = states.indexOf(currentState);
        if (stateIndex > -1 && stateIndex < states.size() - 1) {
            if (allItemsAreChecked)
                actions.add(PROMOTE);
        }
        if (stateIndex > 0) {
            actions.add(DEMOTE);
        }
        return actions.toArray(new String[actions.size()]);
    }

    public void dissociate(RequestContext context) {
        Resource resource = context.getResource();
        if (resource != null) {
            resource.removeProperty(stateProperty);
        }
    }

    public String getCurrentState(Resource resource) {
        return resource.getProperty(stateProperty);
    }   
}
