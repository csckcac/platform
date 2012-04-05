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
package org.wso2.carbon.bam.receiver.test;

import junit.framework.TestCase;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.authenticator.stub.authentication.AuthenticationException;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WorkFlowEventTestMain extends TestCase {

    private static String cookie;
    private static ServiceClient client;


    private static final int CONCURRENCY_LEVEL = 1;
    private static final int NO_OF_INVOCATIONS = 1;

    protected void setUp() {
        String backendServerURL = "http://localhost:9763/services/";
        String serverURL = "https://localhost:9443/services/";
        String username = "admin";
        String password = "admin";
        ConfigurationContext configContext = null;
        String CARBON_HOME = "/opt/installations/trunk/newBam/wso2carbon-core-3.2.1";

        String epr = backendServerURL + "BAMEventReceiverService/Publish";
        try {
            System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" + File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
            authenticate(configContext, serverURL, username, password);

            client = new ServiceClient();
            Options options = client.getOptions();
            options.setTo(new EndpointReference(epr));
//            options.setAction("Publish");
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

            options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            //Increase the time out when sending large attachments
            options.setTimeOutInMilliSeconds(1000000);


        } catch (Exception axisFault) {
            String msg = "Failed to initiate WSRegistry Service workerClient. " + axisFault.getMessage();


        }
        try {
            ExecutorService executor = Executors.newCachedThreadPool();
            for (int i = 0; i < CONCURRENCY_LEVEL; i++) {
                executor.submit(new Worker(new OrderFlowEventGenerator(client)));
            }

            for (int i = 0; i < CONCURRENCY_LEVEL; i++) {
                executor.submit(new Worker(new ShippingFlowEventGenerator(client)));
            }

            executor.shutdown();
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public static void main(String[] args) {
        String backendServerURL = "http://localhost:9763/services/";
        String serverURL = "https://localhost:9443/services/";
        String username = "admin";
        String password = "admin";
        ConfigurationContext configContext = null;
        String CARBON_HOME = "/opt/installations/trunk/newBam/wso2carbon-core-3.2.1";

        String epr = serverURL + "BAMEventReceiverService/Publish";
        try {
            System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" + File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks");
            System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
            System.setProperty("javax.net.ssl.trustStoreType", "JKS");
            //authenticate(configContext, serverURL, username, password);

            client = new ServiceClient();
            Options options = client.getOptions();
            options.setTo(new EndpointReference(epr));
//            options.setAction("Publish");
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

            options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
            //Increase the time out when sending large attachments
            options.setTimeOutInMilliSeconds(1000000);


        } catch (Exception axisFault) {
            String msg = "Failed to initiate WSRegistry Service workerClient. " + axisFault.getMessage();


        }
        try {
            ExecutorService executor = Executors.newCachedThreadPool();
            for (int i = 0; i < CONCURRENCY_LEVEL; i++) {
                executor.submit(new Worker(new OrderFlowEventGenerator(client)));
            }

            for (int i = 0; i < CONCURRENCY_LEVEL; i++) {
                executor.submit(new Worker(new ShippingFlowEventGenerator(client)));
            }

            executor.shutdown();
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static boolean authenticate(ConfigurationContext ctx, String serverURL, String username,
                                       String password) throws Exception {
        String serviceEPR = serverURL + "AuthenticationAdmin";
        AuthenticationAdminStub stub = new AuthenticationAdminStub(serviceEPR);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            boolean result = stub.login(username, password, serviceEPR);
            if (result) {
                cookie = (String) stub._getServiceClient().getServiceContext().
                        getProperty(HTTPConstants.COOKIE_STRING);
            }
            return result;
        } catch (Exception e) {
            String msg = "Error occurred while logging in";
            throw e;
        }
    }

    private static class Worker implements Runnable {

        private EventGenerator generator;

        private Worker(EventGenerator generator) {
            this.generator = generator;
        }

        public void run() {
            try {
                for (int j = 0; j < NO_OF_INVOCATIONS; j++) {
                    try {
                        generator.publishSingleInvocationEvents();
                    } catch (XMLStreamException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


    }

    private static interface EventGenerator {

        void publishSingleInvocationEvents() throws XMLStreamException, AxisFault;

    }

    private static class OrderFlowEventGenerator implements EventGenerator {
        // Flow : ESB --> AS --> AS --> ESB
        ServiceClient client;

        public OrderFlowEventGenerator(ServiceClient client) {
            this.client = client;
        }

        public void publishSingleInvocationEvents() throws XMLStreamException, AxisFault {
            List<OMElement> eventList = new ArrayList<OMElement>();
            String activityId = UUID.randomUUID().toString();

            eventList.add(getPayload("ORDER", "ESB", activityId, "request"));
            eventList.add(getPayload("ORDER", "AS", activityId, "request"));
            eventList.add(getPayload("ORDER", "AS", activityId, "response"));
            eventList.add(getPayload("ORDER", "ESB", activityId, "response"));

            for (OMElement event : eventList) {
                //OMElement meta = (OMElement) event.getChildrenWithLocalName("metadata").next();
                System.out.println("Flow ID: " + ((OMElement) event.getChildrenWithLocalName("workFlowId").next()).getText() +
                                   " Node ID: " + ((OMElement) event.getChildrenWithLocalName("nodeId").next()).getText());
                client.fireAndForget(event);
            }
        }
    }

    private static class ShippingFlowEventGenerator implements EventGenerator {
        // Flow : ESB --> AS --> AS --> ESB --> DS --> DS --> ESB
        ServiceClient client;

        public ShippingFlowEventGenerator(ServiceClient client) {
            this.client = client;
        }

        public void publishSingleInvocationEvents() throws XMLStreamException, AxisFault {
            List<OMElement> eventList = new ArrayList<OMElement>();
            String activityId = UUID.randomUUID().toString();

            eventList.add(getPayload("SHIP", "ESB", activityId, "request"));
            eventList.add(getPayload("SHIP", "AS", activityId, "request"));
            eventList.add(getPayload("SHIP", "AS", activityId, "response"));
            eventList.add(getPayload("SHIP", "ESB", activityId, "response"));
            eventList.add(getPayload("SHIP", "ESB", activityId, "request"));
            eventList.add(getPayload("SHIP", "DS", activityId, "request"));
            eventList.add(getPayload("SHIP", "DS", activityId, "response"));
            eventList.add(getPayload("SHIP", "ESB", activityId, "response"));

            for (OMElement event : eventList) {
                //OMElement meta = (OMElement) event.getChildrenWithLocalName("metadata").next();
                System.out.println("Flow ID: " + ((OMElement) event.getChildrenWithLocalName("workFlowId").next()).getText() +
                                   " Node ID: " + ((OMElement) event.getChildrenWithLocalName("nodeId").next()).getText());
                client.fireAndForget(event);
            }
        }
    }

    private static OMElement getPayload(String flowId, String nodeId, String activityId,
                                        String direction)
            throws XMLStreamException {

/*        String payload = "<eventData xmlns:m0=\"http://receivers.bam.carbon.wso2.org\">" +
                         "<m0:eventdata>" +
                         "<m0:eventKey1>value1</m0:eventKey1>" +
                         "<m0:eventKey2>value2</m0:eventKey2>" +
                         "<m0:eventKey3>value3</m0:eventKey3>" +
                         "</m0:eventdata>" +
                         "<m0:metadata>" +
                         "<m0:FlowID>" + flowId + "</m0:FlowID>" +
                         "<m0:NodeID>" + nodeId + "</m0:NodeID>" +
                         "<m0:Direction>" + direction + "</m0:Direction>" +
                         "</m0:metadata>" +
                         "<m0:correlationdata>" +
                         "</m0:correlationdata>" +
                         "</eventData>";*/

        String payLoad = "     <bam:data xmlns:bam=\"http://wso2.org/bam/2011/07/31\">\n" +
                         "         <workFlowId>" + flowId + "</workFlowId>\n" +
                         "         <nodeId>" + nodeId + "</nodeId>\n" +
                         "         <activityId>" + activityId + "</activityId>\n" +
                         "         <serverAddress>127.0.0.1</serverAddress>\n" +
                         "         <direction>" + direction + "</direction>\n" +
                         "        <requestCount>2</requestCount>\n" +
                         "        <responseCount>4</responseCount>\n" +
                         "        <faultCount>2</faultCount>\n" +
                         "        <maxResponeTime>23.45</maxResponeTime>\n" +
                         "        <minResponeTime>0.34</minResponeTime>\n" +
                         "\n" +
                         "     </bam:data>";

        OMElement payloadElement = AXIOMUtil.stringToOM(payLoad);
/*        OMElement correlationElement = (OMElement) payloadElement.
                getChildrenWithLocalName("correlationdata").next();*/

/*        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace ns = fac.createOMNamespace("http://wso2.org/bam/2011/07/31", "bam");
        OMElement element = fac.createOMElement("activityId", );
        element.setText(activityId);

        payloadElement.addChild(element);*/

        return payloadElement;
    }


}
