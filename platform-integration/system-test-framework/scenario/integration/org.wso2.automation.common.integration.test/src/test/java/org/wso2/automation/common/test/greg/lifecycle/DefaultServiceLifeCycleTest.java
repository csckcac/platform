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
import org.wso2.carbon.admin.service.LifeCycleAdminService;
import org.wso2.carbon.governance.api.services.ServiceManager;
import org.wso2.carbon.governance.api.services.dataobjects.Service;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;

public class DefaultServiceLifeCycleTest {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private Registry governance;
    private LifeCycleAdminService lifeCycleAdminService;

    private final String serviceName = "serviceForLifeCycleTest";
    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
//    private final String ACTION_DEMOTE = "Demote";
    private String servicePathDev;
    private String servicePathTest;
    private String servicePathProd;

    @BeforeClass
    public void init() throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();

        sessionCookie = gregServer.getSessionCookie();
        lifeCycleAdminService = new LifeCycleAdminService(gregServer.getBackEndUrl());
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registry, userId);

        servicePathDev = "/_system/governance" + addService("sns", serviceName);
        Thread.sleep(1000);

    }

    @Test(priority = 1, description = "Add lifecycle to a service")
    public void addLifecycle()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException {
        registry.associateAspect(servicePathDev, aspectName);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathDev);
        Resource service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathDev);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 5), "LifeCycle properties missing some properties");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getValues()[0], "Development",
                            "LifeCycle State Mismatched");

    }

    @Test(priority = 2, dependsOnMethods = {"addLifecycle"}, description = "Promote Service")
    public void promoteServiceToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathDev, aspectName, ACTION_PROMOTE, null);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0-SNAPSHOT/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTest);
        Resource service = registry.get(servicePathTest);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTest);
        Assert.assertTrue(service.getPath().contains("branches/testing"), "Service not in branches/testing. " + servicePathTest);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 5), "LifeCycle properties missing some properties");

        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getValues()[0], "Testing",
                            "LifeCycle State Mismatched");

    }

    @Test(priority = 3, dependsOnMethods = {"promoteServiceToTesting"}, description = "Promote Service")
    public void promoteServiceToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathTest, aspectName, ACTION_PROMOTE, null);
        servicePathProd = "/_system/governance/branches/production/services/sns/1.0.0-SNAPSHOT/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathProd);

        Resource service = registry.get(servicePathProd);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathProd);
        Assert.assertTrue(service.getPath().contains("branches/production"), "Service not in branches/production. " + servicePathProd);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");

        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[0], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[0].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[0].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[0].getValues()[0], "Production",
                            "LifeCycle State Mismatched");

    }

    @AfterClass
    public void destroy() {
        servicePathDev = null;
        servicePathTest = null;
        servicePathProd = null;
    }

    private String addService(String nameSpace, String serviceName)
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
}
