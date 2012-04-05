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
package org.wso2.carbon.eventing.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.savan.SavanException;
import org.apache.savan.configuration.Configurator;
import org.apache.savan.configuration.FilterBean;
import org.apache.savan.configuration.MappingRules;
import org.apache.savan.configuration.Protocol;
import org.apache.savan.configuration.SubscriberBean;
import org.apache.savan.filters.Filter;
import org.apache.savan.storage.SubscriberStore;
import org.apache.savan.subscribers.Subscriber;
import org.apache.savan.util.UtilFactory;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.eventing.internal.EventingServiceComponent;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * 
 * This is responsible for loading Savan configuration data from the registry.
 * 
 */
public class RegistryBasedConfigurator implements Configurator {

    private final String PROTOCOLS = "protocols";
    private final String PROTOCOL = "protocol";
    private final String NAME = "name";
    private final String UTIL_FACTORY = "utilFactory";
    private final String MAPPING_RULES = "mapping-rules";
    private final String ACTION = "action";
    private final String SUBSCRIBER_STORES = "subscriberStores";
    private final String SUBSCRIBER_STORE = "subscriberStore";
    private final String FILTERS = "filters";
    private final String FILTER = "filter";
    private final String KEY = "key";
    private final String CLASS = "class";
    private final String IDENTIFIER = "identifier";
    private final String SUBSCRIBERS = "subscribers";
    private final String SUBSCRIBER = "subscriber";
    private final String DEFAULT_SUBSCRIBER = "defaultSubscriber";
    private final String DEFAULT_FILTER = "defaultFilter";
    private final String URL_APPENDER = "urlAppender";
    private final String PROTOCOL_PATH = RegistryResources.ROOT + "eventing/protocols";
    private final String SUBSCRIBERS_PATH = RegistryResources.ROOT + "eventing/subscribers";
    private final String SUBSCRIBER_STORES_PATH = RegistryResources.ROOT
            + "eventing/subscriberStores";
    private final String FILTERS_PATH = RegistryResources.ROOT + "eventing/filters";

    private HashMap protocolMap = null;
    private HashMap subscriberStoreNamesMap = null;
    private HashMap filterMap = null;
    private HashMap subscribersMap = null;

    public RegistryBasedConfigurator() {

    }

    public void configure() throws SavanException {
        updateRegitry();
        protocolMap = new HashMap();
        subscriberStoreNamesMap = new HashMap();
        filterMap = new HashMap();
        subscribersMap = new HashMap();
        loadRegistryConfiguration();

    }

    /**
     * {@inheritDoc}
     */
    public void configure(ClassLoader arg0) throws SavanException {
        configure();

    }

    /**
     * {@inheritDoc}
     */
    public void configure(File arg0) throws SavanException {
        configure();

    }

    /**
     * {@inheritDoc}
     */
    public HashMap getFilters() {
        return filterMap;
    }

    /**
     * {@inheritDoc}
     */
    public HashMap getProtocolMap() {
        return protocolMap;
    }

    /**
     * {@inheritDoc}
     */
    public HashMap getSubscriberStoreNames() {
        return subscriberStoreNamesMap;
    }

    /**
     * {@inheritDoc}
     */
    public HashMap getSubscribers() {
        return subscribersMap;
    }

    /**
     * 
     * @throws SavanException
     */
    protected void loadRegistryConfiguration() throws SavanException {
        Registry systemRegistry = null;
        Resource resource = null;
        String content = null;

        try {
            systemRegistry = EventingServiceComponent.getRegistry();

            if (systemRegistry.resourceExists(PROTOCOL_PATH)) {
                resource = systemRegistry.get(PROTOCOL_PATH);
                content = new String((byte[]) resource.getContent());
                processProtocols(AXIOMUtil.stringToOM(content));
            }

            if (systemRegistry.resourceExists(SUBSCRIBERS_PATH)) {
                resource = systemRegistry.get(SUBSCRIBERS_PATH);
                content = new String((byte[]) resource.getContent());
                processSubscribers(AXIOMUtil.stringToOM(content));
            }

            if (systemRegistry.resourceExists(SUBSCRIBER_STORES_PATH)) {
                resource = systemRegistry.get(SUBSCRIBER_STORES_PATH);
                content = new String((byte[]) resource.getContent());
                processSubscriberStores(AXIOMUtil.stringToOM(content));
            }

            if (systemRegistry.resourceExists(FILTERS_PATH)) {
                resource = systemRegistry.get(FILTERS_PATH);
                content = new String((byte[]) resource.getContent());
                processFilters(AXIOMUtil.stringToOM(content));
            }

        } catch (Exception e) {
            throw new SavanException(e);
        }
    }

    /**
     * 
     * @throws SavanException
     */
    protected void updateRegitry() throws SavanException {
        Registry systemRegistry = null;
        Resource resource = null;

        try {
            systemRegistry = EventingServiceComponent.getRegistry();

            systemRegistry.beginTransaction();

            if (!systemRegistry.resourceExists(SUBSCRIBERS_PATH)) {
                resource = systemRegistry.newResource();
                resource.setContent(getDefaultSubcribers().toString());
                systemRegistry.put(SUBSCRIBERS_PATH, resource);
            }

            if (!systemRegistry.resourceExists(SUBSCRIBER_STORES_PATH)) {
                resource = systemRegistry.newResource();
                resource.setContent(getDefaultSubcriberStores().toString());
                systemRegistry.put(SUBSCRIBER_STORES_PATH, resource);
            }

            if (!systemRegistry.resourceExists(FILTERS_PATH)) {
                resource = systemRegistry.newResource();
                resource.setContent(getDefaultFilters().toString());
                systemRegistry.put(FILTERS_PATH, resource);
            }

            if (!systemRegistry.resourceExists(PROTOCOL_PATH)) {
                resource = systemRegistry.newResource();
                resource.setContent(getDefaultProtocols().toString());
                systemRegistry.put(PROTOCOL_PATH, resource);
            }

            systemRegistry.commitTransaction();

        } catch (Exception e) {
            try {
            systemRegistry.rollbackTransaction();
            } catch (RegistryException ex) {
                throw new SavanException(ex);    
            }
            throw new SavanException(e);
        }
    }

    /**
     * 
     * @return
     */
    private OMElement getDefaultSubcriberStores() {
        OMElement subscriberStores = null;
        OMFactory factory = null;
        OMElement subscriberStore = null;
        OMElement key = null;
        OMElement clazz = null;

        factory = OMAbstractFactory.getOMFactory();
        subscriberStores = factory.createOMElement(new QName(SUBSCRIBER_STORES));
        subscriberStore = factory.createOMElement(new QName(SUBSCRIBER_STORE));

        key = factory.createOMElement(new QName(KEY));
        key.setText("default");
        clazz = factory.createOMElement(new QName(CLASS));
        clazz.setText("org.wso2.carbon.eventing.subscription.SubscriptionManagerAdapter");

        subscriberStore.addChild(key);
        subscriberStore.addChild(clazz);
        subscriberStores.addChild(subscriberStore);

        return subscriberStores;
    }

    /**
     * 
     * @return
     */
    private OMElement getDefaultSubcribers() {
        OMElement subscribers = null;
        OMFactory factory = null;
        OMElement subscriber = null;
        OMElement name = null;
        OMElement clazz = null;

        factory = OMAbstractFactory.getOMFactory();
        subscribers = factory.createOMElement(new QName(SUBSCRIBERS));
        subscriber = factory.createOMElement(new QName(SUBSCRIBER));

        name = factory.createOMElement(new QName(NAME));
        name.setText("eventing");
        clazz = factory.createOMElement(new QName(CLASS));
        clazz.setText("org.apache.savan.eventing.subscribers.EventingSubscriber");

        subscriber.addChild(name);
        subscriber.addChild(clazz);
        subscribers.addChild(subscriber);

        return subscribers;
    }

    /**
     * 
     * @return
     */
    private OMElement getDefaultFilters() {
        OMElement filters = null;
        OMFactory factory = null;
        OMElement filter = null;
        OMElement name = null;
        OMElement identifier = null;
        OMElement clazz = null;
        OMElement filterXpath = null;
        OMElement nameXpath = null;
        OMElement identifierXpath = null;
        OMElement clazzXpath = null;

        factory = OMAbstractFactory.getOMFactory();
        filters = factory.createOMElement(new QName(FILTERS));
        filter = factory.createOMElement(new QName(FILTER));

        name = factory.createOMElement(new QName(NAME));
        name.setText("empty");
        identifier = factory.createOMElement(new QName(IDENTIFIER));
        identifier.setText("empty");
        clazz = factory.createOMElement(new QName(CLASS));
        clazz.setText("org.apache.savan.filters.EmptyFilter");

        filter.addChild(name);
        filter.addChild(clazz);

        filterXpath = factory.createOMElement(new QName(FILTER));

        nameXpath = factory.createOMElement(new QName(NAME));
        nameXpath.setText("xpath");
        identifierXpath = factory.createOMElement(new QName(IDENTIFIER));
        identifierXpath.setText("http:// www.w3.org/TR/1999/REC-xpath-19991116");
        clazzXpath = factory.createOMElement(new QName(CLASS));
        clazzXpath.setText("org.apache.savan.filters.XPathBasedFilter");

        filterXpath.addChild(nameXpath);
        filterXpath.addChild(clazzXpath);

        filters.addChild(filter);
        filters.addChild(filterXpath);

        return filter;
    }

    /**
     * 
     * @return
     */
    private OMElement getDefaultProtocols() {
        OMElement protocols = null;
        OMFactory factory = null;
        OMElement protocol = null;
        OMElement name = null;
        OMElement utilFactory = null;
        OMElement mappingRules = null;
        OMElement action = null;
        OMElement defaultSubscriber = null;
        OMElement defaultFilter = null;

        factory = OMAbstractFactory.getOMFactory();
        protocols = factory.createOMElement(new QName(PROTOCOLS));
        protocol = factory.createOMElement(new QName(PROTOCOL));

        name = factory.createOMElement(new QName(NAME));
        name.setText("eventing");
        utilFactory = factory.createOMElement(new QName(UTIL_FACTORY));
        utilFactory.setText("org.apache.savan.eventing.EventingUtilFactory");

        mappingRules = factory.createOMElement(new QName(MAPPING_RULES));

        action = factory.createOMElement(new QName(ACTION));
        action.setText("http://schemas.xmlsoap.org/ws/2004/08/eventing/Subscribe");
        mappingRules.addChild(action);

        action = factory.createOMElement(new QName(ACTION));
        action.setText("http://schemas.xmlsoap.org/ws/2004/08/eventing/Renew");
        mappingRules.addChild(action);

        action = factory.createOMElement(new QName(ACTION));
        action.setText("http://schemas.xmlsoap.org/ws/2004/08/eventing/GetStatus");
        mappingRules.addChild(action);

        action = factory.createOMElement(new QName(ACTION));
        action.setText("http://schemas.xmlsoap.org/ws/2004/08/eventing/Unsubscribe");
        mappingRules.addChild(action);

        action = factory.createOMElement(new QName(ACTION));
        action.setText("http://wso2.com/ws/2007/05/eventing/Publish");
        mappingRules.addChild(action);

        defaultSubscriber = factory.createOMElement(new QName(DEFAULT_SUBSCRIBER));
        defaultSubscriber.setText("eventing");

        defaultFilter = factory.createOMElement(new QName(DEFAULT_FILTER));
        defaultFilter.setText("empty");

        protocol.addChild(name);
        protocol.addChild(utilFactory);
        protocol.addChild(mappingRules);
        protocol.addChild(defaultSubscriber);
        protocol.addChild(defaultFilter);

        protocols.addChild(protocol);

        return protocols;
    }

    /**
     * 
     * @param element
     * @throws SavanException
     */
    private void processProtocols(OMElement element) throws SavanException {
        Iterator protocolElementsIterator = null;
        protocolElementsIterator = element.getChildrenWithName(new QName(PROTOCOL));
        while (protocolElementsIterator.hasNext()) {
            OMElement protocolElement = null;
            protocolElement = (OMElement) protocolElementsIterator.next();
            processProtocol(protocolElement);
        }
    }

    /**
     * 
     * @param element
     * @throws SavanException
     */
    private void processProtocol(OMElement element) throws SavanException {
        Protocol protocol = null;
        OMElement nameElement = null;
        String name = null;
        OMElement utilFactoryNameElement = null;
        String utilFactoryName = null;
        Object utilFactory = null;
        OMElement mappingRulesElement = null;
        OMElement defaultSubscriberElement = null;
        String defaultSubscriber = null;
        OMElement defaultFilterElement = null;
        String defaultFilter = null;

        protocol = new Protocol();
        nameElement = element.getFirstChildWithName(new QName(NAME));

        if (nameElement == null) {
            throw new SavanException("Protocol must have a 'Name' subelement");
        }

        name = nameElement.getText();
        protocol.setName(name);
        utilFactoryNameElement = element.getFirstChildWithName(new QName(UTIL_FACTORY));

        if (utilFactoryNameElement == null) {
            throw new SavanException("Protocol must have a 'UtilFactory' subelement");
        }

        utilFactoryName = utilFactoryNameElement.getText();
        utilFactory = getObject(utilFactoryName);

        if (!(utilFactory instanceof UtilFactory)) {
            throw new SavanException("UtilFactory element" + utilFactoryName
                    + "is not a subtype of the UtilFactory class");
        }

        protocol.setUtilFactory((UtilFactory) utilFactory);
        mappingRulesElement = element.getFirstChildWithName(new QName(MAPPING_RULES));

        if (mappingRulesElement == null) {
            throw new SavanException("Protocol must have a 'mappingRules' sub-element");
        }

        processMappingRules(mappingRulesElement, protocol);
        defaultSubscriberElement = element.getFirstChildWithName(new QName(DEFAULT_SUBSCRIBER));

        if (defaultSubscriberElement == null) {
            throw new SavanException("Protocols must have a 'defaultSubscriber' sub-element");
        }

        defaultSubscriber = defaultSubscriberElement.getText();
        protocol.setDefaultSubscriber(defaultSubscriber);
        defaultFilterElement = element.getFirstChildWithName(new QName(DEFAULT_FILTER));

        if (defaultFilterElement == null) {
            throw new SavanException("Protocols must have a 'defaultFilter' sub-element");
        }

        defaultFilter = defaultFilterElement.getText();
        protocol.setDefaultFilter(defaultFilter);
        protocolMap.put(protocol.getName(), protocol);
    }

    /**
     * 
     * @param element
     * @param protocol
     */
    private void processMappingRules(OMElement element, Protocol protocol) {
        MappingRules mappingRules = null;
        Iterator actionsIterator = null;

        mappingRules = protocol.getMappingRules();
        actionsIterator = element.getChildrenWithName(new QName(ACTION));

        while (actionsIterator.hasNext()) {
            OMElement actionElement = null;
            String action = null;
            actionElement = (OMElement) actionsIterator.next();
            action = actionElement.getText();
            mappingRules.addRule(MappingRules.MAPPING_TYPE_ACTION, action);
        }
    }

    /**
     * 
     * @param element
     * @throws SavanException
     */
    private void processSubscriberStores(OMElement element) throws SavanException {
        Iterator subscriberStoreElementsIterator = null;
        subscriberStoreElementsIterator = element.getChildrenWithName(new QName(SUBSCRIBER_STORE));
        while (subscriberStoreElementsIterator.hasNext()) {
            OMElement subscriberStoreElement = (OMElement) subscriberStoreElementsIterator.next();
            processSubscriberStore(subscriberStoreElement);
        }
    }

    /**
     * 
     * @param element
     * @throws SavanException
     */
    private void processSubscriberStore(OMElement element) throws SavanException {
        OMElement keyElement = null;
        String key = null;
        OMElement classElement = null;
        String clazz = null;
        Object subscriberStore = null;

        keyElement = element.getFirstChildWithName(new QName(KEY));
        if (keyElement == null) {
            throw new SavanException("SubscriberStore must have a 'key' subelement");
        }

        key = keyElement.getText();
        classElement = element.getFirstChildWithName(new QName(CLASS));

        if (classElement == null) {
            throw new SavanException("SubscriberStore must have a 'Clazz' subelement'");
        }

        clazz = classElement.getText();
        // initialize the class to check weather it is value
        subscriberStore = getObject(clazz);

        if (!(subscriberStore instanceof SubscriberStore)) {
            String message = "Class " + clazz
                    + " does not implement the  SubscriberStore interface.";
            throw new SavanException(message);
        }

        subscriberStoreNamesMap.put(key, clazz);
    }

    /**
     * 
     * @param className
     * @return
     * @throws SavanException
     */
    private Object getObject(String className) throws SavanException {
        Object obj = null;
        Class clazz = null;

        try {
            clazz = Class.forName(className);
            obj = clazz.newInstance();
        } catch (Exception e) {
            String message = "Can't instantiate the class:" + className;
            throw new SavanException(message, e);
        }
        return obj;
    }

    /**
     * 
     * @param element
     * @throws SavanException
     */
    private void processFilters(OMElement element) throws SavanException {
        Iterator filterElementsIterator = element.getChildrenWithName(new QName(FILTER));
        while (filterElementsIterator.hasNext()) {
            OMElement filterElement = (OMElement) filterElementsIterator.next();
            processFilter(filterElement);
        }
    }

    /**
     * 
     * @param element
     * @throws SavanException
     */
    private void processFilter(OMElement element) throws SavanException {
        OMElement nameElement = null;
        OMElement identifierElement = null;
        OMElement classElement = null;
        String name = null;
        String identifier = null;
        String clazz = null;
        Object filter = null;
        FilterBean bean = null;

        nameElement = element.getFirstChildWithName(new QName(NAME));
        identifierElement = element.getFirstChildWithName(new QName(IDENTIFIER));
        classElement = element.getFirstChildWithName(new QName(CLASS));

        if (nameElement == null) {
            throw new SavanException("Name element is not present within the Filter");
        }
        if (identifierElement == null) {
            throw new SavanException("Identifier element is not present within the Filter");
        }
        if (classElement == null) {
            throw new SavanException("Class element is not present within the Filter");
        }

        name = nameElement.getText();
        identifier = identifierElement.getText();
        clazz = classElement.getText();

        // initialize the class to check weather it is value
        filter = getObject(clazz);

        if (!(filter instanceof Filter)) {
            String message = "Class " + clazz + " does not implement the  Filter interface.";
            throw new SavanException(message);
        }

        bean = new FilterBean();
        bean.setName(name);
        bean.setIdentifier(identifier);
        bean.setClazz(clazz);

        filterMap.put(identifier, bean);
    }

    /**
     * 
     * @param element
     * @throws SavanException
     */
    private void processSubscribers(OMElement element) throws SavanException {
        Iterator subscriberElementsIterator = element.getChildrenWithName(new QName(SUBSCRIBER));
        while (subscriberElementsIterator.hasNext()) {
            OMElement subscriberElement = (OMElement) subscriberElementsIterator.next();
            processSubscriber(subscriberElement);
        }
    }

    /**
     * 
     * @param element
     * @throws SavanException
     */
    private void processSubscriber(OMElement element) throws SavanException {
        OMElement nameElement = null;
        OMElement urlAppenderElement = null;
        OMElement classElement = null;
        String name = null;
        String clazz = null;
        Object subscriber = null;
        SubscriberBean bean = null;

        nameElement = element.getFirstChildWithName(new QName(NAME));
        urlAppenderElement = element.getFirstChildWithName(new QName(URL_APPENDER));
        classElement = element.getFirstChildWithName(new QName(CLASS));

        if (nameElement == null) {
            throw new SavanException("Name element is not present within the AbstractSubscriber");
        }
        if (classElement == null) {
            throw new SavanException("Class element is not present within the Filter");
        }

        name = nameElement.getText();
        clazz = classElement.getText();

        // initialize the class to check weather it is valid
        subscriber = getObject(clazz);

        if (!(subscriber instanceof Subscriber)) {
            String message = "Class " + clazz + " does not implement the  Subscriber interface.";
            throw new SavanException(message);
        }

        bean = new SubscriberBean();
        bean.setName(name);
        bean.setClazz(clazz);
        subscribersMap.put(name, bean);
    }

}