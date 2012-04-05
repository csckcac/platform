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

package org.wso2.carbon.bam.data.publisher.activity.mediation;

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
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.UUID;

public class MessageActivityMediator extends AbstractMediator {

    // get property name from class mediator configuartion
    private String status = "";
    private static Log log = LogFactory.getLog(MessageActivityMediator.class);

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean mediate(MessageContext synCtx) {

        setMessageProperties(synCtx);

        if (log.isDebugEnabled()) {
            log.debug("Processing new message through the BAM activity mediator - " +
                      "Message ID=" + synCtx.getMessageID());
        }

        Object appFailure = synCtx.getProperty(ActivityPublisherConstants.PROP_APPLICATION_FAILURE);
        Object techFailure = synCtx.getProperty(ActivityPublisherConstants.PROP_TECHNICAL_FAILURE);

        if (JavaUtils.isTrueExplicitly(appFailure) || JavaUtils.isTrueExplicitly(techFailure)) {
            // set a new message ID for failure messages
            String messageId = UUID.randomUUID().toString();
            if (log.isDebugEnabled()) {
                log.debug("Setting the new message ID: " + messageId + " on the failure message");
            }
            synCtx.setMessageID(messageId);
        }

        if (!synCtx.isResponse() && !synCtx.isFaultResponse()) {
            ActivityPublisherUtils.publishEvent(synCtx, true);
        } else {
            Object activityProperty = synCtx.getProperty(ActivityPublisherConstants.PROP_ACTIVITY_PROPERTY);
            if (activityProperty != null) {
                // If the response does not have the BAMEvent header, we should set it here
                setActivityIdHeader(synCtx, activityProperty.toString());
            }
            ActivityPublisherUtils.publishEvent(synCtx, false);
        }

        // Remove the properties from the BAMEvent header
        SOAPHeader header = synCtx.getEnvelope().getHeader();
        OMElement bamEvent = header.getFirstChildWithName(ActivityPublisherConstants.BAM_EVENT_QNAME);
        if (bamEvent != null) {
            Iterator iter = bamEvent.getChildElements();
            while (iter.hasNext()) {
                OMElement childElement = (OMElement) iter.next();
                OMAttribute nameAttr = childElement.getAttribute(new QName("name"));
                if (nameAttr != null) {
                    nameAttr.setAttributeValue("Child");
                }

                OMAttribute valueAttr = childElement.getAttribute(new QName("value"));
                if (valueAttr != null) {
                    valueAttr.setAttributeValue("true");
                }
            }
        }

        return true;
    }

    private void setMessageProperties(MessageContext synapseContext) {
        try {

            // Property name would be "Parent_uuid"
            // Property Value would be "Parent_uuid_messageid"
            UUID uuid = UUID.randomUUID();
            String uuid_string = uuid.toString();

            Axis2MessageContext axis2smc = (Axis2MessageContext) synapseContext;
            org.apache.axis2.context.MessageContext axis2MessageContext = axis2smc.getAxis2MessageContext();
            String propName = "";
            String propValue = "";

            if (synapseContext.getProperty("application_failure") != null
                && synapseContext.getProperty("application_failure").equals("true")) {
                propName = "";
                propValue = "";
            } else if (synapseContext.getProperty("technical_failure") != null
                       && synapseContext.getProperty("technical_failure").equals("true")) {
                propName = "";
                propValue = "";
            } else {
                //propName = status + "_" + uuid.toString();
                propName = status;
                propValue = status + "_" + uuid_string + "_" + synapseContext.getMessageID();
            }

            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMNamespace omNs = fac.createOMNamespace("http://wso2.org/ns/2010/10/bam", "ns");
            SOAPEnvelope soapEnvelope = axis2MessageContext.getEnvelope();
            String soapNamespaceURI = soapEnvelope.getNamespace().getNamespaceURI();
            SOAPFactory soapFactory = null;
            SOAPHeaderBlock soapHeaderBlock = null;
            OMElement bampropertyElement = fac
                    .createOMElement(new QName("http://wso2.org/ns/2010/10/bam", "Property", "ns"), null);

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
                        new QName("http://wso2.org/ns/2010/10/bam",
                                  "BAMEvent"));
                if (!itr.hasNext()) {
                    soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock("BAMEvent", omNs);
                    if (synapseContext.getProperty("bam_activity_id") == null) { // this if
                        // condition we add
                        // to track failure messages coming from
                        // DS.That is a new message. So, doesn't have activityID.Getting activityID
                        // from the synapsecontext.property
                        soapHeaderBlock.addAttribute("activityID", uuid_string, null);
                        synapseContext.setProperty("bam_activity_id", uuid_string);
                    } else {
                        soapHeaderBlock.addAttribute("activityID", (String) synapseContext
                                .getProperty("bam_activity_id"), null);
                    }
                    bampropertyElement.addAttribute("name", propName, null);
                    bampropertyElement.addAttribute("value", propValue, null);
                    soapHeaderBlock.addChild(bampropertyElement);
                } else {
                    // If the BAM header already present
                    //    1. If activity id is not present generate a one and include it to BAM header
                    //    2. Set activity id in synapse context for response path
                    OMElement bamHeader = (OMElement)itr.next();
                    OMAttribute activityIdAttr = bamHeader.getAttribute(new QName("activityID"));
                    if (activityIdAttr != null) {
                        String activityId = activityIdAttr.getAttributeValue();
                        synapseContext.setProperty("bam_activity_id", activityId);
                    } else {
                        bamHeader.addAttribute("activityID", uuid_string, null);
                        synapseContext.setProperty("bam_activity_id", uuid_string);
                    }
                }
            }
            // If header is null add BAM headers
            if (soapEnvelope.getHeader() == null) {
                if (soapFactory != null) {
                    (soapFactory).createSOAPHeader(soapEnvelope);
                }
                if (soapEnvelope.getHeader() != null) {
                    soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock("BAMEvent", omNs);
                    if (synapseContext.getProperty("bam_activity_id") == null) { // this if
                        // condition we add
                        // to track failure messages coming from
                        // DS.That is a new message. So, doesn't have activityID.Getting activityID
                        // from the synapsecontext.property
                        soapHeaderBlock.addAttribute("activityID", uuid_string, null);
                        synapseContext.setProperty("bam_activity_id", uuid_string);
                    } else {
                        soapHeaderBlock.addAttribute("activityID", (String) synapseContext
                                .getProperty("bam_activity_id"), null);
                    }
                    bampropertyElement.addAttribute("name", propName, null);
                    bampropertyElement.addAttribute("value", propValue, null);
                    soapHeaderBlock.addChild(bampropertyElement);
                }
            }

            //set the activityID as a synapse property to access by mediation statistics observer properties
            synapseContext.setProperty("activity_property", propName);
            synapseContext.setProperty("activity_property_value", propValue);

            /*
            * header comes with ActivityID, but not with property, so add property(get from
            * configuration)
            */
            Iterator itr = synapseContext.getEnvelope().getHeader()
                    .getChildrenWithName(new QName("http://wso2.org/ns/2010/10/bam", "BAMEvent"));

            if (itr.hasNext()) {
                OMElement element = (OMElement) itr.next();
                String activityID = element.getAttributeValue(new QName("activityID"));
                Iterator childItr = element.getChildElements();
                if (activityID != null) {
                    if (!activityID.equals("")) {
                        OMElement childElement = (OMElement) childItr.next();
                        if (("").equals(childElement.getAttributeValue(new QName("name")))
                            || ("").equals(childElement.getAttributeValue(new QName("value")))) {
                            childElement.getAttribute(new QName("name")).setAttributeValue(propName);
                            childElement.getAttribute(new QName("value")).setAttributeValue(propValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error while processing MessageHeaderMediator...", e);
        }
    }

    private void setActivityIdHeader(MessageContext synCtx, String activityId) {
        SOAPEnvelope soapEnvelope = synCtx.getEnvelope();
        SOAPHeader soapHeader = soapEnvelope.getHeader();

        if (soapHeader != null) {
            OMElement bamEventElement = soapHeader.getFirstChildWithName(ActivityPublisherConstants.BAM_EVENT_QNAME);
            if (bamEventElement != null) {
                return;
            }
        }

        String soapNamespaceURI = soapEnvelope.getNamespace().getNamespaceURI();

        SOAPFactory soapFactory;
        if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(soapNamespaceURI)) {
            soapFactory = OMAbstractFactory.getSOAP11Factory();
        } else {
            soapFactory = OMAbstractFactory.getSOAP12Factory();
        }

        if (soapEnvelope.getHeader() == null) {
            soapFactory.createSOAPHeader(soapEnvelope);
        }

        OMElement bamEventElement = soapEnvelope.getHeader().getFirstChildWithName(
                ActivityPublisherConstants.BAM_EVENT_QNAME);
        if (bamEventElement == null) {
            OMNamespace omNs = soapFactory.createOMNamespace(
                    ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, "ns");
            SOAPHeaderBlock soapHeaderBlock = soapEnvelope.getHeader().addHeaderBlock(
                    "BAMEvent", omNs);
            soapHeaderBlock.addAttribute("activityID", activityId, null);
        }
    }
}