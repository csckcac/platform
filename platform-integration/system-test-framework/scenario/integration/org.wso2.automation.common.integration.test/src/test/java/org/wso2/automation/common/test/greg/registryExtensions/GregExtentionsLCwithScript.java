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

package org.wso2.automation.common.test.greg.registryExtensions;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.greg.lifecycle.utils.Utils;
import org.wso2.automation.common.test.greg.metadatasearch.bean.SearchParameterBean;
import org.wso2.carbon.admin.service.ActivitySearchAdminService;
import org.wso2.carbon.admin.service.LifeCycleAdminService;
import org.wso2.carbon.admin.service.LifeCycleManagerAdminService;
import org.wso2.carbon.admin.service.RegistrySearchAdminService;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.fileutils.FileManager;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;

public class GregExtentionsLCwithScript {

    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminService lifeCycleAdminService;
    private LifeCycleManagerAdminService lifeCycleManagerAdminService;
    private ActivitySearchAdminService activitySearch;
    private RegistrySearchAdminService searchAdminService;
    private UserInfo userInfo;

    private final String ASPECT_NAME = "LCwithScript";
    private String servicePathDev;
    LifecycleBean lifeCycle;

    private final String ACTION_PROMOTE = "Promote";
    private final String ASS_TYPE_DEPENDS = "depends";
    //    private final String ACTION_DEMOTE = "Demote";
    private String servicePathTrunk = null;
    private String servicePathTest;

    @BeforeClass
    public void deployArtifacts() throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();
        userInfo = UserListCsvReader.getUserInfo(userId);
        sessionCookie = gregServer.getSessionCookie();
        lifeCycleAdminService = new LifeCycleAdminService(gregServer.getBackEndUrl());
        activitySearch = new ActivitySearchAdminService(gregServer.getBackEndUrl());
        lifeCycleManagerAdminService = new LifeCycleManagerAdminService(gregServer.getBackEndUrl());
        searchAdminService = new RegistrySearchAdminService(gregServer.getBackEndUrl());
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registry, userId);

        String serviceName = "CustomLifeCycleTestService2";
        //   Utils.deleteLifeCycleIfExist(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);
        servicePathDev = "/_system/governance" + Utils.addService("sns", serviceName, governance);
        Thread.sleep(1000);

    }

    @Test(priority = 1, description = "Add new Life Cycle")
    public void createNewLifeCycle()
            throws IOException, LifeCycleManagementServiceExceptionException, InterruptedException,
                   SearchAdminServiceRegistryExceptionException {
        String filePath = ProductConstant.getResourceLocations(ProductConstant.GREG_SERVER_NAME)
                          + File.separator + "lifecycle" + File.separator + "lcWithScript.xml";
        String lifeCycleConfiguration = FileManager.readFile(filePath);
        Assert.assertTrue(lifeCycleManagerAdminService.addLifeCycle(sessionCookie, lifeCycleConfiguration)
                , "Adding New LifeCycle Failed");
        Thread.sleep(2000);
        lifeCycleConfiguration = lifeCycleManagerAdminService.getLifecycleConfiguration(sessionCookie, ASPECT_NAME);
        Assert.assertTrue(lifeCycleConfiguration.contains("aspect name=\"LCwithScript\""),
                          "LifeCycleName Not Found in lifecycle configuration");

        String[] lifeCycleList = lifeCycleManagerAdminService.getLifecycleList(sessionCookie);
        Assert.assertNotNull(lifeCycleList);
        Assert.assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        Assert.assertTrue(found, "Life Cycle list not contain newly added life cycle");

        //Metadata Search By Life Cycle Name
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length == 1), "No Record Found for Life Cycle " +
                                                                      "Name or more record found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(resource.getName(), ASPECT_NAME,
                                "Life Cycle Name mismatched :" + resource.getResourcePath());
            Assert.assertTrue(resource.getResourcePath().contains("lifecycles"),
                              "Life Cycle Path does not contain lifecycles collection :" + resource.getResourcePath());
        }
    }

    @Test(priority = 2, description = "Add LifeCycle to a service", dependsOnMethods = {"createNewLifeCycle"})
    public void addLifeCycleToService()
            throws RegistryException, InterruptedException,
                   CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryExceptionException {
        registry.associateAspect(servicePathDev, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathDev);
        Resource service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        Assert.assertEquals(getLifeCycleState(lifeCycle), "Created",
                            "LifeCycle State Mismatched");
    }

    @Test(priority = 3, description = "Promote service to Test")
    public void promoteToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   RegistryException, InterruptedException {
        org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString[] parameters = new org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString[2];
        servicePathTest = "/_system/governance/Phidiax/NonApproved/services/sns/CustomLifeCycleTestService";

        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathDev, ASPECT_NAME,
                                           ACTION_PROMOTE, null);
        Thread.sleep(2000);

        Thread.sleep(500);
        Resource service = registry.get(servicePathTest);
        Assert.assertNotNull(service, "Resource is not copied " + servicePathTest);
    }


    @AfterClass
    public void deleteLifeCycle()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException, InterruptedException,
                   SearchAdminServiceRegistryExceptionException {
        registry.removeAspect(ASPECT_NAME);

        if (servicePathDev != null) {
            registry.delete(servicePathDev);

        }
        //Assert.assertTrue(lifeCycleManagerAdminService.deleteLifeCycle(sessionCookie, ASPECT_NAME),
        //                "Life Cycle Deleted failed");
        Thread.sleep(2000);
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Life Cycle Record Found even if it is deleted");


        registry = null;
        activitySearch = null;
        lifeCycleAdminService = null;
    }

    public static String getLifeCycleState(LifecycleBean lifeCycle) {
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");
        String state = null;
        boolean stateFound = false;
        for (Property prop : lifeCycle.getLifecycleProperties()) {
            if ("registry.lifecycle.ApprovalLifeCycle.state".equalsIgnoreCase(prop.getKey())) {
                stateFound = true;
                Assert.assertNotNull(prop.getValues(), "State Value Not Found");
                state = prop.getValues()[0];

            }
        }
        Assert.assertTrue(stateFound, "LifeCycle State property not found");
        return state;
    }
}
