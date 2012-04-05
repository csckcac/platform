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
import org.apache.ode.bpel.o.OProcess;
import org.apache.ode.bpel.runtime.extension.AbstractSyncExtensionOperation;
import org.apache.ode.bpel.runtime.extension.ExtensionContext;
import org.apache.ode.store.DeploymentUnitDir;
import org.apache.ode.utils.DOMUtils;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.*;
import org.wso2.carbon.bam.agent.core.Agent;
import org.wso2.carbon.bam.agent.publish.EventReceiver;
import org.wso2.carbon.bam.service.Event;
import org.wso2.carbon.bpel.bam.publisher.internal.BamPublisherServiceComponent;


import javax.xml.namespace.QName;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.*;

public class BamPublisherExtensionOperation extends AbstractSyncExtensionOperation {
    private static final Log log = LogFactory.getLog(BamPublisherExtensionOperation.class);

    @Override
    protected void runSync(ExtensionContext extensionContext, Element element)
            throws FaultException {

        Integer tenantId = getTenantId(extensionContext);
        EventReceiver eventReceiver = getBamEventReceiver(tenantId);
        Agent agent = getBamAgent(tenantId);
        if (null == eventReceiver) {
            log.error("BAM Publisher Ext - EventReceiver not found for tenant id, " +
                      "please check registry configuration");
            return;
        }

        NodeList list = element.getElementsByTagNameNS(BamPublisherConstants.BAM_PUBLISHER_NS,
                                                       BamPublisherConstants.BAM_KEY);
        if (list.getLength() < 1) {
            log.info("key elements not found under bam:publish element. skipping bam publishing");
            return;
        }

        int length = list.getLength();
        Map<String, ByteBuffer> eventMap = new HashMap<String, ByteBuffer>();
        for (int i = 0; i < length; i++) {
            createEventDataMap(extensionContext, eventMap, list.item(i));
        }

        Event publishEvent = new Event();
        publishEvent.setEvent(eventMap);
        publishEvent.setMeta(createMetaDataMap(extensionContext));
        publishEvent.setCorrelation(new HashMap<String, ByteBuffer>());
        List<Event> events = new ArrayList<Event>();
        events.add(publishEvent);
        agent.publish(events, eventReceiver);
    }

    private Integer getTenantId(ExtensionContext context) {
        DeploymentUnitDir du = new DeploymentUnitDir(new File(context.getDUDir()));
        QName processIdQname = new QName(context.getProcessModel().getQName().getNamespaceURI(),
                                         context.getProcessModel().getQName().getLocalPart() + "-"
                                         + du.getStaticVersion());
        return  BamPublisherServiceComponent.getBPELServer().
                getMultiTenantProcessStore().getTenantId(processIdQname);

    }

    private EventReceiver getBamEventReceiver(Integer tenantId) {
        EventReceiver eventReceiver = TenantBamAgentHolder.getInstance().getEventReceiver(tenantId);
        return eventReceiver;
    }

    private Agent getBamAgent(Integer tenantId) {
        Agent agent = TenantBamAgentHolder.getInstance().getAgent(tenantId);
        return agent;
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

    /**
     * Following is the xml structure that comes into this node for processing
     * <bam:key name="key1">
     * <bam:from part="part1" variable="variable">
     * </bam:from>
     * </bam:key>
     *
     * @param node
     * @return EventMap
     */
    private void createEventDataMap(ExtensionContext extensionContext,
                                    Map<String, ByteBuffer> eventMap, Node node)
            throws FaultException {

        if (node.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        NamedNodeMap attributeMap = node.getAttributes();
        Node keyNode = attributeMap.getNamedItem(BamPublisherConstants.NAME_ATTR);
        String keyValue = keyNode.getTextContent();
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
            Node variableNode = extensionContext.readVariable(variableName);
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
                eventMap.put(BamPublisherConstants.PROCESS_VARIABLE_VALUE,
                             ByteBuffer.wrap(xmlStr.toString().getBytes()));
                eventMap.put(BamPublisherConstants.PROCESS_VARIABLE_NAME,
                             ByteBuffer.wrap(keyValue.getBytes()));
            }
        }
        return;
    }

}
