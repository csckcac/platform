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
package org.wso2.carbon.eventing.subscription;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMAttributeImpl;
import org.apache.axiom.om.impl.llom.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.savan.SavanException;
import org.apache.savan.SavanMessageContext;
import org.apache.savan.eventing.Delivery;
import org.apache.savan.eventing.subscribers.EventingSubscriber;
import org.apache.savan.filters.Filter;
import org.apache.savan.filters.XPathBasedFilter;
import org.apache.savan.storage.DefaultSubscriberStore;
import org.apache.savan.subscribers.Subscriber;
import org.wso2.carbon.core.RegistryResources;
import org.wso2.carbon.eventing.internal.EventingServiceComponent;
import org.wso2.carbon.registry.core.CollectionImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;

/**
 * Persists savan eventing subscribers in the registry.
 * 
 */
public class RegistryBasedSubscriberStore extends DefaultSubscriberStore {

    private String serviceName;
    private SavanMessageContext config;
    private static final Log log = LogFactory.getLog(RegistryBasedSubscriberStore.class);

    private static final String SUBSCRIPTION = "subscription";
    private static final String EVENTING_ROOT =
            RegistryResources.COMPONENTS + "org.wso2.carbon.eventing/";

    /**
     * {@inheritDoc}
     */
    public void init(SavanMessageContext savanContext) throws SavanException {
        this.config = savanContext;
        serviceName = (String) config.getMessageContext().getAxisService().getName();
    }

    /**
     * {@inheritDoc}
     */
    public Subscriber retrieve(String subscriberId) {
        Registry systemRegistry = null;
        Resource resource = null;

        try {
            systemRegistry = EventingServiceComponent.getRegistry();

            if (systemRegistry.resourceExists(EVENTING_ROOT + serviceName + "/subscribers/"
                    + subscriberId)) {
                resource = systemRegistry.get(EVENTING_ROOT + serviceName + "/subscribers/"
                        + subscriberId);
                return getSubscriber(resource);
            }

        } catch (Exception e) {
            log.error("Error while retrieving subscribers for event source " + serviceName, e);
            return null;
        }

        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Iterator retrieveAllSubscribers() {
        Registry systemRegistry = null;
        CollectionImpl resourceCollection = null;
        ArrayList allSubscribers = new ArrayList();

        try {
            systemRegistry = EventingServiceComponent.getRegistry();

            if (systemRegistry.resourceExists(EVENTING_ROOT + serviceName
                    + "/subscribers")) {
                resourceCollection = (CollectionImpl) systemRegistry.get(EVENTING_ROOT
                        + serviceName + "/subscribers");
                String[] resources = resourceCollection.getChildren();
                for (int i = 0; i < resources.length; i++) {
                    allSubscribers.add(getSubscriber(systemRegistry.get(resources[i])));
                }
            }
        } catch (Exception e) {
            log.error("Error while retrieving subscribers for event source " + serviceName, e);
            return null;
        }

        return allSubscribers.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public void store(Subscriber subscriber) {
        Registry systemRegistry = null;
        Resource resource = null;

        try {
            systemRegistry = EventingServiceComponent.getRegistry();

            resource = getResource(subscriber, systemRegistry);
            systemRegistry.put(EVENTING_ROOT + serviceName + "/subscribers/"
                    + subscriber.getId().toASCIIString(), resource);
        } catch (Exception e) {
            log.error("Error while storing subscribers for event source " + serviceName, e);
        }
    }

    protected String getServiceName() {
        return serviceName;
    }

    protected void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    private Subscriber getSubscriber(Resource resource) throws Exception {
        EventingSubscriber subscriber = null;
        String content = null;
        OMElement payload = null;

        subscriber = new EventingSubscriber();

        if (config != null) {
            subscriber.setConfigurationContext(config.getConfigurationContext());
        }

        content = new String((byte[]) resource.getContent());
        payload = AXIOMUtil.stringToOM(content);

        subscriber.setEndToEPr(getEndToEpr(payload));
        subscriber.setDelivery(getDelivery(payload));
        subscriber.setFilter(getFilter(payload));
        subscriber.setId(getId(payload));
        subscriber.setSubscriptionEndingTime(getSubscriptionEndingTime(payload));

        return subscriber;
    }

    private EndpointReference getEndToEpr(OMElement payload) {
        EndpointReference epr = null;
        OMElement eprElement = null;
        epr = new EndpointReference();
        // The namepace only used internally - just for creating the OMElement
        eprElement = payload.getFirstChildWithName((new QName("http://wso2.org/eventing/epr",
                "EndpointReference", "epr")));
        if (eprElement != null) {
            epr.setAddress(eprElement.getFirstChildWithName(
                    new QName("http://www.w3.org/2005/08/addressing", "Address", "wsa")).getText());
        }
        return epr;
    }

    private Date getSubscriptionEndingTime(OMElement payload) {
        OMElement endTimeElement = null;
        endTimeElement = payload.getFirstChildWithName(new QName("EndTime"));
        if (endTimeElement != null) {
            return new Date(endTimeElement.getText());
        }
        return null;
    }

    private URI getId(OMElement payload) throws URISyntaxException {
        return new URI(payload.getAttributeValue(new QName("id")));
    }

    private Delivery getDelivery(OMElement payload) {
        Delivery delivery = null;
        OMElement deliveryElement = null;
        delivery = new Delivery();
        deliveryElement = payload.getFirstChildWithName(new QName("Delivery"));
        if (deliveryElement != null) {
            String mode = deliveryElement.getAttributeValue(new QName("mode"));
            delivery.setDeliveryMode(mode);
            delivery.setDeliveryEPR(getEndToEpr(deliveryElement));
        }
        return delivery;
    }

    private Filter getFilter(OMElement payload) {
        OMElement endTimeElement = null;
        XPathBasedFilter filter = null;
        endTimeElement = payload.getFirstChildWithName(new QName("Filter"));
        if (endTimeElement != null) {
            filter = new XPathBasedFilter();
            filter.setXPathString(endTimeElement.getText());
        }
        return filter;
    }

    private Resource getResource(Subscriber subscriber, Registry registry) throws Exception {
        EventingSubscriber eventingSub = null;
        Resource resource = null;
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMElement payload = factory.createOMElement(SUBSCRIPTION, null);

        if (subscriber instanceof EventingSubscriber) {
            eventingSub = (EventingSubscriber) subscriber;
            addId(payload, eventingSub.getId());
            addSubscriptionEndingTime(payload, eventingSub.getSubscriptionEndingTime());
            if (eventingSub.getDelivery() != null) {
                addDelivery(payload, eventingSub.getDelivery());
            }
            if (eventingSub.getEndToEPr() != null) {
                addEndToEpr(payload, eventingSub.getEndToEPr());
            }
            if (eventingSub.getFilter() != null) {
                addFilter(payload, eventingSub.getFilter());
            }
            resource = registry.newResource();
            resource.setContent(payload.toString());
        }

        return resource;
    }

    private void addEndToEpr(OMElement payload, EndpointReference epr) throws AxisFault {
        // The namepace only used internally - just for creating the OMElement
        payload.addChild((OMNode) epr.toOM("http://wso2.org/eventing/epr", "EndpointReference",
                "epr"));
    }

    private void addSubscriptionEndingTime(OMElement payload, Date endtime) {
        OMFactory factory = null;
        OMElement endTimeElement = null;
        factory = OMAbstractFactory.getOMFactory();
        endTimeElement = factory.createOMElement(new QName("EndTime"));
        endTimeElement.setText(endtime.toLocaleString());
        payload.addChild((OMNode) endTimeElement);
    }

    private void addId(OMElement payload, URI id) {
        OMFactory factory = null;
        OMAttributeImpl attribute = null;
        factory = OMAbstractFactory.getOMFactory();
        attribute = new OMAttributeImpl("id", null, id.toString(), factory);
        payload.addAttribute(attribute);
    }

    private void addDelivery(OMElement payload, Delivery delivery) throws AxisFault {
        OMFactory factory = null;
        OMAttributeImpl attribute = null;
        OMElement deliveryElement = null;
        factory = OMAbstractFactory.getOMFactory();
        attribute = new OMAttributeImpl("mode", null, delivery.getDeliveryMode(), factory);
        deliveryElement = factory.createOMElement(new QName("Delivery"));
        deliveryElement.addAttribute(attribute);
        addEndToEpr(deliveryElement, delivery.getDeliveryEPR());
        payload.addChild((OMNode) deliveryElement);
    }

    private void addFilter(OMElement payload, Filter filter) {
        OMFactory factory = null;
        OMElement filterElement = null;
        factory = OMAbstractFactory.getOMFactory();
        filterElement = factory.createOMElement(new QName("Filter"));
        if (filter.getFilterValue() != null) {
            filterElement.setText(filter.getFilterValue().toString());
            payload.addChild((OMNode) filterElement);
        }
    }
}
