/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.lwevent.core;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.event.core.subscription.Subscription;
import org.wso2.carbon.bam.lwevent.core.internal.LightWeightEventBrokerComponent;
import org.wso2.carbon.bam.lwevent.core.util.EventingConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class LightWeightEventBroker implements LightWeightEventBrokerInterface{

    private static LightWeightEventBroker instance = null;

    private final String registryPath;

    private static final String packageName = LightWeightEventBroker.class.getPackage().getName();

    private static final Log log = LogFactory.getLog(LightWeightEventBroker.class);


    private ThreadLocal<ServiceClient> serviceClientThreadLocal = new ThreadLocal<ServiceClient>() {
        @Override
        protected ServiceClient initialValue() {
            try {
                return new ServiceClient();
            } catch (AxisFault axisFault) {
                log.error("Unable to create service client", axisFault);
            }
            return null;
        }
    };

    protected LightWeightEventBroker(String packageName) throws AxisFault {

        registryPath =  "repository/components/" + packageName + "/subscriptions/";

        backOffCounterMap = new ConcurrentHashMap<String, BackOffCounter>();
    }

    public static LightWeightEventBroker getInstance() throws AxisFault {
        if (instance == null) {
            synchronized (LightWeightEventBroker.class) {
                if (instance == null) {
                    instance = new LightWeightEventBroker(packageName);
                }
            }
        }
        return instance;
    }

    public String subscribe(Subscription subscription) throws RegistryException {
        Registry registry = getRegistry();
        int topicHash = subscription.getTopicName().hashCode();

        String subscriptionPath = registryPath + topicHash;
        if (registry.resourceExists(subscriptionPath)) {
            Collection subscriptionCollection = (Collection) registry.get(subscriptionPath);
            String[] subscriptonPaths = subscriptionCollection.getChildren();
            for (String registrySubscriptionPath : subscriptonPaths) {
                Resource regSubscription = registry.get(registrySubscriptionPath);
                String eventSinkURL = regSubscription.getProperty(ServiceStatisticsPublisherConstants.EVENT_SINK_URL_PROPERTY_NAME);
                if (subscription.getEventSinkURL().equals(eventSinkURL))  {
                    return regSubscription.getProperty(ServiceStatisticsPublisherConstants.UUID_PROPERTY_NAME);
                }
            }
        }
        Resource resource = registry.newResource();
        resource.setProperty(ServiceStatisticsPublisherConstants.TOPIC_REGISTRY_PROPERTY_NAME, subscription.getTopicName());
        resource.setProperty(ServiceStatisticsPublisherConstants.EVENT_SINK_URL_PROPERTY_NAME, subscription.getEventSinkURL());
        String uuid = UUID.randomUUID().toString();
        resource.setProperty(ServiceStatisticsPublisherConstants.UUID_PROPERTY_NAME, uuid);
        registry.put(constructRegistryPath(subscription.getTopicName(), uuid), resource);
        log.info("Subscription added for topic : " + subscription.getTopicName() + " for subscriber URL : " + subscription.getEventSinkURL());
        return uuid;
    }

    private String constructRegistryPath(String topicName, String uuid) {
        return registryPath + topicName.hashCode() + "/"  + uuid;
    }

    private String constructRegistryPathWithoutUUID(String topicName, String uuid) {
        return registryPath + topicName + "/"  + uuid;
    }

    public void unsubscribe(Subscription subscription) throws RegistryException {
        try {
            Registry registry = getRegistry();
            String subcriptionPath = registryPath;
            Collection subscriptionCollection = (Collection) registry.get(subcriptionPath);
            String [] subscriptionTopics = subscriptionCollection.getChildren();
            for (String topic : subscriptionTopics) {
                String subscriptionResourcePath = topic + "/" + subscription.getId();
                if (!registry.resourceExists(subscriptionResourcePath)) {
                    continue;
                }
                Resource subscriptionResource = registry.get(subscriptionResourcePath);
                String uuid = subscriptionResource.getProperty(ServiceStatisticsPublisherConstants.UUID_PROPERTY_NAME);
                if (uuid.equals(subscription.getId())) {
                    String topicName = subscriptionResource.getProperty(ServiceStatisticsPublisherConstants.TOPIC_REGISTRY_PROPERTY_NAME);
                    String eventSinkURL = subscriptionResource.getProperty(ServiceStatisticsPublisherConstants.EVENT_SINK_URL_PROPERTY_NAME);
                    registry.delete(subscriptionResourcePath);
                    log.info("Subscription removed for topic : " + topicName + " for subscriber URL : " + eventSinkURL);
                    return;
                }
            }
        } catch (RegistryException e) {
            log.error("Subscription cannot be found to remove for subscription Id : " + subscription.getId());
            throw new RegistryException("Error removing subscription", e);
        }



    }

    private Registry getRegistry() throws RegistryException {
        RegistryService registryService = LightWeightEventBrokerComponent.getRegistryService();
        Registry registry = registryService.getConfigSystemRegistry(CarbonContext.getCurrentContext().getTenantId());
        return registry;
    }

    private Map<String, BackOffCounter> backOffCounterMap;

    public void publish(String topicName, OMElement event) throws AxisFault, RegistryException {
        Registry registry = getRegistry();
        Collection collection = (Collection) registry.get(registryPath + topicName.hashCode());
        String[] subEndpointsUUIDPaths = collection.getChildren();

        for (String endpointUUIDPath : subEndpointsUUIDPaths) {

            BackOffCounter backOffCounter = backOffCounterMap.get(endpointUUIDPath);
            // initialize back off counter
            if (backOffCounter == null) {
                backOffCounter = new BackOffCounter();
                backOffCounterMap.put(endpointUUIDPath, backOffCounter);
            }

            // Only go into this if the endpoint has failed
            if (backOffCounter.isFailed()) {
                if (backOffCounter.getTotalBackOffCount().get() > backOffCounter.getCurrentBackOffCount().get()) {
                    backOffCounter.getCurrentBackOffCount().incrementAndGet();
                    continue;
                } else {
                    String endpoint  = registry.get(endpointUUIDPath).getProperty(ServiceStatisticsPublisherConstants.EVENT_SINK_URL_PROPERTY_NAME);
                    try {
                        String ipAddr = endpoint.substring(endpoint.indexOf("://"), endpoint.lastIndexOf(":"));
                        boolean reachable = InetAddress.getByName(ipAddr).isReachable(250);
                        if (!reachable) {
                            exponentiateBackOfftime(backOffCounter, new Exception("Cannot reach IP address : " + ipAddr));
                        }
                    } catch (UnknownHostException e) {
                        // this cannot occur since we are submitting the IP directly
                    } catch (IOException e) {
                        // ignore and let it try to send the message
                    }
                    backOffCounter.getCurrentBackOffCount().set(0);
                    backOffCounter.setFailed(false);
                }
            }
            Resource subscription = registry.get(endpointUUIDPath);
            Options  options = new Options();
            options.setAction(EventingConstants.WSE_PUBLISH);
            options.setTo(new EndpointReference(subscription.getProperty(ServiceStatisticsPublisherConstants.EVENT_SINK_URL_PROPERTY_NAME)));
            options.setTimeOutInMilliSeconds(ServiceStatisticsPublisherConstants.EVENT_PUBLISHER_CLIENT_TIMEOUT);
//            options.setTo(new EndpointReference("http://localhost:8280/"));
            ServiceClient serviceClient = serviceClientThreadLocal.get();
            try {
                serviceClient.setOptions(options);
                serviceClient.fireAndForget(event);
                serviceClient.cleanupTransport();
                // back off time is reset only if the message has been successfull sent
                backOffCounter.getTotalBackOffCount().set(0);

            } catch (AxisFault axisFault) {
                exponentiateBackOfftime(backOffCounter, axisFault);
            }
        }

    }

    private void exponentiateBackOfftime(BackOffCounter backOffCounter, Exception axisFault) {
        backOffCounter.setFailed(true);
        int totalBackOffCount = backOffCounter.getTotalBackOffCount().get();
        int backOffDelta = (totalBackOffCount == 0) ? 2 : Math.min(totalBackOffCount * 2, 100);
        backOffCounter.getTotalBackOffCount().set(backOffDelta);
        log.error("Cannot send request due to client failing. Exponentially backing off - Total back off count " + backOffCounter.getTotalBackOffCount().get());
        if (log.isDebugEnabled()) {
            log.error(axisFault.getMessage(), axisFault);
        }
    }
}

