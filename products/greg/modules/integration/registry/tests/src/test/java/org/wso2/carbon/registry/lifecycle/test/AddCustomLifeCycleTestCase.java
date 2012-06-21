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
package org.wso2.carbon.registry.lifecycle.test;

import org.apache.axis2.AxisFault;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.ActivitySearchAdminService;
import org.wso2.carbon.admin.service.LifeCycleAdminService;
import org.wso2.carbon.admin.service.LifeCycleManagerAdminService;
import org.wso2.carbon.admin.service.RegistrySearchAdminService;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.Property;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.Utils;
import org.wso2.carbon.registry.search.metadata.test.bean.SearchParameterBean;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.search.stub.SearchAdminServiceRegistryExceptionException;
import org.wso2.carbon.registry.search.stub.beans.xsd.AdvancedSearchResultsBean;
import org.wso2.carbon.registry.search.stub.beans.xsd.ArrayOfString;
import org.wso2.carbon.registry.search.stub.beans.xsd.CustomSearchParameterBean;
import org.wso2.carbon.registry.search.stub.common.xsd.ResourceData;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Calendar;

public class AddCustomLifeCycleTestCase {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminService lifeCycleAdminService;
    private LifeCycleManagerAdminService lifeCycleManagerAdminService;
    private ActivitySearchAdminService activitySearch;
    private RegistrySearchAdminService searchAdminService;
    private String userName;

    private final String ASPECT_NAME = "IntergalacticServiceLC";
    private String servicePathDev;

    @BeforeClass
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();
        userName = FrameworkSettings.USER_NAME;
        lifeCycleAdminService = new LifeCycleAdminService(SERVER_URL);
        activitySearch = new ActivitySearchAdminService(SERVER_URL);
        lifeCycleManagerAdminService = new LifeCycleManagerAdminService(SERVER_URL);
        searchAdminService = new RegistrySearchAdminService(SERVER_URL);
        registry = GregTestUtils.getRegistry();
        Registry governance = GregTestUtils.getGovernanceRegistry(registry);

        String serviceName = "CustomLifeCycleTestService";
        Utils.deleteLifeCycleIfExist(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);
        servicePathDev = "/_system/governance" + Utils.addService("sns", serviceName, governance);
        Thread.sleep(1000);

    }

    @Test(priority = 1, description = "Add new Life Cycle")
    public void createNewLifeCycle()
            throws IOException, LifeCycleManagementServiceExceptionException, InterruptedException,
                   SearchAdminServiceRegistryExceptionException {
        String filePath = GregTestUtils.getResourcePath()
                          + File.separator + "lifecycle" + File.separator + "customLifeCycle.xml";
        String lifeCycleConfiguration = GregTestUtils.readFile(filePath);
        Assert.assertTrue(lifeCycleManagerAdminService.addLifeCycle(sessionCookie, lifeCycleConfiguration)
                , "Adding New LifeCycle Failed");
        Thread.sleep(2000);
        lifeCycleConfiguration = lifeCycleManagerAdminService.getLifecycleConfiguration(sessionCookie, ASPECT_NAME);
        Assert.assertTrue(lifeCycleConfiguration.contains("aspect name=\"IntergalacticServiceLC\""),
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
        Assert.assertEquals(getLifeCycleState(lifeCycle), "Commencement",
                            "LifeCycle State Mismatched");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Requirements Gathered", "Requirements Gathered Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:Document Requirements", "Document Requirements Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Design UML Diagrams", "Design UML Diagrams Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");

        //Activity search
        Thread.sleep(1000 * 10);
        ActivityBean activityObj = activitySearch.getActivities(sessionCookie, userName
                , servicePathDev, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_ASSOCIATE_ASPECT, 1);
        Assert.assertNotNull(activityObj, "Activity object null for Associate Aspect");
        Assert.assertNotNull(activityObj.getActivity(), "Activity list object null for Associate Aspect");
        Assert.assertTrue((activityObj.getActivity().length > 0), "Activity list object null");
        String activity = activityObj.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "User name not found on activity last activity. " + activity);
        Assert.assertTrue(activity.contains("associated the aspect IntergalacticServiceLC"),
                          "associated the aspect ServiceLifeCycle not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "current time not found on activity. " + activity);
    }

    @Test(priority = 3, description = "delete LifeCycle when there is usage", dependsOnMethods = {"addLifeCycleToService"})
    public void deleteLifeCycleWhenHavingUsage()
            throws LifeCycleManagementServiceExceptionException, RemoteException {
        Assert.assertTrue(lifeCycleManagerAdminService.isLifecycleNameInUse(sessionCookie, ASPECT_NAME),
                          "No Usage Found for Life Cycle");
        try {
            Assert.assertFalse(lifeCycleManagerAdminService.deleteLifeCycle(sessionCookie, ASPECT_NAME),
                               "Life Cycle Deleted even if there is a usage");
            Assert.fail("Life Cycle Deleted even if there is a usage");
        } catch (AxisFault e) {
            Assert.assertEquals(e.getMessage(), "Lifecycle could not be deleted, since it is already in use!",
                                "Message mismatched");
        }
    }


    @AfterClass
    public void deleteLifeCycle()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException, InterruptedException,
                   SearchAdminServiceRegistryExceptionException {
        if (servicePathDev != null) {
            registry.delete(servicePathDev);
        }
        Assert.assertTrue(lifeCycleManagerAdminService.deleteLifeCycle(sessionCookie, ASPECT_NAME),
                          "Life Cycle Deleted failed");
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
            if ("registry.lifecycle.IntergalacticServiceLC.state".equalsIgnoreCase(prop.getKey())) {
                stateFound = true;
                Assert.assertNotNull(prop.getValues(), "State Value Not Found");
                state = prop.getValues()[0];

            }
        }
        Assert.assertTrue(stateFound, "LifeCycle State property not found");
        return state;
    }


}
