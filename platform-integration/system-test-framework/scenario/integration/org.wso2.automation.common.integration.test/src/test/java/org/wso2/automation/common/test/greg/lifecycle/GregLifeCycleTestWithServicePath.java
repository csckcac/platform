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
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.greg.lifecycle.utils.Utils;
import org.wso2.carbon.admin.service.LifeCycleAdminService;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
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
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import java.rmi.RemoteException;

/**
 * Covers the public jira https://wso2.org/jira/browse/CARBON-12975 Missing the service paths
 * while promoting Life Cycle
 */

public class GregLifeCycleTestWithServicePath {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminService lifeCycleAdminService;

    private final String ASPECT_NAME = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ASS_TYPE_DEPENDS = "depends";
    //    private final String ACTION_DEMOTE = "Demote";
    private String servicePathTrunk = null;
    private String servicePathTest;
    private String[] dependencyList = null;
    private String backEndUrl;
    Registry governance;
    private String serviceUrl;
    String wsdlPath = null;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment()
            throws LoginAuthenticationExceptionException, RemoteException, RegistryException {

        int userId = 4;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getGreg().getBackEndUrl();
        serviceUrl = environment.getGreg().getServiceUrl();
        sessionCookie = environment.getGreg().getSessionCookie();
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        lifeCycleAdminService = new LifeCycleAdminService(backEndUrl);
        governance = new RegistryProvider().getGovernance(registry, userId);

    }

    @BeforeClass
    public void deployArtifact() throws Exception {

        wsdlPath = "/_system/governance" + Utils.addWSDL("echoDependency.wsdl", governance);
        Association[] usedBy = registry.getAssociations(wsdlPath, "usedBy");
        for (Association association : usedBy) {
            if (association.getSourcePath().equalsIgnoreCase(wsdlPath)) {
                servicePathTrunk = association.getDestinationPath();
            }
        }

        lifeCycleAdminService.addAspect(sessionCookie, servicePathTrunk, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertEquals(service.getPath(), servicePathTrunk, "Service path changed after adding life cycle. " + servicePathTrunk);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");


    }

    @Test(priority = 1, description = "Promote service to Test")
    public void promoteToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException, InterruptedException {
        ArrayOfString[] parameters = new ArrayOfString[2];
        servicePathTest = "/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/2.0.0/echoyuSer1";

        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathTrunk, ASPECT_NAME,
                                           ACTION_PROMOTE, null);
        Thread.sleep(2000);

        Thread.sleep(500);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTest);
    }

    @Test(priority = 3, description = "Promote service to Production")
    public void promoteToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException, InterruptedException {
        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{"/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/1.0.0-SNAPSHOT/echoyuSer1", "1.0.0"});

        String servicePathProd = "/_system/governance/branches/production/services/org/wso2/carbon/core/services/echo/1.0.0/echoyuSer1";
        lifeCycleAdminService.invokeAspectWithParams(sessionCookie, "/_system/governance/branches/testing/services/org/wso2/carbon/core/services/echo/1.0.0-SNAPSHOT/echoyuSer1", ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);
        Thread.sleep(2000);

        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathProd);
        Resource service = registry.get(servicePathProd);
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Production", "LifeCycle State Mismatched");
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathProd);


    }
}
