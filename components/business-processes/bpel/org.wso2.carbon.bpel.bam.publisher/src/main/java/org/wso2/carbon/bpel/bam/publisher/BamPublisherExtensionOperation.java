/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bpel.bam.publisher;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.dd.TBamStreamDefinitions;
import org.apache.ode.bpel.dd.TDeployment;
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.runtime.extension.AbstractSyncExtensionOperation;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.apache.ode.store.DeploymentUnitDir;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.agent.Agent;
import org.wso2.carbon.agent.DataPublisher;
import org.wso2.carbon.agent.commons.Attribute;
import org.wso2.carbon.agent.commons.AttributeType;
import org.wso2.carbon.agent.commons.Event;
import org.wso2.carbon.agent.commons.EventStreamDefinition;
import org.wso2.carbon.agent.commons.exception.DifferentStreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.agent.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.agent.commons.exception.NoStreamDefinitionExistException;
import org.wso2.carbon.agent.commons.exception.StreamDefinitionException;
import org.wso2.carbon.agent.exception.AgentException;
import org.wso2.carbon.agent.commons.utils.EventConverter;
import org.wso2.carbon.bpel.bam.publisher.internal.BamPublisherServiceComponent;

import javax.xml.namespace.QName;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BamPublisherExtensionOperation extends AbstractSyncExtensionOperation {
    private static final Log log = LogFactory.getLog(BamPublisherExtensionOperation.class);

    @Override
    protected void runSync(ExtensionContext extensionContext, Element element)
            throws FaultException {

        Integer tenantId = getTenantId(extensionContext);
        DataPublisher dataPublisher = getBamDataPublisher(tenantId);
        Agent agent = getBamAgent(tenantId);

        if (null == dataPublisher) {
            log.error("BAM Publisher Ext Data Publisher not found for tenant id, " +
                      "please check registry configuration");
            return;
        }

        String streamName = element.getAttribute(BamPublisherConstants.STREAM_NAME_ATTR);
        String streamVersion = element.getAttribute(BamPublisherConstants.STREAM_VERSION_ATTR);

        NodeList list = element.getElementsByTagNameNS(BamPublisherConstants.BAM_PUBLISHER_NS,
                                                       BamPublisherConstants.BAM_KEY);
        if (list.getLength() < 1) {
            log.info("key elements not found under bam:publish element. skipping bam publishing");
            return;
        }


        String eventStreamId = null;
        try {
            eventStreamId = dataPublisher.findEventStream(streamName, streamVersion);
        } catch (AgentException e) {
            log.error("Agent exception when finding event stream definition " + e.getMessage());
            return;
        } catch (StreamDefinitionException e) {
            log.error("Stream definition exception when finding event stream definition " + e.getMessage());
            return;
        } catch (NoStreamDefinitionExistException e) {
            log.debug("Stream is not defined");
        }

        String streamDef = getEventStream(extensionContext, streamName, streamVersion);

        if(eventStreamId == null) {
            if(streamDef != null)
                try {
                    eventStreamId = dataPublisher.defineEventStream(streamDef);
                } catch (AgentException e) {
                    log.error("Agent exception " + e);
                    return;
                } catch (MalformedStreamDefinitionException e) {
                    log.error("Malformed Stream Definition exception " + e);
                    return;
                } catch (StreamDefinitionException e) {
                    log.error("Stream definition exception " + e);
                    return;
                } catch (DifferentStreamDefinitionAlreadyDefinedException e) {
                    log.error("Different stream definition exists " + e);
                    return;
                }
        }

        try {
            createEvent(streamDef, eventStreamId, extensionContext, dataPublisher, list);
        } catch (MalformedStreamDefinitionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (AgentException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private Integer getTenantId(ExtensionContext context) {
            DeploymentUnitDir du = new DeploymentUnitDir(new File(context.getDUDir()));
            QName processIdQname = new QName(context.getProcessModel().getQName().getNamespaceURI(),
                                             context.getProcessModel().getQName().getLocalPart() + "-"
                                             + du.getStaticVersion());
            return  BamPublisherServiceComponent.getBPELServer().
                    getMultiTenantProcessStore().getTenantId(processIdQname);
        }

    private DataPublisher getBamDataPublisher(Integer tenantId) {
        DataPublisher dataPublisher = TenantBamAgentHolder.getInstance().getDataPublisher(tenantId);
        return dataPublisher;
    }

    private Agent getBamAgent(Integer tenantId) {
        Agent agent = TenantBamAgentHolder.getInstance().getAgent(tenantId);
        return agent;
    }

        /**
         *
         * @param context    ExtensionContext
         * @param streamName   Name of the event stream definition
         * @param streamVersion   Version of the event stream definition
         * @return   event stream definition upon successful return of an string and null otherwise
         */

        private String getEventStream(ExtensionContext context, String streamName, String streamVersion) {
            DeploymentUnitDir du = new DeploymentUnitDir(new File(context.getDUDir()));
            List<TDeployment.Process> processList = du.getDeploymentDescriptor().getDeploy().getProcessList();

            for(TDeployment.Process process : processList) {
                if(process.getName().equals(context.getProcessModel().getName())) {
                    List<TBamStreamDefinitions.Stream> streamList = process.getBamStreamDefinitions().getStreamList();
                    for(TBamStreamDefinitions.Stream stream:streamList){
                        String eventStreamName  = stream.getStreamName();
                        String eventStreamVersion = stream.getStreamVersion();
                        if(eventStreamName.equals(streamName) && eventStreamVersion.equals(streamVersion)){
                            return stream.xmlText();
                        }
                    }
                }
            }
            return null;
        }


    private void createEvent(String eventStreamDefinition , String streamId ,ExtensionContext context,
                              DataPublisher publisher, NodeList list)
                        throws MalformedStreamDefinitionException, FaultException, AgentException {
        EventStreamDefinition streamDefinition = EventConverter.convertFromJson(eventStreamDefinition);
        List<Attribute> payloadData = streamDefinition.getPayloadData();
        List<Attribute> correlationData = streamDefinition.getCorrelationData();
        List<Attribute> metaData = streamDefinition.getMetaData();

        HashMap<String, String> payloadDataMap = new HashMap<String, String>();
        HashMap<String, String> metaDataMap = new HashMap<String, String>();
        HashMap<String, String> correlationDataMap = new HashMap<String, String>();

        int length = list.getLength();
        if(length < 1) {
            log.error("Key elements are not found under the bam:publish element");
            return;
        }

        for(int i = 0; i < length; i++ ) {
            Node node = list.item(i);
            if(node.getNodeType() != Node.ELEMENT_NODE) {
                log.info("Invalid xml node found for key");
                continue;
            }

            NamedNodeMap attributeMap = node.getAttributes();
            Node keyNode = attributeMap.getNamedItem(BamPublisherConstants.NAME_ATTR);
            String keyValue = keyNode.getTextContent();

            Node typeNode = attributeMap.getNamedItem(BamPublisherConstants.TYPE_ATTR);
            String typeValue = typeNode.getTextContent();

            Element fromElement = DOMUtils.findChildByName((Element) node,
                                                                new QName(BamPublisherConstants.BAM_PUBLISHER_NS,
                                                                    BamPublisherConstants.BAM_FROM));
            if (null == fromElement) {
                log.error("From Element not found with the key element");
                return;
            }

            String variableName = DOMUtils.getAttribute(fromElement, BamPublisherConstants.VARIABLE_ATTR);
            String partName = DOMUtils.getAttribute(fromElement, BamPublisherConstants.PART_ATTR);

            if (variableName != null) {
                Node variableNode = context.readVariable(variableName);
                if (variableNode != null) {
                    String xmlStr = "";
                    if (partName != null && variableNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element partNode = DOMUtils.findChildByName((Element) variableNode, new QName(partName));
                        if (partNode != null) {
                            Element firstChildElement = DOMUtils.getFirstChildElement(partNode);
                            xmlStr = DOMUtils.domToString(firstChildElement);
                        }
                    } else {
                        xmlStr = DOMUtils.domToString(variableNode);
                    }

                    if(typeValue.equals(BamPublisherConstants.EVENT_VALUE_TYPE_PAYLOAD)) {
                        payloadDataMap.put(keyValue, xmlStr);
                    } else if(typeValue.equals(BamPublisherConstants.EVENT_VALUE_TYPE_CORRELATION)) {
                        correlationDataMap.put(keyValue, xmlStr);
                    } else if(typeValue.equals(BamPublisherConstants.EVENT_VALUE_TYPE_META)) {
                        metaDataMap.put(keyValue, xmlStr);
                    }
                }
            }
        }
        Object[] payloadDataArray = null;

        int size = payloadDataMap.size();
        if(size > 0) {
            payloadDataArray = new Object[size];
            for(int i = 0; i < size; i++) {
                Attribute attribute = payloadData.get(i);
                convertAndSetValue(payloadDataArray, i, attribute.getType(),
                                   payloadDataMap.get(attribute.getName()));
            }
        }

        Object[] metaDataArray = null;
        if(size > 0 ) {
            metaDataArray = new Object[size];
            for(int i = 0; i < size; i++) {
                Attribute attribute = metaData.get(i);
                convertAndSetValue(metaDataArray, i, attribute.getType(),
                    metaDataMap.get(attribute.getName()));
            }
        }

        Object[] correlationDataArray = null;
        size = correlationDataMap.size();
        if(size > 0) {
           correlationDataArray = new Object[size];
            for(int i = 0; i < size; i++) {
                Attribute attribute = correlationData.get(i);
                convertAndSetValue(correlationDataArray, i, attribute.getType(),
                                   correlationDataMap.get(attribute.getName()));
            }
        }

        Event event = new Event(streamId, System.currentTimeMillis(), metaDataArray,
                                correlationDataArray, payloadDataArray);
        publisher.publish(event);
    }

    private void convertAndSetValue(Object[] dataArray, int index, AttributeType type, String str) {
        switch (type) {
            case STRING:
                // nothing to do
                dataArray[index] = str;
            case INT:
                // convert to int
                dataArray[index] = Integer.parseInt(str);

            case DOUBLE:
                // convert to double
                dataArray[index] = Double.parseDouble(str);

            case LONG:
                dataArray[index] = Long.parseLong(str);

            case FLOAT:
                dataArray[index] = Float.parseFloat(str);

            case BOOL:
                dataArray[index] = Boolean.parseBoolean(str);
        }
    }

    private Map<String, ByteBuffer> createMetaDataMap(ExtensionContext ctx) {
        OProcess processModel = ctx.getProcessModel();
        DeploymentUnitDir du = new DeploymentUnitDir(new File(ctx.getDUDir()));
        QName processIdQname = new QName(ctx.getProcessModel().getQName().getNamespaceURI(),
                                         ctx.getProcessModel().getQName().getLocalPart() + "-"
                                         + du.getStaticVersion());
        Integer tenantId = BamPublisherServiceComponent.getBPELServer().
                getMultiTenantProcessStore().getTenantId(processIdQname);
        Map<String, ByteBuffer> metaDataMap = new HashMap<String, ByteBuffer>();
        metaDataMap.put(BamPublisherConstants.SERVER, ByteBuffer.wrap(
                BamPublisherConstants.DEFAULT_SERVER_NAME.getBytes()));
        metaDataMap.put(BamPublisherConstants.PROCESS_NAME, ByteBuffer.wrap(
                processModel.getName().getBytes()));
        metaDataMap.put(BamPublisherConstants.TENANT_ID, ByteBuffer.wrap(
                Integer.toString(tenantId).getBytes()));
        return metaDataMap;
    }
}
