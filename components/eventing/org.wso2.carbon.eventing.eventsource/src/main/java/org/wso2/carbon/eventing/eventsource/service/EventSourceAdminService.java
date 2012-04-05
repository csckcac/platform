/**
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
 */

package org.wso2.carbon.eventing.eventsource.service;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.eventing.SynapseEventSource;
import org.apache.synapse.eventing.managers.DefaultInMemorySubscriptionManager;
import org.wso2.carbon.eventing.eventsource.EventSourceAdminException;
import org.wso2.carbon.eventing.eventsource.service.dto.EventSourceDTO;
import org.wso2.carbon.mediation.initializer.AbstractServiceBusAdmin;
import org.wso2.carbon.mediation.initializer.ServiceBusConstants;
import org.wso2.carbon.mediation.initializer.ServiceBusUtils;
import org.wso2.carbon.mediation.initializer.persistence.MediationPersistenceManager;
import org.wso2.eventing.SubscriptionManager;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * EventSource Admin Web Service
 */
@SuppressWarnings({"UnusedDeclaration"})
public class EventSourceAdminService extends AbstractServiceBusAdmin {
    
    private static Log log = LogFactory.getLog(EventSourceAdminService.class);

    /**
     * Get all EventSources listed in the configuration
     *
     * @return current event source descriptions
     */
    public EventSourceDTO[] getEventSources() {
        List<EventSourceDTO> lstEventSource = new LinkedList<EventSourceDTO>();
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            for (SynapseEventSource eventSource : synapseConfiguration.getEventSources()) {
                lstEventSource.add(buildEventSourceDTO(eventSource));
            }
        } catch (Exception e) {
            log.error("Configuration creation error" + e.toString());
        } finally {
            lock.unlock();
        }
        return lstEventSource.toArray(new EventSourceDTO[lstEventSource.size()]);
    }

    /**
     * Get EventSource by name
     *
     * @param eventSourceName name of the event source
     * @return description of the specified event source
     */
    public EventSourceDTO getEventSource(String eventSourceName) {
        EventSourceDTO eventSourceDTO = null;
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();   
            SynapseEventSource synapseEventSource =
                    synapseConfiguration.getEventSource(eventSourceName);
            if (synapseEventSource != null) {
                eventSourceDTO = buildEventSourceDTO(synapseEventSource);
            }
        } catch (Exception e) {
            log.error("Configuration creation error" + e.toString());
        } finally {
            lock.unlock();
        }
        return eventSourceDTO;
    }

    /**
     * Add EventSource to the configuration
     *
     * @param eventsource event source description to be saved
     */
    public void addEventSource(EventSourceDTO eventsource) {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            SynapseEventSource synapseEventSource = buildEventSource(eventsource);
            synapseEventSource.buildService(getAxisConfig());
            synapseEventSource.setFileName(
                    ServiceBusUtils.generateFileName(synapseEventSource.getName()));
            synapseConfiguration.addEventSource(eventsource.getName(), synapseEventSource);
            persistEventSource(synapseEventSource);
        } catch (Exception e) {
            log.error("Configuration creation error" + e.toString());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Save EventSource
     *
     * @param eventsource event source description to be updated
     */
    public void saveEventSource(EventSourceDTO eventsource) {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            SynapseEventSource oldEventSrc = synapseConfiguration.getEventSource(eventsource.getName());
            if (oldEventSrc == null) {
                log.warn("No event source exists by the name : " + eventsource.getName());
                return;
            }

            AxisConfiguration axisCfg = getSynapseConfiguration().getAxisConfiguration();
            axisCfg.removeService(eventsource.getName());
            SynapseEventSource synapseEventSource = buildEventSource(eventsource);
            synapseEventSource.buildService(axisCfg);
            synapseEventSource.setFileName(oldEventSrc.getFileName());
            synapseConfiguration.removeEventSource(eventsource.getName());
            synapseConfiguration.addEventSource(eventsource.getName(), synapseEventSource);
            persistEventSource(synapseEventSource);
        } catch (Exception e) {
            log.error("Configuration creation error" + e.toString());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Delete EventSource
     *
     * @param eventsourceName Name of the event source to be deleted
     */
    public void removeEventSource(String eventsourceName) {
        final Lock lock = getLock();
        try {
            lock.lock();
            SynapseConfiguration synapseConfiguration = getSynapseConfiguration();
            SynapseEventSource eventSource = synapseConfiguration.getEventSource(eventsourceName);
            if (eventSource != null) {
                synapseConfiguration.removeEventSource(eventsourceName);
                MediationPersistenceManager pm = getMediationPersistenceManager();
                pm.deleteItem(eventsourceName, eventSource.getFileName(),
                        ServiceBusConstants.ITEM_TYPE_EVENT_SRC);
            } else {
                log.warn("No event source exists by the name : " + eventsourceName);
            }
        } catch (Exception e) {
            log.error("Configuration creation error" + e.toString());
        } finally {
            lock.unlock();
        }
    }

    protected void handleFault(Log log, String message, Exception e) throws AxisFault {

        if (e == null) {
            AxisFault af = new AxisFault(message);
            log.error(message, af);
            throw af;

        } else {
            message = message + " :: " + e.getMessage();
            log.error(message, e);
            throw new AxisFault(message, e);
        }
    }

    /**
     * Build the event source DTO from the config
     *
     * @param eventSource Synapse event source from which to obtain the event source description
     * @return an event source description
     */
    private EventSourceDTO buildEventSourceDTO(SynapseEventSource eventSource) {
        EventSourceDTO eventSourceDTO = new EventSourceDTO();
        eventSourceDTO.setName(eventSource.getName());
        eventSourceDTO.setClassName(eventSource.getSubscriptionManager().getClass().getName());
        /*Properties properties = new Properties();
        properties.setProperty("topicHeaderName", eventSource.getSubscriptionManager().getPropertyValue("topicHeaderName"));
        properties.setProperty("topicHeaderNS", eventSource.getSubscriptionManager().getPropertyValue("topicHeaderNS"));*/
        eventSourceDTO.setTopicHeaderName(eventSource.getSubscriptionManager().getPropertyValue(
                "topicHeaderName"));   //TODO add to constants
        eventSourceDTO.setTopicHeaderNS(
                eventSource.getSubscriptionManager().getPropertyValue("topicHeaderNS"));
        if (eventSourceDTO.getClassName()
                .equals("org.apache.synapse.eventing.managers.DefaultInMemorySubscriptionManager")) {
            eventSourceDTO.setType("DefaultInMemory");
        }else if (eventSourceDTO.getClassName()
                .equals("org.apache.synapse.eventing.managers.org.wso2.carbon.eventing.impl.EmbeddedRegistryBasedSubscriptionManager")) {
            eventSourceDTO.setType("EmbRegistry");
        } else {
            eventSourceDTO.setType(
                    "Registry");  //TODO this needs to be change to support any Subscription manager inherit from Subacription Manager
            eventSourceDTO.setRegistryUrl(
                    eventSource.getSubscriptionManager().getPropertyValue("registryURL"));
            eventSourceDTO
                    .setUsername(eventSource.getSubscriptionManager().getPropertyValue("username"));
            eventSourceDTO
                    .setPassword(eventSource.getSubscriptionManager().getPropertyValue("password"));
        }
        return eventSourceDTO;
    }

    /**
     * Build the eventsource from the DTO
     *
     * @param eventSourceDTO Event source description to be built
     * @return a Synapse event source instance
     */
    private SynapseEventSource buildEventSource(EventSourceDTO eventSourceDTO) {
        SynapseEventSource synapseEventSource = new SynapseEventSource(eventSourceDTO.getName());
        SubscriptionManager subscriptionManager;
        try {
            if (eventSourceDTO.getType().equals("DefaultInMemory")) {
                subscriptionManager = new DefaultInMemorySubscriptionManager();
            }else  if (eventSourceDTO.getType().equals("EmbRegistry")) {
                subscriptionManager = (SubscriptionManager) Class.forName(
                        "org.wso2.carbon.eventing.impl.EmbeddedRegistryBasedSubscriptionManager")
                        .newInstance();
                subscriptionManager.init();
            } else {
                subscriptionManager = (SubscriptionManager) Class.forName(
                        "org.wso2.carbon.eventing.impl.RemoteRegistryBasedSubscriptionManager")
                        .newInstance();
            }
            subscriptionManager
                    .addProperty("topicHeaderName", eventSourceDTO.getTopicHeaderName());
            subscriptionManager
                    .addProperty("topicHeaderNS", eventSourceDTO.getTopicHeaderNS());
            //TODO this needs to be change to support any Subscription manager inherit from Subacription Manager
            if (eventSourceDTO.getType().equals("Registry")) {
                if (eventSourceDTO.getRegistryUrl() != null) {
                    subscriptionManager
                            .addProperty("registryURL", eventSourceDTO.getRegistryUrl());
                }
                if (eventSourceDTO.getUsername() != null) {
                    subscriptionManager
                            .addProperty("username", eventSourceDTO.getUsername());
                }
                if (eventSourceDTO.getRegistryUrl() != null) {
                    subscriptionManager
                            .addProperty("password", eventSourceDTO.getPassword());
                }
            }
            synapseEventSource.setSubscriptionManager(subscriptionManager);
        } catch (ClassNotFoundException e) {
            log.error("Subscription manager cannot create " + e.toString());
        } catch (IllegalAccessException e) {
            log.error("Subscription manager cannot create " + e.toString());
        } catch (InstantiationException e) {
            log.error("Subscription manager cannot create " + e.toString());
        }
        return synapseEventSource;
    }

    private void persistEventSource(SynapseEventSource eventsource)
            throws EventSourceAdminException {
        MediationPersistenceManager pm = getMediationPersistenceManager();
        pm.saveItem(eventsource.getName(), ServiceBusConstants.ITEM_TYPE_EVENT_SRC);
    }
}