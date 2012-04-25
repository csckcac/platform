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
package org.wso2.automation.common.test.as.service;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceResourceAdmin;
import org.wso2.carbon.registry.api.RegistryException;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceExceptionException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClientUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.activation.DataHandler;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/**
 * Deploy a service which refers external schema hosted in governance registry.
 */
public class ExternalSchemaReferenceTest {
    private static final Log log = LogFactory.getLog(ExternalSchemaReferenceTest.class);
    private static String AXIS2SERVICE_EPR;
    private static WSRegistryServiceClient registry = null;
    private EnvironmentBuilder builder;
    private ManageEnvironment environment;

    @BeforeTest(alwaysRun = true)
    public void initializeProperties()
            throws org.wso2.carbon.registry.core.exceptions.RegistryException, AxisFault {
        log.info("Running service read wsdl from G-Reg repo test...");
        int userId = 1;
        String serviceName = "calculatorImportSchema";
        builder = new EnvironmentBuilder().as(userId);
        environment = builder.build();
        AXIS2SERVICE_EPR = environment.getAs().getServiceUrl() + "/" + serviceName;
        registry = new RegistryProvider().getRegistry
                (userId, ProductConstant.APP_SERVER_NAME); //get remote registry instance
    }

    @Test(groups = "wso2.as", description = "Put claculator schema to G-Reg", priority = 1)
    public void addCalculatorSchema()
            throws FileNotFoundException, RegistryException, MalformedURLException, RemoteException,
                   ResourceAdminServiceExceptionException {
        String filePath = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + File.separator +
                          "artifacts" + File.separator + "GREG" + File.separator +
                          "schema" + File.separator + "calculator.xsd";
        String schemaPath = "/_system/governance/trunk/schemas/org/charitha/calculator.xsd";
        URL garURL = new URL("file:///" + filePath);
        DataHandler garDh = new DataHandler(garURL);
        AdminServiceResourceAdmin adminServiceResourceAdmin =
                new AdminServiceResourceAdmin(environment.getAs().getBackEndUrl());
        Assert.assertTrue(adminServiceResourceAdmin.addResource
                (environment.getAs().getSessionCookie(), schemaPath, "application/x-xsd+xml", "desc", garDh),"Resource Adding failed");
    }

    @Test(groups = {"wso2.as"},
          description = "sample service to test schema imports when the wsdl " +
                        "is placed at META-INF and the schema is located in a url repo", priority = 2)
    public void testServiceWithReferenceSchemas() throws AxisFault, InterruptedException {
        OMElement result;
        OMElement payload = createPayLoad();
        ServiceClient serviceclient = new ServiceClient();
        Options opts = new Options();

        opts.setTo(new EndpointReference(AXIS2SERVICE_EPR));
        opts.setAction("http://charitha.org/echoString");
        opts.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
        serviceclient.setOptions(opts);
        log.debug("Axis2Service EPR " + AXIS2SERVICE_EPR);
        AxisServiceClientUtils.waitForServiceDeployment(AXIS2SERVICE_EPR);
        long deploymentDelay =
                builder.getFrameworkSettings().getEnvironmentVariables().getDeploymentDelay();
        Thread.sleep(deploymentDelay);//force wait - Even though the WSDL is available it take
        result = serviceclient.sendReceive(payload);
        log.debug("Service response " + result);
        assertTrue((result.toString().indexOf("420")) > 0, "expected response not found");
    }

    private static OMElement createPayLoad() {
        log.debug("Creating payload");
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace("http://charitha.org", "char");
        OMElement method = fac.createOMElement("addition", omNs);
        OMElement valueOfx = fac.createOMElement("x", omNs);
        OMElement valueOfy = fac.createOMElement("y", omNs);
        valueOfx.addChild(fac.createOMText(valueOfx, "200"));
        valueOfy.addChild(fac.createOMText(valueOfy, "220"));
        method.addChild(valueOfx);
        method.addChild(valueOfy);
        log.debug("Payload is :" + method);

        return method;
    }
}
