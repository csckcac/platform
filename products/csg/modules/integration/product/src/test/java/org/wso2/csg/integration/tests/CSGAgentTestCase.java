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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;

/**
 * This has the tests cases for testing the CSGAgent functionality with CSG server.
 * 1. Deploying and invoking a CSG services for;
 * - SOAP service
 * - REST service
 * - JSON service
 * <p/>
 * 2. Various publishing options
 * - automatic
 * - manual and there functionality
 *
 * 3. If possible we need to check for individual service types
 * - SOAP, REST, JSON (AS)
 * - BRS services
 * - BPS services
 * - DSS services
 * - CEP services
 * - MS services
 */
public class CSGAgentTestCase extends CSGIntegrationTestCase {

    private ProxyServiceAdminStub proxyServiceAdminStub;

    private static final String CSG_SERVICE_NAME = "SimpleStockQuoteService";
    
    protected Log log = LogFactory.getLog(CSGAgentTestCase.class);

    public CSGAgentTestCase(String adminService) {
        super("ProxyServiceAdmin");
    }

    @Override
    protected void init() throws Exception {
        super.init();

    }
}
