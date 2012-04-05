/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.nhttp.test;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.AuthenticateStub;
import org.wso2.carbon.integration.core.FrameworkSettings;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.integration.core.utils.ArtifactReader;
import org.wso2.carbon.integration.core.utils.Axis2Client;
import org.wso2.carbon.mediation.configadmin.stub.ConfigServiceAdminStub;

public class SSLConfigTest extends TestTemplate {
    private static final Log log = LogFactory.getLog(SSLConfigTest.class);
    String clientLog = "";

    @Override
    public void init() {
        log.info("Initializing SSL Config Tests");
        log.debug("SSL Config Tests Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running SSL Config SuccessCase ");

        try {
            AuthenticateStub authenticateStub = new AuthenticateStub();
            ConfigServiceAdminStub configServiceAdminStub = new ConfigServiceAdminStub("https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/ConfigServiceAdmin");
            authenticateStub.authenticateAdminStub(configServiceAdminStub, sessionCookie);
            ArtifactReader artifactReader = new ArtifactReader();

            OMElement omElement = artifactReader.getOMElement(RestUrlPostFixTest.class.getResource("/sslConfig.xml").getPath());

            configServiceAdminStub.updateConfiguration(omElement);

            clientLog = Axis2Client.fireClient("ant stockquote -Dtrpurl=http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/sslProxy");

            System.out.println(clientLog);

            Assert.assertTrue(clientLog.contains("Standard :: Stock price"));
            log.info("Running SSL Config success case");
            log.debug("Running SSL Config success case");
            clientLog = "";
        }

        catch (Exception e) {
            Assert.fail("SSL Config Test Failed : " + e);
            log.error("SSL Config Test Failed : " + e.getMessage());
        }
    }


    @Override
    public void runFailureCase() {
        Assert.assertFalse(clientLog.contains("Standard :: Stock price"));
        log.info("Running SSL Config failure case");
        log.debug("Running SSL Config  failure case");
    }

    @Override
    public void cleanup() {
    }
}
