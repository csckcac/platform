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
package org.wso2.carbon.bam.mediationstats.data.publisher.publish;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.data.publisher.util.stats.AtomicIntSingleton;
import org.wso2.carbon.bam.mediationstats.data.publisher.conf.MediationStatConfig;
import org.wso2.carbon.bam.mediationstats.data.publisher.conf.Property;
import org.wso2.carbon.bam.mediationstats.data.publisher.data.MediationData;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.MediationDataPublisherConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.TenantMediationStatConfigData;
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.bam.service.ReceiverService;
import org.wso2.carbon.bam.service.SessionTimeOutException;
import org.wso2.carbon.mediation.statistics.StatisticsRecord;

import java.net.URL;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class DataPublisher {

    private static Log log = LogFactory.getLog(DataPublisher.class);

    private static boolean isSocketTransportUsed = true;

    public static void process(MediationData mediationData, int tenantID) {
        Map<String, ByteBuffer> correlationData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> metaData = new HashMap<String, ByteBuffer>();
        Map<String, ByteBuffer> eventData = new HashMap<String, ByteBuffer>();

        Map<Integer, MediationStatConfig> mediationStatConfigMap = TenantMediationStatConfigData.
                getTenantSpecificEventingConfigData();
        MediationStatConfig mediationStatConfig = mediationStatConfigMap.get(tenantID);

        addEventData(eventData, mediationData);
        addMetaData(metaData, mediationData,mediationStatConfig);
        addCorrelationData(correlationData, mediationData);

        Event event = new Event();
        event.setCorrelation(correlationData);
        event.setMeta(metaData);
        event.setEvent(eventData);
        publish(event, tenantID);
    }

    private static void addCorrelationData(Map<String, ByteBuffer> correlationData,
                                    MediationData mediationData) {
    }

    private static void addMetaData(Map<String, ByteBuffer> metaData, MediationData mediationData,
                                    MediationStatConfig mediationStatConfig) {
        Property[] properties = mediationStatConfig.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && property.getKey() != "") {
                    putDataIntoMap(metaData, property.getKey(), property.getValue());
                }
            }
        }
    }

    private static void addEventData(Map<String, ByteBuffer> eventData,
                              MediationData mediationData) {
        putDataIntoMap(eventData, MediationDataPublisherConstants.DIRECTION,
                       mediationData.getDirection());
        putDataIntoMap(eventData, BAMDataPublisherConstants.TIMESTAMP,
                       mediationData.getTimestamp().toString());
        putDataIntoMap(eventData, MediationDataPublisherConstants.RESOURCE_ID,
                       mediationData.getResourceId());
        putDataIntoMap(eventData, MediationDataPublisherConstants.STATS_TYPE,
                       mediationData.getStatsType());

        StatisticsRecord record = mediationData.getStatisticsRecord();
        putDataIntoMap(eventData, MediationDataPublisherConstants.MAX_PROCESS_TIME,
                       Long.toString(record.getMaxTime()));
        putDataIntoMap(eventData, MediationDataPublisherConstants.AVG_PROCESS_TIME,
                       Double.toString(record.getAvgTime()));
        putDataIntoMap(eventData, MediationDataPublisherConstants.MIN_PROCESS_TIME,
                       Long.toString(record.getMinTime()));
        putDataIntoMap(eventData, MediationDataPublisherConstants.FAULT_COUNT,
                       Integer.toString(record.getFaultCount()));
        putDataIntoMap(eventData, MediationDataPublisherConstants.COUNT,
                       Integer.toString(record.getTotalCount()));


        Map<String, Object> errorMap = mediationData.getErrorMap();
        if (errorMap != null) {
            for (Map.Entry<String, Object> errorEntry : errorMap.entrySet()) {
                Object entryValue = errorEntry.getValue();
                if (entryValue instanceof Integer) {
                    putDataIntoMap(eventData, errorEntry.getKey(),
                                   ((Integer) entryValue).toString());
                } else if (entryValue instanceof String) {
                    putDataIntoMap(eventData, errorEntry.getKey(), ((String) entryValue));
                }
            }
        }
    }

    private static void publish(Event event, int tenantID) {
        Map<Integer, MediationStatConfig> mediationStatConfigMap = TenantMediationStatConfigData.
                getTenantSpecificEventingConfigData();
        MediationStatConfig mediationStatConfig = mediationStatConfigMap.get(tenantID);
        if (isSocketTransportUsed) {
            publishUsingSocketTransport(event, mediationStatConfig);
        } else {
            publishUsingHttp(event, mediationStatConfig);
        }
    }

    private static void publishUsingSocketTransport(Event event, MediationStatConfig mediationStatConfig) {
        TTransport transport = null;

        String sessionId = ThriftUtil.getSessionId(mediationStatConfig);


        try {
            URL url = new URL(mediationStatConfig.getUrl());
            String hostName = url.getHost();
            transport = new TFramedTransport(new TSocket(hostName, mediationStatConfig.getPort()));

            TProtocol protocol = new TCompactProtocol(transport);
            ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);
            transport.open();
            receiverClient.publish(event, sessionId);
            if (log.isDebugEnabled()) {
                AtomicIntSingleton.getAtomicInteger().incrementAndGet();
            }
        } catch (TTransportException e) {
            log.warn("TransportException ", e);
        } catch (TException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (SessionTimeOutException e) {
            log.warn("Session Timeout, retrying .........");
            publishRetryUsingTSocket(event, mediationStatConfig);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            transport.close();
        }
    }


    private static void publishRetryUsingTSocket(Event event,
                                          MediationStatConfig mediationStatConfig) {
        TTransport transport = null;
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
                String sessionId = ThriftUtil.getSessionId(mediationStatConfig);
                URL url = new URL(mediationStatConfig.getUrl());
                String hostName = url.getHost();
                transport = new TFramedTransport(new TSocket(hostName, mediationStatConfig.getPort()));
                protocol = new TCompactProtocol(transport);
                ReceiverService.Client senderClient = new ReceiverService.Client(protocol);
                transport.open();
                senderClient.publish(event, sessionId);
                if (log.isDebugEnabled()) {
                    AtomicIntSingleton.getAtomicInteger().incrementAndGet();
                }
                return;
            } catch (TTransportException e) {
                log.error("Unable to publish event to BAM", e);
            } catch (TException e) {
                log.error("Unable to publish event to BAM", e);
            } catch (SessionTimeOutException e) {
                log.warn("Session Timeout, retrying .........");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                transport.close();
            }
        }
    }


    private static void publishUsingHttp(Event event, MediationStatConfig mediationStatConfig){
               THttpClient client = null;
        TProtocol protocol = null;

        String sessionId = ThriftUtil.getSessionId(mediationStatConfig);
        try {
            client = new THttpClient(mediationStatConfig.getUrl() + "thriftReceiver");
            protocol = new TCompactProtocol(client);
            ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);
            client.open();
            receiverClient.publish(event, sessionId);
        } catch (TTransportException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (TException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (SessionTimeOutException e) {
            log.warn("Session Timeout, retrying .........");
            publishRetryUsingHttp(event, mediationStatConfig);
        } finally {
            client.close();
        }
    }


    private static void publishRetryUsingHttp(Event event, MediationStatConfig mediationStatConfig) {


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
            String sessionId = ThriftUtil.getSessionId(mediationStatConfig);
            try {
                client = new THttpClient(mediationStatConfig.getUrl() + "thriftReceiver");
                protocol = new TCompactProtocol(client);
                ReceiverService.Client receiverClient = new ReceiverService.Client(protocol);
                client.open();
                receiverClient.publish(event, sessionId);
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


    private static void putDataIntoMap(Map<String, ByteBuffer> data, String key, String value) {
        if (value != null) {
            data.put(key, ByteBuffer.wrap(value.getBytes()));
        }
    }

}
