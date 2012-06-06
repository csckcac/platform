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
package org.wso2.automation.common.test.greg.lifecycle.utils;

import org.testng.Assert;
import org.wso2.automation.common.test.greg.multitenancy.LifeCycleComparisonServiceTestClient;
import org.wso2.carbon.governance.api.policies.PolicyManager;
import org.wso2.carbon.governance.api.policies.dataobjects.Policy;
import org.wso2.carbon.governance.api.schema.SchemaManager;
import org.wso2.carbon.governance.api.schema.dataobjects.Schema;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.registry.core.Comment;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.fileutils.FileManager;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
    public static String addService(String nameSpace, String serviceName, Registry governance)
            throws Exception {
        ServiceManager serviceManager = new ServiceManager(governance);
        Service service;
        service = serviceManager.newService(new QName(nameSpace, serviceName));
        serviceManager.addService(service);
        for (String serviceId : serviceManager.getAllServiceIds()) {
            service = serviceManager.getService(serviceId);
            if (service.getPath().endsWith(serviceName) && service.getPath().contains("trunk")) {

                return service.getPath();
            }

        }
        throw new Exception("Getting Service path failed");


    }

    public static String addPolicy(String policyName, Registry governance)
            throws RegistryException, IOException {
        PolicyManager policyManager = new PolicyManager(governance);
        String policyFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                                + File.separator + "policy" + File.separator;
        Policy policy = policyManager.newPolicy(FileManager.readFile(policyFilePath + "UTPolicy.xml").getBytes(), policyName);
        policyManager.addPolicy(policy);
        policy = policyManager.getPolicy(policy.getId());
        return policy.getPath();

    }

    public static String addWSDL(String name, Registry governance)
            throws IOException, RegistryException {
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        String wsdlFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                              + File.separator + "wsdl" + File.separator;
        wsdl = wsdlManager.newWsdl(FileManager.readFile(wsdlFilePath + "echo.wsdl").getBytes(), name);
        wsdlManager.addWsdl(wsdl);
        wsdl = wsdlManager.getWsdl(wsdl.getId());

        return wsdl.getPath();
    }

    public static String addSchema(String name, Registry governance)
            throws IOException, RegistryException {
        SchemaManager schemaManager = new SchemaManager(governance);
        String schemaFilePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                                + File.separator + "schema" + File.separator;
        Schema schema = schemaManager.newSchema(FileManager.readFile(schemaFilePath + "Person.xsd").getBytes(), name);
        schemaManager.addSchema(schema);
        schema = schemaManager.getSchema(schema.getId());
        return schema.getPath();

    }

    public static String getLifeCycleState(LifecycleBean lifeCycle) {
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");
        String state = null;
        boolean stateFound = false;
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if ("registry.lifecycle.ServiceLifeCycle.state".equalsIgnoreCase(prop.getKey())) {
                stateFound = true;
                Assert.assertNotNull(prop.getValues(), "State Value Not Found");
                state = prop.getValues()[0];

            }
        }
        Assert.assertTrue(stateFound, "LifeCycle State property not found");
        return state;
    }

    public static String formatDate(Date date) {
        Format formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(date);
    }
}
