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

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.AuthenticateStub;
import org.wso2.carbon.integration.core.FrameworkSettings;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.core.utils.ArtifactReader;
import org.wso2.carbon.integration.core.utils.StockQuoteClient;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/*POST request to an endpoint which returns the response with content type application/x-www-form-urlencoded*/

public class HeaderProtocol_10Test extends TestTemplate {
    private static final Log log = LogFactory.getLog(HeaderProtocol_10Test.class);

    @Override
    public void init() {
        log.info("Initializing Header Protocol 10 with POST Tests");
        log.debug("Header Protocol 10 with POST Tests Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running HeaderProtocol 10 with POST SuccessCase");
        StockQuoteClient stockQuoteClient = new StockQuoteClient();

        try {
            AuthenticateStub authenticateStub = new AuthenticateStub();
            ConfigServiceAdminStub configServiceAdminStub = new ConfigServiceAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ConfigServiceAdmin");
            authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
            ArtifactReader artifactReader = new ArtifactReader();

            OMElement omElement = artifactReader.getOMElement(HeaderProtocol_10Test.class.getResource("/with_content_type_post.xml").getPath());

            configServiceAdminStub.updateConfiguration(omElement);

            String trpUrl = "http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT;
            OMElement result = stockQuoteClient.stockQuoteClientForHeaderProtocol10(trpUrl, null, "IBM");
            log.info(result);
            System.out.println(result);

            // Test for HeaderProtocol 10 with POST request

            URL url = new URL(trpUrl + "/services");
            String contentType = ((HttpURLConnection) url.openConnection())
                    .getContentType();
            System.out.println("********** " + contentType + "********** ");

            if (!contentType.contains("text/html")) {

                Assert.fail("Test Failed for HeaderProtocol10 with POST");
                log.error("Test Failed for HeaderProtocol10 with POST");
            }

        }
        catch (MalformedURLException e1) {
            log.error("Test Failed for HeaderProtocol10 with POST : " + e1.getMessage());


        }
        catch (Exception e) {
            log.error("Test Failed for HeaderProtocol10 with POST : " + e.getMessage());

        }

    }


    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {

    }
}
