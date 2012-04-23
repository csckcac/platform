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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.cloud.csg.common.thrift.CSGThriftClient;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;
import org.wso2.csg.integration.tests.util.BackendServer;
import org.wso2.csg.integration.tests.util.SampleAxis2Server;
import org.wso2.csg.integration.tests.util.StockQuoteClient;

/**
 * This has the tests cases for testing the CSGAgent functionality with CSG server.
 * 1. Deploying and invoking a CSG services for;
 * - SOAP service
 * - REST service
 * - JSON service
 * - Secure SOAP service
 * - RM enabled SOAP service etc..etc..
 * <p/>
 * 2. Various publishing options
 * - automatic
 * - manual and there functionality
 * <p/>
 * 3. If possible we need to check for individual service types
 * - SOAP, REST, JSON (AS)
 * - BRS services
 * - BPS services
 * - DSS services
 * - CEP services
 * - MS services
 * - ESB proxy service
 */
public class CSGAgentTestCase extends CSGIntegrationTestCase {

    private ProxyServiceAdminStub proxyServiceAdminStub;

    protected BackendServer backendServer;

    private StockQuoteClient csgServiceClient;

    public static final String CSG_SERVER_NAME = "TestServer";

    private CSGThriftClient client;


    private static final String QUOTE_STRING = "CSG";

    private static final String CSG_SERVICE_NAME = "SimpleStockQuoteService";

    protected Log log = LogFactory.getLog(CSGAgentTestCase.class);

    public CSGAgentTestCase(String adminService) {
        super("ProxyServiceAdmin");
    }

    @Override
    protected void init() throws Exception {
        super.init();
        proxyServiceAdminStub = new ProxyServiceAdminStub(getAdminServiceURL());
        authenticate(proxyServiceAdminStub);
        startBackEndAxisServer(new String[]{CSG_SERVICE_NAME});
        csgServiceClient = new StockQuoteClient();
    }

    private void startBackEndAxisServer(String services[]) throws Exception {
        backendServer = new SampleAxis2Server();
        // deploy each service
        for (String s : services) {
            backendServer.deployService(s);
        }
        backendServer.start();
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        try {
            proxyServiceAdminStub.cleanup();
            csgServiceClient.destroy();
            if (backendServer != null && backendServer.isStarted()) {
                backendServer.stop();
            }
        } catch (Exception e) {
            log.error("Error while cleaning up the resources", e);
        }
    }

    @Test(groups = {"wso2.csg"}, description = "A Test to check CSG Agent functionality for a SOAP" +
            " service")
    public void testSOAPTest() throws Exception {

    }
}
