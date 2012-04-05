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
package org.wso2.carbon.cloud.csg.common;

import junit.framework.TestCase;
import org.wso2.carbon.cloud.csg.common.thrift.gen.CSGService;
import org.wso2.carbon.cloud.csg.common.thrift.gen.Message;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class CSGUtilsTest extends TestCase {
    public void testGetCSGThriftClient() throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        URL url = cl.getResource("wso2carbon.jks");
        assertNotNull("KeyStore URL can not be null", url);

        try {
            CSGService.Client client =
                    CSGUtils.getCSGThriftClient("localhost", 15001, 80, url.getPath(), "wso2carbon");
        } catch (Exception e) {
            // there is no server running, so this error is accepted, the test is to make sure
            // the API of the Thrift client
            String expectedException = "Connection refused";
            assertTrue("Expected exception should start with [" + expectedException +
                    "]. But actual exception is [" + e.getCause().getCause().getMessage() + "]", 
                    e.getCause().getCause().getMessage().startsWith(expectedException));
        }
    }

    public void testMoveElements() throws Exception {
        int blockSize = 10;
        Message msg = new Message();
        Message msg1 = new Message();
        Message msg2 = new Message();
        Message msg3 = new Message();

        BlockingQueue<Message> buffer = new LinkedBlockingDeque<Message>();

        buffer.add(msg);
        buffer.add(msg1);
        buffer.add(msg2);
        buffer.add(msg3);

        List<Message> list = new ArrayList<Message>();
        CSGUtils.moveElements(buffer, list, blockSize);

        assertEquals("Some elements are missing", 4, list.size());
        assertEquals("Some elements are still there to drain", 0, buffer.size());
    }

    public void testGetStringProperty() throws Exception {
        String hostName = CSGUtils.getCSGThriftServerHostName();
        assertEquals("The host name is invalid", "rajika.org", hostName);
    }

    public void testIntProperty() throws Exception {
        int port = CSGUtils.getIntProperty(CSGConstant.THRIFT_SERVER_PORT,
                CSGConstant.DEFAULT_PORT);
        assertEquals("The port is invalid", 12321, port);
    }

    public void testStringDefaultProperty() throws Exception {
        int timeOut = CSGUtils.getIntProperty(CSGConstant.CSG_THRIFT_CLIENT_TIMEOUT,
                CSGConstant.DEFAULT_TIMEOUT);
        assertEquals("In valid value of time out", CSGConstant.DEFAULT_TIMEOUT, timeOut);
    }

    public void testLongProperty() throws Exception {
        long timeOut = CSGUtils.getLongProperty(CSGConstant.CSG_SEMAPHORE_TIMEOUT,
                86400L);
        assertEquals("Invalid value of semaphore time out", 23L, timeOut);
    }

    public void testDoubleProperty() throws Exception {
        double factor = CSGUtils.getDoubleProperty(CSGConstant.PROGRESSION_FACTOR, 2.0);
        assertEquals("Invalid value for progression factor", 2.0, factor);
    }

    public void testGetQueueNameFromEPR() throws Exception {
        String url = CSGConstant.CSG_TRANSPORT_PREFIX + "test1@test1.org-SimpleStockQuoteService";
        assertEquals("test1@test1.org-SimpleStockQuoteService", CSGUtils.getQueueNameFromEPR(url));

        String url2 = CSGConstant.CSG_TRANSPORT_PREFIX + "SimpleStockQuoteService";
        assertEquals("SimpleStockQuoteService", CSGUtils.getQueueNameFromEPR(url2));
    }

    public void testGetFullUserName() throws Exception {
        String userName = "rajika";
        String domainName = "rajika.org";
        assertEquals("rajika@rajika.org", CSGUtils.getFullUserName(userName, domainName));
        assertEquals("rajika", CSGUtils.getFullUserName(userName, null));
    }

    public void testGetCSGServiceName() throws Exception {
        String csgServiceName = CSGUtils.getCSGServiceName("HelloService", "rajika");
        assertEquals("Invalid CSG service name", "rajika-HelloService", csgServiceName);

        csgServiceName = CSGUtils.getCSGServiceName("HelloService", "rajika@rajika.org");
        assertEquals("Invalid CSG service name", "rajika@rajika.org-HelloService", csgServiceName);
    }

    public void testGetCSGEPR() throws Exception {
        String csgServiceName = CSGUtils.getCSGEPR("rajika.org", "test-server", "HelloService");
        assertEquals("Invalid CSG service name", "csg://rajika.org/test-server/HelloService",
                csgServiceName);

        csgServiceName = CSGUtils.getCSGEPR(null, "test-server", "HelloService");
        assertEquals("Invalid CSG service name", "csg://test-server/HelloService", csgServiceName);
    }

    public void testGetPortFromServerURL() throws Exception {
        String serverURL = "https://localhost:9443";
        String port = CSGUtils.getPortFromServerURL(serverURL);
        assertEquals("Invalid port", "9443", port);

        serverURL = "https://localhost:9443/";
        port = CSGUtils.getPortFromServerURL(serverURL);
        assertEquals("Invalid port", "9443", port);
    }

    public void testGetHostFromServerURL() throws Exception {
        String serverURL = "https://localhost:9443";
        String host = CSGUtils.getHostFromServerURL(serverURL);
        assertEquals("Invalid host name", "localhost", host);

        serverURL = "https://localhost:9443/";
        host = CSGUtils.getHostFromServerURL(serverURL);
        assertEquals("Invalid host name", "localhost", host);
    }

    public void testGetUserNameFromTenantUserNameL() throws Exception {
        String userName = CSGUtils.getUserNameFromTenantUserName("admin@mydomain.org");
        assertEquals("Invalid user name", "admin", userName);

        userName = CSGUtils.getUserNameFromTenantUserName("admin");
        assertEquals("Invalid user name", "admin", userName);
    }

    public void testGetDomainNameFromTenantUserName() throws Exception {
        String domainName = CSGUtils.getDomainNameFromTenantUserName("admin@mydomain.org");
        assertEquals("Invalid domain name", "mydomain.org", domainName);

        domainName = CSGUtils.getDomainNameFromTenantUserName("admin");
        assertNull("Cloud not have any non NULL value", domainName);
    }

    public void testgetTryItURLFromWSDLURL() throws Exception {
        String wsdl = "http://localhost:8280/services/SimpleStockQuoteService?wsdl";
        String tryit = CSGUtils.getTryItURLFromWSDLURL(wsdl);
        assertEquals("Invalid try it URL",
                "http://localhost:8280/services/SimpleStockQuoteService?tryit", tryit);
    }
}
