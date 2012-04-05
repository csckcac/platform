/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.bam.service.data.publisher.publish;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericKeyedObjectPool;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.PublisherConfiguration;
import org.wso2.carbon.bam.data.publisher.util.stats.AtomicIntSingleton;
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.bam.service.ReceiverService;
import org.wso2.carbon.bam.service.SessionTimeOutException;
import org.wso2.carbon.bam.service.data.publisher.data.BAMServerInfo;
import org.wso2.carbon.bam.service.data.publisher.pool.TFramedTransportPool;
import org.wso2.carbon.bam.service.data.publisher.pool.TFramedTransportPoolFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;


public class DataPublisher implements StatsProcessor {

    private static Log log = LogFactory.getLog(DataPublisher.class);

    boolean isSocketTransportUsed = true;

    public void process(ArrayList<Event> events, BAMServerInfo bamServerInfo) {
        if (bamServerInfo.isSocketTransportEnable()) {
            publishUsingTSocketTransport(events, bamServerInfo);
        } else if (bamServerInfo.isHttpTransportEnable()) {
            publishUsingHttp(events, bamServerInfo);
        }
    }

    private void publishUsingTSocketTransport(ArrayList<Event> events,
                                              BAMServerInfo bamServerInfo) {
        int i = 0;
        TTransport transport = null;
        String key = null;
        String sessionId = ThriftUtil.getSessionId(bamServerInfo);
        PublisherConfiguration configuration = ServiceAgentUtil.getPublisherConfiguration();
        GenericKeyedObjectPool transportPool = TFramedTransportPool.getClientPool(
                new TFramedTransportPoolFactory(), configuration.getMaxPoolSize(),
                configuration.getMaxIdleConnections(), true, configuration.getEvictionTimePeriod(),
                configuration.getMinIdleTimeInPool());
        try {
            URL url = new URL(bamServerInfo.getBamServerURL());
            String hostName = url.getHost();
            key = hostName + BAMDataPublisherConstants.HOSTNAME_AND_PORT_SEPARATOR
                  + bamServerInfo.getPort();
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
            publishRetryUsingTSocket(events, i, bamServerInfo, transportPool);
        } catch (TException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (SessionTimeOutException e) {
            publishRetryUsingTSocket(events, i, bamServerInfo, transportPool);
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

    private void publishRetryUsingTSocket(ArrayList<Event> events, int i,
                                          BAMServerInfo bamServerInfo,
                                          GenericKeyedObjectPool transportPool) {
        ArrayList<Event> newEventList = new ArrayList<Event>();
        TTransport transport = null;
        String key = null;
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
            try {
                URL url = new URL(bamServerInfo.getBamServerURL());
                String hostName = url.getHost();
                key = hostName + BAMDataPublisherConstants.HOSTNAME_AND_PORT_SEPARATOR
                      + bamServerInfo.getPort();

                String sessionId = ThriftUtil.getSessionId(bamServerInfo);
                transport = (TTransport) transportPool.borrowObject(key);
                protocol = new TCompactProtocol(transport);
                ReceiverService.Client senderClient = new ReceiverService.Client(protocol);
                for (Event event : events) {
                    senderClient.publish(event, sessionId);
                    if (log.isDebugEnabled()) {
                        AtomicIntSingleton.getAtomicInteger().incrementAndGet();
                    }
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

    private void publishUsingHttp(ArrayList<Event> events, BAMServerInfo bamServerInfo) {
        THttpClient client = null;
        TProtocol protocol = null;
        String sessionId = ThriftUtil.getSessionId(bamServerInfo);
        int i = 0;
        try {
            client = new THttpClient(bamServerInfo.getBamServerURL() + "thriftReceiver");
            protocol = new TCompactProtocol(client);
        } catch (TTransportException e) {
            e.printStackTrace();
        }
        ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);

        try {
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
            publishRetryUsingHttp(events, bamServerInfo, i);
            log.warn("Session Timeout, retrying .........");
        } finally {
            client.close();
        }
    }


    private void publishRetryUsingHttp(ArrayList<Event> events, BAMServerInfo bamServerInfo,
                                       int i) {

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
            String sessionId = ThriftUtil.getSessionId(bamServerInfo);
            try {
                client = new THttpClient(bamServerInfo.getBamServerURL() + "thriftReceiver");
                protocol = new TCompactProtocol(client);
                ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);

                client.open();
                for (Event event : events) {
                    receiverClient.publish(event, sessionId);
                    if (log.isDebugEnabled()) {
                        AtomicIntSingleton.getAtomicInteger().incrementAndGet();
                    }
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


    public void destroy() {

    }
}
