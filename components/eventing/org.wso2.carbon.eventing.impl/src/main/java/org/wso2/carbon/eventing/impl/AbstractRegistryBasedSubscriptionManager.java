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
package org.wso2.carbon.eventing.impl;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.databinding.utils.ConverterUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.CoreRegistry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.eventing.Event;
import org.wso2.eventing.EventingConstants;
import org.wso2.eventing.Subscription;
import org.wso2.eventing.SubscriptionData;
import org.wso2.eventing.SubscriptionManager;
import org.wso2.eventing.exceptions.EventException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayOutputStream;
import java.util.*;

/*
*  Copyright (c) 2005-2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

public abstract class AbstractRegistryBasedSubscriptionManager<T>
    implements SubscriptionManager<T> {
    public CoreRegistry registry;
    private final Map<String, String> properties = new HashMap<String, String>();
    protected Resource resTopicIndex;
    private static final Log log = LogFactory.getLog(AbstractRegistryBasedSubscriptionManager.class)
            ;

    public static final String EPR_TYPE = "application/vnd.epr";
    public static final String SEQUENCE_TYPE = "application/vnd.sequence";
    public static final String SUBSCRIPTION_COLLECTION_NAME = "system.subscriptions";
    public static final String MEDIATION_SEQUENCE_NAME = "_sequence.xml";
    public static final String DEFAULT_EVENTING_ROOT = RegistryResources.COMPONENTS + "org.wso2.carbon.eventing";
    public static final String TOPIC_INDEX = "/index/TopicIndex";

    public static final String SUBSCRIPTION = "subscription";
    public static final String ENDPOINT = "endpoint";
    public static final String ADDRESS = "address";
    public static final String URI = "uri";
    public static final String EXPIRES = "expires";
    public static final String STATIC_FLAG = "staticFlag";
    public static final String SUB_MANAGER_URI = "subManagerURI";
    public static final String SUBSCRIPTION_DATA = "subscriptionData";
    public static final String FILTER_VALUE = "filterValue";
    public static final String FILTER_DIALECT = "filterDialect";

    public static final String SUB_STORE_CTX = "subscriptionStoragePath";

    /**
     * {@inheritDoc}
     */
    public String subscribe(Subscription subscription) throws EventException {
        try {
            Resource resource = registry.newResource();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            OMElement subElem = subscriptionToRegStorage(subscription, resource, true);
            subElem.serialize(outStream);
            resource.setContent(outStream.toByteArray());
            resource.setMediaType(EPR_TYPE);
            String topic = subscription.getFilterValue();
            if (!topic.startsWith("/")) {
                topic = "/" + topic;
            }
            String subStorePath = getSubscriptionStoragePath();
            if (subStorePath != null) {
                subStorePath = subStorePath + topic;
            } else {
                subStorePath = topic;
            }
            registry.put(
                    subStorePath + "/" + SUBSCRIPTION_COLLECTION_NAME + "/" + subscription.getId(),
                    resource);
            // add to the index call the sync method
            updateTopicIndex(true, subscription.getId(), topic);
            log.debug("Subscribed to topic: " + topic);
        } catch (RegistryException e) {
            log.error("Unable to add the subscriptio to the registry" + e.toString());
        } catch (XMLStreamException e) {
            log.error("Unable to serialize the subscription endpoint" + e.toString());
        }
        return subscription.getId();
    }

    /**
     * {@inheritDoc}
     */
    public boolean unsubscribe(String id) throws EventException {
        try {
            resTopicIndex = registry.get(getTopicIndexPath());
            String topic = resTopicIndex.getProperty(id);
            String subStorePath = getSubscriptionStoragePath();
            if (subStorePath != null) {
                subStorePath = subStorePath + topic;
            } else {
                subStorePath = topic;
            }
            String regPath = subStorePath + "/" + id;
            registry.delete(regPath);
            // remove from the index call the sync method
            updateTopicIndex(false, id, null);
        } catch (RegistryException e) {
            log.error("Resource cannot remove from the registry", e);
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public boolean renew(Subscription subscription) throws EventException {
        Subscription subscriptionOrig = getSubscription(subscription.getId());
        if (subscriptionOrig.getId() != null) {
            try {
                resTopicIndex = registry.get(getTopicIndexPath());
                String topic = resTopicIndex.getProperty(subscription.getId());
                String subStorePath = getSubscriptionStoragePath();
                if (subStorePath != null) {
                    subStorePath = subStorePath + topic;
                } else {
                    subStorePath = topic;
                }
                String regPath = subStorePath + "/" + subscription.getId();
                Resource resource = registry.get(regPath);
                subscriptionOrig.setExpires(subscription.getExpires());
                // set the new content
                OMElement subElem = subscriptionToRegStorage(subscriptionOrig, resource, false);
                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                subElem.serialize(outStream);
                resource.setContent(outStream.toByteArray());
                resource.setMediaType(EPR_TYPE);
                registry.put(regPath, resource);
            } catch (RegistryException e) {
                log.error("Unable to update the registry", e);
                return false;
            } catch (XMLStreamException e) {
                log.error("Unable to process the XML info set", e);
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    public List<Subscription> getSubscriptions() throws EventException {
        return getAllSubscriptions();
    }

    /**
     * {@inheritDoc}
     */
    public List<Subscription> getAllSubscriptions() throws EventException {
        LinkedList<Subscription> subscriptionList = new LinkedList<Subscription>();
        try {
            if (registry != null) {
                resTopicIndex = registry.get(getTopicIndexPath());
                Properties property = resTopicIndex.getProperties();
                if (property != null) {
                    if (!property.isEmpty()) {
                        for (Enumeration e = property.propertyNames() ; e.hasMoreElements() ;) {
                            String id = (String)e.nextElement();
                            String topic = resTopicIndex.getProperty(id);
                            String subStorePath = getSubscriptionStoragePath();
                            if (subStorePath != null) {
                                subStorePath = subStorePath + topic;
                            } else {
                                subStorePath = topic;
                            }
                            String regPath = subStorePath + "/" + id;
                            Resource resource = registry.get(regPath);
                            if (resource != null) {
                                Subscription sub;
                                if (EPR_TYPE.equals(resource.getMediaType())) {
                                    sub = regStorageToSubscription(resource);
                                    sub.setId(id);
                                    subscriptionList.add(sub);
                                }
                            }
                        }
                    }
                }
            }
        } catch (RegistryException e) {
            log.error("Error reading subscription" + e.toString());
        } catch (XMLStreamException e) {
            log.error("Error processing subscription" + e.toString());
        }
        return subscriptionList;
    }

    /**
     * {@inheritDoc}
     */
    public List<Subscription> getMatchingSubscriptions(Event<T> event)
            throws EventException {
        String topic = event.getTopic();
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        if (topic == null) {
            log.error("Topic was not found for the given event.");
            return subscriptions;
        }
        if (!topic.startsWith("/")) {
            topic = "/" + topic;
        }
        String subStorePath = getSubscriptionStoragePath();
        if (subStorePath != null) {
            subStorePath = subStorePath + topic;
        } else {
            subStorePath = topic;
        }
        while (subStorePath != null) {
            subscriptions.addAll(getSubscribersOfTopic(subStorePath));
            subStorePath = RegistryUtils.getParentPath(subStorePath);
        }
        log.debug("Found " + subscriptions.size() + " subscribers");
        return subscriptions;
    }

    /**
     * {@inheritDoc}
     */
    public Subscription getSubscription(String id) throws EventException {
        Subscription sub = null;
        try {
            resTopicIndex = registry.get(getTopicIndexPath());
            String topic = resTopicIndex.getProperty(id);
            String subStorePath = getSubscriptionStoragePath();
            if (subStorePath != null) {
                subStorePath = subStorePath + topic;
            } else {
                subStorePath = topic;
            }
            Resource resource = registry.get(subStorePath + "/" + id);
            if (resource != null) {
                if (EPR_TYPE.equals(resource.getMediaType())) {
                    sub = regStorageToSubscription(resource);
                    sub.setId(id);
                }
            }
        } catch (RegistryException e) {
            log.error("Error reading subscription" + e.toString());
        } catch (XMLStreamException e) {
            log.error("Error processing subscription" + e.toString());
        }
        return sub;
    }

    /**
     * {@inheritDoc}
     */
    public Subscription getStatus(String s) throws EventException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    /**
     * {@inheritDoc}
     */
    public List<Subscription> getStaticSubscriptions() {

        LinkedList<Subscription> subscriptionList = null;
        try {
            subscriptionList = (LinkedList<Subscription>) getSubscriptions();
        } catch (EventException e) {
            handleException("Get subscription error", e);
        }
        LinkedList<Subscription> staticSubscriptionList =
                new LinkedList<Subscription>();
        for (Subscription subscription : subscriptionList) {
            if (subscription.isStaticEntry()) {
                staticSubscriptionList.add(subscription);
            }
        }
        return staticSubscriptionList;
    }

    /**
     * {@inheritDoc}
     */


    private void handleException(String message, Exception e) {
        log.error(message, e);
        throw new RuntimeException(message, e);
    }

    private void handleException(String message) {
        log.error(message);
        throw new RuntimeException(message);
    }

    /**
     * Generate the XML document use to store the subscription in the registry
     *
     * @param subscription
     * @param resource
     * @param mode         - true = insert false = update
     * @return OMElement Registry storage
     */
    private OMElement subscriptionToRegStorage(Subscription subscription,
                                               Resource resource, boolean mode) {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement subElem = factory.createOMElement(SUBSCRIPTION, null);
        //try {
        /*EndpointReference endpointReference = new EndpointReference(subscription.getEndpointUrl());
        EndpointReferenceHelper.fromString(subscription.getEndpointUrl());
OMElement epElem = EndpointReferenceHelper.toOM(factory, endpointReference,
        new QName(EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EN_SUBSCRIPTION_MANAGER,
                EventingConstants.WSE_EVENTING_PREFIX),
        AddressingConstants.Submission.WSA_NAMESPACE);
        ;*/
        //TODO: Make the implementation namespace aware
        OMElement epElem = factory.createOMElement(ENDPOINT, EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EVENTING_PREFIX);
        OMElement addrElem = factory.createOMElement(ADDRESS, EventingConstants.WSE_EVENTING_NS,
                EventingConstants.WSE_EVENTING_PREFIX);
        addrElem.addAttribute(URI, subscription.getEndpointUrl(), null);
        epElem.addChild(addrElem);
        subElem.addChild(epElem);
        //} catch (AxisFault axisFault) {
        //handleException("EPR creation failure", axisFault);
        //}
        if (subscription.getExpires() != null) {
            if (mode) {
                resource.addProperty(EXPIRES,
                        ConverterUtil.convertToString(subscription.getExpires()));
            } else {
                resource.setProperty(EXPIRES,
                        ConverterUtil.convertToString(subscription.getExpires()));
            }
        } else {
            if (mode) {
                resource.addProperty(EXPIRES, "*");
            } else {
                resource.setProperty(EXPIRES, "*");
            }
        }

        if (subscription.isStaticEntry()) {
            resource.addProperty(STATIC_FLAG, "true");
        } else {
            resource.addProperty(STATIC_FLAG, "false");
        }
        SubscriptionData subscriptionData = subscription.getSubscriptionData();
        if (subscriptionData != null && subscriptionData.getProperties() != null) {
            Map properties = subscriptionData.getProperties();
            if (properties.size() > 0) {
                Set<Map.Entry<String, Object>> entries = properties.entrySet();
                for (Map.Entry<String, Object> e : entries) {
                    if (e.getKey() != null) {
                        resource.addProperty(e.getKey(), (String) e.getValue());
                    }
                }
            }
        }
        resource.addProperty(SUB_MANAGER_URI, subscription.getSubManUrl());
        resource.addProperty(FILTER_VALUE, subscription.getFilterValue());
        resource.addProperty(FILTER_DIALECT, subscription.getFilterDialect());
        return subElem;
    }

    /**
     * Create the subscription from the registry storage
     *
     * @param resource
     * @return
     */
    private Subscription regStorageToSubscription(Resource resource)
            throws RegistryException, XMLStreamException {
        Subscription subscription = new Subscription();
        SubscriptionData subscriptionData = new SubscriptionData();
        String eprContent = new String((byte[]) resource.getContent());
        OMElement payload = AXIOMUtil.stringToOM(eprContent);
        if (payload.getFirstElement() != null) {
            OMElement element = payload.getFirstElement();
            try {
                if (element.getFirstElement() == null) {
                    handleException("EPR creation failure");
                }
                String url = element.getFirstElement().getAttributeValue(new QName(URI));
                subscription.setEndpointUrl(url);
                subscription.setAddressUrl(url);
            } catch (Exception e) {
                handleException("EPR creation failure", e);
            }
        }
        Properties property = resource.getProperties();
        if (property != null) {
            if (!property.isEmpty()) {
                for (Enumeration e = property.propertyNames() ; e.hasMoreElements() ;) {
                    String propName = (String)e.nextElement();
                    if (propName.equals(EXPIRES)) {
                        if (resource.getProperty(EXPIRES).equals("*")) {
                            subscription.setExpires(null); // never expire subscription
                        } else {
                            subscription.setExpires(ConverterUtil.convertToDateTime(
                                    resource.getProperty(EXPIRES)));
                        }
                    } else if (propName.equals(SUB_MANAGER_URI)) {
                        subscription
                                .setSubManUrl(resource.getProperty(SUB_MANAGER_URI));
                    } else if (propName.equals(FILTER_VALUE)) {
                        subscription.setFilterValue(resource.getProperty(FILTER_VALUE));
                    } else if (propName.equals(FILTER_DIALECT)) {
                        subscription.setFilterDialect(resource.getProperty(FILTER_DIALECT));
                    } else {
                        subscriptionData.setProperty(propName, resource.getProperty(propName));
                    }
                }
            }
        }
        subscription.setSubscriptionData(subscriptionData);
        return subscription;
    }


    /**
     * Update the topic index by holding the thread
     *
     * @param mode  Insert | Delete
     * @param id    subscription id
     * @param topic topic name
     * @throws Exception RegistryException
     */
    private synchronized void updateTopicIndex(boolean mode, String id, String topic)
            throws RegistryException {
        if (mode) {
            resTopicIndex = registry.get(getTopicIndexPath());
            resTopicIndex.addProperty(id, topic + "/" + SUBSCRIPTION_COLLECTION_NAME);
            registry.put(getTopicIndexPath(), resTopicIndex);
        } else {
            resTopicIndex = registry.get(getTopicIndexPath());
            resTopicIndex.removeProperty(id);
            registry.put(getTopicIndexPath(), resTopicIndex);
        }
    }

    /**
     * Get the subscriptions by topic
     *
     * @param topic name
     * @return subscriptions
     */
    private List<Subscription> getSubscribersOfTopic(String topic) {
        List<Subscription> subscriptions = new ArrayList<Subscription>();
        try {
            if (registry.resourceExists(topic)) {
                String subscriptionsCollection = topic + "/" + SUBSCRIPTION_COLLECTION_NAME;
                if (registry.resourceExists(subscriptionsCollection)) {
                    Collection subs = (Collection) registry.get(subscriptionsCollection);
                    String[] subsPaths = (String[]) subs.getContent();
                    for (String subsPath : subsPaths) {
                        Resource resource = registry.get(subsPath);
                        String id = resource.getId();
                        Subscription sub = new Subscription();
                        if (EPR_TYPE.equals(resource.getMediaType())) {
                            sub = regStorageToSubscription(resource);
                        }
                        // check for expiration
                        Calendar current =
                                Calendar.getInstance(); //Get current date and time
                        if (sub.getExpires() != null) {
                            if (current.before(sub.getExpires())) {
                                // add only valid subscriptions by checking the expiration
                                subscriptions.add(sub);
                            }
                        } else {
                            // If a expiration dosen't exisits treat it as a never expire subscription, valid till unsubscribe
                            subscriptions.add(sub);
                        }
                    }
                } else {
                    log.debug("Couldn't find the subscription endpoint collection");
                }
            } else if (log.isDebugEnabled()) {
                log.warn("Couldn't find the specified topic in the registry");
            }
        } catch (RegistryException e) {
            log.error("Couldn't retrieve the subscription information for the topic " + topic, e);
        } catch (XMLStreamException e) {
            log.error("Error on processing the stored subscription " + topic, e);
        }
        return subscriptions;
    }

    public void addProperty(String name, String value) {
        properties.put(name, value);
    }

    public java.util.Collection<String> getPropertyNames() {
        return properties.keySet();
    }

    public String getPropertyValue(String name) {
        return properties.get(name);
    }

    /**
     * Get the userdefine subscription storage path
     *
     * @return path
     */
    private String getSubscriptionStoragePath() {
        String subStoreCtx = this.getPropertyValue(SUB_STORE_CTX);
        if (subStoreCtx != null) {
            if (!subStoreCtx.startsWith("/")) {
                subStoreCtx = "/" + subStoreCtx;
            }
            if (subStoreCtx.endsWith("/")) {
                subStoreCtx = subStoreCtx
                        .substring(0, subStoreCtx.length() - "/".length()); // remove the final /
            }
        }
        return subStoreCtx;
    }

    /**
     * Get the topic index path
     *
     * @return
     */
    protected String getTopicIndexPath() {
        String topicIndexPath = null;
        String subStoreCtx = getSubscriptionStoragePath();
        if (subStoreCtx != null) {
            topicIndexPath = subStoreCtx + TOPIC_INDEX;
        } else {
            topicIndexPath = DEFAULT_EVENTING_ROOT + TOPIC_INDEX;
        }
        return topicIndexPath;
    }
}
