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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.greg.lifecycle.utils.Utils;
import org.wso2.carbon.admin.service.LifeCycleAdminService;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import java.rmi.RemoteException;

public class PreserveOriginalDefaultServiceLifeCycle {

    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private Registry governance;
    private LifeCycleAdminService lifeCycleAdminService;

    private final String serviceNamePreserveFalse = "servicePreserveOriginalFalse";
    private final String serviceNamePreserveTrue = "servicePreserveOriginal";
    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    //    private final String ACTION_DEMOTE = "Demote";
    private String trunk;
    private String trunkPreserve;
    private String testBranch;
    private String proBranch;


    @BeforeClass
    public void init() throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();

        sessionCookie = gregServer.getSessionCookie();
        lifeCycleAdminService = new LifeCycleAdminService(gregServer.getBackEndUrl());
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registry, userId);

        trunk = "/_system/governance" + Utils.addService("sns", serviceNamePreserveFalse, governance);
        trunkPreserve = "/_system/governance" + Utils.addService("sns", serviceNamePreserveTrue, governance);
        Thread.sleep(500);
        registry.associateAspect(trunk, aspectName);
        registry.associateAspect(trunkPreserve, aspectName);

    }

    @Test(priority = 1, description = "Promote Service and delete original")
    public void preserveOriginalFalseAndPromoteToTesting() throws Exception {
        Thread.sleep(1000);

        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{trunk, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "false"});

        lifeCycleAdminService.invokeAspectWithParams(sessionCookie, trunk, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        testBranch = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceNamePreserveFalse;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, testBranch);
        Resource service = registry.get(testBranch);
        Assert.assertNotNull(service, "Service Not found on registry path " + testBranch);
        Assert.assertEquals(service.getPath(), testBranch, "Service not in branches/testing. " + testBranch);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 5), "LifeCycle properties missing some properties");

        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getValues()[0], "Testing",
                            "LifeCycle State Mismatched");


        try {
            registry.get(trunk);
            Assert.fail(trunk + " Resource exist");
        } catch (RegistryException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Resource does not exist at path /_system/governance/trunk/services/"));
        }

    }

    @Test(priority = 2, dependsOnMethods = {"preserveOriginalFalseAndPromoteToTesting"}, description = "Promote Service and delete original")
    public void preserveOriginalFalseAndPromoteToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{testBranch, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "false"});

        lifeCycleAdminService.invokeAspectWithParams(sessionCookie, testBranch, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        proBranch = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceNamePreserveFalse;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, proBranch);

        Resource service = registry.get(proBranch);
        Assert.assertNotNull(service, "Service Not found on registry path " + proBranch);
        Assert.assertEquals(service.getPath(), proBranch, "Service not in branches/production. " + proBranch);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");

        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[0], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[0].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[0].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[0].getValues()[0], "Production",
                            "LifeCycle State Mismatched");

        try {
            registry.get(testBranch);
            Assert.fail(trunk + " Resource exist");
        } catch (RegistryException e) {
            Assert.assertTrue(e.getCause().getMessage().contains("Resource does not exist at path /_system/governance/branches/testing/services/"));
        }

    }

    @Test(priority = 3, description = "Promote Service preserve original")
    public void preserveOriginalAndPromoteToTesting() throws Exception {
        Thread.sleep(1000);

        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{trunkPreserve, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(sessionCookie, trunkPreserve, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        testBranch = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceNamePreserveTrue;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, testBranch);
        Resource service = registry.get(testBranch);
        Assert.assertNotNull(service, "Service Not found on registry path " + testBranch);
        Assert.assertEquals(service.getPath(), testBranch, "Service not in branches/testing. " + testBranch);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 5), "LifeCycle properties missing some properties");

        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getValues()[0], "Testing",
                            "LifeCycle State Mismatched");


        Assert.assertEquals(registry.get(trunkPreserve).getPath(), trunkPreserve, "Resource not exist on trunk");

    }

    @Test(priority = 4, dependsOnMethods = {"preserveOriginalAndPromoteToTesting"},
          description = "Promote Service and preserve original")
    public void preserveOriginalAndPromoteToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{testBranch, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(sessionCookie, testBranch, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        proBranch = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceNamePreserveTrue;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, proBranch);

        Resource service = registry.get(proBranch);
        Assert.assertNotNull(service, "Service Not found on registry path " + proBranch);
        Assert.assertEquals(service.getPath(), proBranch, "Service not in branches/production. " + proBranch);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");

        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[0], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[0].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[0].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[0].getValues()[0], "Production",
                            "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(proBranch).getPath(), proBranch, "Resource not exist on branch");
    }


}
