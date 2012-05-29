package org.wso2.automation.common.test.greg.governance;/*
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

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaFilter;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import static org.testng.Assert.assertTrue;

/**
 * This class will test Schema Manager related Governance API methods
 */

public class SchemaManagerAPITest {
    public static SchemaManager schemaManager;
    public static Schema schemaObj;
    public static Schema[] schemaArray;
    public String schemaName = "calculator.xsd";

    @BeforeClass(alwaysRun = true)
    public void initializeAPIObject() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registryWS, userId);
        schemaManager = new SchemaManager(governance);
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing newSchema API method", priority = 1)
    public void testNewSchemaUrl() throws GovernanceException {
        try {
            schemaObj = schemaManager.newSchema("http://svn.wso2.org/repos/wso2/carbon" +
                    "/platform/trunk/platform-integration/system-test-framework/core/org.wso2." +
                    "automation.platform.core/src/main/resources/artifacts/GREG/schema/calculator.xsd");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:newSchema with " +
                    "URL method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testNewSchemaUrl"}, description = "Testing " +
            "addSchema API method", priority = 2)
    public void testAddSchema() throws GovernanceException {
        try {
            schemaManager.addSchema(schemaObj);
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:addSchema method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testAddSchema"}, description = "Testing " +
            "getAllSchemas API method", priority = 3)
    public void testGetAllSchema() throws GovernanceException {
        try {
            schemaArray = schemaManager.getAllSchemas();
            assertTrue(schemaArray.length > 0, "Error occurred while executing SchemaManager:" +
                    "getAllSchemas method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:getAllSchemas method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testGetAllSchema"}, description = "Testing " +
            "getSchema API method", priority = 4)
    public void testGetSchema() throws GovernanceException {
        try {
            schemaObj = schemaManager.getSchema(schemaArray[0].getId());
            assertTrue(schemaObj.getQName().getLocalPart().equalsIgnoreCase(schemaName), "SchemaManager:" +
                    "getSchema API method not contain expected schema name");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:getSchema method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, dependsOnMethods = {"testGetSchema"}, description = "Testing " +
            "updateSchema API method", priority = 5)
    public void testUpdateSchema() throws GovernanceException {
        String lcName = "ServiceLifeCycle";
        try {
            schemaObj.attachLifecycle(lcName);
            schemaManager.updateSchema(schemaObj);
            Schema localSchema = schemaManager.getSchema(schemaObj.getId());
            assertTrue(localSchema.getLifecycleName().equalsIgnoreCase(lcName), "Updated schema doesn't " +
                    "have lifecycle Information.SchemaManager:updateSchema didn't work");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:updateSchema method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing FindSchema", priority = 6)
    public void testFindService() throws GovernanceException {
        try {
            Schema[] schemaArray = schemaManager.findSchemas(new SchemaFilter() {
                public boolean matches(Schema schema) throws GovernanceException {
                    String name = schema.getQName().getLocalPart();
                    assertTrue(name.contains(schemaName), "Error occurred while executing " +
                            "findSchema API method");
                    return name.contains(schemaName);
                }
            }
            );
            assertTrue(schemaArray.length > 0, "Error occurred while executing findSchema API " +
                    "method");
        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing WsdlManager:findSchemas method" + e);
        }
    }


//    Schema data object specific test cases

    @Test(groups = {"wso2.greg.api"}, description = "Testing getQName API method with schema object", priority = 7)
    public void testGetQName() throws Exception {
        boolean isSchemaFound = false;
        Schema[] schema = schemaManager.getAllSchemas();
        try {
            for (Schema s : schema) {
                if (s.getQName().getLocalPart().equalsIgnoreCase(schemaName)) {
                    isSchemaFound = true;
                }
            }
            assertTrue(isSchemaFound, "getQName method prompt error while executing with schema object");
        } catch (Exception e) {
            throw new Exception("Error occurred while executing WsdlManager:getQName method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing getUrl API method with schema object", priority = 8)
    public void testGetUrl() throws Exception {
        boolean isSchemaFound = false;
        Schema[] schema = schemaManager.getAllSchemas();
        try {
            for (Schema s : schema) {
                if (!(s.getUrl() == null)) {
                    isSchemaFound = true;
                }
            }
            assertTrue(isSchemaFound, "getUrl method prompt error while executing with schema object");
        } catch (Exception e) {
            throw new Exception("Error occurred while executing WsdlManager:getUrl method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing getSchemaElement API method with schema object", priority = 9)
    public void testGetSchemaElement() throws Exception {
        boolean isSchemaFound = false;
        Schema[] schema = schemaManager.getAllSchemas();
        try {
            for (Schema s : schema) {
                if (s.getQName().getLocalPart().equalsIgnoreCase(schemaName)) {
                    OMElement omElement = s.getSchemaElement();
                    if (omElement.toString().contains("http://charitha.org/")) {
                        isSchemaFound = true;
                    }
                }
            }
            assertTrue(isSchemaFound, "getSchemaElement method prompt error while executing with schema object");
        } catch (Exception e) {
            throw new Exception("Error occurred while executing getSchemaElement method" + e);
        }
    }

    @Test(groups = {"wso2.greg.api"}, description = "Testing removeSchema API method", priority = 10)
    public void testRemoveSchema() throws GovernanceException {
        try {
            schemaManager.removeSchema(schemaObj.getId());
            schemaArray = schemaManager.getAllSchemas();
            for (Schema s : schemaArray) {
                assertTrue(s.getId().equalsIgnoreCase(schemaObj.getId()), "SchemaManager:removeSchema API method having error");
            }

        } catch (GovernanceException e) {
            throw new GovernanceException("Error occurred while executing SchemaManager:removeSchema method" + e);
        }
    }
}
