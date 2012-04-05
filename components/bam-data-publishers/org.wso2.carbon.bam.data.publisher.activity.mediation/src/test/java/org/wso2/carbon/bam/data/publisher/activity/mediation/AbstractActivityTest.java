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

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.wso2.carbon.base.ServerConfiguration;

import javax.xml.namespace.QName;

public abstract class AbstractActivityTest extends TestCase {

    public static final String ACTIVITY_ID = "urn:uuid:1234567890";
    public static final String SERVICE = "IDocReceivingProxy";
    public static final String OPERATION = "MyOperation";
    public static final String PAYLOAD = "<foo><bar>text</bar></foo>";

    protected MessageContext getTestContext() {
        SynapseConfiguration testConfig = new SynapseConfiguration();
        SynapseEnvironment synEnv = new Axis2SynapseEnvironment(new ConfigurationContext(new AxisConfiguration()),
                                          testConfig);
        org.apache.axis2.context.MessageContext msgCtx = new org.apache.axis2.context.MessageContext();
        MessageContext synCtx = new Axis2MessageContext(msgCtx, testConfig, synEnv);

        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        try {
            envelope.getBody().addChild(AXIOMUtil.stringToOM(PAYLOAD));
            SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
            OMNamespace omNs = soapFactory.createOMNamespace(
                    ActivityPublisherConstants.BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, "ns");
            SOAPHeaderBlock soapHeaderBlock = envelope.getHeader().addHeaderBlock(
                    "BAMEvent", omNs);
            soapHeaderBlock.addAttribute("activityID", ACTIVITY_ID, null);
            synCtx.setEnvelope(envelope);
        } catch (Exception ignored) {

        }

        msgCtx.setAxisService(new AxisService(SERVICE));
        msgCtx.setAxisOperation(new InOutAxisOperation(new QName(OPERATION)));
        return synCtx;
    }

    protected void initSerialization() {
        System.setProperty("carbon.https.port", String.valueOf(9443));
        ActivityPublisherUtils.setServerConfiguration(ServerConfiguration.getInstance());
    }

    protected void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            System.err.println("Sleeping thread interrupted");
            e.printStackTrace();
        }
    }
}