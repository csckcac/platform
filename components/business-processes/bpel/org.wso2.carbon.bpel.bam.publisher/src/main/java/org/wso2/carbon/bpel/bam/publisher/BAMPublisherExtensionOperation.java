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
import org.apache.ode.bpel.runtime.extension.AbstractSyncExtensionOperation;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.apache.ode.store.DeploymentUnitDir;
import org.apache.ode.utils.DOMUtils;
import org.w3c.dom.Element;
import org.wso2.carbon.bpel.bam.publisher.internal.BAMPublisherServiceComponent;
import org.wso2.carbon.bpel.core.ode.integration.config.bam.BAMKey;
import org.wso2.carbon.bpel.core.ode.integration.config.bam.BAMServerProfile;
import org.wso2.carbon.bpel.core.ode.integration.config.bam.BAMStreamConfiguration;
import org.wso2.carbon.bpel.core.ode.integration.store.TenantProcessStore;
import org.wso2.carbon.databridge.agent.thrift.Agent;
import org.wso2.carbon.databridge.agent.thrift.DataPublisher;
import org.wso2.carbon.databridge.agent.thrift.conf.AgentConfiguration;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.exception.*;

import javax.xml.namespace.QName;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;

public class BAMPublisherExtensionOperation extends AbstractSyncExtensionOperation {
    private static final Log log = LogFactory.getLog(BAMPublisherExtensionOperation.class);

    @Override
    protected void runSync(ExtensionContext extensionContext, Element element)
            throws FaultException {
        String bamServerProfileName = element.getAttribute("bamServerProfile");
        String streamName = element.getAttribute(BAMPublisherConstants.STREAM_NAME_ATTR);
        String streamVersion = element.getAttribute(BAMPublisherConstants.STREAM_VERSION);
        String streamId;
        Integer tenantId = getTenantId(extensionContext);
        Agent agent = createAgent(tenantId, bamServerProfileName);
        DataPublisher dataPublisher = createDataPublisher(tenantId, bamServerProfileName, agent);
        BAMStreamConfiguration stream = getEventStream(tenantId, bamServerProfileName, streamName);
        streamId = defineEventStream(dataPublisher, stream);
        if (null == dataPublisher) {
            log.error("BAM Publisher Ext Data Publisher not found for tenant id, " +
                    "please check registry configuration");
            return;
        }

        Event event = createEvent(streamId, stream, extensionContext);
        try {
            dataPublisher.publish(event);
        } catch (AgentException e) {
            String errMsg = "Problem with Agent while publishing.";
            handleException(errMsg);
        }
    }

    private Integer getTenantId(ExtensionContext context) {
        DeploymentUnitDir du = new DeploymentUnitDir(new File(context.getDUDir()));
        QName processIdQname = new QName(context.getProcessModel().getQName().getNamespaceURI(),
                context.getProcessModel().getQName().getLocalPart() + "-"
                        + du.getStaticVersion());
        return BAMPublisherServiceComponent.getBPELServer().
                getMultiTenantProcessStore().getTenantId(processIdQname);
    }

//    private DataPublisher getBamDataPublisher(Integer tenantId) {
//        DataPublisher dataPublisher = TenantBamAgentHolder.getInstance().getDataPublisher(tenantId);
//        return dataPublisher;
//    }
//
//    private Agent getBamAgent(Integer tenantId) {
//        Agent agent = TenantBamAgentHolder.getInstance().getAgent(tenantId);
//        return agent;
//    }
//
//    /**
//     * @param context       ExtensionContext
//     * @param streamName    Name of the event stream definition
//     * @param streamVersion Version of the event stream definition
//     * @return event stream definition upon successful return of an string and null otherwise
//     */
//
//    private String getEventStream(ExtensionContext context, String streamName, String streamVersion) {
//        DeploymentUnitDir du = new DeploymentUnitDir(new File(context.getDUDir()));
//        List<TDeployment.Process> processList = du.getDeploymentDescriptor().getDeploy().getProcessList();
//
//        for (TDeployment.Process process : processList) {
//            if (process.getName().equals(context.getProcessModel().getName())) {
//                List<TBamStreamDefinitions.Stream> streamList = process.getBamStreamDefinitions().getStreamList();
//                for (TBamStreamDefinitions.Stream stream : streamList) {
//                    String eventStreamName = stream.getStreamName();
//                    String eventStreamVersion = stream.getStreamVersion();
//                    if (eventStreamName.equals(streamName) && eventStreamVersion.equals(streamVersion)) {
//                        return stream.xmlText();
//                    }
//                }
//            }
//        }
//        return null;
//    }
//
//
//    private Event createEvent(String eventStreamDefinition, String streamId, ExtensionContext context,
//                              NodeList list)
//            throws MalformedStreamDefinitionException, FaultException, AgentException {
//        EventStreamDefinition streamDefinition = EventConverter.convertFromJson(eventStreamDefinition);
//        List<Attribute> payloadData = streamDefinition.getPayloadData();
//        List<Attribute> correlationData = streamDefinition.getCorrelationData();
//        List<Attribute> metaData = streamDefinition.getMetaData();
//
//        HashMap<String, String> payloadDataMap = new HashMap<String, String>();
//        HashMap<String, String> metaDataMap = new HashMap<String, String>();
//        HashMap<String, String> correlationDataMap = new HashMap<String, String>();
//
//        int length = list.getLength();
//        if (length < 1) {
//            log.error("Key elements are not found under the bam:publish element");
//            return;
//        }
//
//        for (int i = 0; i < length; i++) {
//            Node node = list.item(i);
//            if (node.getNodeType() != Node.ELEMENT_NODE) {
//                log.info("Invalid xml node found for key");
//                continue;
//            }
//
//            NamedNodeMap attributeMap = node.getAttributes();
//            Node keyNode = attributeMap.getNamedItem(BAMPublisherConstants.NAME_ATTR);
//            String keyValue = keyNode.getTextContent();
//
//            Node typeNode = attributeMap.getNamedItem(BAMPublisherConstants.TYPE_ATTR);
//            String typeValue = typeNode.getTextContent();
//
//            Element fromElement = DOMUtils.findChildByName((Element) node,
//                    new QName(BAMPublisherConstants.BAM_PUBLISHER_NS,
//                            BAMPublisherConstants.BAM_FROM));
//            if (null == fromElement) {
//                log.error("From Element not found with the key element");
//                return;
//            }
//
//            String variableName = DOMUtils.getAttribute(fromElement, BAMPublisherConstants.VARIABLE_ATTR);
//            String partName = DOMUtils.getAttribute(fromElement, BAMPublisherConstants.PART_ATTR);
//
//            if (variableName != null) {
//                Node variableNode = context.readVariable(variableName);
//                if (variableNode != null) {
//                    String xmlStr = "";
//                    if (partName != null && variableNode.getNodeType() == Node.ELEMENT_NODE) {
//                        Element partNode = DOMUtils.findChildByName((Element) variableNode, new QName(partName));
//                        if (partNode != null) {
//                            Element firstChildElement = DOMUtils.getFirstChildElement(partNode);
//                            xmlStr = DOMUtils.domToString(firstChildElement);
//                        }
//                    } else {
//                        xmlStr = DOMUtils.domToString(variableNode);
//                    }
//
//                    if (typeValue.equals(BAMPublisherConstants.EVENT_VALUE_TYPE_PAYLOAD)) {
//                        payloadDataMap.put(keyValue, xmlStr);
//                    } else if (typeValue.equals(BAMPublisherConstants.EVENT_VALUE_TYPE_CORRELATION)) {
//                        correlationDataMap.put(keyValue, xmlStr);
//                    } else if (typeValue.equals(BAMPublisherConstants.EVENT_VALUE_TYPE_META)) {
//                        metaDataMap.put(keyValue, xmlStr);
//                    }
//                }
//            }
//        }
//        Object[] payloadDataArray = null;
//
//        int size = payloadDataMap.size();
//        if (size > 0) {
//            payloadDataArray = new Object[size];
//            for (int i = 0; i < size; i++) {
//                Attribute attribute = payloadData.get(i);
//                convertAndSetValue(payloadDataArray, i, attribute.getType(),
//                        payloadDataMap.get(attribute.getName()));
//            }
//        }
//
//        Object[] metaDataArray = null;
//        if (size > 0) {
//            metaDataArray = new Object[size];
//            for (int i = 0; i < size; i++) {
//                Attribute attribute = metaData.get(i);
//                convertAndSetValue(metaDataArray, i, attribute.getType(),
//                        metaDataMap.get(attribute.getName()));
//            }
//        }
//
//        Object[] correlationDataArray = null;
//        size = correlationDataMap.size();
//        if (size > 0) {
//            correlationDataArray = new Object[size];
//            for (int i = 0; i < size; i++) {
//                Attribute attribute = correlationData.get(i);
//                convertAndSetValue(correlationDataArray, i, attribute.getType(),
//                        correlationDataMap.get(attribute.getName()));
//            }
//        }
//
//        return new Event(streamId, System.currentTimeMillis(), metaDataArray,
//                correlationDataArray, payloadDataArray);
//    }
//
//    private void convertAndSetValue(Object[] dataArray, int index, AttributeType type, String str) {
//        switch (type) {
//            case STRING:
//                // nothing to do
//                dataArray[index] = str;
//            case INT:
//                // convert to int
//                dataArray[index] = Integer.parseInt(str);
//
//            case DOUBLE:
//                // convert to double
//                dataArray[index] = Double.parseDouble(str);
//
//            case LONG:
//                dataArray[index] = Long.parseLong(str);
//
//            case FLOAT:
//                dataArray[index] = Float.parseFloat(str);
//
//            case BOOL:
//                dataArray[index] = Boolean.parseBoolean(str);
//        }
//    }
//
//    private Map<String, ByteBuffer> createMetaDataMap(ExtensionContext ctx) {
//        OProcess processModel = ctx.getProcessModel();
//        DeploymentUnitDir du = new DeploymentUnitDir(new File(ctx.getDUDir()));
//        QName processIdQname = new QName(ctx.getProcessModel().getQName().getNamespaceURI(),
//                ctx.getProcessModel().getQName().getLocalPart() + "-"
//                        + du.getStaticVersion());
//        Integer tenantId = BAMPublisherServiceComponent.getBPELServer().
//                getMultiTenantProcessStore().getTenantId(processIdQname);
//        Map<String, ByteBuffer> metaDataMap = new HashMap<String, ByteBuffer>();
//        metaDataMap.put(BAMPublisherConstants.SERVER, ByteBuffer.wrap(
//                BAMPublisherConstants.DEFAULT_SERVER_NAME.getBytes()));
//        metaDataMap.put(BAMPublisherConstants.PROCESS_ID, ByteBuffer.wrap(
//                processModel.getName().getBytes()));
//        metaDataMap.put(BAMPublisherConstants.TENANT_ID, ByteBuffer.wrap(
//                Integer.toString(tenantId).getBytes()));
//        return metaDataMap;
//    }
//

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private Agent createAgent(int tenantId, String bamServerProfileName) {
        BAMServerProfile bamServerProfile = getBAMServerProfile(tenantId, bamServerProfileName);
        AgentConfiguration agentConfiguration = new AgentConfiguration();

        //TODO do we need to set this if secure attribute is false
        agentConfiguration.setTrustStore(bamServerProfile.getKeyStoreLocation());
        agentConfiguration.setTrustStorePassword(bamServerProfile.getKeyStorePassword());
        System.setProperty("javax.net.ssl.trustStore", bamServerProfile.getKeyStoreLocation());
        System.setProperty("javax.net.ssl.trustStorePassword", bamServerProfile.getKeyStorePassword());

        return new Agent(agentConfiguration);
    }

    private DataPublisher createDataPublisher(int tenantId, String bamServerProfileName,
                                              Agent agent) throws FaultException {
        BAMServerProfile bamServerProfile = getBAMServerProfile(tenantId, bamServerProfileName);
        DataPublisher dataPublisher = null;
        try {
            if (bamServerProfile.isSecurityEnabled()) {
                dataPublisher = new DataPublisher("ssl://" + bamServerProfile.getIp() + ":" +
                        bamServerProfile.getAuthenticationPort(),
                        "ssl://" + bamServerProfile.getIp() + ":" +
                                bamServerProfile.getAuthenticationPort(),
                        bamServerProfile.getUserName(), bamServerProfile.getPassword(), agent);
            } else {
                dataPublisher = new DataPublisher("ssl://" + bamServerProfile.getIp() + ":" +
                        bamServerProfile.getAuthenticationPort(),
                        "tcp://" + bamServerProfile.getIp() + ":" +
                                bamServerProfile.getReceiverPort(),
                        bamServerProfile.getUserName(), bamServerProfile.getPassword(), agent);
            }
        } catch (MalformedURLException e) {
            String errorMsg = "Given URLs are incorrect.";
            handleException(errorMsg, e);
        } catch (AgentException e) {
            String errorMsg = "Problem while using the Agent.";
            handleException(errorMsg, e);
        } catch (AuthenticationException e) {
            String errorMsg = "Authentication failed.";
            handleException(errorMsg, e);
        } catch (TransportException e) {
            String errorMsg = "Transport layer problem.";
            handleException(errorMsg, e);
        }

        log.info("Data Publisher Created.");
        return dataPublisher;
    }

    private BAMServerProfile getBAMServerProfile(int tenantId, String bamServerProfileName) {
        TenantProcessStore tenantsProcessStore = BAMPublisherServiceComponent.getBPELServer().
                getMultiTenantProcessStore().getTenantsProcessStore(tenantId);
        return tenantsProcessStore.getBAMServerProfile(bamServerProfileName);
    }

    private String defineEventStream(DataPublisher dataPublisher, BAMStreamConfiguration stream)
            throws FaultException {
        String streamDefinition = "{" +
                "  'name':'" + stream.getName() + "'," +
                "  '" + BAMPublisherConstants.STREAM_VERSION + "':'" + stream.getVersion() + "'," +
                "  '" + BAMPublisherConstants.STREAM_NICK_NAME + "': '" + stream.getNickName() + "'," +
                "  '" + BAMPublisherConstants.STREAM_DESCRIPTION + "': '" + stream.getDescription() + "'," +
                "  'correlationData':[" +
                "          {'name':'" + BAMPublisherConstants.INSTANCE_ID + "','type':'STRING'}" +
                getStreamDefinitionString(BAMKey.BAMKeyType.CORRELATION, stream) +
                "  ]," +
                "  'metaData':[" +
                "          {'name':'" + BAMPublisherConstants.TENANT_ID + "','type':'INT'}" +
                "          {'name':'" + BAMPublisherConstants.PROCESS_ID + "','type':'STRING'}" +
                getStreamDefinitionString(BAMKey.BAMKeyType.META, stream) +
                "  ]," +
                "  'payloadData':[" +
                getStreamDefinitionString(BAMKey.BAMKeyType.PAYLOAD, stream) +
                "  ]" +
                "}";
        try {
            return dataPublisher.defineStream(streamDefinition);
        } catch (AgentException e) {
            String errorMsg = "Problem using creating the Agent.";
            handleException(errorMsg, e);
        } catch (MalformedStreamDefinitionException e) {
            String errorMsg = "Invalid Stream definition: " + streamDefinition;
            handleException(errorMsg, e);
        } catch (StreamDefinitionException e) {
            String errorMsg = "Problem with Stream Definition: " + streamDefinition;
            handleException(errorMsg, e);
        } catch (DifferentStreamDefinitionAlreadyDefinedException ignore) {
            //TODO If the stream is already defined, just ignore and continue.
            // Need to improve the logic by using the dataPublisher.find method before using
            // dataPublisher.publish.
            // Also check whether streams are defined while deploying the process and keep the status,
            // so that we can check it before call define stream method.
//            String errorMsg = "Already there is a different Stream Definition exists for the Name and Version. " + e.getMessage();
        }
        handleException("Error occurred while defining the stream: " + stream.getName());
        return null;
    }

    private String getStreamDefinitionString(BAMKey.BAMKeyType type, BAMStreamConfiguration stream) throws FaultException {
        String keyString = "";
        List<BAMKey> keys = null;
        switch (type) {
            case PAYLOAD:
                keys = stream.getPayloadBAMKeyList();
                break;
            case META:
                keys = stream.getMetaBAMKeyList();
                break;
            case CORRELATION:
                keys = stream.getCorrelationBAMKeyList();
                break;
            default:
                String errMsg = "Unknown BAM key type: " + type;
                handleException(errMsg);
        }
        for (BAMKey key : keys) {
            keyString = keyString + ",        {'name':'" + key.getName() + "','type':'STRING'}";
        }

        return keyString;
    }

    private BAMStreamConfiguration getEventStream(int tenantId, String bamServerProfileName,
                                                  String streamName) {
        BAMServerProfile bamServerProfile = getBAMServerProfile(tenantId, bamServerProfileName);
        return bamServerProfile.getBAMStreamConfiguration(streamName);
    }

    private void handleException(String errMsg, Throwable t) throws FaultException {
        log.error(errMsg, t);
        throw new FaultException(BAMPublisherConstants.BAM_FAULT, errMsg, t);
    }

    private void handleException(String errMsg) throws FaultException {
        log.error(errMsg);
        throw new FaultException(BAMPublisherConstants.BAM_FAULT, errMsg);
    }

    private Event createEvent(String streamId, BAMStreamConfiguration stream,
                              ExtensionContext context)
            throws FaultException {
        return new Event(streamId, System.currentTimeMillis(),
                createMetadata(stream, context),
                createCorrelationData(stream, context),
                createPayloadData(stream, context));
    }

    private Object[] createCorrelationData(BAMStreamConfiguration stream, ExtensionContext context)
            throws FaultException {
        List<BAMKey> payloadBAMKeyList = stream.getPayloadBAMKeyList();
        int objectListSize = payloadBAMKeyList.size() + 1;
        Object[] dataArray = new Object[objectListSize];
        dataArray[0] = context.getInternalInstance().getPid().toString();
        int startIndex = 1;
        fillDataArray(dataArray, payloadBAMKeyList, startIndex, context);
        return dataArray;
    }

    private Object[] createMetadata(BAMStreamConfiguration stream, ExtensionContext context)
            throws FaultException {
        List<BAMKey> payloadBAMKeyList = stream.getPayloadBAMKeyList();
        int objectListSize = payloadBAMKeyList.size() + 2;
        Object[] dataArray = new Object[objectListSize];
        dataArray[0] = getTenantId(context);
        dataArray[1] = context.getProcessModel().getQName().toString();
        int startIndex = 2;
        fillDataArray(dataArray, payloadBAMKeyList, startIndex, context);
        return dataArray;
    }

    private Object[] createPayloadData(BAMStreamConfiguration stream, ExtensionContext context)
            throws FaultException {
        List<BAMKey> payloadBAMKeyList = stream.getPayloadBAMKeyList();
        int objectListSize = payloadBAMKeyList.size();
        Object[] dataArray = new Object[objectListSize];
        int startIndex = 0;
        fillDataArray(dataArray, payloadBAMKeyList, startIndex, context);
        return dataArray;
    }

    private void fillDataArray(Object[] dataArray, List<BAMKey> payloadBAMKeyList, int startIndex,
                               ExtensionContext context) throws FaultException {
        for (int i = 0; i < payloadBAMKeyList.size(); i++) {
            BAMKey bamKey = payloadBAMKeyList.get(i);
            if (bamKey.getExpression() != null) {
                //TODO
            } else if (bamKey.getVariable() != null && bamKey.getPart() == null) {
                if (bamKey.getQuery() == null) {
                    dataArray[i + startIndex] =
                            DOMUtils.domToString(context.readVariable(bamKey.getVariable()));
                } else {
                    //TODO
                }
            } else if (bamKey.getVariable() != null && bamKey.getPart() != null) {
                //TODO
            }
        }
    }
}
