/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.bam.agent.publish;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.bam.agent.queue.EventReceiverComposite;
import org.wso2.carbon.bam.agent.conf.AgentConfiguration;
import org.wso2.carbon.bam.agent.pool.TFramedTransportPool;
import org.wso2.carbon.bam.agent.pool.TFramedTransportPoolFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.stats.AtomicIntSingleton;
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.bam.service.ReceiverService;
import org.wso2.carbon.bam.service.SessionTimeOutException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataPublisher implements EventPublisher {

    private static Log log = LogFactory.getLog(DataPublisher.class);

    private GenericKeyedObjectPool transportPool;

    private Map<TTransport, ReceiverService.Client> receiverClientCache =
            new ConcurrentHashMap<TTransport, ReceiverService.Client>();

    private ThriftAuthenticationClient authenticationClient = new ThriftAuthenticationClient();

    public DataPublisher(AgentConfiguration configuration) {
        this.transportPool = new TFramedTransportPool().getClientPool(
                new TFramedTransportPoolFactory(), configuration.getMaxPoolSize(),
                configuration.getMaxIdleConnections(), true, configuration.getEvictionTimePeriod(),
                configuration.getMinIdleTimeInPool());
    }


    @Override
    public void shutdown() {
//       transportPool.clear();
//        try {
//            transportPool.close();
//        } catch (Exception e) {
//            log.error("Error shutting down connection pool", e);
//        }
    }

    @Override()
    public void publish(ArrayList<EventReceiverComposite> eventReceiverComposites) {

        for (EventReceiverComposite eventReceiverComposite : eventReceiverComposites) {
            if (eventReceiverComposite.getEventReceiver().isSocketTransportEnabled()) {

                publishUsingTSocketTransport(eventReceiverComposite);
            } else if (eventReceiverComposite.getEventReceiver().isHttpTransportEnabled()) {
                publishUsingHttp(eventReceiverComposite);
            }
        }

    }

    private Event fixEvent(Event event) {
       if  (event.getCorrelation() == null) {
           event.setCorrelation(new HashMap());
       }
       if (event.getEvent() == null) {
          event.setEvent(new HashMap());
       }
       if (event.getMeta() == null) {
           event.setMeta(new HashMap());
       }
       return event;
    }


    private void publishUsingTSocketTransport(EventReceiverComposite eventReceiverComposite) {
        int i = 0;
        TTransport transport = null;
        String key = null;
        try {
            EventReceiver eventReceiver = eventReceiverComposite.getEventReceiver();
            String sessionId = authenticationClient.getSessionId(eventReceiver);
            URL url = new URL(eventReceiver.getUrl());
            String hostName = url.getHost();
            key = hostName + BAMDataPublisherConstants.HOSTNAME_AND_PORT_SEPARATOR
                    + eventReceiver.getPort();
            transport = (TTransport) transportPool.borrowObject(key);
            ReceiverService.Client receiverClient = getReceiverClient(transport);

            for (Event event : eventReceiverComposite.getEvent()) {
                receiverClient.publish(fixEvent(event), sessionId);
                if (log.isTraceEnabled()) {
                    log.trace(event + " event published to url : " + eventReceiverComposite.getEventReceiver().getUrl());
                }
            }
            if (log.isDebugEnabled()) {
                AtomicIntSingleton.getAtomicInteger().incrementAndGet();
            }
            i++;
            if (log.isDebugEnabled()) {
                log.debug("No of active connections in pool : " + this.transportPool.getNumActive());
            }
        } catch (TTransportException e) {
            log.warn("TransportException, retrying to publish again..", e);
            //Need to clear connection with correct key
            transportPool.clear(key);
            publishRetryUsingTSocket(eventReceiverComposite, transportPool);
        } catch (TException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (SessionTimeOutException e) {
            log.warn("Session Timeout, retrying .........");
            publishRetryUsingTSocket(eventReceiverComposite, transportPool);
        } catch (MalformedURLException e) {
            log.error("BAM url is not correct", e);
        } catch (Throwable e) {
            log.error("Unable to publish event to BAM", e);
        } finally {
            try {
                transportPool.returnObject(key, transport);
            } catch (Exception e) {
                log.warn("Error occurred while returning object to connection pool");
            }
        }

    }

    private ReceiverService.Client getReceiverClient(TTransport transport) {
        if (receiverClientCache.containsKey(transport)) {
            return receiverClientCache.get(transport);
        }

        TProtocol protocol = new TCompactProtocol(transport);
        ReceiverService.Client client = new ReceiverService.Client(protocol);
        receiverClientCache.put(transport, client);
        return client;
    }

    private void publishRetryUsingTSocket(EventReceiverComposite eventReceiverComposite,
                                          GenericKeyedObjectPool transportPool) {


        authenticationClient.removeSessionId(eventReceiverComposite.getEventReceiver());
        for (int k = 0; k < 3; k++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Restoring the interrupted status after catching InterruptedException
                // instead of Swallowing
                Thread.currentThread().interrupt();
            }
            TTransport transport = null;
            String key = null;
            try {
                EventReceiver eventReceiver = eventReceiverComposite.getEventReceiver();
                String sessionId = authenticationClient.getSessionId(eventReceiver);
                URL url = new URL(eventReceiver.getUrl());
                String hostName = url.getHost();
                key = hostName + BAMDataPublisherConstants.HOSTNAME_AND_PORT_SEPARATOR
                        + eventReceiver.getPort();
                transport = (TTransport) transportPool.borrowObject(key);
                ReceiverService.Client receiverClient = getReceiverClient(transport);

                for (Event event : eventReceiverComposite.getEvent()) {
                    receiverClient.publish(fixEvent(event), sessionId);
                }
                if (log.isDebugEnabled()) {
                    AtomicIntSingleton.getAtomicInteger().incrementAndGet();
                }

            } catch (TTransportException e) {
                log.error("Unable to publish event to BAM", e);
            } catch (TException e) {
                log.error("Unable to publish event to BAM", e);
            } catch (SessionTimeOutException e) {
                log.warn("Session Timeout, retrying .........");
            } catch (MalformedURLException e) {
                log.error("BAM url is not correct", e);
            } catch (Exception e) {
                log.error("Unable to publish event to BAM", e);
            } finally {
                try {
                    transportPool.returnObject(key, transport);
                } catch (Exception e) {
                    log.warn("Error occurred while returning object to connection pool");
                }
            }
        }
    }

    private void publishUsingHttp(EventReceiverComposite eventReceiverComposite) {
        THttpClient client = null;
        TProtocol protocol = null;
        String sessionId = null;
        int i = 0;
        try {
            EventReceiver eventReceiver = eventReceiverComposite.getEventReceiver();
            sessionId = authenticationClient.getSessionId(eventReceiver);
            client = new THttpClient(eventReceiver.getUrl() + "thriftReceiver");
            protocol = new TCompactProtocol(client);
            ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);
            client.open();

            for (Event event : eventReceiverComposite.getEvent()) {
                receiverClient.publish(fixEvent(event), sessionId);
            }
            if (log.isDebugEnabled()) {
                AtomicIntSingleton.getAtomicInteger().incrementAndGet();
            }
            i++;

        } catch (TTransportException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (TException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (SessionTimeOutException e) {
            log.warn("Session Timeout, retrying .........");
            publishRetryUsingHttp(eventReceiverComposite);
        } finally {
            client.close();
        }
    }


    private void publishRetryUsingHttp(EventReceiverComposite eventReceiverComposite) {


        EventReceiver eventReceiver = eventReceiverComposite.getEventReceiver();

        authenticationClient.removeSessionId(eventReceiver);
        for (int k = 0; k < 3; k++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Restoring the interrupted status after catching InterruptedException
                // instead of Swallowing
                Thread.currentThread().interrupt();
            }
            THttpClient client = null;
            TProtocol protocol = null;
            String sessionId = authenticationClient.getSessionId(eventReceiver);
            try {
                client = new THttpClient(eventReceiver.getUrl() + "thriftReceiver");
                protocol = new TCompactProtocol(client);

                ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);
                client.open();
                for (Event event : eventReceiverComposite.getEvent()) {
                    receiverClient.publish(fixEvent(event), sessionId);
                }

            } catch (TTransportException e) {
                log.error("Unable to publish event to BAM", e);
            } catch (TException e) {
                log.error("Unable to publish event to BAM", e);
            } catch (SessionTimeOutException e) {
                log.warn("Session Timeout, retrying .........");
            } finally {
                client.close();
            }
        }
    }

}
