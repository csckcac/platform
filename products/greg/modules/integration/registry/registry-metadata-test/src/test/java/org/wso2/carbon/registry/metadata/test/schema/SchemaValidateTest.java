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

package org.wso2.carbon.registry.metadata.test.schema;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.metadata.test.util.RegistryConsts;
import org.wso2.carbon.registry.metadata.test.util.TestUtils;
import org.wso2.carbon.registry.properties.stub.PropertiesAdminServiceStub;
import org.wso2.carbon.registry.properties.stub.beans.xsd.PropertiesBean;
import org.wso2.carbon.registry.properties.stub.utils.xsd.Property;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;

import javax.activation.DataHandler;
import java.io.File;
import java.net.URL;

import static org.wso2.carbon.registry.metadata.test.util.TestUtils.isResourceExist;


public class SchemaValidateTest extends TestTemplate {

    private static final Log log = LogFactory.getLog(SchemaValidateTest.class);
    private String schemaPath = "/_system/governance/schemas/";
    private ResourceAdminServiceStub resourceAdminServiceStub;
    private PropertiesAdminServiceStub propertiesAdminServiceStub;

    @Override
    public void init() {
        log.info("Initializing Add Schema Resource Tests");
        log.debug("Add Add Schema Resource Initialised");
    }

    @Override
    public void runSuccessCase() {
        log.debug("Running SuccessCase");
        resourceAdminServiceStub = TestUtils.getResourceAdminServiceStub(sessionCookie);
        propertiesAdminServiceStub = TestUtils.getPropertiesAdminServiceStub(sessionCookie);
        addValidSchemaTest();
        addInvalidSchemaTest();
        addCompressSchemaFile();
    }

    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {
    }

    /**
     * Check schema validation status for correctness.
     */
    private void addValidSchemaTest() {
        String resourceUrl = "http://ww2.wso2.org/~qa/greg/Patient.xsd";
        String resourceName = "Patient.xsd";

        try {
            resourceAdminServiceStub.importResource(schemaPath + resourceName, resourceName,
                    RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile", resourceUrl, null);

            assertTrue("Schema validation status incorrect", validateProperties(schemaPath + "http/ihc/org/xsd?patient/" + resourceName, "Schema Validation", "Valid"));
            assertTrue("Target namespace not found", validateProperties(schemaPath + "http/ihc/org/xsd?patient/" + resourceName, "targetNamespace", "http://ihc.org/xsd?patient"));

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "http/ihc/org/xsd?patient/" + resourceName);

            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            } else {
                log.error("Unable to get text content");
                Assert.fail("Unable to get text content");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "http/ihc/org/xsd?patient/" + resourceName);

            //check if the deleted file exists in registry
            if (!isResourceExist(sessionCookie, schemaPath +
                    "http/ihc/org/xsd?patient/", resourceName, resourceAdminServiceStub)) {
                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from the registry");
                Assert.fail("Resource not deleted from the registry");
            }
        } catch (Exception e) {
            Assert.fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }

    /**
     * Check schema properties to find out whether the schema validation status is valid or invalid
     */
    private void addInvalidSchemaTest() {
        String resourceUrl = "http://ww2.wso2.org/~charitha/xsds/calculator-no-element-name-invalid.xsd";
        String resourceName = "calculator-no-element-name-invalid.xsd";

        try {
            resourceAdminServiceStub.importResource(schemaPath + resourceName, resourceName,
                    RegistryConsts.APPLICATION_X_XSD_XML, "schemaFile", resourceUrl, null);

            assertTrue("Schema validation status incorrect", validateProperties(schemaPath + "http/charitha/org/" +
                    resourceName, "Schema Validation", "Invalid"));
            assertTrue("Target namespace not found", validateProperties(schemaPath + "http/charitha/org/" +
                    resourceName, "targetNamespace", "http://charitha.org/"));
            assertTrue("Schema validation error not found", validateProperties(schemaPath + "http/charitha/org/" +
                    resourceName, "Schema Validation Message 1", "Error: s4s-att-must-appear: Attribute 'name' must " +
                    "appear in element 'element'."));

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "http/charitha/org/" + resourceName);

            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            } else {
                log.error("Unable to get text content");
                Assert.fail("Unable to get text content");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "http/charitha/org/" + resourceName);

            //check if the deleted file exists in registry
            if (!isResourceExist(sessionCookie, schemaPath +
                    "http/charitha/org/", resourceName, resourceAdminServiceStub)) {
                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from the registry");
                Assert.fail("Resource not deleted from the registry");
            }
        } catch (Exception e) {
            Assert.fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }

    /**
     * Add Schemas to registry using a zip file, validation status of all uploaded files checked.
     */
    private void addCompressSchemaFile() {
        String resourceName = "registry-new.zip";

        try {
            String resource = frameworkPath + File.separator + ".." + File.separator + ".." + File.separator + ".."
                    + File.separator + "src" + File.separator + "test" + File.separator + "java" + File.separator
                    + "resources" + File.separator +resourceName;

            resourceAdminServiceStub.addResource(schemaPath + resourceName,
                    RegistryConsts.APPLICATION_WSO2_GOVERNANCE_ARCHIVE, "schemaFile",
                    new DataHandler(new URL("file:///" + resource)), null);
//
            assertTrue("Schema validation status incorrect", validateProperties(schemaPath +
                    "http/www/dr/dk/namespaces/schemas/application/mas/whatson/production/production.xsd",
                    "Schema Validation", "Valid"));
            assertTrue("Target namespace not found", validateProperties(schemaPath +
                    "http/www/dr/dk/namespaces/schemas/application/mas/whatson/production/production.xsd",
                    "targetNamespace", "http://www.dr.dk/namespaces/schemas/application/mas/whatson/production"));
            assertTrue("Schema validation status incorrect", validateProperties(schemaPath +
                    "http/www/dr/dk/namespaces/schemas/common/types/types.xsd",
                    "Schema Validation", "Valid"));
            assertTrue("Target namespace not found", validateProperties(schemaPath +
                    "http/www/dr/dk/namespaces/schemas/common/types/types.xsd",
                    "targetNamespace", "http://www.dr.dk/namespaces/schemas/application/mas/whatson/production"));

            String textContent = resourceAdminServiceStub.getTextContent(schemaPath +
                    "http/www/dr/dk/namespaces/schemas/common/types/types.xsd");


            if (!textContent.equals(null)) {
                log.info("Resource successfully added to the registry and retrieved contents successfully");
            } else {
                log.error("Unable to get text content");
                Assert.fail("Unable to get text content");
            }

            if (isResourceExist(sessionCookie, schemaPath +
                    "http/www/dr/dk/namespaces/schemas/common/types", "types.xsd",resourceAdminServiceStub) &&
                    isResourceExist(sessionCookie, schemaPath +
                            "http/www/dr/dk/namespaces/schemas/application/mas/whatson/production", "production.xsd",
                            resourceAdminServiceStub)) {

                log.info("Resources have been uploaded to registry successfully");

            } else {
                log.error("Resources not exist in registry");
                Assert.fail("Resources not exist in registry");
            }

            //delete the added resource
            resourceAdminServiceStub.delete(schemaPath +
                    "http/www/dr/dk/namespaces/schemas/common/types/types.xsd");
            resourceAdminServiceStub.delete(schemaPath +
                    "http/www/dr/dk/namespaces/schemas/application/mas/whatson/production/production.xsd");

            //check if the deleted file exists in registry
            if (!(isResourceExist(sessionCookie, schemaPath +
                    "http/www/dr/dk/namespaces/schemas/common/types", "types.xsd",resourceAdminServiceStub) &&
                    isResourceExist(sessionCookie, schemaPath +
                            "http/www/dr/dk/namespaces/schemas/application/mas/whatson/production", "production.xsd",
                            resourceAdminServiceStub))) {

                log.info("Resource successfully deleted from the registry");

            } else {
                log.error("Resource not deleted from the registry");
                Assert.fail("Resource not deleted from the registry");
            }
        } catch (Exception e) {
            Assert.fail("Unable to get text content " + e);
            log.error(" : " + e.getMessage());
        }
    }


    public boolean validateProperties(String resourcePath, String key, String value) {
        boolean validationState = false;
        try {
            PropertiesBean propertiesBean = propertiesAdminServiceStub.getProperties(resourcePath, "yes");
            Property[] property = propertiesBean.getProperties();
            for (int i = 0; i <= property.length - 1; i++) {
                if (property[i].getKey().equalsIgnoreCase(key) && property[i].getValue().equalsIgnoreCase(value)) {
                    validationState = true;
                    log.info("Property key and value found");
                }
            }
        } catch (Exception e) {
            log.error("Error on finding resource properties : " + e);
            throw new RuntimeException("Error on finding resource properties : " + e);
        }
        return validationState;
    }
}

