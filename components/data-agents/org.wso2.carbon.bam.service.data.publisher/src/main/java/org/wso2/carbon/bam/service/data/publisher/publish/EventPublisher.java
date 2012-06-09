package org.wso2.carbon.bam.service.data.publisher.publish;


import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.agent.Agent;
import org.wso2.carbon.agent.DataPublisher;
import org.wso2.carbon.agent.commons.Attribute;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.AuthenticationException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.conf.AgentConfiguration;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.exception.TransportException;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.data.Event;
import org.wso2.carbon.bam.service.data.publisher.util.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.util.StatisticsType;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class EventPublisher {


    private static String streamDefForActivity;
    private static String streamDefForServiceStats;
    private static String streamDefForActivityServiceStats;

    private static Log log = LogFactory.getLog(EventPublisher.class);

    public void publish(Event event, EventingConfigData configData) {

        List<Object> correlationData = event.getCorrelationData();
        List<Object> metaData = event.getMetaData();
        List<Object> payLoadData = event.getEventData();
        StatisticsType statisticsType = event.getStatisticsType();


        AgentConfiguration agentConfiguration = new AgentConfiguration();
        Agent agent = new Agent(agentConfiguration);

        //create data publisher
        try {

            DataPublisher dataPublisher = new DataPublisher(configData.getUrl(),
                                                            configData.getUserName(),
                                                            configData.getPassword(),
                                                            agent);

            String streamId = null;


            switch (statisticsType) {

                case ACTIVITY_STATS:
                    if (streamDefForActivity == null) {
                        streamDefForActivity = streamDefinitionForActivity(configData);
                    }
                    streamId = streamDefForActivity;
                    break;
                case SERVICE_STATS:
                    if (streamDefForServiceStats == null) {
                        streamDefForServiceStats = streamDefinitionForServiceStats(configData);
                    }
                    streamId = streamDefForServiceStats;
                    break;
                case ACTIVITY_SERVICE_STATS:
                    if (streamDefForActivityServiceStats == null) {
                        streamDefForActivityServiceStats = streamDefinitionForActivityServiceStats(configData);
                    }
                    streamId = streamDefForActivityServiceStats;
                    break;
            }

            dataPublisher.publish(streamId, metaData.toArray(), correlationData.toArray(),
                                  payLoadData.toArray());

        } catch (MalformedURLException e) {
            log.error("Malformed URL, please check the URL", e);
        } catch (AgentException e) {
            log.error("Error occurred while sending the event", e);
        } catch (AuthenticationException e) {
            log.error("Please check the user name and password",
                      e);
        } catch (TransportException e) {
            log.error("Error occurred while sending the event", e);
        }

    }

    private String streamDefinitionForActivity(EventingConfigData configData) {
        String streamDefinition = null;
        try {
            EventStreamDefinition streamDef = new EventStreamDefinition(
                    configData.getStreamName(), configData.getVersion());
            streamDef.setNickName(configData.getNickName());
            streamDef.setDescription(configData.getDescription());

            streamDef.setMetaData(setMetadata());

            List<Attribute> payLoadData = new ArrayList<Attribute>();
            payLoadData = addCommonPayLoadData(payLoadData);
            payLoadData = addInOnlyPayLoadData(payLoadData);
            payLoadData = addOutOnlyPayLoadData(payLoadData);
            streamDef.setPayloadData(payLoadData);

            streamDef.setCorrelationData(setActivityCorrelationData());
            Gson gson = new Gson();
            streamDefinition = gson.toJson(streamDef);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Malformed Stream Definition", e);
        }
        return streamDefinition;
    }

    private String streamDefinitionForServiceStats(EventingConfigData configData) {
        String streamDefinition = null;
        try {
            EventStreamDefinition streamDef = new EventStreamDefinition(
                    configData.getStreamName(), configData.getVersion());
            streamDef.setNickName(configData.getNickName());
            streamDef.setDescription(configData.getDescription());

            streamDef.setMetaData(setMetadata());

            List<Attribute> payLoadData = new ArrayList<Attribute>();
            payLoadData = addCommonPayLoadData(payLoadData);
            payLoadData = addServiceStatsPayLoadData(payLoadData);
            streamDef.setPayloadData(payLoadData);

            streamDef.setCorrelationData(setActivityCorrelationData());
            Gson gson = new Gson();
            streamDefinition = gson.toJson(streamDef);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Malformed Stream Definition", e);
        }
        return streamDefinition;
    }

    private String streamDefinitionForActivityServiceStats(EventingConfigData configData) {
        String streamDefinition = null;
        try {
            EventStreamDefinition streamDef = new EventStreamDefinition(
                    configData.getStreamName(), configData.getVersion());
            streamDef.setNickName(configData.getNickName());
            streamDef.setDescription(configData.getDescription());

            streamDef.setMetaData(setMetadata());

            List<Attribute> payLoadData = new ArrayList<Attribute>();
            payLoadData = addCommonPayLoadData(payLoadData);
            payLoadData = addInOnlyPayLoadData(payLoadData);
            payLoadData = addOutOnlyPayLoadData(payLoadData);
            payLoadData = addServiceStatsPayLoadData(payLoadData);
            streamDef.setPayloadData(payLoadData);

            streamDef.setCorrelationData(setActivityCorrelationData());
            Gson gson = new Gson();
            streamDefinition = gson.toJson(streamDef);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Malformed Stream Definition", e);
        }
        return streamDefinition;
    }


    private List<Attribute> setActivityCorrelationData() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.add(new Attribute(BAMDataPublisherConstants.MSG_ACTIVITY_ID, AttributeType.STRING));
        return attributeList;
    }

    private List<Attribute> addInOnlyPayLoadData(List<Attribute> payLoadData) {
        payLoadData.add(new Attribute(BAMDataPublisherConstants.IN_MSG_ID,
                                      AttributeType.STRING));
        payLoadData.add(new Attribute(BAMDataPublisherConstants.IN_MSG_BODY,
                                      AttributeType.STRING));
        return payLoadData;
    }

    private List<Attribute> addOutOnlyPayLoadData(List<Attribute> payLoadData) {
        payLoadData.add(new Attribute(BAMDataPublisherConstants.OUT_MSG_ID,
                                      AttributeType.STRING));
        payLoadData.add(new Attribute(BAMDataPublisherConstants.OUT_MSG_BODY,
                                      AttributeType.STRING));
        return payLoadData;
    }

    private List<Attribute> addCommonPayLoadData(List<Attribute> payLoadData) {
        payLoadData.add(new Attribute(BAMDataPublisherConstants.SERVICE_NAME,
                                      AttributeType.STRING));
        payLoadData.add(new Attribute(BAMDataPublisherConstants.OPERATION_NAME,
                                      AttributeType.STRING));
        payLoadData.add(new Attribute(BAMDataPublisherConstants.TIMESTAMP,
                                      AttributeType.LONG));
        return payLoadData;
    }

    private List<Attribute> addServiceStatsPayLoadData(List<Attribute> payLoadData) {
        payLoadData.add(new Attribute(ServiceStatisticsPublisherConstants.RESPONSE_TIME,
                                      AttributeType.LONG));
        payLoadData.add(new Attribute(ServiceStatisticsPublisherConstants.REQUEST_COUNT,
                                      AttributeType.INT));
        payLoadData.add(new Attribute(ServiceStatisticsPublisherConstants.RESPONSE_COUNT,
                                      AttributeType.INT));
        payLoadData.add(new Attribute(ServiceStatisticsPublisherConstants.FAULT_COUNT,
                                      AttributeType.INT));
        return payLoadData;
    }

    private List<Attribute> setMetadata() {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        attributeList.add(new Attribute(BAMDataPublisherConstants.REQUEST_URL,
                                        AttributeType.STRING));
        attributeList.add(new Attribute(BAMDataPublisherConstants.REMOTE_ADDRESS,
                                        AttributeType.STRING));
        attributeList.add(new Attribute(BAMDataPublisherConstants.CONTENT_TYPE,
                                        AttributeType.STRING));
        attributeList.add(new Attribute(BAMDataPublisherConstants.USER_AGENT,
                                        AttributeType.STRING));
        attributeList.add(new Attribute(BAMDataPublisherConstants.HOST,
                                        AttributeType.STRING));
        attributeList.add(new Attribute(BAMDataPublisherConstants.REFERER,
                                        AttributeType.STRING));
        return attributeList;
    }
}
