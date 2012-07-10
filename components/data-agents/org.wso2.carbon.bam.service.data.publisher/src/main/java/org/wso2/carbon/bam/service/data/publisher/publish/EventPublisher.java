package org.wso2.carbon.bam.service.data.publisher.publish;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.conf.EventPublisherConfig;
import org.wso2.carbon.bam.service.data.publisher.conf.EventingConfigData;
import org.wso2.carbon.bam.service.data.publisher.conf.Property;
import org.wso2.carbon.bam.service.data.publisher.data.Event;
import org.wso2.carbon.bam.service.data.publisher.util.ServiceStatisticsPublisherConstants;
import org.wso2.carbon.bam.service.data.publisher.util.StatisticsType;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

public class EventPublisher {


    private StreamDefinition streamDefForActivity;
    private StreamDefinition streamDefForServiceStats;
    private StreamDefinition streamDefForActivityServiceStats;

    private boolean isStreamDefinitionAlreadyExist = false;

    private static Log log = LogFactory.getLog(EventPublisher.class);

    public void publish(Event event, EventingConfigData configData) {

        List<Object> correlationData = event.getCorrelationData();
        List<Object> metaData = event.getMetaData();
        List<Object> payLoadData = event.getEventData();
        StatisticsType statisticsType = event.getStatisticsType();

        String key = configData.getUrl() + "_" + configData.getUserName() + "_" + configData.getPassword();
        EventPublisherConfig eventPublisherConfig = ServiceAgentUtil.getEventPublisherConfig(key);

        StreamDefinition streamDef = getStreamDefinition(configData, statisticsType);
        String streamId = null;
        //create data publisher
        try {

            if(eventPublisherConfig==null){
                eventPublisherConfig = new EventPublisherConfig();
                AgentConfiguration agentConfiguration = new AgentConfiguration();
                DataPublisher dataPublisher = new DataPublisher(configData.getUrl(),
                                                                configData.getUserName(),
                                                                configData.getPassword());
                eventPublisherConfig.setAgentConfiguration(agentConfiguration);
                eventPublisherConfig.setDataPublisher(dataPublisher);

                ServiceAgentUtil.getEventPublisherConfigMap().put(key,eventPublisherConfig);
            }

            DataPublisher dataPublisher = eventPublisherConfig.getDataPublisher();

            try {
                streamId = dataPublisher.findStream(configData.getStreamName(), configData.getVersion());
            } catch (NoStreamDefinitionExistException e) {
                streamId = dataPublisher.defineStream(streamDef);
            }

            dataPublisher.publish(streamId, getObjectArray(metaData), getObjectArray(correlationData),
                                  getObjectArray(payLoadData));

        } catch (MalformedURLException e) {
            log.error("Malformed URL, please check the URL", e);
        } catch (AgentException e) {
            log.error("Error occurred while sending the event", e);
        } catch (AuthenticationException e) {
            log.error("Please check the user name and password",
                      e);
        } catch (TransportException e) {
            log.error("Error occurred while sending the event", e);
        } catch (StreamDefinitionException e) {
            log.error("Error occurred while defining the event", e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            log.error("Stream definition already exist", e);
        } catch (MalformedStreamDefinitionException e) {
            log.error("Malformed stream definition", e);
        }

    }

    private Object[] getObjectArray(List<Object> list){
        if(list.size()>0){
            return list.toArray();
        }
        return null;
    }

    private StreamDefinition getStreamDefinition(EventingConfigData configData,
                                                      StatisticsType statisticsType) {
        StreamDefinition streamDef= null;
        switch (statisticsType) {

            case ACTIVITY_STATS:
                if (streamDefForActivity == null) {
                    streamDefForActivity = streamDefinitionForActivity(configData);
                }
                streamDef = streamDefForActivity;
                break;
            case SERVICE_STATS:
                if (streamDefForServiceStats == null) {
                    streamDefForServiceStats = streamDefinitionForServiceStats(configData);
                }
                streamDef = streamDefForServiceStats;
                break;
            case ACTIVITY_SERVICE_STATS:
                if (streamDefForActivityServiceStats == null) {
                    streamDefForActivityServiceStats = streamDefinitionForActivityServiceStats(configData);
                }
                streamDef = streamDefForActivityServiceStats;
                break;
        }
        return streamDef;
    }

    private StreamDefinition streamDefinitionForActivity(EventingConfigData configData) {
        StreamDefinition streamDef = null;
        try {
            streamDef = new StreamDefinition(
                    configData.getStreamName(), configData.getVersion());
            streamDef.setNickName(configData.getNickName());
            streamDef.setDescription(configData.getDescription());

            List<Attribute> metaDataAttributeList = new ArrayList<Attribute>();
            setUserAgentMetadata(metaDataAttributeList);
            setPropertiesAsMetaData(metaDataAttributeList,configData);

            streamDef.setMetaData(metaDataAttributeList);

            List<Attribute> payLoadData = new ArrayList<Attribute>();
            payLoadData = addCommonPayLoadData(payLoadData);
            payLoadData = addInOnlyPayLoadData(payLoadData);
            payLoadData = addOutOnlyPayLoadData(payLoadData);
            streamDef.setPayloadData(payLoadData);

            streamDef.setCorrelationData(setActivityCorrelationData());
        } catch (MalformedStreamDefinitionException e) {
            log.error("Malformed Stream Definition", e);
        }
        return streamDef;
    }

    private StreamDefinition streamDefinitionForServiceStats(EventingConfigData configData) {
        StreamDefinition streamDef = null;
        try {
            streamDef = new StreamDefinition(
                    configData.getStreamName(), configData.getVersion());
            streamDef.setNickName(configData.getNickName());
            streamDef.setDescription(configData.getDescription());

            List<Attribute> metaDataAttributeList = new ArrayList<Attribute>();
            setUserAgentMetadata(metaDataAttributeList);
            setPropertiesAsMetaData(metaDataAttributeList,configData);

            streamDef.setMetaData(metaDataAttributeList);

            List<Attribute> payLoadData = new ArrayList<Attribute>();
            payLoadData = addCommonPayLoadData(payLoadData);
            payLoadData = addServiceStatsPayLoadData(payLoadData);
            streamDef.setPayloadData(payLoadData);

        } catch (MalformedStreamDefinitionException e) {
            log.error("Malformed Stream Definition", e);
        }
        return streamDef;
    }

    private StreamDefinition streamDefinitionForActivityServiceStats(EventingConfigData configData) {
        StreamDefinition streamDef = null;
        try {
            streamDef = new StreamDefinition(
                    configData.getStreamName(), configData.getVersion());
            streamDef.setNickName(configData.getNickName());
            streamDef.setDescription(configData.getDescription());

            List<Attribute> metaDataAttributeList = new ArrayList<Attribute>();
            setUserAgentMetadata(metaDataAttributeList);
            setPropertiesAsMetaData(metaDataAttributeList,configData);

            streamDef.setMetaData(metaDataAttributeList);

            List<Attribute> payLoadData = new ArrayList<Attribute>();
            payLoadData = addCommonPayLoadData(payLoadData);
            payLoadData = addInOnlyPayLoadData(payLoadData);
            payLoadData = addOutOnlyPayLoadData(payLoadData);
            payLoadData = addServiceStatsPayLoadData(payLoadData);
            streamDef.setPayloadData(payLoadData);

            streamDef.setCorrelationData(setActivityCorrelationData());
        } catch (MalformedStreamDefinitionException e) {
            log.error("Malformed Stream Definition", e);
        }
        return streamDef;
    }

    private void setPropertiesAsMetaData(List<Attribute> metaDataAttributeList,
                                         EventingConfigData configData) {
        Property[] properties = configData.getProperties();
        if (properties != null) {
            for (int i = 0; i < properties.length; i++) {
                Property property = properties[i];
                if (property.getKey() != null && !property.getKey().isEmpty()) {
                    metaDataAttributeList.add(new Attribute(property.getKey(), AttributeType.STRING));
                }
            }
        }
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

    private void setUserAgentMetadata(List<Attribute> attributeList) {
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
    }
}
