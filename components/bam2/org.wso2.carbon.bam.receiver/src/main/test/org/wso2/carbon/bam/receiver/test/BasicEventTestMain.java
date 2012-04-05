/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.core.common.AuthenticationException;


import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BasicEventTestMain extends TestCase{
    private static String cookie;
    private String epr;
    private static ServiceClient client;

    @Override
    protected void setUp() throws Exception {
        String backendServerURL = "http://localhost:9763/services/";
        String serverURL = "https://localhost:9443/services/";
        String username = "admin";
        String password = "admin";
        ConfigurationContext configContext = null;
        String CARBON_HOME = "/opt/installations/trunk/wso2carbon-core-3.2.1";

        String epr = backendServerURL + "BAMEventReceiverService/Publish";
        try {
            System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" + File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks");
             System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType","JKS");
//            authenticate(configContext, serverURL, username, password);

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

            throw new Exception(msg, axisFault);
        }

    }


    public static void main(String[] args) {
            String backendServerURL = "http://localhost:9763/services/";
        String serverURL = "https://localhost:9443/services/";
        String username = "admin";
        String password = "admin";
        ConfigurationContext configContext = null;
        String CARBON_HOME = "/opt/installations/trunk/wso2carbon-core-3.2.1";

        String epr = backendServerURL + "BAMEventReceiverService/Publish";
        try {
            System.setProperty("javax.net.ssl.trustStore", CARBON_HOME + File.separator + "repository" + File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks");
             System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        System.setProperty("javax.net.ssl.trustStoreType","JKS");
//            authenticate(configContext, serverURL, username, password);

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
                executor.submit(new Worker(client));
            }
            executor.shutdown();
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public boolean authenticate(ConfigurationContext ctx, String serverURL, String username, String password) throws AxisFault,
                                                                                                                     AuthenticationException {
        String serviceEPR = serverURL + "AuthenticationAdmin";
        AuthenticationAdminStub stub = new AuthenticationAdminStub(serviceEPR);
        ServiceClient client = stub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        try {
            boolean result = stub.login(username, password, serviceEPR);
            if (result){
                cookie = (String) stub._getServiceClient().getServiceContext().
                        getProperty(HTTPConstants.COOKIE_STRING);
            }
            return result;
        } catch (Exception e) {
            String msg = "Error occurred while logging in";
            throw new AuthenticationException(msg, e);
        }
    }

    public static final int CONCURRENCY_LEVEL = 1;

    public static final int NO_OF_MSGS = 1;


    public void testBasicEvent() {
        try {
            ExecutorService executor = Executors.newCachedThreadPool();
            for (int i = 0; i < CONCURRENCY_LEVEL; i++) {
                executor.submit(new Worker(client));
            }
            executor.shutdown();
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    public void testDummy() {
        try {
            ExecutorService executor = Executors.newCachedThreadPool();
            for (int i = 0; i < CONCURRENCY_LEVEL; i++) {
                executor.submit(new Worker(client));
            }
//            executor.shutdown();
        } catch (Throwable e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private static class Worker implements Runnable {

        private ServiceClient workerClient;

        private Worker(ServiceClient client) {
            this.workerClient = client;
        }

        @Override
        public void run() {
            try {
                for (int j = 0; j < NO_OF_MSGS; j++) {
                    workerClient.fireAndForget(getPayload());
                }
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (XMLStreamException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }


    }

    private static OMElement getPayload() throws XMLStreamException {

        String payload = "     <bam:data xmlns:bam=\"http://wso2.org/bam/2011/07/31\">\n" +
                         "         <workFlowId>3</workFlowId>\n" +
                         "         <nodeId>2</nodeId>\n" +
                         "         <serverAddress>127.0.0.1</serverAddress>\n" +
                         "        <requestCount>2</requestCount>\n" +
                         "        <responseCount>4</responseCount>\n" +
                         "        <faultCount>2</faultCount>\n" +
                         "        <maxResponeTime>23.45</maxResponeTime>\n" +
                         "        <minResponeTime>0.34</minResponeTime>\n" +
                         "\n" +
                         "     </bam:data>";
        return AXIOMUtil.stringToOM(payload);
    }

}


