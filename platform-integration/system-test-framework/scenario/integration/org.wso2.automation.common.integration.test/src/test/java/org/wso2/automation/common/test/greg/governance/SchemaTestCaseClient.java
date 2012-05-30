/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.wso2.automation.common.test.greg.governance;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static junit.framework.Assert.assertTrue;

public class SchemaTestCaseClient {
    private Registry governance;
    private static final Log log = LogFactory.getLog(SchemaTestCaseClient.class);

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registryWS, userId);
    }

    @Test(groups = {"wso2.greg"})
    public void testAddSchema() throws Exception {
        log.info("############## testAddSchema started ...###################");
        SchemaManager schemaManager = new SchemaManager(governance);

        Schema schema = schemaManager.newSchema("http://svn.wso2.org/repos/wso2/trunk/graphite/components/" +
                                                "governance/org.wso2.carbon.governance.api/src/test/resources/" +
                                                "test-resources/xsd/purchasing.xsd");
        schema.addAttribute("creator", "it is me");
        schema.addAttribute("version", "0.01");
        schemaManager.addSchema(schema);

        Schema newSchema = schemaManager.getSchema(schema.getId());
        Assert.assertEquals(newSchema.getSchemaElement().toString(), schema.getSchemaElement().toString());

        // change the target namespace and check
        String oldSchemaPath = newSchema.getPath();
        Assert.assertEquals(oldSchemaPath, "/trunk/schemas/org/bar/purchasing/purchasing.xsd");
        Assert.assertTrue(governance.resourceExists("/trunk/schemas/org/bar/purchasing/purchasing.xsd"));

        OMElement schemaElement = newSchema.getSchemaElement();
        schemaElement.addAttribute("targetNamespace", "http://ww2.wso2.org/schema-test", null);
        schemaElement.declareNamespace("http://ww2.wso2.org/schema-test", "tns");
        schemaManager.updateSchema(newSchema);
        doSleep();

        Assert.assertEquals(newSchema.getPath(), "/trunk/schemas/org/wso2/ww2/schema_test/purchasing.xsd");
        Assert.assertFalse(governance.resourceExists("/trunk/test_schemas/org/bar/purchasing.xsd"));

        // doing an update without changing anything.
        schemaManager.updateSchema(newSchema);
        doSleep();

        Assert.assertEquals(newSchema.getPath(), "/trunk/schemas/org/wso2/ww2/schema_test/purchasing.xsd");
        Assert.assertEquals(newSchema.getAttribute("version"), "0.01");

        newSchema = schemaManager.getSchema(schema.getId());
        Assert.assertEquals(newSchema.getAttribute("creator"), "it is me");
        Assert.assertEquals(newSchema.getAttribute("version"), "0.01");

        Schema[] schemas = schemaManager.findSchemas(new SchemaFilter() {
            public boolean matches(Schema schema) throws GovernanceException {
                if (schema.getAttribute("version").equals("0.01")) {
                    log.info("########Schema name" + schema.getQName().toString() + "  schemaID : " + schema.getId());
                    return true;
                }
                return false;
            }
        });
        doSleep();
        log.info("########Schema Len:" + schemas.length);
        Assert.assertEquals(schemas.length, 2);
        Assert.assertEquals(newSchema.getId(), schemas[0].getId());

        // deleting the schema
        schemaManager.removeSchema(newSchema.getId());
        Schema deletedSchema = schemaManager.getSchema(newSchema.getId());
        Assert.assertNull(deletedSchema);
    }

    @Test(groups = {"wso2.greg"})
    public void testAddSchemaFromContent() throws Exception {
        SchemaManager schemaManager = new SchemaManager(governance);
        byte[] bytes = null;
        try {
            InputStream inputStream = new URL("http://svn.wso2.org/repos/wso2/trunk/graphite/components/" +
                                              "governance/org.wso2.carbon.governance.api/src/test/resources/" +
                                              "test-resources/xsd/purchasing.xsd").openStream();
            try {
                bytes = IOUtils.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            Assert.fail("Unable to read WSDL content");
        }
        Schema schema = schemaManager.newSchema(bytes, "newPurchasing.xsd");
        schema.addAttribute("creator", "it is me");
        schema.addAttribute("version", "0.01");
        schemaManager.addSchema(schema);

        Schema newSchema = schemaManager.getSchema(schema.getId());
        Assert.assertEquals(newSchema.getSchemaElement().toString(),
                            schema.getSchemaElement().toString());
        Assert.assertEquals(newSchema.getAttribute("creator"), "it is me");
        Assert.assertEquals(newSchema.getAttribute("version"), "0.01");

        // change the target namespace and check
        String oldSchemaPath = newSchema.getPath();
        Assert.assertEquals(oldSchemaPath, "/trunk/schemas/org/bar/purchasing/newPurchasing.xsd");
        Assert.assertTrue(governance.resourceExists("/trunk/schemas/org/bar/purchasing/newPurchasing.xsd"));
    }

    @Test(groups = {"wso2.greg"})
    public void testAddSchemaFromContentNoName() throws Exception {
        SchemaManager schemaManager = new SchemaManager(governance);
        byte[] bytes = null;
        try {
            InputStream inputStream = new URL("http://svn.wso2.org/repos/wso2/trunk/graphite/components/" +
                                              "governance/org.wso2.carbon.governance.api/src/test/resources/" +
                                              "test-resources/xsd/purchasing.xsd").openStream();
            try {
                bytes = IOUtils.toByteArray(inputStream);
            } finally {
                inputStream.close();
            }
        } catch (IOException e) {
            Assert.fail("Unable to read WSDL content");
        }
        Schema schema = schemaManager.newSchema(bytes);
        schema.addAttribute("creator", "it is me");
        schema.addAttribute("version", "0.01");
        schemaManager.addSchema(schema);

        Schema newSchema = schemaManager.getSchema(schema.getId());
        Assert.assertEquals(newSchema.getSchemaElement().toString(),
                            schema.getSchemaElement().toString());
        Assert.assertEquals(newSchema.getAttribute("creator"), "it is me");
        Assert.assertEquals(newSchema.getAttribute("version"), "0.01");

        // change the target namespace and check
        String oldSchemaPath = newSchema.getPath();
        Assert.assertEquals(oldSchemaPath, "/trunk/schemas/org/bar/purchasing/" + schema.getId() + ".xsd");
        Assert.assertTrue(governance.resourceExists("/trunk/schemas/org/bar/purchasing/" + schema.getId() + ".xsd"));
    }

    private void doSleep() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
    }

    @Test(groups = {"wso2.greg"}, expectedExceptions = {GovernanceException.class})
    public void testInvalidSchema() throws GovernanceException {
        SchemaManager schemaManager = new SchemaManager(governance);
        Schema schema;
        try {
            schema = schemaManager.newSchema("https://svn.wso2.org/repos/wso2/carbon/platform/trunk/platform-integration" +
                    "/system-test-framework/core/org.wso2.automation.platform.core/src/main/resources/artifacts" +
                    "/GREG/schema/XmlInvalidSchema.xsd");
            assertTrue("Invalid schema added without any exceptions", false);
        } catch (GovernanceException e) {
            throw new GovernanceException("Exception thrown while adding Invalid schema : " + e.getMessage());
        }
        schemaManager.addSchema(schema);
    }

    @Test(groups = {"wso2.greg"})
    public void testInvalidSchemaContent() throws GovernanceException {
        Schema schema;
        String schemaContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xsd:schema targetNamespace=\"http://OrderProcessingLib/bo\"\n" +
                "            xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
                "    <xsd:complexType>\n" +
                "        <xsd:sequence>\n" +
                "            <xsd:element minOccurs=\"0\" name=\"vendorID\"\n" +
                "                         type=\"xsd:string\">\n" +
                "            </xsd:element>\n" +
                "            <xsd:element minOccurs=\"0\" name=\"deliveryCity\"\n" +
                "                         type=\"xsd:string\">\n" +
                "            </xsd:element>\n" +
                "            <xsd:element minOccurs=\"0\" name=\"totalAmount\"\n" +
                "                         type=\"xsd:double\">\n" +
                "            </xsd:element>\n" +
                "            <xsd:element minOccurs=\"0\" name=\"status\"\n" +
                "                         type=\"xsd:string\">\n" +
                "            </xsd:element>\n" +
                "        </xsd:sequence>\n" +
                "    </xsd:complexType>\n" +
                "</xsd:schema>";

        SchemaManager schemaManager = new SchemaManager(governance);
        try {
            schema = schemaManager.newSchema(schemaContent.getBytes(), "InvalidSchema.xsd");
            schemaManager.addSchema(schema);
            assertTrue("getAttribute(\"Schema Validation\") schema validation didn't executed",
                    schema.getAttribute("Schema Validation").equalsIgnoreCase("invalid"));
        } catch (GovernanceException e) {
            throw new GovernanceException("Exception thrown while adding Invalid schema : " + e.getMessage());
        }
    }
}
