/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.csg.integration.tests;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.synapse.SynapseConstants;
import org.testng.annotations.Test;
import org.wso2.carbon.cloud.csg.common.CSGConstant;
import org.wso2.carbon.cloud.csg.common.CSGUtils;
import org.wso2.carbon.cloud.csg.common.thrift.CSGThriftClient;
import org.wso2.carbon.cloud.csg.common.thrift.gen.Message;
import org.wso2.carbon.cloud.csg.common.thrift.gen.NotAuthorizedException;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;
import org.wso2.carbon.utils.NetworkUtils;
import org.wso2.csg.integration.tests.util.StockQuoteClient;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.testng.Assert.*;

/**
 * This contains a list of test which test the CSG specific functionality such as
 * 1. Deploying and invoking a CSG services for a SOAP service, REST service, JSON service
 * 2. Dead message cleanup task of CSG server
 * 3. Test the operations(login, exchange) associated with the in-VM thrift server
 * 4. Test nhttp transport + message relay for receiving messages
 * 5. And list goes..
 */
public class CSGServiceTestCase extends CSGIntegrationTestCase {

    private ProxyServiceAdminStub proxyServiceAdminStub;

    private StockQuoteClient csgServiceClient;

    private static final String QUOTE_STRING = "CSG";

    private static final String CSG_SERVICE_NAME = "SimpleStockQuoteService";

    public static final String CSG_SERVER_NAME = "TestServer";

    public CSGServiceTestCase() {
        super("ProxyServiceAdmin");
    }

    @Override
    protected void init() throws Exception {
        proxyServiceAdminStub = new ProxyServiceAdminStub(getAdminServiceURL());
        csgServiceClient = new StockQuoteClient();
        authenticate(proxyServiceAdminStub);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        try {
            proxyServiceAdminStub.cleanup();
            csgServiceClient.destroy();
        } catch (AxisFault axisFault) {
            log.error("Error while cleaning up the proxy admin stub", axisFault);
        }
    }

    @Test(groups = {"wso2.csg"},
            description = "Test nhttp transport together with message relay ")
    public void testNhttpTransportWithMR() throws RemoteException, ProxyServiceAdminProxyAdminException {

        // first deploy a proxy
        // then send a message to proxy
        // check the response to see if that what we need
        // this will cover a message path client->nhttp transport(with MR)
        // ->receive(via MR)

        String responseString = "<ns:getQuote xmlns:ns=\"http://services.samples\"><ns:request>" +
                "<ns:symbol>" + QUOTE_STRING + "</ns:symbol></ns:request></ns:getQuote>";

        ProxyData proxyData = new ProxyData();
        proxyData.setName(CSG_SERVICE_NAME);
        proxyData.setInSeqXML("<inSequence xmlns=\"" + SynapseConstants.SYNAPSE_NAMESPACE + "\">" +
                "                <log level=\"full\"/>" +
                "                <header name=\"To\" action=\"remove\"/>" +
                "                <property name=\"RESPONSE\" value=\"true\"/>" +
                "                <send/>" +
                "            </inSequence>");

        try {
            proxyServiceAdminStub.addProxy(proxyData);
        } catch (Exception e) {
            fail("Deploying proxy failed!. " + e.getMessage(), e);
        }

        assertNotNull(proxyServiceAdminStub.getProxy(CSG_SERVICE_NAME));
        OMElement response = csgServiceClient.sendSimpleStockQuoteRequest(
                getProxyServiceURL(CSG_SERVICE_NAME, false), null, QUOTE_STRING);

        assertEquals(response.toString(), responseString);
        // finally delete the proxy
        proxyServiceAdminStub.deleteProxyService(CSG_SERVICE_NAME);
    }

    @Test(groups = {"wso2.csg"},
            description = "Test operations on the CSG in VM Thrift server operations(login, exchange" +
                    " etc..")
    public void testThriftServerOperations() throws Exception {
        String trustStorePath = FrameworkSettings.TEST_FRAMEWORK_HOME + File.separator +
                "repository" + File.separator + "resources" + File.separator +
                "security" + File.separator + "client-truststore.jks";

        // don't pass "localhost" as the host name since the thrift client can't connect
        // it seems the thrift server binds into the ip address
        CSGThriftClient client = new CSGThriftClient(CSGUtils.getCSGThriftClient(
                NetworkUtils.getLocalHostname(), 15001, 20000, trustStorePath, "wso2carbon"));
        String domainName;
        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            domainName = null;
        } else {
            domainName = FrameworkSettings.TENANT_NAME;
        }
        String queueName = CSGUtils.getCSGEPR(domainName, CSG_SERVER_NAME, CSG_SERVICE_NAME);
        String token = null;
        
        // check login operation
        try {
            token = client.login(CSGConstant.DEFAULT_CSG_USER,
                    CSGConstant.DEFAULT_CSG_USER_PASSWORD, queueName);
        } catch (NotAuthorizedException e) {
            fail("Login operation fails!. " + e.getMessage(), e);
        }

        BlockingQueue<Message> source = new LinkedBlockingQueue<Message>();

        List<Message> requestMsgList = new ArrayList<Message>();
        CSGUtils.moveElements(source, requestMsgList, 10);

        // check exchange operation
        try {
            client.exchange(requestMsgList, 10, token);
        } catch (Exception e) {
            fail("Exchange operation fails!. " + e.getMessage(), e);
        }
    }
}
