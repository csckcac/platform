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
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.mediators.AbstractMediator;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.eventbridge.agent.thrift.Agent;
import org.wso2.carbon.eventbridge.agent.thrift.DataPublisher;
import org.wso2.carbon.eventbridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.eventbridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.eventbridge.commons.Event;
import org.wso2.carbon.eventbridge.commons.exception.*;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.UUID;

import org.wso2.carbon.eventbridge.commons.thrift.exception.ThriftAuthenticationException;
import org.wso2.carbon.mediator.bam.config.stream.Property;
import org.wso2.carbon.mediator.bam.util.BamMediatorConstants;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;

/**
 * Transforms the current message payload using the given BAM configuration.
 * The current message context is replaced with the result as XML.
 */
public class BamMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(BamMediator.class);
    private static final String ADMIN_SERVICE_PARAMETER = "adminService";
    private static final String HIDDEN_SERVICE_PARAMETER = "hiddenService";

    private String serverProfile = "";
    private String streamName = "";
    private String streamVersion = "";
    private String streamNickName = "";
    private String streamDescription = "";
    private String serverIp = "";
    private String serverPort = "";
    private String userName = "";
    private String password = "";
    private List<Property> properties = new ArrayList<Property>();
    private String streamId = null;
    private DataPublisher dataPublisher = null;

    public BamMediator() {

    }

    public boolean mediate(MessageContext messageContext) {

        SynapseLog synLog = getLog(messageContext);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : BAM mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

        AxisService service = msgCtx.getAxisService();
        if (service == null ||
            service.getParameter(ADMIN_SERVICE_PARAMETER) != null ||
            service.getParameter(HIDDEN_SERVICE_PARAMETER) != null) {
            return true;
        }

        this.setActivityIdInSOAPHeader(messageContext);

        try {
            logMessage(messageContext);
        } catch (AgentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (StreamDefinitionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MalformedURLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (TransportException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ThriftAuthenticationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : BAM mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        return true;
    }

    private void logMessage(MessageContext messageContext)
            throws AgentException, MalformedStreamDefinitionException, StreamDefinitionException,
                   DifferentStreamDefinitionAlreadyDefinedException,
                   MalformedURLException, AuthenticationException, TransportException, ThriftAuthenticationException {

        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        AxisConfiguration axisConfiguration = msgCtx.getConfigurationContext().getAxisConfiguration();
        int tenantId = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        boolean direction = (!messageContext.isResponse() && !messageContext.isFaultResponse());
        String service = msgCtx.getAxisService().getName();
        String operation = msgCtx.getAxisOperation().getName().getLocalPart();

        if (streamId == null) {
            Agent agent = this.createAgent();
            this.createDataPublisher(agent);
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
            dataPublisher.publish(event);
        } else {
            log.info("streamId is empty.");
        }
    }

    private void setActivityIdInSOAPHeader(MessageContext synapseContext) {

        try {
            // Property name would be "Parent_uuid"
            // Property Value would be "Parent_uuid_messageid"
            UUID uuid = UUID.randomUUID();
            String uuid_string = uuid.toString();

            Axis2MessageContext axis2smc = (Axis2MessageContext) synapseContext;
            org.apache.axis2.context.MessageContext axis2MessageContext = axis2smc.getAxis2MessageContext();

            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace(BamMediatorConstants.BAM_HEADER_NAMESPACE_URI, "ns");
            SOAPEnvelope soapEnvelope = axis2MessageContext.getEnvelope();
            String soapNamespaceURI = soapEnvelope.getNamespace().getNamespaceURI();
            SOAPFactory soapFactory = null;
            SOAPHeaderBlock soapHeaderBlock = null;

            if (soapNamespaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                soapFactory = OMAbstractFactory.getSOAP11Factory();
            } else if (soapNamespaceURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                soapFactory = OMAbstractFactory.getSOAP12Factory();
            } else {
                log.error("Not a standard soap message");
            }

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
                        soapHeaderBlock.addAttribute(BamMediatorConstants.ACTIVITY_ID, uuid_string, null);
                        synapseContext.setProperty(BamMediatorConstants.MSG_ACTIVITY_ID, uuid_string);
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
                        bamHeader.addAttribute(BamMediatorConstants.ACTIVITY_ID, uuid_string, null);
                        synapseContext.setProperty(BamMediatorConstants.MSG_ACTIVITY_ID, uuid_string);
                    }
                }
            } else {
                if (soapFactory != null) {
                    (soapFactory).createSOAPHeader(soapEnvelope); // TODO
                }
                if (soapEnvelope.getHeader() != null) {
                    soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock(BamMediatorConstants.BAM_EVENT, omNs);
                    if (synapseContext.getProperty(BamMediatorConstants.MSG_ACTIVITY_ID) == null) { // this if
                        // condition we add
                        // to track failure messages coming from
                        // DS.That is a new message. So, doesn't have activityID.Getting activityID
                        // from the synapseContext.property
                        soapHeaderBlock.addAttribute(BamMediatorConstants.ACTIVITY_ID, uuid_string, null);
                        synapseContext.setProperty(BamMediatorConstants.MSG_ACTIVITY_ID, uuid_string);
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

    private Agent createAgent(){
        AgentConfiguration agentConfiguration = new AgentConfiguration();
        String keyStorePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                              File.separator + "resources" + File.separator + "security" +
                              File.separator + "client-truststore.jks";
        String keyStorePassword = "wso2carbon";
        agentConfiguration.setTrustStore(keyStorePath);
        agentConfiguration.setTrustStorePassword(keyStorePassword);
        System.setProperty("javax.net.ssl.trustStore", keyStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);
        return new Agent(agentConfiguration);
    }

    private void createDataPublisher(Agent agent) throws AgentException, MalformedStreamDefinitionException, StreamDefinitionException,
                                                         DifferentStreamDefinitionAlreadyDefinedException,
                                                         MalformedURLException, AuthenticationException, TransportException {
        //create data publisher
        dataPublisher = new DataPublisher("tcp://" + this.serverIp + ":" + this.serverPort,
                                          this.userName, this.password, agent);

        //Define event stream
        streamId = dataPublisher.defineEventStream("{" +
                                                   "  'name':'" + this.streamName + "'," +
                                                   "  'version':'"+ this.streamVersion + "'," +
                                                   "  'nickName': '" + this.streamNickName + "'," +
                                                   "  'description': '" + this.streamDescription + "'," +
                                                   "  'correlationData':[" +
                                                   "          {'name':'ActivityId','type':'STRING'}" +
                                                   "  ]," +
                                                   "  'metaData':[" +
                                                   "          {'name':'TenantId','type':'INT'}" +
                                                   "  ]," +
                                                   "  'payloadData':[" +
                                                   "          {'name':'Direction','type':'STRING'}," +
                                                   "          {'name':'Service','type':'STRING'}," +
                                                   "          {'name':'Operation','type':'STRING'}," +
                                                   "          {'name':'MessageId','type':'STRING'}," +
                                                   "          {'name':'SOAPHeader','type':'STRING'}," +
                                                   "          {'name':'SOAPBody','type':'STRING'}" +
                                                   this.getPropertyString() +
                                                   "  ]" +
                                                   "}");
        log.info("Event Stream Created.");
    }

    private Object[] createPayloadData(MessageContext messageContext,
                                       boolean direction, String service, String operation){
        int numOfProperties = properties.size();

        Object[] payloadData = new Object[numOfProperties + 6];
        payloadData[0] = direction ?
                         BamMediatorConstants.DIRECTION_IN : BamMediatorConstants.DIRECTION_OUT;
        payloadData[1] = service;
        payloadData[2] = operation;
        payloadData[3] = messageContext.getMessageID();
        payloadData[4] = messageContext.getEnvelope().getHeader().toString();
        payloadData[5] = messageContext.getEnvelope().getBody().toString();

        for (int i=0; i<numOfProperties; i++) {
            payloadData[6 + i] = properties.get(i).getValue();
        }

        return payloadData;
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
    
    private String getPropertyString(){
        String propertyString = "";
        for (Property property : properties) {
            propertyString = propertyString + ",        {'name':'" + property.getKey() + "','type':'STRING'}";
        }
        return propertyString;
    }

    public void setServerIP(String newValue) {
        serverIp = newValue;
    }

    public String getServerIP() {
        return serverIp;
    }

    public void setUserName(String newValue){
        userName = newValue;
    }

    public String getUserName(){
        return password;
    }

    public void setPassword(String newValue){
        password = newValue;
    }

    public String getPassword(){
        return password;
    }

    public void setServerProfile(String newValue){
        serverProfile = newValue;
    }

    public String getServerProfile(){
        return serverProfile;
    }

    public void setStreamName(String newValue){
        this.streamName = newValue;
    }

    public String getStreamName(){
        return this.streamName;
    }

    public void setStreamVersion(String newValue){
        this.streamVersion = newValue;
    }

    public String getStreamVersion(){
        return this.streamVersion;
    }

    public String getStreamNickName() {
        return streamNickName;
    }

    public void setStreamNickName(String streamNickName) {
        this.streamNickName = streamNickName;
    }

    public String getStreamDescription() {
        return streamDescription;
    }

    public void setStreamDescription(String streamDescription) {
        this.streamDescription = streamDescription;
    }

    public void setServerPort(String newValue) {
        serverPort = newValue;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setProperties(List<Property> newValue){
        properties = newValue;
    }

    public List<Property> getProperties(){
        return properties;
    }

}