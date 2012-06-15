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
import org.wso2.carbon.agent.Agent;
import org.wso2.carbon.agent.DataPublisher;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.*;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.conf.MediationStatConfig;
import org.wso2.carbon.bam.mediationstats.data.publisher.conf.Property;
import org.wso2.carbon.bam.mediationstats.data.publisher.data.MediationData;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.MediationDataPublisherConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.util.TenantMediationStatConfigData;
import org.wso2.carbon.mediation.statistics.StatisticsRecord;


import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Publisher {


    private static Log log = LogFactory.getLog(Publisher.class);

    //private static boolean isSocketTransportUsed = true;

    public static void process(MediationData mediationData, int tenantID) {
        Map<String, String> correlationData = new HashMap<String, String>();
        List<String> metaDataKeySet = new ArrayList<String>();
        List<String> metaDataValueSet = new ArrayList<String>();
//        Map<String, String> metaData = new HashMap<String, String>();
        //Map<String, ByteBuffer> eventData = new HashMap<String, ByteBuffer>();

        List<String> eventData = new ArrayList<String>();

        Map<Integer, MediationStatConfig> mediationStatConfigMap = TenantMediationStatConfigData.
                getTenantSpecificEventingConfigData();
        MediationStatConfig mediationStatConfig = mediationStatConfigMap.get(tenantID);

        addEventData(eventData, mediationData);
        addMetaData(metaDataKeySet,metaDataValueSet, mediationData, mediationStatConfig);
        addCorrelationData(correlationData, mediationData);

        //Event event = new Event();
        //event.setCorrelation(correlationData);
        //event.setMeta(metaData);
        //event.setEvent(eventData);
        //publish(event, tenantID);

        publishToAgent(eventData, metaDataKeySet, metaDataValueSet, mediationStatConfig);
    }

    private static void addCorrelationData(Map<String, String> correlationData,
                                           MediationData mediationData) {
    }

    private static void addMetaData(List<String> metaDataKeySet,List<String> metaDataValueSet, MediationData mediationData,
                                    MediationStatConfig mediationStatConfig) {
        Property[] properties = mediationStatConfig.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && property.getKey() != "") {
                    //putDataIntoMap(metaData, property.getKey(), property.getValue());
                    metaDataKeySet.add(property.getKey());
                    metaDataValueSet.add(property.getValue());
                }
            }
        }
    }


    private static void addEventData(List<String> eventData,
                                     MediationData mediationData) {
        eventData.add(mediationData.getDirection());
        eventData.add(mediationData.getTimestamp().toString());
        eventData.add(mediationData.getResourceId());
        eventData.add(mediationData.getStatsType());
        StatisticsRecord record = mediationData.getStatisticsRecord();
        eventData.add(Long.toString(record.getMaxTime()));
        eventData.add(Double.toString(record.getAvgTime()));
        eventData.add(Long.toString(record.getMinTime()));
        eventData.add(Integer.toString(record.getFaultCount()));
        eventData.add(Integer.toString(record.getTotalCount()));
        Map<String, Object> errorMap = mediationData.getErrorMap();
        if (errorMap != null) {
            for (Map.Entry<String, Object> errorEntry : errorMap.entrySet()) {
                Object entryValue = errorEntry.getValue();
                if (entryValue instanceof Integer) {
                    eventData.add(((Integer)entryValue).toString());
                } else if (entryValue instanceof String) {
                    eventData.add(((String) entryValue));
                }
            }
        }
    }

    /*private static void putDataIntoMap(Map<String, String> data, String key, String value) {
        if (value != null) {
            data.put(key, value);
        }
    }*/

    private static DataPublisher dataPublisher = null;
    private static String streamId = null;

    private static void publishToAgent(List<String> eventData, List<String> metaDataKeySet,List<String> metaDataValueSet, MediationStatConfig mediationStatConfig) {

        try {
            URL url = new URL(mediationStatConfig.getUrl());
            String serverUrl = "tcp://"+url.getHost()+":7611";
            String userName = mediationStatConfig.getUserName();
            String passWord = mediationStatConfig.getPassword();
            Object[] metaData = metaDataKeySet.toArray();



            if (dataPublisher == null) {
                dataPublisher = new DataPublisher(serverUrl, userName, passWord);
                Agent agent = new Agent();



                dataPublisher.setAgent(agent);
                EventStreamDefinition eventStreamDefinition = new EventStreamDefinition("org.wso2.esb.MediatorStatistics","1.3.0");
                eventStreamDefinition.setDescription("Some Desc");
                for(int i = 0; i < metaData.length; i++){
                    eventStreamDefinition.addMetaData(metaData[i].toString(), AttributeType.STRING);
                }
                eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.DIRECTION,
                        AttributeType.STRING);
                eventStreamDefinition.addPayloadData(BAMDataPublisherConstants.TIMESTAMP,
                        AttributeType.STRING);
                eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.RESOURCE_ID,
                        AttributeType.STRING);
                eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.STATS_TYPE,
                        AttributeType.STRING);
                eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.MAX_PROCESS_TIME,
                        AttributeType.STRING);
                eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.AVG_PROCESS_TIME,
                        AttributeType.STRING);
                eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.MIN_PROCESS_TIME,
                        AttributeType.STRING);
                eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.FAULT_COUNT,
                        AttributeType.STRING);
                eventStreamDefinition.addPayloadData(MediationDataPublisherConstants.COUNT,
                        AttributeType.STRING);
                /*streamId = dataPublisher.defineEventStream("{" +
                        "  'name':'org.wso2.esb.MediatorStatistics'," +
                        "  'version':'1.3.0'," +
                        "  'nickName': 'Stock_Quote_Information'," +
                        "  'description': 'Some Desc'," +
                        "  'metaData':[" +
                        "          {'name':'test','type':'STRING'}," +
                        "          {'name':'testTwo','type':'STRING'}" +
                        "  ]," +
                        "  'payloadData':[" +
                        "          {'name':'" + MediationDataPublisherConstants.DIRECTION + "','type':'STRING'}," +
                        "          {'name':'" + BAMDataPublisherConstants.TIMESTAMP + "','type':'STRING'}," +
                        "          {'name':'" + MediationDataPublisherConstants.RESOURCE_ID + "','type':'STRING'}," +
                        "          {'name':'" + MediationDataPublisherConstants.STATS_TYPE + "','type':'STRING'}," +
                        "          {'name':'" + MediationDataPublisherConstants.MAX_PROCESS_TIME + "','type':'STRING'}," +
                        "          {'name':'" + MediationDataPublisherConstants.AVG_PROCESS_TIME + "','type':'STRING'}," +
                        "          {'name':'" + MediationDataPublisherConstants.MIN_PROCESS_TIME + "','type':'STRING'}," +
                        "          {'name':'" + MediationDataPublisherConstants.FAULT_COUNT + "','type':'STRING'}," +
                        "          {'name':'" + MediationDataPublisherConstants.COUNT + "','type':'STRING'}" +
                        "  ]" +
                        "}");*/
            }


            dataPublisher.publish(streamId, metaDataValueSet.toArray(), null,
                    eventData.toArray());
            log.info("metadata information" + metaDataKeySet);


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

}
