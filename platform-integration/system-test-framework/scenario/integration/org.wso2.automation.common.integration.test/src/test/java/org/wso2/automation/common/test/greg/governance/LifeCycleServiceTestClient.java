/*
* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.automation.common.test.greg.governance;


import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.governance.api.wsdls.WsdlManager;
import org.wso2.carbon.governance.api.wsdls.dataobjects.Wsdl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;
import org.wso2.platform.test.core.utils.frameworkutils.productvariables.EnvironmentVariables;
import org.wso2.platform.test.core.utils.gregutils.GregUserIDEvaluator;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;
import org.testng.annotations.*;

import static org.testng.Assert.*;


public class LifeCycleServiceTestClient {
    private static final Log log = LogFactory.getLog(LifeCycleServiceTestClient.class);
    private static WSRegistryServiceClient registry = null;
    private static Registry governance = null;
    private static final String StateProperty = "registry.lifecycle.ServiceLifeCycle.state";
    String wsdl_path = "/_system/governance/trunk/wsdls/com/foo/BizService.wsdl";
    String service_path = "/_system/governance/trunk/services/com/foo/BizService";
    int sleepTime = 1000 * 3; //three seconds

    @BeforeClass(alwaysRun = true)
    public void init() throws RegistryException, AxisFault {
        int userId = new GregUserIDEvaluator().getTenantID();
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        governance = new RegistryProvider().getGovernance(registry, userId);
        deleteWSDL(); //Delete wsdl
    }


    @Test(groups = {"wso2.greg"}, description = "Add WSDL first to check life cycle test scenarios", priority = 1)
    public void testAddWSDL() throws GovernanceException {
        String wsdl_url = "http://people.wso2.com/~evanthika/wsdls/BizService.wsdl";
//        String wsdl_url = "http://svn.wso2.org/repos/wso2/trunk/carbon/components/governance/org.wso2.carbon.governance.api/src/test/resources/test-resources/wsdl/BizService.wsdl";
        WsdlManager wsdlManager = new WsdlManager(governance);
        Wsdl wsdl;
        try {
            wsdl = wsdlManager.newWsdl(wsdl_url);
            wsdl.addAttribute("creator2", "it is me");
            wsdl.addAttribute("version2", "0.01");
            wsdlManager.addWsdl(wsdl);
            log.info("LifeCycleServiceTestClient -WSDL added successfully");
        } catch (GovernanceException e) {
            log.error("Failed to add WSDL:" + e);
            throw new GovernanceException("Failed to add WSDL :" + e);
        }
    }

    @Test(groups = {"wso2.greg"}, description = "check service life cycle promote/demote test scenarios",
          dependsOnMethods = {"testAddWSDL"})
    public void testCheckLifeCycle() throws RegistryException, InterruptedException {
        String testStageState;
        FrameworkProperties properties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.GREG_SERVER_NAME);

        if (properties.getEnvironmentSettings().is_runningOnStratos()) {
            testStageState = "Tested";
        } else {
            testStageState = "Testing";
        }

        try {
            registry.associateAspect(wsdl_path, "ServiceLifeCycle");
//            System.out.println(registry.get(wsdl_path).getProperty(StateProperty));
            assertEquals(registry.get(wsdl_path).getProperty(StateProperty), "Development",
                         "Default Service Life Cycle Development State Fail:");
            Thread.sleep(sleepTime);

            registry.invokeAspect(wsdl_path, "ServiceLifeCycle", "Promote");  //Promote Life cycle to Tested State
            assertEquals(registry.get(wsdl_path).getProperty(StateProperty), testStageState,
                         "Service Life Cycle Promote to Test state fail :");
            Thread.sleep(3000);

            registry.invokeAspect(wsdl_path, "ServiceLifeCycle", "Promote");  //Promote Life cycle to Production State
            assertEquals(registry.get(wsdl_path).getProperty(StateProperty), "Production",
                         "Service Life Cycle Promote to Production state fail:");
            Thread.sleep(3000);

            registry.invokeAspect(wsdl_path, "ServiceLifeCycle", "Demote");   //Demote Life cycle to Tested State
            assertEquals(registry.get(wsdl_path).getProperty(StateProperty), testStageState,
                         "Service Life Cycle Demote to Test State fail :");
            Thread.sleep(3000);

            registry.invokeAspect(wsdl_path, "ServiceLifeCycle", "Demote"); //Demote Life cycle to Development State
            assertEquals(registry.get(wsdl_path).getProperty(StateProperty), "Development",
                         "Service Life Cycle Demote to initial state fail:");
            Thread.sleep(3000);
            deleteWSDL();      //Delete wsdl
            log.info("LifeCycleServiceTestClient testCheckLifeCycle() - Passed");
        } catch (RegistryException e) {
            log.error("Failed to Promote/Demote Life Cycle :" + e);
            throw new RegistryException("Failed to Promote/Demote Life Cycle :" + e);
        } catch (InterruptedException e) {
            log.error("Failed to Promote/Demote Life Cycle :" + e);
            throw new InterruptedException("Failed to Promote/Demote Life Cycle :" + e);
        }
    }

    private void deleteWSDL() throws RegistryException {
        try {
            if (registry.resourceExists(wsdl_path)) {
                registry.delete(wsdl_path);
            }

            if (registry.resourceExists(service_path)) {
                registry.delete(service_path);
            }
        } catch (RegistryException e) {
            log.error("Failed to delete WSDL :" + e);
            throw new RegistryException("Failed to delete WSDL :" + e);
        }
    }
}
