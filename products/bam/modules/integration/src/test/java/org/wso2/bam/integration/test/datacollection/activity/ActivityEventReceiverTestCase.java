/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.bam.integration.test.datacollection.activity;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.InOutAxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.test.common.publisher.JDBCPublisher;
import org.wso2.bam.integration.test.datacollection.activity.mockobjects.MockActivityPublisherAdmin;
import org.wso2.bam.integration.test.datacollection.activity.mockobjects.MockLwEventBroker;
import org.wso2.carbon.bam.data.publisher.activity.service.PublisherUtils;
import org.wso2.carbon.bam.data.publisher.activity.service.config.EventingConfigData;
import org.wso2.carbon.bam.data.publisher.activity.service.modules.ActivityInHandler;
import org.wso2.carbon.bam.data.publisher.activity.service.modules.XPathLookupHandler;
import org.wso2.carbon.bam.data.publisher.activity.service.ServiceHolder;
import org.wso2.carbon.bam.stub.statquery.ActivityForServer;
import org.wso2.carbon.bam.stub.statquery.BAMStatQueryDSStub;
import org.wso2.carbon.bam.stub.statquery.MessageData;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import javax.sql.DataSource;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Properties;

/**
 * This test case verifies whether the activity events are received successfully by BAM
 */
public class ActivityEventReceiverTestCase {


    private static final Log log = LogFactory.getLog(ActivityEventReceiverTestCase.class);

    private static final String SERVICE = "TestService";
    private static final String OPERATION = "TestOperation";

    public final static String BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI = "http://wso2.org/ns/2010/10/bam";
    public static final String ACTIVITY_ID = "urn:uuid:1234567890";
    private static final String ACTIVITY_PAYLOAD_FILE = "ActivityPayload.xml";

    private static final String LOCALHOST = "https://127.0.0.1:9443";

    private LoginLogoutUtil util = new LoginLogoutUtil();

    BAMStatQueryDSStub statQueryStub;
    DataSource datasource;

    @BeforeClass(groups = {"wso2.bam"})
    public void login() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        String sessionCookie = util.login();
        initStubs(sessionCookie);
    }

    @Test(groups = {"wso2.bam.test"}, description = "Test for activity event reception")
    public void receiveActivityEvent() throws Exception {
        runActivityServiceScenario();
    }

    private void cleanup() throws AxisFault {
        statQueryStub._getServiceClient().cleanupTransport();
        statQueryStub._getServiceClient().cleanup();
        statQueryStub.cleanup();
    }

    @AfterClass(groups = {"wso2.bam"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
    }

    private void runActivityServiceScenario() throws Exception {
        EventingConfigData configData = new EventingConfigData();
        configData.setEnableEventing("ON");

        configData.setMessageThreshold(0);
        MockActivityPublisherAdmin admin = new MockActivityPublisherAdmin();
        admin.configureEventing(configData);
        MockLwEventBroker broker = new MockLwEventBroker();

        PublisherUtils.setActivityPublisherAdmin(admin);
        PublisherUtils.setLWEventBroker(broker);
        PublisherUtils.setServerName(LOCALHOST);

        ServiceHolder.setLWEventBroker(broker);
        MessageContext msgCtx = getTestContext();
        invokeHandlers(msgCtx);
        cleanup();
    }

    private MessageContext getTestContext() throws AxisFault, XMLStreamException {

        AxisConfiguration configuration = new AxisConfiguration();
        AxisService service = new AxisService(SERVICE);
        AxisOperation operation = new InOutAxisOperation(new QName(OPERATION));
        configuration.addService(service);
        service.addOperation(operation);

        ConfigurationContext context = new ConfigurationContext(configuration);
        ServiceContext svcCtx = new ServiceContext();
        svcCtx.setMyEPR(new EndpointReference(LOCALHOST + "/" + "services/" + SERVICE));
        OperationContext opCtx = new OperationContext(operation, svcCtx);

        MessageContext msgCtx = new MessageContext();

        msgCtx.setAxisService(service);
        msgCtx.setAxisOperation(operation);
        msgCtx.setConfigurationContext(context);

        SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory().getDefaultEnvelope();
        envelope.getBody().addChild(getActivityPayload());
        SOAPFactory soapFactory = OMAbstractFactory.getSOAP11Factory();
        OMNamespace omNs = soapFactory.createOMNamespace(BAM_ACTIVITY_ID_HEADER_NAMESPACE_URI, "ns");
        SOAPHeaderBlock soapHeaderBlock = envelope.getHeader().addHeaderBlock(
                "BAMEvent", omNs);
        soapHeaderBlock.addAttribute("activityID", ACTIVITY_ID, null);
        msgCtx.setEnvelope(envelope);
        return msgCtx;
    }

    private OMElement getActivityPayload() throws XMLStreamException {
        return new StAXOMBuilder(ActivityEventReceiverTestCase.class.getResourceAsStream(
                "/" + ACTIVITY_PAYLOAD_FILE)).getDocumentElement();
    }

    private void invokeHandlers(MessageContext msgCtx) throws AxisFault {
        ActivityInHandler inHandler = new ActivityInHandler();
        inHandler.invoke(msgCtx);

        XPathLookupHandler xpathHandler = new XPathLookupHandler();
        xpathHandler.invoke(msgCtx);
    }

    private void verifyActivityData() throws RemoteException {
        ActivityForServer[] activities = statQueryStub.getActivityDetailsForServer(LOCALHOST);
    }

    private void initStubs(String sessionCookie) throws AxisFault {
        statQueryStub = new BAMStatQueryDSStub("https://localhost:9443/services/BAMStatQueryDS");
        setSessionCookie(statQueryStub, sessionCookie);
    }

    private void setSessionCookie(Stub stub, String sessionCookie) {
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
    }
}


