/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.cep.integration.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.TestServerManager;

import java.io.IOException;

/**
 * Prepares the WSO2 CEP for test runs, starts the server, and stops the server after
 * test runs
 */
public class CEPTestServerManager extends TestServerManager {
    private static final Log log = LogFactory.getLog(CEPTestServerManager.class);

    @Override
    @BeforeSuite(timeOut = 180000)
    public String startServer() throws IOException {
        String carbonHome = super.startServer();
        System.setProperty("carbon.home", carbonHome);

        //waiting for Thrift ports
        ClientConnectionUtil.waitForPort(7611);
//        ClientConnectionUtil.waitForPort(7711);

        return carbonHome;
    }

    @Override
    @AfterSuite(timeOut = 180000)
    public void stopServer() throws Exception {
        super.stopServer();
    }

    protected void copyArtifacts(String carbonHome) throws IOException {
        // No artifacts need to be copied
    }
}
