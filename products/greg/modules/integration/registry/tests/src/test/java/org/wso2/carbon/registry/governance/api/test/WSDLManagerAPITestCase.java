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

package org.wso2.carbon.registry.governance.api.test;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.endpoints.EndpointManager;
import org.wso2.carbon.governance.api.endpoints.dataobjects.Endpoint;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.WsdlFilter;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.governance.api.test.util.FileManagerUtil;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * Class will test all API methods of WSDL manager
 */
public class WSDLManagerAPITestCase {

    public static WsdlManager wsdlManager;
    public static EndpointManager endpointManager;
    public static SchemaManager schemaManager;
    private static Wsdl wsdlObj;
    private static Wsdl[] wsdlArray;
    public String sampleWsdlURL = "http://ws.strikeiron.com/donotcall2_5?WSDL";
    private String wsdlName = "donotcall2_5.wsdl";
    private static String resourcePath;

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        Registry governance = TestUtils.getRegistry();
        TestUtils.cleanupResources(governance);
        TestUtils.cleanupResources(governance);
        wsdlManager = new WsdlManager(governance);
        endpointManager = new EndpointManager(governance);
        schemaManager = new SchemaManager(governance);
        resourcePath = FrameworkSettings.getFrameworkPath() + File.separator + ".." + File.separator + ".." + File.separator + ".."
                       + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                       + "resources";
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing newWsdl API method")
    public void testNewWsdl() throws GovernanceException {
        try {
            wsdlObj = wsdlManager.newWsdl(sampleWsdlURL);
            cleanWSDL();
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:newWsdl method" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testNewWsdl"}, description = "Testing " +
                                                                                        "addWsdl API method")
    public void testAddWsdl() throws GovernanceException {
        try {

            wsdlManager.addWsdl(wsdlObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:addWsdl method" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddWsdl"}, description = "Testing " +
                                                                                        "getAllWsdls API method")
    public void testGetAllWsdl() throws GovernanceException {
        boolean isWsdlFound = false;
        try {
            wsdlArray = wsdlManager.getAllWsdls();
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:addWsdl method" + e.getMessage());
        }
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                isWsdlFound = true;
            }
        }
        assertTrue(isWsdlFound, "Return object of getAllWsdls" +
                                " method doesn't have all information ");
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddWsdl"}, description = "Testing " +
                                                                                        "getWsdl API method")
    public void testGetWsdl() throws GovernanceException {
        Wsdl localWsdlObj = null;
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                try {
                    localWsdlObj = wsdlManager.getWsdl(w.getId());

                } catch (GovernanceException e) {
                    throw new GovernanceException("Error occurred while executing WsdlManager:getWsdl method" + e.getMessage());
                }
            }
        }
        if (localWsdlObj != null) {
            assertTrue(localWsdlObj.getQName().getLocalPart().equalsIgnoreCase(wsdlName), "getWsdl method doesn't work");
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddWsdl"}, description = "Testing " +
                                                                                        "getWsdl API method")
    public void testUpdateWsdl() throws GovernanceException {
        String lcName = "ServiceLifeCycle";
        boolean isLCFound = false;
        try {
            wsdlObj.attachLifecycle(lcName);
            wsdlManager.updateWsdl(wsdlObj);
            wsdlArray = wsdlManager.getAllWsdls();
            for (Wsdl w : wsdlArray) {
                if (w.getLifecycleName().equalsIgnoreCase(lcName)) {
                    isLCFound = true;
                }
            }
            assertTrue(isLCFound, "Error occurred while executing WsdlManager:updateWsdl method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:updateWsdl method" + e.getMessage());
        }

    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing FindWSDL", dependsOnMethods = "testUpdateWsdl")
    public void testFindService() throws GovernanceException {
        try {
            Wsdl[] wsdlArray = wsdlManager.findWsdls(new WsdlFilter() {
                public boolean matches(Wsdl wsdl) throws GovernanceException {
                    String name = wsdl.getQName().getLocalPart();
                    assertTrue(name.contains(wsdlName), "Error occured while executing findWSDL API method");
                    return name.contains(wsdlName);
                }
            });
            assertTrue(wsdlArray.length > 0, "Error occured while executing findWSDL API method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:findWsdls method" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing AddWSDL with Inline content",
          dependsOnMethods = "testFindService")
    public void testAddWSDLContentWithName() throws GovernanceException, IOException {
        cleanWSDL();
        String wsdlFileLocation = resourcePath + File.separator + "wsdl" + File.separator + "Automated.wsdl";
        try {
            Wsdl wsdl = wsdlManager.newWsdl(FileManagerUtil.readFile(wsdlFileLocation).getBytes(), "AutomatedSample.wsdl");
            wsdlManager.addWsdl(wsdl);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:newWsdl method " +
                                          "which have Inline wsdl content and wsdl Name" + e.getMessage());
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing AddWSDL with Inline content",
          dependsOnMethods = "testAddWSDLContentWithName")
    public void testAddWSDLContentWithoutName() throws GovernanceException, IOException {
        cleanWSDL();
        String wsdlFileLocation = resourcePath + File.separator + "wsdl" + File.separator + "Automated.wsdl";
        try {
            boolean isWSDLFound = false;
            Wsdl wsdl = wsdlManager.newWsdl(FileManagerUtil.readFile(wsdlFileLocation).getBytes());
            wsdlManager.addWsdl(wsdl);
            Wsdl[] wsdlArray = wsdlManager.getAllWsdls();
            for (Wsdl w : wsdlArray) {
                if (w.getQName().getNamespaceURI().equalsIgnoreCase("http://www.strikeiron.com")) {
                    isWSDLFound = true;
                }
            }
            assertTrue(isWSDLFound, "WsdlManager:newWsdl method doesn't not execute with inline wsdl content");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:newWsdl method " +
                                          "which have Inline wsdl content" + e.getMessage());
        } catch (RegistryException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing RemoveWSDL",
          dependsOnMethods = "testAddWSDLContentWithoutName")
    public void testRemoveWSDL() throws GovernanceException {
        try {
            cleanWSDL();
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:removeWsdl method " +
                                          ":" + e.getMessage());
        }
    }

    private void cleanWSDL() throws GovernanceException {
        wsdlArray = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlArray) {
            if (w.getQName().getNamespaceURI().contains("www.strikeiron.com")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }
    }

// WSDL data objects tests......

    @Test(groups = {"wso2.greg.api"}, description = "Testing getQName method in WSDL object",
          dependsOnMethods = "testRemoveWSDL")
    public void testGetQName() throws GovernanceException {
        try {
            wsdlObj = wsdlManager.newWsdl(sampleWsdlURL);
            wsdlManager.addWsdl(wsdlObj);

            wsdlObj = wsdlManager.getWsdl(wsdlObj.getId());
            assertTrue(wsdlObj.getQName().getLocalPart().equalsIgnoreCase(wsdlName), "WSDL:getQName API method thrown error");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WSDL:getQName method " +
                                          ":" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing getUrl method in WSDL object",
          dependsOnMethods = "testGetQName")
    public void testGetURL() throws GovernanceException {
        try {
            wsdlObj = wsdlManager.newWsdl(sampleWsdlURL);
            wsdlManager.addWsdl(wsdlObj);
            assertTrue(wsdlObj.getUrl().equalsIgnoreCase(sampleWsdlURL), "WSDL:getQName API method thrown error");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WSDL:getQName method " +
                                          ":" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing attachEndpoint method in WSDL object",
          enabled = true)
    public void testAttachEndpoint() throws GovernanceException {
        Endpoint endpoint = endpointManager.newEndpoint("http://localhost:9763/services/TestEndPointManager");
        endpointManager.addEndpoint(endpoint);
        try {
            wsdlObj.attachEndpoint(endpoint);
        } catch (GovernanceException e) {
            throw new GovernanceException("WSDL:attachEndpoint method throwing an error : " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing getAttachEndpoint method in WSDL object",
          priority = 13, enabled = true)
    public void testGetAttachEndpoint() throws GovernanceException {
        boolean isEndpointFound = false;
        try {
            Endpoint[] endpoints = wsdlObj.getAttachedEndpoints();
            for (Endpoint e : endpoints) {
                if (e.getUrl().equalsIgnoreCase("http://localhost:9763/services/TestEndPointManager")) {
                    isEndpointFound = true;
                }
            }
            assertTrue(isEndpointFound, "WSDL:getAttachEndpoint throwing an error");
        } catch (GovernanceException e) {
            throw new GovernanceException("WSDL:getAttachEndpoint method throwing an error : " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing attachSchema method in WSDL object",
          priority = 13, enabled = true)
    public void testAttachSchema() throws GovernanceException {
        Schema schema = schemaManager.newSchema("http://svn.wso2.org/repos/wso2/carbon/platform/trunk" +
                                                "/products/greg/modules/integration/registry/tests/src/" +
                                                "test/java/resources/schema/calculator.xsd");
        schemaManager.addSchema(schema);
        try {
            wsdlObj.attachSchema(schema);
        } catch (GovernanceException e) {
            throw new GovernanceException("WSDL:attachSchema method throwing an error : " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing GetAttachSchema method in WSDL object",
          priority = 14, enabled = true)
    public void testGetAttachSchema() throws GovernanceException {
        boolean isSchemaFound = false;
        try {
            Schema[] schema = wsdlObj.getAttachedSchemas();
            for (Schema s : schema) {
                if (s.getQName().getLocalPart().equalsIgnoreCase("calculator.xsd")) {
                    isSchemaFound = true;
                }
            }
            assertTrue(isSchemaFound, "Error occurred while executing getAttachedSchemas API method with WSDL object.");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing getAttachedSchemas API " +
                                          "method with WSDL object" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing GetAttachSchema method in WSDL object",
          priority = 15, enabled = true)
    public void testDetachSchema() throws GovernanceException {
        try {
            Schema[] schema = wsdlObj.getAttachedSchemas();
            for (Schema s : schema) {
                if (s.getQName().getLocalPart().equalsIgnoreCase("calculator.xsd")) {
                    wsdlObj.detachSchema(s.getId());
                }
            }
            schema = wsdlObj.getAttachedSchemas();
            for (Schema s : schema) {
                if (s.getQName().getLocalPart().equalsIgnoreCase("calculator.xsd")) {
                    assertTrue(false, "detachSchema method didn't work with WSDL object");
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing detachSchema API " +
                                          "method with WSDL object" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing getWsdlElement method in WSDL object",
          dependsOnMethods = "testGetURL")
    public void testGetWsdlElement() throws GovernanceException {
        try {
            Wsdl[] allWSDLs = wsdlManager.getAllWsdls();
            for (Wsdl w : allWSDLs) {
                if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                    OMElement omElement = w.getWsdlElement();
                    assertTrue(omElement.toString().contains("Do Not Call List Service"),
                               "Error occurred while executing getWsdlElement API method with WSDL object");
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing getWsdlElement API " +
                                          "method with WSDL object" + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing getWsdlElement method in WSDL object",
          dependsOnMethods = "testGetWsdlElement")
    public void testLoadWSDLDetails() throws GovernanceException {
        Wsdl[] allWSDLs = wsdlManager.getAllWsdls();
        try {
            for (Wsdl w : allWSDLs) {
                if (w.getQName().getLocalPart().equalsIgnoreCase(wsdlName)) {
                    w.loadWsdlDetails();
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing loadWsdlDetails API method with WSDL object" + e.getMessage());
        }
    }

    @AfterClass
    public void cleanTestArtifacts() throws GovernanceException {
        cleanWSDL();
    }


}
