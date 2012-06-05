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
import org.wso2.carbon.admin.service.ActivitySearchAdminService;
import org.wso2.carbon.admin.service.LifeCycleAdminService;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import java.rmi.RemoteException;
import java.util.Calendar;

public class DefaultServiceLifeCycleTest {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminService lifeCycleAdminService;
    private ActivitySearchAdminService activitySearch;
    private UserInfo userInfo;

    private final String serviceName = "serviceForLifeCycleTest";
    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    //    private final String ACTION_DEMOTE = "Demote";
    private String servicePathDev;
    private String servicePathTest;
    private String servicePathProd;

    private String testBranch;

    @BeforeClass
    public void init() throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();
        userInfo = UserListCsvReader.getUserInfo(userId);
        sessionCookie = gregServer.getSessionCookie();
        lifeCycleAdminService = new LifeCycleAdminService(gregServer.getBackEndUrl());
        activitySearch = new ActivitySearchAdminService(gregServer.getBackEndUrl());
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registry, userId);

        servicePathDev = "/_system/governance" + Utils.addService("sns", serviceName, governance);
        Thread.sleep(1000);

    }

    @Test(priority = 1, description = "Add lifecycle to a service")
    public void addLifecycle()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException, RegistryExceptionException {
        registry.associateAspect(servicePathDev, aspectName);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathDev);
        Resource service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 5), "LifeCycle properties missing some properties");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getValues()[0], "Development",
                            "LifeCycle State Mismatched");


        //Activity search
        Thread.sleep(1000 * 10);
        ActivityBean activityObj = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , servicePathDev, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_ASSOCIATE_ASPECT, 1);
        Assert.assertNotNull(activityObj, "Activity object null");
        Assert.assertNotNull(activityObj.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObj.getActivity().length > 0), "Activity list object null");
        for (String activity : activityObj.getActivity()) {
            Assert.assertTrue(activity.contains(userInfo.getUserName()), "User name not found on activity last activity. " + activity);
            Assert.assertTrue(activity.contains("associated the aspect ServiceLifeCycle"),
                              "associated the aspect ServiceLifeCycle not contain in last activity. " + activity);
            Assert.assertTrue(activity.contains("0m ago"), "current time not found on activity. " + activity);
            break;
        }

    }

    @Test(priority = 2, dependsOnMethods = {"addLifecycle"}, description = "Promote Service")
    public void promoteServiceToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathDev, aspectName, ACTION_PROMOTE, null);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0-SNAPSHOT/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTest);
        Resource service = registry.get(servicePathTest);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTest);
        Assert.assertEquals(service.getPath(), servicePathTest, "Service not in branches/testing. " + servicePathTest);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 5), "LifeCycle properties missing some properties");

        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[4].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[4].getValues()[0], "Testing",
                            "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , servicePathDev, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        for (String activity : activityObjTrunk.getActivity()) {
            Assert.assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
            Assert.assertTrue(activity.contains("has updated the resource"),
                              "Activity not found. has updated not contain in last activity. " + activity);
            Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);
            break;
        }

        //activity search for test branch
        ActivityBean activityObjTest = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , servicePathTest, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_ALL, 1);
        Assert.assertNotNull(activityObjTest, "Activity object null");
        Assert.assertNotNull(activityObjTest.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTest.getActivity().length > 0), "Activity list object null");
        for (String activity : activityObjTest.getActivity()) {
            Assert.assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
            Assert.assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                              "Activity not found. has added or has updated not contain in last activity. " + activity);
            Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);
            break;
        }

    }

    @Test(priority = 3, dependsOnMethods = {"promoteServiceToTesting"}, description = "Promote Service")
    public void promoteServiceToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathTest, aspectName, ACTION_PROMOTE, null);
        servicePathProd = "/_system/governance/branches/production/services/sns/1.0.0-SNAPSHOT/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathProd);

        Resource service = registry.get(servicePathProd);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathProd);
        Assert.assertEquals(service.getPath(), servicePathProd, "Service not in branches/production. " + servicePathProd);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");

        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[0], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[0].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[0].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[0].getValues()[0], "Production",
                            "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(servicePathTest).getPath(), servicePathTest, "Preserve original failed");


        //activity search for test branch
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTest = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , servicePathTest, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTest, "Activity object null");
        Assert.assertNotNull(activityObjTest.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTest.getActivity().length > 0), "Activity list object null");
        for (String activity : activityObjTest.getActivity()) {
            Assert.assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
            Assert.assertTrue(activity.contains("has updated the resource"),
                              "Activity not found. has updated not contain in last activity. " + activity);
            Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on activity. " + activity);
            break;
        }

        //activity search for production branch
        ActivityBean activityObjProd = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , servicePathProd, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_ALL, 1);
        Assert.assertNotNull(activityObjProd, "Activity object null");
        Assert.assertNotNull(activityObjProd.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjProd.getActivity().length > 0), "Activity list object null");
        for (String activity : activityObjProd.getActivity()) {
            Assert.assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on activity. " + activity);
            Assert.assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                              "Activity not found. has added not contain in activity. " + activity);
            Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on activity. " + activity);
            break;
        }

    }

    @Test(priority = 4, dependsOnMethods = {"addLifecycle"}, description = "Promote Service to testing with new version")
    public void promoteServiceToTestingWithNewVersion()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "1.0.0"});

        lifeCycleAdminService.invokeAspectWithParams(sessionCookie, servicePathDev, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        testBranch = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceName;
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

        Assert.assertEquals(registry.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , servicePathDev, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        for (String activity : activityObjTrunk.getActivity()) {
            Assert.assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
            Assert.assertTrue(activity.contains("has updated the resource"),
                              "Activity not found. has updated not contain in last activity. " + activity);
            Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);
            break;
        }

        //activity search for test branch
        ActivityBean activityObjTest = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , testBranch, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_ALL, 1);
        Assert.assertNotNull(activityObjTest, "Activity object null");
        Assert.assertNotNull(activityObjTest.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTest.getActivity().length > 0), "Activity list object null");
        for (String activity : activityObjTest.getActivity()) {
            Assert.assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
            Assert.assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                              "Activity not found. has added or has updated not contain in last activity. " + activity);
            Assert.assertTrue(activity.contains("1.0.0"), "Activity not found. version not found on last activity. " + activity);
            Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);
            break;
        }

    }

    @Test(priority = 5, dependsOnMethods = {"promoteServiceToTestingWithNewVersion"},
          description = "Promote Service to production with new version")
    public void promoteServiceToProductionWithNewVersion()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        Assert.assertNotNull(testBranch, "test brunch path not found");
        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{testBranch, "1.0.0"});

        lifeCycleAdminService.invokeAspectWithParams(sessionCookie, testBranch, aspectName, ACTION_PROMOTE
                , null, parameters);
        String prodBranch = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, prodBranch);

        Resource service = registry.get(prodBranch);
        Assert.assertNotNull(service, "Service Not found on registry path " + prodBranch);
        Assert.assertEquals(service.getPath(), prodBranch, "Service not in branches/production. " + prodBranch);
        Assert.assertTrue((lifeCycle.getLifecycleProperties().length > 0), "LifeCycle properties missing some properties");

        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[0], "LifeCycle State property not found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[0].getKey(), "registry.lifecycle.ServiceLifeCycle.state",
                            "LifeCycle State property not found");
        Assert.assertNotNull(lifeCycle.getLifecycleProperties()[0].getValues(), "State Value Not Found");
        Assert.assertEquals(lifeCycle.getLifecycleProperties()[0].getValues()[0], "Production",
                            "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(testBranch).getPath(), testBranch, "Preserve original failed");

        //activity search for test branch
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTest = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , testBranch, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTest, "Activity object null");
        Assert.assertNotNull(activityObjTest.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTest.getActivity().length > 0), "Activity list object null");
        for (String activity : activityObjTest.getActivity()) {
            Assert.assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
            Assert.assertTrue(activity.contains("has updated the resource"),
                              "Activity not found. has updated not contain in last activity. " + activity);
            Assert.assertTrue(activity.contains("1.0.0"), "Activity not found. version not found on last activity. " + activity);
            Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on activity. " + activity);
            break;
        }

        //activity search for production branch
        ActivityBean activityObjProd = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , prodBranch, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_ALL, 1);
        Assert.assertNotNull(activityObjProd, "Activity object null");
        Assert.assertNotNull(activityObjProd.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjProd.getActivity().length > 0), "Activity list object null");
        for (String activity : activityObjProd.getActivity()) {
            Assert.assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on activity. " + activity);
            Assert.assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                              "Activity not found. has added not contain in activity. " + activity);
            Assert.assertTrue(activity.contains("1.0.0"), "Activity not found. version not found on last activity. " + activity);
            Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on activity. " + activity);
            break;
        }


    }


    @AfterClass
    public void destroy() {
        servicePathDev = null;
        servicePathTest = null;
        servicePathProd = null;
    }


}
