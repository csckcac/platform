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
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.activation.DataHandler;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * Class will test Meta data related test
 */

public class AddWsdlMetaDataTest {
    public static WsdlManager wsdlManager;
    public static SchemaManager schemaManager;
    public static PolicyManager policyManager;
    private ManageEnvironment environment;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, RemoteException, LoginAuthenticationExceptionException {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registryWS, userId);
        wsdlManager = new WsdlManager(governance);
        schemaManager = new SchemaManager(governance);
        policyManager = new PolicyManager(governance);
        schemaManager = new SchemaManager(governance);
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        environment = builder.build();
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

    @Test(groups = {"wso2.greg.metadata"}, description = "Testing add wsdl zip from file path", priority = 2)
    public void testAddWsdlViaZip() throws GovernanceException, IOException, ResourceAdminServiceExceptionException {
        String wsdlFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator
                + "artifacts" + File.separator + "GREG" + File.separator + "gregresources" + File.separator
                + "sampleWSDL.zip";
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

    @Test(groups = {"wso2.greg.metadata"}, description = "Testing add wsdl from file path", priority = 3)
    public void testAddWsdlViaFilePath() throws GovernanceException, MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {

        String wsdlFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator
                + "artifacts" + File.separator + "GREG" + File.separator + "wsdl" + File.separator
                + "echo.wsdl";

        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("echo.wsdl")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        String registryLocation = "/echo.wsdl";
        URL wsdlURL = new URL("file:///" + wsdlFileLocation);
        DataHandler wsdlDataHandler = new DataHandler(wsdlURL);
        AdminServiceResourceAdmin adminServiceResourceAdmin =
                new AdminServiceResourceAdmin(environment.getGreg().getBackEndUrl());

        Assert.assertTrue(adminServiceResourceAdmin.addResource
                (environment.getGreg().getSessionCookie(), registryLocation, "application/wsdl+xml",
                        "desc", wsdlDataHandler), "WSDL Adding failed");

    }

    @Test(groups = {"wso2.greg.metadata"}, description = "Testing add wsdl which has policy import via " +
            "wsdl URL", priority = 4)
    public void testAddWsdlWithPolicyViaURL() throws GovernanceException {
        String wsdlUrl = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                "system-test-framework/core/org.wso2.automation.platform.core/src/main/resources/" +
                "artifacts/GREG/wsdl/wsdl_with_SigEncr.wsdl";

        boolean isWsdlFound = false;
        boolean isPolicyFound = false;

        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        Policy[] policyList = policyManager.getAllPolicies();

        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("wsdl_with_SigEncr")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        for (Policy p : policyList) {
            if (p.getQName().getLocalPart().equalsIgnoreCase("SgnEncrAnonymous.xml")) {
                policyManager.removePolicy(p.getId());
            }
        }

        Wsdl wsdl = wsdlManager.newWsdl(wsdlUrl);
        wsdlManager.addWsdl(wsdl);

        wsdlList = wsdlManager.getAllWsdls();
        policyList = policyManager.getAllPolicies();

        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().equalsIgnoreCase("wsdl_with_SigEncr.wsdl")) {
                isWsdlFound = true;
            }
        }

        for (Policy p : policyList) {
            if (p.getQName().getLocalPart().equalsIgnoreCase("SgnEncrAnonymous.xml")) {
                isPolicyFound = true;
            }
        }

        assertTrue(isWsdlFound, "WSDL not added from governance registry");
        assertTrue(isPolicyFound, "Imported policy with WSDL not added from governance registry");
    }

    @Test(groups = {"wso2.greg.metadata"}, description = "Testing add wsdl which has policy import via " +
            "wsdl file path", priority = 5)
    public void testAddWsdlWithPolicyViaFilePath() throws GovernanceException, MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {
        String wsdlFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator
                + "artifacts" + File.separator + "GREG" + File.separator + "wsdl" + File.separator
                + "wsdl_with_SigEncr.wsdl";
        String registryLocation = "/wsdl_with_SigEncr.wsdl";
        URL wsdlURL = new URL("file:///" + wsdlFileLocation);

        boolean isWsdlFound = false;
        boolean isPolicyFound = false;

        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        Policy[] policyList = policyManager.getAllPolicies();

        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("wsdl_with_SigEncr")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        for (Policy p : policyList) {
            if (p.getQName().getLocalPart().equalsIgnoreCase("SgnEncrAnonymous.xml")) {
                policyManager.removePolicy(p.getId());
            }
        }

        DataHandler wsdlDataHandler = new DataHandler(wsdlURL);
        AdminServiceResourceAdmin adminServiceResourceAdmin =
                new AdminServiceResourceAdmin(environment.getGreg().getBackEndUrl());

        adminServiceResourceAdmin.addResource(environment.getGreg().getSessionCookie(), registryLocation,
                "application/wsdl+xml", "desc", wsdlDataHandler);

        wsdlList = wsdlManager.getAllWsdls();
        policyList = policyManager.getAllPolicies();

        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().equalsIgnoreCase("wsdl_with_SigEncr.wsdl")) {
                isWsdlFound = true;
            }
        }

        for (Policy p : policyList) {
            if (p.getQName().getLocalPart().equalsIgnoreCase("SgnEncrAnonymous.xml")) {
                isPolicyFound = true;
            }
        }

        assertTrue(isWsdlFound, "WSDL not added");
        assertTrue(isPolicyFound, "Imported policy not added");
    }


    @Test(groups = {"wso2.greg.metadata"}, description = "Add wsdl which has policy and schema import via " +
            "wsdl file path", priority = 6)
    public void testAddWsdlWithPolicyAndSchemaViaFilePath() throws GovernanceException, MalformedURLException,
            RemoteException, ResourceAdminServiceExceptionException {
        String wsdlFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator
                + "artifacts" + File.separator + "GREG" + File.separator + "wsdl" + File.separator
                + "wsdl_with_EncrOnlyAnonymous.wsdl";
        String registryLocation = "/wsdl_with_EncrOnlyAnonymous.wsdl";
        URL wsdlURL = new URL("file:///" + wsdlFileLocation);

        boolean isWsdlFound = false;
        boolean isPolicyFound = false;
        boolean isSchemaFound = false;

        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        Policy[] policyList = policyManager.getAllPolicies();
        Schema[] schemaList = schemaManager.getAllSchemas();

        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("wsdl_with_EncrOnlyAnonymous.wsdl")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        for (Policy p : policyList) {
            if (p.getQName().getLocalPart().equalsIgnoreCase("EncrOnlyAnonymous.xml")) {
                policyManager.removePolicy(p.getId());
            }
        }

        for (Schema s : schemaList) {
            if (s.getQName().getLocalPart().equalsIgnoreCase("SampleSchema.xsd")) {
                policyManager.removePolicy(s.getId());
            }
        }

        DataHandler wsdlDataHandler = new DataHandler(wsdlURL);
        AdminServiceResourceAdmin adminServiceResourceAdmin =
                new AdminServiceResourceAdmin(environment.getGreg().getBackEndUrl());

        adminServiceResourceAdmin.addResource(environment.getGreg().getSessionCookie(), registryLocation,
                "application/wsdl+xml", "desc", wsdlDataHandler);

        wsdlList = wsdlManager.getAllWsdls();
        policyList = policyManager.getAllPolicies();
        schemaList = schemaManager.getAllSchemas();

        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().equalsIgnoreCase("wsdl_with_EncrOnlyAnonymous.wsdl")) {
                isWsdlFound = true;
            }
        }

        for (Policy p : policyList) {
            if (p.getQName().getLocalPart().equalsIgnoreCase("EncrOnlyAnonymous.xml")) {
                isPolicyFound = true;
            }
        }

        for (Schema s : schemaList) {
            if (s.getQName().getLocalPart().equalsIgnoreCase("SampleSchema.xsd")) {
                isSchemaFound = true;
            }
        }

        assertTrue(isWsdlFound, "WSDL not found : wsdl_with_EncrOnlyAnonymous.wsdl");
        assertTrue(isPolicyFound, "Imported policy not found : EncrOnlyAnonymous.xml");
        assertTrue(isSchemaFound, "Imported schema not found : SampleSchema.xsd");
    }


    @Test(groups = {"wso2.greg.metadata"}, description = "Add wsdl which has policy and schema import via " +
            "wsdl URL", priority = 7)
    public void testAddWsdlWithPolicyAndSchemaViaUrl() throws GovernanceException, MalformedURLException,
            RemoteException, ResourceAdminServiceExceptionException {
        String wsdlURL = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                "system-test-framework/core/org.wso2.automation.platform.core/src/main/resources/artifacts/" +
                "GREG/wsdl/wsdl_with_EncrOnlyAnonymous.wsdl";

        boolean isWsdlFound = false;
        boolean isPolicyFound = false;
        boolean isSchemaFound = false;

        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        Policy[] policyList = policyManager.getAllPolicies();
        Schema[] schemaList = schemaManager.getAllSchemas();

        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("wsdl_with_EncrOnlyAnonymous.wsdl")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        for (Policy p : policyList) {
            if (p.getQName().getLocalPart().equalsIgnoreCase("EncrOnlyAnonymous.xml")) {
                policyManager.removePolicy(p.getId());
            }
        }

        for (Schema s : schemaList) {
            if (s.getQName().getLocalPart().equalsIgnoreCase("SampleSchema.xsd")) {
                policyManager.removePolicy(s.getId());
            }
        }

        Wsdl wsdl = wsdlManager.newWsdl(wsdlURL);
        wsdlManager.addWsdl(wsdl);

        wsdlList = wsdlManager.getAllWsdls();
        policyList = policyManager.getAllPolicies();
        schemaList = schemaManager.getAllSchemas();

        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().equalsIgnoreCase("wsdl_with_EncrOnlyAnonymous.wsdl")) {
                isWsdlFound = true;
            }
        }

        for (Policy p : policyList) {
            if (p.getQName().getLocalPart().equalsIgnoreCase("EncrOnlyAnonymous.xml")) {
                isPolicyFound = true;
            }
        }

        for (Schema s : schemaList) {
            if (s.getQName().getLocalPart().equalsIgnoreCase("SampleSchema.xsd")) {
                isSchemaFound = true;
            }
        }

        assertTrue(isWsdlFound, "WSDL not found : wsdl_with_EncrOnlyAnonymous.wsdl");
        assertTrue(isPolicyFound, "Imported policy not found : EncrOnlyAnonymous.xml");
        assertTrue(isSchemaFound, "Imported schema not found : SampleSchema.xsd");
    }


    @Test(groups = {"wso2.greg.metadata"}, description = "Add wsdl which has wsdl import via " +
            "wsdl URL", priority = 8)
    public void testAddWsdlWithWsdlImportViaUrl() throws GovernanceException {
        boolean isMainWsdlFound = false;
        boolean isImportWsdlFound = false;

        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("Axis2Service_Wsdl_With_Wsdl_Imports.wsdl")) {
                wsdlManager.removeWsdl(w.getId());
            }
            if (w.getQName().getLocalPart().contains("Axis2ImportedWsdl.wsdl")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        String wsdlUrl = "https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration/" +
                "system-test-framework/core/org.wso2.automation.platform.core/src/main/resources/" +
                "artifacts/GREG/wsdl/Axis2Service_Wsdl_With_Wsdl_Imports.wsdl";

        Wsdl wsdl = wsdlManager.newWsdl(wsdlUrl);
        wsdlManager.addWsdl(wsdl);

        wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("Axis2Service_Wsdl_With_Wsdl_Imports.wsdl")) {
                isMainWsdlFound = true;
            }
            if (w.getQName().getLocalPart().contains("Axis2ImportedWsdl.wsdl")) {
                isImportWsdlFound = true;
            }
        }
        assertTrue(isMainWsdlFound, "WSDL not found : Axis2Service_Wsdl_With_Wsdl_Imports.wsdl");
        assertTrue(isImportWsdlFound, "Imported wsdl not found : Axis2ImportedWsdl.wsdl");
    }

    @Test(groups = {"wso2.greg.metadata"}, description = "Add wsdl which has wsdl import via " +
            "wsdl File Path", priority = 9)
    public void testAddWsdlWithWsdlImportViaFilePath() throws GovernanceException, MalformedURLException, RemoteException, ResourceAdminServiceExceptionException {

        boolean isMainWsdlFound = false;
        boolean isImportWsdlFound = false;

        String wsdlFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator
                + "artifacts" + File.separator + "GREG" + File.separator + "wsdl" + File.separator
                + "Axis2Service_Wsdl_With_Wsdl_Imports.wsdl";
        String registryLocation = "/Axis2Service_Wsdl_With_Wsdl_Imports.wsdl";
        URL wsdlURL = new URL("file:///" + wsdlFileLocation);

        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("Axis2Service_Wsdl_With_Wsdl_Imports.wsdl")) {
                wsdlManager.removeWsdl(w.getId());
            }
            if (w.getQName().getLocalPart().contains("Axis2ImportedWsdl.wsdl")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        DataHandler wsdlDataHandler = new DataHandler(wsdlURL);
        AdminServiceResourceAdmin adminServiceResourceAdmin =
                new AdminServiceResourceAdmin(environment.getGreg().getBackEndUrl());

        adminServiceResourceAdmin.addResource(environment.getGreg().getSessionCookie(), registryLocation,
                "application/wsdl+xml", "desc", wsdlDataHandler);

        wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("Axis2Service_Wsdl_With_Wsdl_Imports.wsdl")) {
                isMainWsdlFound = true;
            }
            if (w.getQName().getLocalPart().contains("Axis2ImportedWsdl.wsdl")) {
                isImportWsdlFound = true;
            }
        }
        assertTrue(isMainWsdlFound, "WSDL not found : Axis2Service_Wsdl_With_Wsdl_Imports.wsdl");
        assertTrue(isImportWsdlFound, "Imported wsdl not found : Axis2ImportedWsdl.wsdl");
    }

    @Test(groups = {"wso2.greg.metadata"}, description = "Add wsdl which has $ in wsdl name", priority = 10)
    public void testSpecialWsdlName() throws GovernanceException, IOException {
        boolean isWsdl1Found = false;
        boolean isWsdl2Found = false;
        boolean isWsdl3Found = false;
        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("SpecialWsdlName")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }

        String wsdlFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator
                + "artifacts" + File.separator + "GREG" + File.separator + "wsdl" + File.separator + "echo.wsdl";
        try {
            Wsdl wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFileLocation).getBytes(),
                    "SpecialWsdlName$.wsdl");
            wsdlManager.addWsdl(wsdl);

            wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFileLocation).getBytes(),
                    "$SpecialWsdlName.wsdl");
            wsdlManager.addWsdl(wsdl);

            wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFileLocation).getBytes(),
                    "Special$WsdlName.wsdl");
            wsdlManager.addWsdl(wsdl);

        } catch (GovernanceException e) {
            throw new GovernanceException("Exception occurred while adding wsdl which wsdl name has " +
                    "special character $ : " + e.getMessage());

        }
        wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().equalsIgnoreCase("SpecialWsdlName$.wsdl")) {
                isWsdl1Found = true;
            }
            if (w.getQName().getLocalPart().equalsIgnoreCase("$SpecialWsdlName.wsdl")) {
                isWsdl2Found = true;
            }
            if (w.getQName().getLocalPart().equalsIgnoreCase("Special$WsdlName.wsdl")) {
                isWsdl3Found = true;
            }
        }
        assertTrue(isWsdl1Found, "WSDL not found which has special character $ : SpecialWsdlName$.wsdl");
        assertTrue(isWsdl2Found, "WSDL not found which has special character $ : $SpecialWsdlName.wsdl");
        assertTrue(isWsdl3Found, "WSDL not found which has special character $ : Special$WsdlName.wsdl");

    }

}
