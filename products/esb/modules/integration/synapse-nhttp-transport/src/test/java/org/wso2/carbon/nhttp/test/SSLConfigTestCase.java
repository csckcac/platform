/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.nhttp.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.core.utils.Axis2Client;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.esb.integration.ESBIntegrationTestCase;

import java.rmi.RemoteException;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class SSLConfigTestCase extends ESBIntegrationTestCase{

    private static final Log log = LogFactory.getLog(SSLConfigTestCase.class);


    @BeforeMethod(groups = "wso2.esb")
    public void init() throws Exception {
    }

    @Test(groups = "wso2.esb")
    public void testSSLConfig() throws RemoteException {

        loadESBConfigurationFromClasspath("/sslConfig.xml");
        
        String clientLog = Axis2Client.fireClient("ant stockquote -Dtrpurl=http://" +
                FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/sslProxy");

        assertNotNull(clientLog);
        assertTrue(clientLog.contains("Standard :: Stock price"));
    }

    @AfterMethod(groups = "wso2.esb")
    public void close() throws Exception {
    }
    
}
