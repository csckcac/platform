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


package org.wso2.automation.common.test.greg.governance;

import org.apache.axis2.AxisFault;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import java.io.File;
import java.io.IOException;

import static junit.framework.Assert.assertTrue;

/**
 * Contain tests for volume tests carried out using governance API
 */
public class MetaDataVolumeTestClient {
    private Registry governance;

    @BeforeClass(groups = {"wso2.greg"})
    public void initTest() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        WSRegistryServiceClient registryWS = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registryWS, userId);
    }

    @Test(groups = {"wso2.greg"}, description = "Adding large number of schemas")
    public void testAddLargeNoOfSchemas() throws GovernanceException {
        Schema schema;
        String schemaContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<xsd:schema xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" targetNamespace=\"http://www.company.org\" xmlns=\"http://www.company.org\" elementFormDefault=\"qualified\">\n" +
                "    <xsd:complexType name=\"PersonType\">\n" +
                "        <xsd:sequence>\n" +
                "           <xsd:element name=\"Name\" type=\"xsd:string\"/>\n" +
                "           <xsd:element name=\"SSN\" type=\"xsd:string\"/>\n" +
                "        </xsd:sequence>\n" +
                "    </xsd:complexType>\n" +
                "</xsd:schema>";

        SchemaManager schemaManager = new SchemaManager(governance);
        Schema[] schemaList = schemaManager.getAllSchemas();
        for (Schema s : schemaList) {
            if (s.getQName().getLocalPart().contains("Automated")) {
                schemaManager.removeSchema(s.getId());
            }
        }
        try {
            for (int i = 0; i <= 10000; i++) {
                schema = schemaManager.newSchema(schemaContent.getBytes(), "AutomatedSchema" + i + ".xsd");
                schemaManager.addSchema(schema);
                if (!schemaManager.getSchema(schema.getId()).getQName().getLocalPart().equalsIgnoreCase("AutomatedSchema" + i + ".xsd")) {
                    assertTrue("Schema not added..", false);
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error found while adding multiple schemas : " + e.getMessage());
        }
    }

    @Test(groups = {"wso2.greg"}, description = "Test adding multiple wsdl")
    public void testMultipleWsdl() throws GovernanceException, IOException {
        Wsdl wsdl;
        WsdlManager wsdlManager = new WsdlManager(governance);
        String wsdlFileLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator
                + "artifacts" + File.separator + "GREG" + File.separator + "wsdl" + File.separator + "Automated.wsdl";


        Wsdl[] wsdlList = wsdlManager.getAllWsdls();
        for (Wsdl w : wsdlList) {
            if (w.getQName().getLocalPart().contains("Automated")) {
                wsdlManager.removeWsdl(w.getId());
            }
        }
        try {
            for (int i = 0; i <= 10000; i++) {
                wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFileLocation).getBytes(),
                        "AutomatedWsdl" + i + ".wsdl");
                wsdlManager.addWsdl(wsdl);
                if (!wsdlManager.getWsdl(wsdl.getId()).getQName().getLocalPart().equalsIgnoreCase
                        ("AutomatedWsdl" + i + ".wsdl")) {
                    assertTrue("Wsdl not added..", false);
                }
            }
        } catch (GovernanceException e) {
            throw new GovernanceException("Error found while adding multiple Wsdl : " + e.getMessage());
        }
    }


}
