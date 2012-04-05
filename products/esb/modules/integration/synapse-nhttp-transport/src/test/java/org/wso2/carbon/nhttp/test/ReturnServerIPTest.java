package org.wso2.carbon.nhttp.test;

/*  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.

  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.AuthenticateStub;
import org.wso2.carbon.integration.core.FrameworkSettings;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.core.utils.ArtifactReader;
import org.wso2.carbon.integration.core.utils.StockQuoteClient;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;


/*checking for get-property('SERVER_IP') test returning whether the correct host ip*/

/*uncomment and add the server IP for the following line in the axis2.xml file, "<parameter name="bind-address" locked="false">127.0.0.1</parameter>,
the same value should be displayed in the log to pass this test"*/

public class ReturnServerIPTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(ReturnServerIPTest.class);

    @Override
    public void init() {
        log.info("Initializing Server IP Message Relay Tests");
        log.debug("Server IP Message Relay Tests Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running Server IP Message Relay SuccessCase ");
        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        try {
            AuthenticateStub authenticateStub = new AuthenticateStub();
            ConfigServiceAdminStub configServiceAdminStub = new ConfigServiceAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ConfigServiceAdmin");
            authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
            ArtifactReader artifactReader = new ArtifactReader();

            OMElement omElement = artifactReader.getOMElement(RestUrlPostFixTest.class.getResource("/serverIP.xml").getPath());

            configServiceAdminStub.updateConfiguration(omElement);

            String trpUrl = "http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT;
            OMElement result = stockQuoteClient.stockQuoteClientforProxy(trpUrl, null, "IBM");
            log.info(result);
            System.out.println(result);

            System.out.println(result);

        }

        catch (Exception e) {
            log.error("Message Relay for Server IP Test doesn't work : " + e.getMessage());

        }

    }


    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {

    }
}
