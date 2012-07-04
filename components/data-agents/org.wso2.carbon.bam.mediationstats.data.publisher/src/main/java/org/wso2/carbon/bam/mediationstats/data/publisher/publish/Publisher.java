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
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.bam.mediationstats.data.publisher.conf.MediationStatConfig;
import org.wso2.carbon.bam.mediationstats.data.publisher.conf.Property;
import org.wso2.carbon.bam.mediationstats.data.publisher.data.MediationData;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.MediationDataPublisherConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.PublisherUtils;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.TenantMediationStatConfigData;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.mediation.statistics.StatisticsRecord;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Publisher {


    private static Log log = LogFactory.getLog(Publisher.class);
    private static boolean isStreamDefinitionAlreadyExist = false;

    public static void process(MediationData mediationData, int tenantID) {
        List<String> metaDataKeyList = new ArrayList<String>();
        List<String> metaDataValueList = new ArrayList<String>();

        List<Object> eventData = new ArrayList<Object>();

        Map<Integer, MediationStatConfig> mediationStatConfigMap = TenantMediationStatConfigData.
                getTenantSpecificEventingConfigData();
        MediationStatConfig mediationStatConfig = mediationStatConfigMap.get(tenantID);

        addEventData(eventData, mediationData);
        addMetaData(metaDataKeyList, metaDataValueList, mediationStatConfig);

        publishToAgent(eventData, metaDataKeyList, metaDataValueList, mediationStatConfig);
    }


    private static void addMetaData(List<String> metaDataKeyList, List<String> metaDataValueList,
                                    MediationStatConfig mediationStatConfig) {
        Property[] properties = mediationStatConfig.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && !property.getKey().isEmpty()) {
                    metaDataKeyList.add(property.getKey());
                    metaDataValueList.add(property.getValue());
                }
            }
        }
    }


    private static void addEventData(List<Object> eventData,
                                     MediationData mediationData) {
        eventData.add(mediationData.getDirection());
        eventData.add(mediationData.getTimestamp().getTime());
        eventData.add(mediationData.getResourceId());
        eventData.add(mediationData.getStatsType());
        StatisticsRecord record = mediationData.getStatisticsRecord();
        eventData.add(record.getMaxTime());
        eventData.add(record.getAvgTime());
        eventData.add(record.getMinTime());
        eventData.add(record.getFaultCount());
        eventData.add(record.getTotalCount());

        //TODO : Send these values to BAM side.
/*        Map<String, Object> errorMap = mediationData.getErrorMap();
        if (errorMap != null) {
            for (Map.Entry<String, Object> errorEntry : errorMap.entrySet()) {
                Object entryValue = errorEntry.getValue();
                if (entryValue instanceof Integer) {
                    eventData.add(((Integer) entryValue).toString());
                } else if (entryValue instanceof String) {
                    eventData.add(((String) entryValue));
                }
            }
        }*/
    }


    private static DataPublisher dataPublisher = null;
    private static String streamId = null;

    private static void publishToAgent(List<Object> eventData,
                                       List<String> metaDataKeyList,
                                       List<String> metaDataValueList,
                                       MediationStatConfig mediationStatConfig) {

        try {
            String serverUrl = mediationStatConfig.getUrl();
            String userName = mediationStatConfig.getUserName();
            String passWord = mediationStatConfig.getPassword();
            Object[] metaData = metaDataKeyList.toArray();

            String key = serverUrl + "_" + userName
                         + "_" + passWord;
            EventPublisherConfig eventPublisherConfig = PublisherUtils.getEventPublisherConfig(key);

            StreamDefinition streamDef = getEventStreamDefinition(mediationStatConfig, metaData);
            try {
                if (eventPublisherConfig == null) {
                    eventPublisherConfig = new EventPublisherConfig();
                    AgentConfiguration agentConfiguration = new AgentConfiguration();
                    dataPublisher = new DataPublisher(serverUrl,
                                                                    userName,
                                                                    passWord);
                    eventPublisherConfig.setAgentConfiguration(agentConfiguration);
                    eventPublisherConfig.setDataPublisher(dataPublisher);

                    PublisherUtils.getEventPublisherConfigMap().put(key, eventPublisherConfig);
                }

                try {
                    streamId = dataPublisher.findStream(mediationStatConfig.getStreamName(),
                                                        mediationStatConfig.getVersion());
                } catch (NoStreamDefinitionExistException e) {
                    streamId = dataPublisher.defineStream(streamDef);
                } catch (StreamDefinitionException e) {
                    e.printStackTrace();
                }

                dataPublisher.publish(streamId, metaDataValueList.toArray(), null,
                                      eventData.toArray());

            } catch (StreamDefinitionException e1) {
                e1.printStackTrace();
            } catch (DifferentStreamDefinitionAlreadyDefinedException e1) {
                e1.printStackTrace();
            }

        } catch (MalformedURLException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (AuthenticationException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (TransportException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (AgentException e) {
            log.error("Unable to publish event to BAM", e);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Unable to publish event to BAM", e);
        }

    }

    public static StreamDefinition getEventStreamDefinition(
            MediationStatConfig mediationStatConfig,
            Object[] metaData)
            throws MalformedStreamDefinitionException {
        StreamDefinition eventStreamDefinition = new StreamDefinition(
                mediationStatConfig.getStreamName(), mediationStatConfig.getVersion());
        eventStreamDefinition.setNickName(mediationStatConfig.getNickName());
        eventStreamDefinition.setDescription(mediationStatConfig.getDescription());
        for (int i = 0; i < metaData.length; i++) {
            eventStreamDefinition.addMetaData(metaData[i].toString(), AttributeType.STRING);
        }
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.DIRECTION,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(BAMDataPublisherConstants.TIMESTAMP,
                                             AttributeType.LONG);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.RESOURCE_ID,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.STATS_TYPE,
                                             AttributeType.STRING);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.MAX_PROCESS_TIME,
                                             AttributeType.LONG);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.AVG_PROCESS_TIME,
                                             AttributeType.DOUBLE);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.MIN_PROCESS_TIME,
                                             AttributeType.LONG);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.FAULT_COUNT,
                                             AttributeType.INT);
        eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.COUNT,
                                             AttributeType.INT);
        return eventStreamDefinition;
    }

}
