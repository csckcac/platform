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
import org.wso2.automation.common.test.greg.metadatasearch.bean.SearchParameterBean;
import org.wso2.carbon.admin.service.LifeCycleManagerAdminService;
import org.wso2.carbon.admin.service.RegistrySearchAdminService;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;

import java.rmi.RemoteException;

public class EditLifeCycleName {
    private String sessionCookie;

    private LifeCycleManagerAdminService lifeCycleManagerAdminService;
    private RegistrySearchAdminService searchAdminService;

    private final String ASPECT_NAME = "NewServiceLifeCycle";
    private final String NEW_ASPECT_NAME = "EditedServiceLC";

    @BeforeClass
    public void init() throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();
        sessionCookie = gregServer.getSessionCookie();
        lifeCycleManagerAdminService = new LifeCycleManagerAdminService(gregServer.getBackEndUrl());
        searchAdminService = new RegistrySearchAdminService(gregServer.getBackEndUrl());

        Utils.deleteLifeCycleIfExist(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);
        Utils.deleteLifeCycleIfExist(sessionCookie, NEW_ASPECT_NAME, lifeCycleManagerAdminService);
        Thread.sleep(1000);
        Utils.createNewLifeCycle(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);
        Thread.sleep(1000);

    }

    @Test(description = "Edit Life Cycle Name")
    public void editLifeCycleName()
            throws LifeCycleManagementServiceExceptionException, RemoteException,
                   InterruptedException {
        String config = lifeCycleManagerAdminService.getLifecycleConfiguration(sessionCookie, ASPECT_NAME);
        Assert.assertTrue(config.contains("aspect name=\"" + ASPECT_NAME + "\""),
                          "LifeCycleName Not Found in lifecycle configuration");
        String newLifeCycleConfiguration = config.replace(ASPECT_NAME, NEW_ASPECT_NAME);
        Assert.assertTrue(lifeCycleManagerAdminService.editLifeCycle(sessionCookie, ASPECT_NAME, newLifeCycleConfiguration)
                , "Editing LifeCycle Name Failed");
        Thread.sleep(1000);

        newLifeCycleConfiguration = lifeCycleManagerAdminService.getLifecycleConfiguration(sessionCookie, NEW_ASPECT_NAME);
        Assert.assertTrue(newLifeCycleConfiguration.contains("aspect name=\"" + NEW_ASPECT_NAME + "\""),
                          "New LifeCycleName Not Found in lifecycle configuration");

        String[] lifeCycleList = lifeCycleManagerAdminService.getLifecycleList(sessionCookie);
        Assert.assertNotNull(lifeCycleList);
        Assert.assertTrue(lifeCycleList.length > 0, "Life Cycle List length zero");
        boolean found = false;
        for (String lc : lifeCycleList) {
            if (NEW_ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        Assert.assertTrue(found, "Life Cycle list not contain edited life cycle");

        //Old Name should not be here
        found = false;
        for (String lc : lifeCycleList) {
            if (ASPECT_NAME.equalsIgnoreCase(lc)) {
                found = true;
            }
        }
        Assert.assertFalse(found, "Life Cycle list not contain Old life cycle");
    }

    @Test(description = "Metadata Search for Edited Life Cycle", dependsOnMethods = {"editLifeCycleName"})
    public void searchNewLifeCycleByName()
            throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(NEW_ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNotNull(result.getResourceDataList(), "No Record Found");
        Assert.assertTrue((result.getResourceDataList().length == 1), "No Record Found for Life Cycle " +
                                                                      "Name or more record found");
        for (ResourceData resource : result.getResourceDataList()) {
            Assert.assertEquals(resource.getName(), NEW_ASPECT_NAME,
                                "Life Cycle Name mismatched :" + resource.getResourcePath());
            Assert.assertTrue(resource.getResourcePath().contains("lifecycles"),
                              "Life Cycle Path does not contain lifecycles collection :" + resource.getResourcePath());
        }
    }

    @Test(description = "Metadata search by old Life Cycle Name", dependsOnMethods = {"editLifeCycleName"})
    public void searchOldLifeCycleByName() throws SearchAdminServiceRegistryExceptionException, RemoteException {
        CustomSearchParameterBean searchQuery = new CustomSearchParameterBean();
        SearchParameterBean paramBean = new SearchParameterBean();
        paramBean.setResourceName(ASPECT_NAME);
        ArrayOfString[] paramList = paramBean.getParameterList();

        searchQuery.setParameterValues(paramList);
        AdvancedSearchResultsBean result = searchAdminService.getAdvancedSearchResults(sessionCookie, searchQuery);
        Assert.assertNull(result.getResourceDataList(), "Life Cycle Record Found even if it is deleted");

    }

}
