/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.bam.activity.mediation.data.publisher.mediator;


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
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.bam.activity.mediation.data.publisher.conf.ActivityConfigData;
import org.wso2.carbon.bam.activity.mediation.data.publisher.util.ActivityPublisherConstants;
import org.wso2.carbon.bam.activity.mediation.data.publisher.util.ActivityPublisherUtils;
import org.wso2.carbon.bam.activity.mediation.data.publisher.util.TenantActivityConfigData;
import org.wso2.carbon.bam.data.publisher.util.BAMDataPublisherConstants;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class MessageActivityMediator extends AbstractMediator {

    private boolean extractSoapBody = false;

    @Override
    public boolean mediate(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        AxisService service = msgCtx.getAxisService();
        if (service == null ||
            service.getParameter(ActivityPublisherConstants.ADMIN_SERVICE_PARAMETER) != null ||
            service.getParameter(ActivityPublisherConstants.HIDDEN_SERVICE_PARAMETER) != null) {
            return true;
        }

        AxisConfiguration axisConfiguration = msgCtx.getConfigurationContext().getAxisConfiguration();
        int tenantID = SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
        Map<Integer, ActivityConfigData> tenantSpecificActivity = TenantActivityConfigData.getTenantSpecificEventingConfigData();
        ActivityConfigData activityConfigData = tenantSpecificActivity.get(tenantID);

        if (activityConfigData.isMessageDumpingEnable()) {
            long currentTime = System.currentTimeMillis();
            setActivityIdInSOAPHeader(synCtx);

            if (log.isDebugEnabled()) {
                log.debug("Processing new message through the BAM activity mediator - " +
                          "Message ID=" + synCtx.getMessageID());
            }

            if (!synCtx.isResponse() && !synCtx.isFaultResponse()) {
                ActivityPublisherUtils.publishEvent(synCtx, currentTime, extractSoapBody, true);
            } else {
                ActivityPublisherUtils.publishEvent(synCtx, currentTime, extractSoapBody, false);
            }
        }
        return true;
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
            OMNamespace omNs = fac.createOMNamespace(ActivityPublisherConstants.BAM_HEADER_NAMESPACE_URI, "ns");
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
                        new QName(ActivityPublisherConstants.BAM_HEADER_NAMESPACE_URI,
                                  ActivityPublisherConstants.BAM_EVENT));
                if (!itr.hasNext()) {
                    soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock(
                            ActivityPublisherConstants.BAM_EVENT, omNs);
                    if (synapseContext.getProperty(BAMDataPublisherConstants.MSG_ACTIVITY_ID) == null) { // this if
                        // condition we add
                        // to track failure messages coming from DS.That is a new message. So, doesn't have activityID.Getting activityID
                        // from the synapseContext.property
                        soapHeaderBlock.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, uuid_string, null);
                        synapseContext.setProperty(BAMDataPublisherConstants.MSG_ACTIVITY_ID, uuid_string);
                    } else {
                        soapHeaderBlock.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, (String) synapseContext
                                .getProperty(BAMDataPublisherConstants.MSG_ACTIVITY_ID), null);
                    }
                } else {// If header is not null check for  BAM headers

                    // If the BAM header already present
                    //    1. If activity id is not present generate a one and include it to BAM header
                    //    2. Set activity id in synapse context for response path
                    OMElement bamHeader = (OMElement) itr.next();
                    OMAttribute activityIdAttr = bamHeader.getAttribute(new QName(
                            ActivityPublisherConstants.ACTIVITY_ID));
                    if (activityIdAttr != null) {
                        String activityId = activityIdAttr.getAttributeValue();
                        synapseContext.setProperty(BAMDataPublisherConstants.MSG_ACTIVITY_ID,
                                                   activityId);
                    } else {
                        bamHeader.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, uuid_string, null);
                        synapseContext.setProperty(BAMDataPublisherConstants.MSG_ACTIVITY_ID, uuid_string);
                    }
                }
            } else {
                if (soapFactory != null) {
                    (soapFactory).createSOAPHeader(soapEnvelope);
                }
                if (soapEnvelope.getHeader() != null) {
                    soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock(ActivityPublisherConstants.BAM_EVENT, omNs);
                    if (synapseContext.getProperty(BAMDataPublisherConstants.MSG_ACTIVITY_ID) == null) { // this if
                        // condition we add
                        // to track failure messages coming from
                        // DS.That is a new message. So, doesn't have activityID.Getting activityID
                        // from the synapseContext.property
                        soapHeaderBlock.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, uuid_string, null);
                        synapseContext.setProperty(BAMDataPublisherConstants.MSG_ACTIVITY_ID, uuid_string);
                    } else {
                        soapHeaderBlock.addAttribute(ActivityPublisherConstants.ACTIVITY_ID, (String) synapseContext
                                .getProperty(BAMDataPublisherConstants.MSG_ACTIVITY_ID), null);
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error while processing MessageHeaderMediator...", e);
        }

    }

    public void setExtractSoapBody(boolean extractSoapBody) {
        this.extractSoapBody = extractSoapBody;
    }
}
