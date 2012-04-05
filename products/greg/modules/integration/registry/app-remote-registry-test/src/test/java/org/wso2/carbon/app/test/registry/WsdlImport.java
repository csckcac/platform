/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.wso2.carbon.app.test.registry;

import org.wso2.carbon.integration.core.FrameworkSettings;
import org.wso2.carbon.integration.core.TestTemplate;
import org.wso2.carbon.registry.app.RemoteRegistry;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import java.util.List;

public class WsdlImport extends TestTemplate {
     public RemoteRegistry registry;

    @Override
    public void init() {
        InitializeAPI initializeAPI = new InitializeAPI();
        registry = initializeAPI.getRegistry(FrameworkSettings.CARBON_HOME,FrameworkSettings.HTTPS_PORT,FrameworkSettings.HTTP_PORT);
    }
    @Override
    public void runSuccessCase() {
        try {
            WsdlimportTest();
        }
        catch (Exception e) {
            e.printStackTrace();
            fail("WSDL Import Test Failed");
        }
    }

    @Override
    public void runFailureCase() {
    }

    @Override
    public void cleanup() {
    }

    public void WsdlimportTest() throws RegistryException {

        String url = "http://131.107.153.205/soapwsdl_complexdatatypes_xmlformatter_service_indigo/complexdatatypesrpclit.svc?wsdl";
        Resource r1 = registry.newResource();
        r1.setDescription("WSDL imported from url");
        r1.setMediaType("application/wsdl+xml");
        String path = "/MyTestWSDL/ComplexDataTypesRpcLit.svc.wsdl";

        registry.importResource(path, url, r1);

        String wsdlPath = "/_system/governance/wsdls/org/tempuri/complexdatatypesrpclit.svc.wsdl";

        assertTrue("ComplexDataTypesRpcLit.svc.wsdl", resourceExists(registry, wsdlPath));
        String servicePath = "/_system/governance/servi" +
                "ces/org/tempuri/ComplexDataTypesRpcLitService/1.0.0/service";
        assertTrue("ComplexDataTypesRpcLitService is not available", resourceExists(registry, servicePath));


        String schemaLocation0 = "/_system/governance/schemas/com/microsoft/schemas/_2003/_10/" +
                "serialization/arrays/ComplexDataTypesRpcLit.xsd";
        String schemaLocation1 = "/_system/governance/schemas/com/microsoft/schemas/_2003/_10/" +
                "serialization/ComplexDataTypesRpcLit1.xsd";
        String schemaLocation2 = "/_system/governance/schemas/org/datacontract/schemas/_2004/_07/" +
                "system/ComplexDataTypesRpcLit2.xsd";
        String schemaLocation3 = "/_system/governance/schemas/org/datacontract/schemas/_2004/_07/" +
                "xwsinterop_soapwsdl_complexdatatypes_xmlformatter_service_indigo/" +
                "ComplexDataTypesRpcLit3.xsd";
        assertTrue("ComplexDataTypesRpcLit.xsd not found", resourceExists(registry,
                schemaLocation0));
        assertTrue("ComplexDataTypesRpcLit1.xsd not found", resourceExists(registry, schemaLocation1));
        assertTrue("ComplexDataTypesRpcLit2.xsd not found", resourceExists(registry, schemaLocation2));
        assertTrue("ComplexDataTypesRpcLit3.xsd not found", resourceExists(registry, schemaLocation3));
        String endPointPath = "/_system/governance/endpoints/_205/_153/_107/_131/" +
                "soapwsdl_complexdatatypes_xmlformatter_service_indigo/ep-ComplexDataTypesRpcLit-svc";
        assertTrue("ep-ComplexDataTypesRpcLit-svc endpoint not found", resourceExists(registry,
                endPointPath));

        assertTrue("association Destination path not exist", associationPathExists(wsdlPath,
                servicePath));

        assertTrue("association Destination path not exist", associationPathExists(wsdlPath,
                endPointPath));

        assertTrue("association Destination path not exist", associationPathExists(wsdlPath,
                schemaLocation0));

        assertTrue("association Destination path not exist", associationPathExists(wsdlPath,
                schemaLocation1));

        assertTrue("association Destination path not exist", associationPathExists(wsdlPath,
                schemaLocation2));

        assertTrue("association Destination path not exist", associationPathExists(wsdlPath,
                schemaLocation3));

        /*check wsdl properties*/

        Resource r2b = registry.get(wsdlPath);

        assertEquals("WSDL validation status", "Valid", r2b.getProperty("WSDL Validation"));
        assertEquals("WS-I validation status", "Valid", r2b.getProperty("WSI Validation"));

        /*check for enpoint dependencies*/
        assertTrue("association Destination path not exist", associationPathExists(endPointPath,
                wsdlPath));
        assertTrue("association Destination path not exist", associationPathExists(endPointPath,
                                                                                   servicePath));

        /*check for xsd dependencies*/

        assertTrue("association Destination path not exist", associationPathExists(schemaLocation0,
                schemaLocation3));

        assertTrue("association Destination path not exist", associationPathExists(schemaLocation0, wsdlPath));

        assertTrue("association Destination path not exist", associationPathExists(schemaLocation3, schemaLocation0));

        assertTrue("association Destination path not exist", associationPathExists(schemaLocation3, wsdlPath));
    }

    public static boolean resourceExists(RemoteRegistry registry, String fileName)
            throws RegistryException {
        boolean value = registry.resourceExists(fileName);
        return value;
    }

    public boolean associationPathExists(String path, String assoPath)
            throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;

        for (int i = 0; i < association.length; i++) {
            //System.out.println(association[i].getDestinationPath());
            if (assoPath.equals(association[i].getDestinationPath())) {
                value = true;
            }
        }


        return value;
    }

    public boolean associationExists(String path, String pathValue)
            throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = false;
        for (int i = 0; i < association.length; i++) {
            if (pathValue.equals(association[i].getDestinationPath())) {
                value = true;
            }
        }

        return value;
    }

    public boolean associationNotExists(String path) throws RegistryException {
        Association association[] = registry.getAllAssociations(path);
        boolean value = true;
        if (association.length > 0) {
            value = false;
        }
        return value;
    }

    public boolean getProperty(String path, String key, String value) throws RegistryException {
        Resource r3 = registry.newResource();
        try {
            r3 = registry.get(path);
        }
        catch (RegistryException e) {
            fail((new StringBuilder()).append("Couldn't get file from the path :").append(path).toString());
        }
        List propertyValues = r3.getPropertyValues(key);
        Object valueName[] = propertyValues.toArray();
        boolean propertystatus = containsString(valueName, value);
        return propertystatus;
    }

    private boolean containsString(Object[] array, String value) {
        boolean found = false;
        for (Object anArray : array) {
            String s = anArray.toString();
            if (s.startsWith(value)) {
                found = true;
                break;
            }
        }

        return found;
    }
}
