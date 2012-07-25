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
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.mediator.bam.config.BamMediatorException;
import org.wso2.carbon.mediator.bam.util.BamMediatorConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.UUID;

/**
 * Set the Activity ID to the SOAP Header.
 */
public class ActivityIDSetter {

    private static final Log log = LogFactory.getLog(ActivityIDSetter.class);
    private String uuidString;
    private SOAPEnvelope soapEnvelope;
    private OMNamespace omNs;
    private MessageContext synapseContext;

    public void setActivityIdInSOAPHeader(MessageContext synapseContext) {

        try {
            // Property name would be "Parent_uuid"
            // Property Value would be "Parent_uuid_messageid"
            UUID uuid = UUID.randomUUID();
            this.uuidString = uuid.toString();
            this.synapseContext = synapseContext;

            Axis2MessageContext axis2smc = (Axis2MessageContext) synapseContext;
            org.apache.axis2.context.MessageContext axis2MessageContext = axis2smc.getAxis2MessageContext();

            OMFactory fac = OMAbstractFactory.getOMFactory();
            this.omNs = fac.createOMNamespace(BamMediatorConstants.BAM_HEADER_NAMESPACE_URI, "ns");
            this.soapEnvelope = axis2MessageContext.getEnvelope();
            String soapNamespaceURI = this.soapEnvelope.getNamespace().getNamespaceURI();
            SOAPFactory soapFactory = null;

            if (soapNamespaceURI.equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                soapFactory = OMAbstractFactory.getSOAP11Factory();
            } else if (soapNamespaceURI.equals(SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                soapFactory = OMAbstractFactory.getSOAP12Factory();
            } else {
                log.error("Not a standard soap message");
            }

            this.setActivityIDInSOAPHeaderWithConditioning(soapFactory);
        } catch (Exception e) {
            String errorMsg = "Error while setting Activity ID in SOAP Header. " + e.getMessage();
            log.error(errorMsg, e);
        }

    }

    private void setActivityIDInSOAPHeaderWithConditioning(SOAPFactory soapFactory) throws BamMediatorException {
        try {
            // If header is not null check for  BAM headers
            if (this.soapEnvelope.getHeader() != null) {
                Iterator itr = this.soapEnvelope.getHeader().getChildrenWithName(
                        new QName(BamMediatorConstants.BAM_HEADER_NAMESPACE_URI,
                                  BamMediatorConstants.BAM_EVENT));
                if (!itr.hasNext()) {
                    this.processActivityIDWhenSOAPHeaderIsNull();
                } else {// If header is not null check for  BAM headers

                    // If the BAM header already present
                    //    1. If activity id is not present generate a one and include it to BAM header
                    //    2. Set activity id in synapse context for response path
                    OMElement bamHeader = (OMElement) itr.next();
                    OMAttribute activityIdAttr = bamHeader.getAttribute(new QName(
                            BamMediatorConstants.ACTIVITY_ID));
                    if (activityIdAttr != null) {
                        String activityId = activityIdAttr.getAttributeValue();
                        this.synapseContext.setProperty(BamMediatorConstants.MSG_ACTIVITY_ID,
                                                   activityId);
                    } else {
                        bamHeader.addAttribute(BamMediatorConstants.ACTIVITY_ID, uuidString, null);
                        this.synapseContext.setProperty(BamMediatorConstants.MSG_ACTIVITY_ID, this.uuidString);
                    }
                }
            } else {
                if (soapFactory != null) {
                    (soapFactory).createSOAPHeader(this.soapEnvelope); // TO DO
                }
                if (this.soapEnvelope.getHeader() != null) {
                    this.processActivityIDWhenSOAPHeaderIsNull();
                }
            }
        } catch (Exception e) {
            String errorMsg = "Error while setting Activity ID in SOAP Header with conditioning. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        }
    }

    private void processActivityIDWhenSOAPHeaderIsNull() throws BamMediatorException {
        try {
            SOAPHeader soapHeader = this.soapEnvelope.getHeader();
            SOAPHeaderBlock soapHeaderBlock = this.soapEnvelope.getHeader().addHeaderBlock(BamMediatorConstants.BAM_EVENT, this.omNs);
            if (this.synapseContext.getProperty(BamMediatorConstants.MSG_ACTIVITY_ID) == null) { // this if
                // condition we add
                // to track failure messages coming from
                // DS.That is a new message. So, doesn't have activityID.Getting activityID
                // from the synapseContext.property
                soapHeaderBlock.addAttribute(BamMediatorConstants.ACTIVITY_ID, this.uuidString, null);
                this.synapseContext.setProperty(BamMediatorConstants.MSG_ACTIVITY_ID, this.uuidString);
            } else {
                soapHeaderBlock.addAttribute(BamMediatorConstants.ACTIVITY_ID, (String) this.synapseContext
                        .getProperty(BamMediatorConstants.MSG_ACTIVITY_ID), null);
            }
        } catch (Exception e) {
            String errorMsg = "Error while processing Activity ID when SOAP Header is Null. " + e.getMessage();
            log.error(errorMsg, e);
            throw new BamMediatorException(errorMsg, e);
        }
    }
}
