/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.mediator.bam;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.AuthenticationException;
import org.wso2.carbon.databridge.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.StreamDefinitionException;
import org.wso2.carbon.databridge.commons.exception.TransportException;
import org.wso2.carbon.mediator.bam.config.BamMediatorException;
import org.wso2.carbon.mediator.bam.config.stream.Property;
import org.wso2.carbon.mediator.bam.config.stream.StreamEntry;
import org.wso2.carbon.mediator.bam.util.BamMediatorConstants;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the main class of the Event Stream that extract data from mediator and send events.
 */
public class Stream {
    private static final Log log = LogFactory.getLog(Stream.class);

    private String streamName;
    private String streamVersion;
    private String streamNickName;
    private String streamDescription;
    private List<Property> properties;
    private List<StreamEntry> streamEntries;
    private String streamId;
    private DataPublisher dataPublisher;
    private boolean security;
    private String serverIp;
    private String authenticationPort;
    private String receiverPort;
    private String userName;
    private String password;
    private ActivityIDSetter activityIDSetter;

    public Stream () {
        streamName = "";
        streamVersion = "";
        streamNickName = "";
        streamDescription = "";
        properties = new ArrayList<Property>();
        streamEntries = new ArrayList<StreamEntry>();
        streamId = null;
        dataPublisher = null;
        security = true;
        serverIp = "";
        authenticationPort = "";
        receiverPort = "";
        userName = "";
        password = "";
        activityIDSetter = new ActivityIDSetter();
    }

    public void sendEvents(MessageContext messageContext) throws BamMediatorException{
        this.activityIDSetter.setActivityIdInSOAPHeader(messageContext);
        try {
            logMessage(messageContext);
        } catch (BamMediatorException e) {
            String errorMsg = "Problem occurred while logging events in the BAM Mediator. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        }
    }

    private void logMessage(MessageContext messageContext)
            throws BamMediatorException {

        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        AxisConfiguration axisConfiguration = msgCtx.getConfigurationContext().getAxisConfiguration();
        int tenantId = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        boolean direction = (!messageContext.isResponse() && !messageContext.isFaultResponse());
        String service = msgCtx.getAxisService().getName();
        String operation = msgCtx.getAxisOperation().getName().getLocalPart();

        if (streamId == null) {
            Agent agent = this.createAgent();
            this.createDataPublisher(agent);
            this.defineEventStream();
        }

        //Publish event for a valid stream
        if (streamId != null && !streamId.isEmpty()) {
            if(log.isDebugEnabled()){
                log.debug("Stream ID: " + streamId);
            }
            // Event for each message
            Event event = new Event(streamId, System.currentTimeMillis(),
                                    this.createMetadata(tenantId),
                                    this.createCorrelationData(messageContext),
                                    this.createPayloadData(messageContext, direction, service, operation)
            );
            try {
                dataPublisher.publish(event);
            } catch (AgentException e) {
                String errorMsg = "Problem with Agent while publishing. " + e.getMessage();
                log.error(errorMsg, e);
                throw new BamMediatorException(errorMsg, e);
            }
        } else {
            if(log.isDebugEnabled()){
                log.debug("streamId is empty.");
            }
        }
    }

    private void createDataPublisher(Agent agent) throws BamMediatorException{
        try {
            if(this.security){
                dataPublisher = new DataPublisher("ssl://" + this.serverIp + ":" + this.authenticationPort, "ssl://" + this.serverIp + ":" + this.authenticationPort, this.userName, this.password, agent);
            } else {
                dataPublisher = new DataPublisher("ssl://" + this.serverIp + ":" + this.authenticationPort, "tcp://" + this.serverIp + ":" + this.receiverPort, this.userName, this.password, agent);
            }
        } catch (MalformedURLException e) {
            String errorMsg = "Given URLs are incorrect. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        } catch (AgentException e) {
            String errorMsg = "Problem while creating the Agent. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        } catch (AuthenticationException e) {
            String errorMsg = "Authentication failed. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        } catch (TransportException e) {
            String errorMsg = "Transport layer problem. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        }

        log.info("Data Publisher Created.");
    }

    private void defineEventStream() throws BamMediatorException{
        StreamIDBuilder streamIDBuilder = new StreamIDBuilder();
        try {
            streamId = dataPublisher.defineStream(streamIDBuilder.createStreamID
                    (this.streamName, this.streamVersion, this.streamNickName, this.streamDescription,
                     this.properties, this.streamEntries));
        } catch (AgentException e) {
            String errorMsg = "Problem while creating the Agent. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        } catch (MalformedStreamDefinitionException e) {
            String errorMsg = "Stream definition is incorrect. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        } catch (StreamDefinitionException e) {
            String errorMsg = "Problem with Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            String errorMsg = "Already there is a different Stream Definition exists for the Name and Version. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        }
        log.info("Event Stream Defined.");
    }

    private Object[] createPayloadData(MessageContext messageContext,
                                       boolean direction, String service, String operation) throws BamMediatorException{
        int numOfProperties = properties.size();
        int numOfEntities = streamEntries.size();
        int i = 0;

        Object[] payloadData = new Object[numOfProperties + numOfEntities + BamMediatorConstants.NUM_OF_CONST_PAYLOAD_PARAMS];
        payloadData[i++] = direction ?
                         BamMediatorConstants.DIRECTION_IN : BamMediatorConstants.DIRECTION_OUT;
        payloadData[i++] = service;
        payloadData[i++] = operation;
        payloadData[i++] = messageContext.getMessageID();
        payloadData[i++] = this.getHttpIp(messageContext, "wso2statistics.request.received.time");
        payloadData[i++] = this.getHttpIp(messageContext, "HTTP_METHOD");
        payloadData[i++] = this.getHttpIp(messageContext, "CHARACTER_SET_ENCODING");
        payloadData[i++] = this.getHttpIp(messageContext, "REMOTE_ADDR");
        payloadData[i++] = this.getHttpIp(messageContext, "TransportInURL");
        payloadData[i++] = this.getHttpIp(messageContext, "messageType");
        payloadData[i++] = this.getHttpIp(messageContext, "REMOTE_HOST");
        payloadData[i] = this.getHttpIp(messageContext, "SERVICE_PREFIX");

        for (i=0; i<numOfProperties; i++) {
            payloadData[BamMediatorConstants.NUM_OF_CONST_PAYLOAD_PARAMS + i] =
                    this.producePropertyValue(properties.get(i), messageContext);
        }

        for (i=0; i<numOfEntities; i++) {
            payloadData[BamMediatorConstants.NUM_OF_CONST_PAYLOAD_PARAMS + numOfProperties + i] =
                    this.produceEntityValue(streamEntries.get(i).getValue(), messageContext);
        }

        return payloadData;
    }

    private Agent createAgent(){
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        /*String keyStorePath = this.ksLocation;
        String keyStorePassword = this.ksPassword;
        agentConfiguration.setTrustStore(keyStorePath);
        agentConfiguration.setTrustStorePassword(keyStorePassword);
        System.setProperty("javax.net.ssl.trustStore", keyStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);*/
        return new Agent(agentConfiguration);
    }


    private Object getHttpIp(MessageContext messageContext, String propertyName){
        org.apache.axis2.context.MessageContext msgCtx =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String output = (String)msgCtx.getLocalProperty(propertyName);
        if(output != null && !output.equals("")){
            return output;
        } else {
            return "";
        }
    }

    private Object[] createMetadata(int tenantId){
        Object[] metaData = new Object[BamMediatorConstants.NUM_OF_CONST_META_PARAMS];
        int i = 0;
        metaData[i] = tenantId;
        return metaData;
    }

    private Object[] createCorrelationData(MessageContext messageContext){
        Object[] correlationData = new Object[BamMediatorConstants.NUM_OF_CONST_CORRELATION_PARAMS];
        int i= 0;
        correlationData[i] = messageContext.getProperty(BamMediatorConstants.MSG_ACTIVITY_ID);
        return correlationData;
    }

    private Object producePropertyValue(Property property, MessageContext messageContext){
        try {
            if(property.isExpression()){
                SynapseXPath synapseXPath = new SynapseXPath(property.getValue());
                return synapseXPath.stringValueOf(messageContext);
            } else {
                return property.getValue();
            }
        } catch (JaxenException e) {
            String errorMsg = "SynapseXPath cannot be created for the Stream Property. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return "";
    }

    private Object produceEntityValue(String valueName, MessageContext messageContext){
        if(valueName.startsWith("$")){ // When entity value is a mediator parameter
            if("$SOAPHeader".equals(valueName)){
                return messageContext.getEnvelope().getHeader().toString();
            } else if ("$SOAPBody".equals(valueName)){
                return messageContext.getEnvelope().getBody().toString();
            } else {
                return "Invalid Entity Parameter !";
            }
        } else {
            return valueName;
        }
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public String getStreamVersion() {
        return streamVersion;
    }

    public void setStreamVersion(String streamVersion) {
        this.streamVersion = streamVersion;
    }

    public void setStreamNickName(String streamNickName) {
        this.streamNickName = streamNickName;
    }

    public void setStreamDescription(String streamDescription) {
        this.streamDescription = streamDescription;
    }

    public void setProperties(List<Property> properties) {
        this.properties = properties;
    }

    public void setStreamEntries(List<StreamEntry> streamEntries) {
        this.streamEntries = streamEntries;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }

    public void setAuthenticationPort(String authenticationPort) {
        this.authenticationPort = authenticationPort;
    }

    public void setReceiverPort(String receiverPort) {
        this.receiverPort = receiverPort;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
