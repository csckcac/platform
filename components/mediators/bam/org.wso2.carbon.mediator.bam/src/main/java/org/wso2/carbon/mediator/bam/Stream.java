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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
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

import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * This is the main class of the Event Stream that extract data from mediator and send events.
 */
public class Stream {
    private static final Log log = LogFactory.getLog(Stream.class);

    private String streamName = "";
    private String streamVersion = "";
    private String streamNickName = "";
    private String streamDescription = "";
    private List<Property> properties = new ArrayList<Property>();
    private List<StreamEntry> streamEntries = new ArrayList<StreamEntry>();
    private String streamId = null;
    private DataPublisher dataPublisher = null;
    private boolean security = true;
    private String ksLocation = "";
    private String ksPassword = "";
    private String serverIp = "";
    private String authenticationPort = "";
    private String receiverPort = "";
    private String userName = "";
    private String password = "";
    
    public void sendEvents(MessageContext messageContext){
        this.setActivityIdInSOAPHeader(messageContext);
        try {
            logMessage(messageContext);
        } catch (BamMediatorException e) {
            String errorMsg = "Problem occurred while logging in the BAM Mediator. " + e.getMessage();
            log.error(errorMsg, e);
        }
    }

    private void setActivityIdInSOAPHeader(MessageContext synapseContext) {


            // Property name would be "Parent_uuid"
            // Property Value would be "Parent_uuid_messageid"
            UUID uuid = UUID.randomUUID();
            String uuidString = uuid.toString();

            Axis2MessageContext axis2smc = (Axis2MessageContext) synapseContext;
            org.apache.axis2.context.MessageContext axis2MessageContext = axis2smc.getAxis2MessageContext();

            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace(BamMediatorConstants.BAM_HEADER_NAMESPACE_URI, "ns");
            SOAPEnvelope soapEnvelope = axis2MessageContext.getEnvelope();
            String soapNamespaceURI = soapEnvelope.getNamespace().getNamespaceURI();
            SOAPFactory soapFactory = null;


            if (soapNamespaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                soapFactory = OMAbstractFactory.getSOAP11Factory();
            } else if (soapNamespaceURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                soapFactory = OMAbstractFactory.getSOAP12Factory();
            } else {
                log.error("Not a standard soap message");
            }

            this.setActivityIDInSOAPHeaderWithConditioning(soapEnvelope, omNs, synapseContext, uuidString, soapFactory);


    }

    private void setActivityIDInSOAPHeaderWithConditioning(SOAPEnvelope soapEnvelope,
                                                           OMNamespace omNs, MessageContext synapseContext,
                                                           String uuidString, SOAPFactory soapFactory){
        try {
            SOAPHeaderBlock soapHeaderBlock;

            // If header is not null check for  BAM headers
            if (soapEnvelope.getHeader() != null) {
                Iterator itr = soapEnvelope.getHeader().getChildrenWithName(
                        new QName(BamMediatorConstants.BAM_HEADER_NAMESPACE_URI,
                                  BamMediatorConstants.BAM_EVENT));
                if (!itr.hasNext()) {
                    soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock(
                            BamMediatorConstants.BAM_EVENT, omNs);
                    if (synapseContext.getProperty(BamMediatorConstants.MSG_ACTIVITY_ID) == null) { // this if
                        // condition we add
                        // to track failure messages coming from DS.That is a new message. So, doesn't have activityID.Getting activityID
                        // from the synapseContext.property
                        soapHeaderBlock.addAttribute(BamMediatorConstants.ACTIVITY_ID, uuidString, null);
                        synapseContext.setProperty(BamMediatorConstants.MSG_ACTIVITY_ID, uuidString);
                    } else {
                        soapHeaderBlock.addAttribute(BamMediatorConstants.ACTIVITY_ID, (String) synapseContext
                                .getProperty(BamMediatorConstants.MSG_ACTIVITY_ID), null);
                    }
                } else {// If header is not null check for  BAM headers

                    // If the BAM header already present
                    //    1. If activity id is not present generate a one and include it to BAM header
                    //    2. Set activity id in synapse context for response path
                    OMElement bamHeader = (OMElement) itr.next();
                    OMAttribute activityIdAttr = bamHeader.getAttribute(new QName(
                            BamMediatorConstants.ACTIVITY_ID));
                    if (activityIdAttr != null) {
                        String activityId = activityIdAttr.getAttributeValue();
                        synapseContext.setProperty(BamMediatorConstants.MSG_ACTIVITY_ID,
                                                   activityId);
                    } else {
                        bamHeader.addAttribute(BamMediatorConstants.ACTIVITY_ID, uuidString, null);
                        synapseContext.setProperty(BamMediatorConstants.MSG_ACTIVITY_ID, uuidString);
                    }
                }
            } else {
                if (soapFactory != null) {
                    (soapFactory).createSOAPHeader(soapEnvelope); // TO DO
                }
                if (soapEnvelope.getHeader() != null) {
                    soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock(BamMediatorConstants.BAM_EVENT, omNs);
                    if (synapseContext.getProperty(BamMediatorConstants.MSG_ACTIVITY_ID) == null) { // this if
                        // condition we add
                        // to track failure messages coming from
                        // DS.That is a new message. So, doesn't have activityID.Getting activityID
                        // from the synapseContext.property
                        soapHeaderBlock.addAttribute(BamMediatorConstants.ACTIVITY_ID, uuidString, null);
                        synapseContext.setProperty(BamMediatorConstants.MSG_ACTIVITY_ID, uuidString);
                    } else {
                        soapHeaderBlock.addAttribute(BamMediatorConstants.ACTIVITY_ID, (String) synapseContext
                                .getProperty(BamMediatorConstants.MSG_ACTIVITY_ID), null);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error while processing MessageHeaderMediator...", e);
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
            log.info("Stream ID: " + streamId);
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
            log.info("streamId is empty.");
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

        try {
            streamId = dataPublisher.defineStream("{" +
                                                  "  'name':'" + this.streamName + "'," +
                                                  "  '" + BamMediatorConstants.VERSION + "':'" + this.streamVersion + "'," +
                                                  "  '" + BamMediatorConstants.NICK_NAME + "': '" + this.streamNickName + "'," +
                                                  "  '" + BamMediatorConstants.DESCRIPTION + "': '" + this.streamDescription + "'," +
                                                  "  'correlationData':[" +
                                                  "          {'name':'" + BamMediatorConstants.ACTIVITY_ID + "','type':'STRING'}" +
                                                  "  ]," +
                                                  "  'metaData':[" +
                                                  "          {'name':'tenantId','type':'INT'}" +
                                                  "  ]," +
                                                  "  'payloadData':[" +
                                                  this.getConstantStreamDefinitionString() +
                                                  this.getPropertyStreamDefinitionString() +
                                                  this.getEntityStreamDefinitionString() +
                                                  "  ]" +
                                                  "}");
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

        Object[] payloadData = new Object[numOfProperties + numOfEntities + 12];
        payloadData[0] = direction ?
                         BamMediatorConstants.DIRECTION_IN : BamMediatorConstants.DIRECTION_OUT;
        payloadData[1] = service;
        payloadData[2] = operation;
        payloadData[3] = messageContext.getMessageID();
        payloadData[4] = this.getHttpIp(messageContext, "wso2statistics.request.received.time");
        payloadData[5] = this.getHttpIp(messageContext, "HTTP_METHOD");
        payloadData[6] = this.getHttpIp(messageContext, "CHARACTER_SET_ENCODING");
        payloadData[7] = this.getHttpIp(messageContext, "REMOTE_ADDR");
        payloadData[8] = this.getHttpIp(messageContext, "TransportInURL");
        payloadData[9] = this.getHttpIp(messageContext, "messageType");
        payloadData[10] = this.getHttpIp(messageContext, "REMOTE_HOST");
        payloadData[11] = this.getHttpIp(messageContext, "SERVICE_PREFIX");

        for (int i=0; i<numOfProperties; i++) {
            payloadData[12 + i] = this.producePropertyValue(properties.get(i), messageContext);
        }

        for (int i=0; i<numOfEntities; i++) {
            payloadData[12 + numOfProperties + i] = this.produceEntityValue(streamEntries.get(i).getValue(), messageContext);
        }

        return payloadData;
    }

    private Agent createAgent(){
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        String keyStorePath = this.ksLocation;
        String keyStorePassword = this.ksPassword;
        agentConfiguration.setTrustStore(keyStorePath);
        agentConfiguration.setTrustStorePassword(keyStorePassword);
        System.setProperty("javax.net.ssl.trustStore", keyStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);
        return new Agent(agentConfiguration);
    }


    private Object getHttpIp(MessageContext messageContext, String propertyName){
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        String output = (String)msgCtx.getLocalProperty(propertyName);
        if(output != null && !output.equals("")){
            return output;
        } else {
            return "";
        }
    }

    private Object[] createMetadata(int tenantId){
        Object[] metaData = new Object[1];
        metaData[0] = tenantId;
        return metaData;
    }

    private Object[] createCorrelationData(MessageContext messageContext){
        Object[] correlationData = new Object[1];
        correlationData[0] = messageContext.getProperty(BamMediatorConstants.MSG_ACTIVITY_ID);
        return correlationData;
    }

    private String getPropertyStreamDefinitionString(){
        String propertyString = "";
        for (Property property : properties) {
            propertyString = propertyString + "," + this.getStreamDefinitionEntryString(property.getKey(), BamMediatorConstants.STRING);
        }
        return propertyString;
    }

    private String getEntityStreamDefinitionString(){
        String entityString = "";
        for (StreamEntry streamEntry : streamEntries) {
            entityString = entityString + "," + this.getStreamDefinitionEntryString(streamEntry.getName(), streamEntry.getType());
        }
        return entityString;
    }

    private String getConstantStreamDefinitionString(){
        String[] nameStrings = new String[11];
        nameStrings[0] = BamMediatorConstants.SERVICE_NAME;
        nameStrings[1] = BamMediatorConstants.OPERATION_NAME;
        nameStrings[2] = BamMediatorConstants.MSG_ID;
        nameStrings[3] = BamMediatorConstants.REQUEST_RECEIVED_TIME;
        nameStrings[4] = BamMediatorConstants.HTTP_METHOD;
        nameStrings[5] = BamMediatorConstants.CHARACTER_SET_ENCODING;
        nameStrings[6] = BamMediatorConstants.REMOTE_ADDRESS;
        nameStrings[7] = BamMediatorConstants.TRANSPORT_IN_URL;
        nameStrings[8] = BamMediatorConstants.MESSAGE_TYPE;
        nameStrings[9] = BamMediatorConstants.REMOTE_HOST;
        nameStrings[10] = BamMediatorConstants.SERVICE_PREFIX;

        String outputString = "          {'name':'" + BamMediatorConstants.MSG_DIRECTION + "','type':'STRING'}";

        for (String nameString : nameStrings) {
            outputString = outputString + "," + this.getStreamDefinitionEntryString(nameString, BamMediatorConstants.STRING);
        }

        return outputString;
    }

    private String getStreamDefinitionEntryString(String name, String type){
        return  "        {'name':'" + name + "','type':'" + type +"'}";
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

    public void setKsLocation(String ksLocation) {
        this.ksLocation = ksLocation;
    }

    public void setKsPassword(String ksPassword) {
        this.ksPassword = ksPassword;
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
