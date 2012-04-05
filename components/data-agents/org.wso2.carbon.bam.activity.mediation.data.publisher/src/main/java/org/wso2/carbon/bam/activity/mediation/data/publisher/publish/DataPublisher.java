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
package org.wso2.carbon.bam.activity.mediation.data.publisher.publish;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.bam.activity.mediation.data.publisher.conf.ActivityConfigData;
import org.wso2.carbon.bam.activity.mediation.data.publisher.pool.TFramedTransportPool;
import org.wso2.carbon.bam.activity.mediation.data.publisher.pool.TFramedTransportPoolFactory;
import org.wso2.carbon.bam.activity.mediation.data.publisher.util.ActivityPublisherUtils;
import org.wso2.carbon.bam.activity.mediation.data.publisher.util.TenantActivityConfigData;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.PublisherConfiguration;
import org.wso2.carbon.bam.data.publisher.util.stats.AtomicIntSingleton;
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.bam.service.ReceiverService;
import org.wso2.carbon.bam.service.SessionTimeOutException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

public class DataPublisher implements ActivityProcessor {

    private static Log log = LogFactory.getLog(DataPublisher.class);

    @Override
    public void destroy() {

    }

    @Override()
    public void process(ArrayList<Event> events, int tenantId) {
        Map<Integer, ActivityConfigData> tenantSpecificActivityConfig = TenantActivityConfigData.
                getTenantSpecificEventingConfigData();
        ActivityConfigData activityConfigData = tenantSpecificActivityConfig.get(tenantId);

        if (activityConfigData.isSocketTransportEnable()) {
            publishUsingTSocketTransport(events, activityConfigData);
        } else {
            publishUsingHttp(events, activityConfigData);
        }
    }

    private void publishUsingTSocketTransport(ArrayList<Event> events,
                                              ActivityConfigData activityConfigData) {
        int i = 0;
        TTransport transport = null;
        String sessionId = ThriftUtil.getSessionId(activityConfigData);
        PublisherConfiguration configuration = ActivityPublisherUtils.getPublisherConfiguration();
        GenericKeyedObjectPool transportPool = TFramedTransportPool.getClientPool(
                new TFramedTransportPoolFactory(), configuration.getMaxPoolSize(),
                configuration.getMaxIdleConnections(), true, configuration.getEvictionTimePeriod(),
                configuration.getMinIdleTimeInPool());
        String key = null;
        try {
            URL url = new URL(activityConfigData.getUrl());
            String hostName = url.getHost();
            key = hostName + BAMDataPublisherConstants.HOSTNAME_AND_PORT_SEPARATOR
                  + activityConfigData.getPort();
            transport = (TTransport) transportPool.borrowObject(key);
            TProtocol protocol = new TCompactProtocol(transport);

            ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);
            for (Event event : events) {
                receiverClient.publish(event, sessionId);
                if (log.isDebugEnabled()) {
                    AtomicIntSingleton.getAtomicInteger().incrementAndGet();
                }
                i++;
            }
        } catch (TTransportException e) {
            log.warn("TransportException, retrying to publish again..", e);
            //Need to clear connection with correct key
            transportPool.clear(key);
            publishRetryUsingTSocket(events, i, activityConfigData, transportPool);
        } catch (TException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (SessionTimeOutException e) {
            log.warn("Session Timeout, retrying .........");
            publishRetryUsingTSocket(events, i, activityConfigData, transportPool);
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

    private void publishRetryUsingTSocket(ArrayList<Event> events, int i,
                                          ActivityConfigData activityConfigData,
                                          GenericKeyedObjectPool transportPool) {
        ArrayList<Event> newEventList = new ArrayList<Event>();
        TTransport transport = null;
        for (int j = i; j < events.size(); j++) {
            newEventList.add(events.get(j));
        }

        ThriftUtil.setSessionId(null);

        for (int k = 0; k < 30; k++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Restoring the interrupted status after catching InterruptedException
                // instead of Swallowing
                Thread.currentThread().interrupt();
            }
            TProtocol protocol = null;
            String key = null;
            try {
                String sessionId = ThriftUtil.getSessionId(activityConfigData);
                URL url = new URL(activityConfigData.getUrl());
                String hostName = url.getHost();
                key = hostName + BAMDataPublisherConstants.HOSTNAME_AND_PORT_SEPARATOR
                      + activityConfigData.getPort();
                transport = (TTransport) transportPool.borrowObject(key);
                protocol = new TCompactProtocol(transport);
                ReceiverService.Client senderClient = new ReceiverService.Client(protocol);
                for (Event event : events) {
                    senderClient.publish(event, sessionId);
                    i++;
                }
                return;
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

    private void publishUsingHttp(ArrayList<Event> events, ActivityConfigData activityConfigData) {
        THttpClient client = null;
        TProtocol protocol = null;
        String sessionId = null;
        int i = 0;
        try {
            sessionId = ThriftUtil.getSessionId(activityConfigData);
            client = new THttpClient(activityConfigData.getUrl() + "thriftReceiver");
            protocol = new TCompactProtocol(client);
            ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);
            client.open();
            for (Event event : events) {
                receiverClient.publish(event, sessionId);
                if (log.isDebugEnabled()) {
                    AtomicIntSingleton.getAtomicInteger().incrementAndGet();
                }
                i++;
            }
        } catch (TTransportException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (TException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (SessionTimeOutException e) {
            log.warn("Session Timeout, retrying .........");
            publishRetryUsingHttp(events, i, activityConfigData);
        } finally {
            client.close();
        }
    }


    private void publishRetryUsingHttp(ArrayList<Event> events, int i,
                                       ActivityConfigData activityConfigData) {

        ArrayList<Event> newEventList = new ArrayList<Event>();
        for (int j = i; j < events.size(); j++) {
            newEventList.add(events.get(j));
        }

        ThriftUtil.setSessionId(null);

        for (int k = 0; k < 30; k++) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Restoring the interrupted status after catching InterruptedException
                // instead of Swallowing
                Thread.currentThread().interrupt();
            }
            THttpClient client = null;
            TProtocol protocol = null;
            String sessionId = ThriftUtil.getSessionId(activityConfigData);
            try {
                client = new THttpClient(activityConfigData.getUrl() + "thriftReceiver");
                protocol = new TCompactProtocol(client);
            } catch (TTransportException e) {
                e.printStackTrace();
            }
            ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);

            try {
                client.open();
                for (Event event : events) {
                    receiverClient.publish(event, sessionId);
                }
                return;
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
