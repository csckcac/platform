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
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import java.rmi.RemoteException;

public class GRegPromoteLifeCycleWithResource {

    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminService lifeCycleAdminService;

    private final String serviceName = "serviceForLifeCycleWithDependency";
    private final String serviceDependencyName = "UTPolicyDependency.xml";
    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ASS_TYPE_DEPENDS = "depends";
    //    private final String ACTION_DEMOTE = "Demote";
    private String servicePathDev;
    private String servicePathTest;
    private String servicePathProd;

    private String policyPathDev;
    private String policyPathTest;

    @BeforeClass
    public void init() throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();

        sessionCookie = gregServer.getSessionCookie();
        lifeCycleAdminService = new LifeCycleAdminService(gregServer.getBackEndUrl());
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registry, userId);

        servicePathDev = "/_system/governance" + Utils.addService("sns", serviceName, governance);
        policyPathDev = "/_system/governance" + Utils.addPolicy(serviceDependencyName, governance);
        addDependency(servicePathDev, policyPathDev);
        Thread.sleep(1000);
        Association[] dependency = registry.getAssociations(servicePathDev, ASS_TYPE_DEPENDS);
        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");
        Assert.assertTrue(dependency.length == 1, "Additional dependency found");
        Assert.assertEquals(dependency[0].getDestinationPath(), policyPathDev, "Dependency Name mismatched");
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

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development",
                            "LifeCycle State Mismatched");

        Assert.assertTrue((lifeCycleAdminService.getAllDependencies(sessionCookie, servicePathDev).length == 2),
                          "Dependency Count mismatched");


    }

    @Test(priority = 2, dependsOnMethods = {"addLifecycle"}, description = "Promote Service")
    public void promoteServiceToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathDev, "1.0.0"});

        lifeCycleAdminService.invokeAspectWithParams(sessionCookie, servicePathDev, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceName;
        policyPathTest = "/_system/governance/branches/testing/policies/1.0.0/" + serviceDependencyName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTest);
        Resource service = registry.get(servicePathTest);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTest);
        Assert.assertTrue(service.getPath().contains("branches/testing"), "Service not in branches/testing. " + servicePathTest);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Testing", "LifeCycle State Mismatched");

        Association[] dependency = registry.getAssociations(servicePathTest, ASS_TYPE_DEPENDS);
        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");
        Assert.assertEquals(dependency[0].getDestinationPath(), policyPathTest, "Dependency Name mismatched");

        Assert.assertTrue((lifeCycleAdminService.getAllDependencies(sessionCookie, servicePathTest).length == 2),
                          "Dependency Count mismatched");

        Assert.assertEquals(registry.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed for service");
        Assert.assertEquals(registry.get(policyPathDev).getPath(), policyPathDev, "Preserve original failed for dependency");

    }

    @Test(priority = 3, dependsOnMethods = {"promoteServiceToTesting"}, description = "Promote Service")
    public void promoteServiceToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathTest, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{policyPathTest, "1.0.0"});
        lifeCycleAdminService.invokeAspectWithParams(sessionCookie, servicePathTest, aspectName,
                                                     ACTION_PROMOTE, null, parameters);

        servicePathProd = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceName;
        String policyPathProd = "/_system/governance/branches/production/policies/1.0.0/" + serviceDependencyName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathProd);

        Resource service = registry.get(servicePathProd);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathProd);
        Assert.assertTrue(service.getPath().contains("branches/production"), "Service not in branches/production. " + servicePathProd);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");

        Association[] dependency = registry.getAssociations(servicePathProd, ASS_TYPE_DEPENDS);
        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");
        Assert.assertEquals(dependency[0].getDestinationPath(), policyPathProd, "Dependency Name mismatched");

        Assert.assertTrue((lifeCycleAdminService.getAllDependencies(sessionCookie, servicePathProd).length == 2),
                          "Dependency Count mismatched");

        Assert.assertEquals(registry.get(servicePathTest).getPath(), servicePathTest, "Preserve original failed for service");
        Assert.assertEquals(registry.get(policyPathTest).getPath(), policyPathTest, "Preserve original failed for dependency");

    }

    @AfterClass
    public void destroy() {
        servicePathDev = null;
        servicePathTest = null;
        servicePathProd = null;
    }


    private void addDependency(String resourcePath, String dependencyPath)
            throws RegistryException {
        registry.addAssociation(resourcePath, dependencyPath, ASS_TYPE_DEPENDS);
    }

}
