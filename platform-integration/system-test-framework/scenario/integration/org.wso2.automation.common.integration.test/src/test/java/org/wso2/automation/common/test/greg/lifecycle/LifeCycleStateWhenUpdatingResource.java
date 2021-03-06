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
package org.wso2.automation.common.test.greg.lifecycle;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.greg.lifecycle.utils.Utils;
import org.wso2.carbon.admin.service.LifeCycleAdminService;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import java.io.IOException;

public class LifeCycleStateWhenUpdatingResource {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminService lifeCycleAdminService;
    Registry governance;

    private final String ASPECT_NAME = "ServiceLifeCycle";
    
    private String wsdlPathDev;
    private String servicePathDev;
    private String policyPathDev;
    private String schemaPathDev;


    @BeforeClass
    public void init() throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();
        sessionCookie = gregServer.getSessionCookie();
        lifeCycleAdminService = new LifeCycleAdminService(gregServer.getBackEndUrl());
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registry, userId);


    }

    @Test(priority = 1, description = "Add lifecycle to a Schema and update Schema")
    public void SchemaAddLifecycleAndUpdateResource()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   IOException, InterruptedException, RegistryExceptionException {
        schemaPathDev = "/_system/governance" + Utils.addSchema("LifeCycleState.xsd", governance);
        lifeCycleAdminService.addAspect(sessionCookie, schemaPathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, schemaPathDev);
        Resource service = registry.get(schemaPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + schemaPathDev);
        Assert.assertEquals(service.getPath(), schemaPathDev, "Service path changed after adding life cycle. " + schemaPathDev);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        schemaPathDev = "/_system/governance" + Utils.addSchema("LifeCycleState.xsd", governance);
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, schemaPathDev);
        service = registry.get(schemaPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + schemaPathDev);
        Assert.assertEquals(service.getPath(), schemaPathDev, "Service path changed after adding life cycle. " + schemaPathDev);

        Assert.assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");


    }

    @Test(priority = 1, description = "Add lifecycle to a policy and update policy")
    public void policyAddLifecycleAndUpdateResource()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   IOException, InterruptedException, RegistryExceptionException {
        policyPathDev = "/_system/governance" + Utils.addPolicy("PolicyLifeCycleState.xml", governance);
        lifeCycleAdminService.addAspect(sessionCookie, policyPathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, policyPathDev);
        Resource service = registry.get(policyPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + policyPathDev);
        Assert.assertEquals(service.getPath(), policyPathDev, "Service path changed after adding life cycle. " + policyPathDev);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        policyPathDev = "/_system/governance" + Utils.addPolicy("PolicyLifeCycleState.xml", governance);
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, policyPathDev);
        service = registry.get(policyPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + policyPathDev);
        Assert.assertEquals(service.getPath(), policyPathDev, "Service path changed after adding life cycle. " + policyPathDev);

        Assert.assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");

    }

    @Test(priority = 1, description = "Add lifecycle to a WSDl and update WSDL")
    public void WSDLAddLifecycleAndUpdateResource()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   IOException, InterruptedException, RegistryExceptionException {
        wsdlPathDev = "/_system/governance" + Utils.addWSDL("echoWsdlLifeCycleState.wsdl", governance);
        lifeCycleAdminService.addAspect(sessionCookie, wsdlPathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, wsdlPathDev);
        Resource service = registry.get(wsdlPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + wsdlPathDev);
        Assert.assertEquals(service.getPath(), wsdlPathDev, "Service path changed after adding life cycle. " + wsdlPathDev);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        wsdlPathDev = "/_system/governance" + Utils.addWSDL("echoWsdlLifeCycleState.wsdl", governance);
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, wsdlPathDev);
        service = registry.get(wsdlPathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + wsdlPathDev);
        Assert.assertEquals(service.getPath(), wsdlPathDev, "Service path changed after adding life cycle. " + wsdlPathDev);

        Assert.assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");

    }

    @Test(priority = 1, description = "Add lifecycle to a Service and update Service")
    public void serviceAddLifecycleAndUpdateResource()
            throws Exception {
        servicePathDev = "/_system/governance" + Utils.addService("sns", "ServiceLifeCycleState", governance);
        lifeCycleAdminService.addAspect(sessionCookie, servicePathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathDev);
        Resource service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        servicePathDev = "/_system/governance" + Utils.addService("sns", "ServiceLifeCycleState", governance);
        Thread.sleep(500);

        lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathDev);
        service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);

        Assert.assertNotNull(lifeCycle, "Life Cycle Not Found after updating resource");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties(), "Life Cycle properties Not Found after updating resource");
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched after updating resource");

    }


    @AfterClass
    public void cleanUp() throws RegistryException {
        if (schemaPathDev != null) {
            registry.delete(schemaPathDev);
        }
        if (policyPathDev != null) {
            registry.delete(policyPathDev);
        }
        if (wsdlPathDev != null) {
            registry.delete(wsdlPathDev);
        }
        if (servicePathDev != null) {
            registry.delete(servicePathDev);
        }
        registry = null;
        lifeCycleAdminService = null;
    }


}
