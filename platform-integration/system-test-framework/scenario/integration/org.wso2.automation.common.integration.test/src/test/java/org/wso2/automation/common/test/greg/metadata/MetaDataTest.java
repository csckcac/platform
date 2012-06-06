package org.wso2.automation.common.test.greg.metadata;/*
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

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceResourceAdmin;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;
import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import static org.testng.Assert.assertTrue;

/**
 * Class will test Meta data related test
 */

public class MetaDataTest {
    public static WsdlManager wsdlManager;
    private ManageEnvironment environment;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, RemoteException, LoginAuthenticationExceptionException {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registryWS, userId);
        wsdlManager = new WsdlManager(governance);

        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
//       endpointManager = new EndpointManager(governance);

    }

    @Test(groups = {"wso2.greg.metadata"}, description = "Testing add wsdl from wsdl URL", priority = 1)
    public void testAddWsdlViaURL() throws GovernanceException {
        String wsdlUrl = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                "system-test-framework/core/org.wso2.automation.platform.core/src/main/resources/" +
                "artifacts/GREG/wsdl/wsdl_with_SigEncr.wsdl";
        boolean isWsdlFound = false;
        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("wsdl_with_SigEncr.wsdl")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        Wsdl wsdl = wsdlManager.newWsdl(wsdlUrl);
        wsdlManager.addWsdl(wsdl);
        wsdlList = wsdlManager.getAllWsdls();

        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("wsdl_with_SigEncr.wsdl")) {
                isWsdlFound = true;
            }
        }

        assertTrue(isWsdlFound, "Wsdl not get added from the governance registry.");
    }

    @Test(groups = {"wso2.greg.metadata"}, description = "Testing add wsdl with wsdl file path", priority = 2)
    public void testAddWsdlViaZip() throws GovernanceException, IOException, ResourceAdminServiceExceptionException {
        String wsdlFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator
                + "artifacts" + File.separator + "GREG" + File.separator + "gregresources" +File.separator
                +"sampleWSDL.zip";
        String registryLocation = "/sample.wsdl";
        URL wsdlURL = new URL("file:///" + wsdlFileLocation);

        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("sample.wsdl")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        DataHandler zipDataHandler = new DataHandler(wsdlURL);
        AdminServiceResourceAdmin adminServiceResourceAdmin =
                new AdminServiceResourceAdmin(environment.getGreg().getBackEndUrl());

        Assert.assertTrue(adminServiceResourceAdmin.addResource
                (environment.getGreg().getSessionCookie(), registryLocation, "application/vnd.wso2.governance-archive",
                        "desc", zipDataHandler), "WSDL.zip Adding failed");

    }
}
