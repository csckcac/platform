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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleAdminServiceClient;
import org.wso2.carbon.automation.api.clients.governance.LifeCycleManagementClient;
import org.wso2.carbon.automation.api.clients.registry.ActivityAdminServiceClient;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.lifecycle.test.utils.Utils;
import org.wso2.carbon.registry.search.metadata.test.utils.GregTestUtils;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;

import java.rmi.RemoteException;
import java.util.Calendar;

public class DefaultServiceLifeCycleTestCase {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminServiceClient lifeCycleAdminService;
    private ActivityAdminServiceClient activitySearch;
    private String userName;

    private final String serviceName = "serviceForLifeCycleTest";
    private final String aspectName = "ServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ACTION_DEMOTE = "Demote";
    private String servicePathDev;
    private String servicePathTest;
    private String servicePathProd;

    private String testBranch;

    @BeforeClass
    public void init() throws Exception {
        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        sessionCookie = new LoginLogoutUtil().login();
        final String SERVER_URL = GregTestUtils.getServerUrl();;
        userName = FrameworkSettings.USER_NAME;
        lifeCycleAdminService = new LifeCycleAdminServiceClient(SERVER_URL, sessionCookie);
        activitySearch = new ActivityAdminServiceClient(SERVER_URL);
        registry = GregTestUtils.getRegistry();
        Registry governance = GregTestUtils.getGovernanceRegistry(registry);

        servicePathDev = "/_system/governance" + Utils.addService("sns", serviceName, governance);
        Thread.sleep(1000);

    }

    @Test(priority = 1, description = "Add lifecycle to a service")
    public void addLifecycle()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException, RegistryExceptionException {
        registry.associateAspect(servicePathDev, aspectName);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathDev);
        Resource service = registry.get(servicePathDev);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathDev);
        Assert.assertEquals(service.getPath(), servicePathDev, "Service path changed after adding life cycle. " + servicePathDev);
        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Development", "LifeCycle State Mismatched");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Code Completed", "Code Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:WSDL, Schema Created", "WSDL, Schema Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:QoS Created", "QoS Created Check List Item Not Found");

        //Activity search
        Thread.sleep(1000 * 10);
        ActivityBean activityObj = activitySearch.getActivities(sessionCookie, userName
                , servicePathDev, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ASSOCIATE_ASPECT, 1);
        Assert.assertNotNull(activityObj, "Activity object null for Associate Aspect");
        Assert.assertNotNull(activityObj.getActivity(), "Activity list object null for Associate Aspect");
        Assert.assertTrue((activityObj.getActivity().length > 0), "Activity list object null");
        String activity = activityObj.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "User name not found on activity last activity. " + activity);
        Assert.assertTrue(activity.contains("associated the aspect ServiceLifeCycle"),
                          "associated the aspect ServiceLifeCycle not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "current time not found on activity. " + activity);


    }

    @Test(priority = 2, dependsOnMethods = {"addLifecycle"}, description = "Promote Service")
    public void promoteServiceToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        lifeCycleAdminService.invokeAspect(servicePathDev, aspectName, ACTION_PROMOTE, null);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0-SNAPSHOT/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTest);
        Resource service = registry.get(servicePathTest);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTest);
        Assert.assertEquals(service.getPath(), servicePathTest, "Service not in branches/testing. " + servicePathTest);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Testing",
                            "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Effective Inspection Completed", "Effective Inspection Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:Test Cases Passed", "Test Cases Passed  Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:Smoke Test Passed", "Smoke Test Passed Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathDev, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


        //activity search for test branch
        ActivityBean activityObjTest = activitySearch.getActivities(sessionCookie, userName
                , servicePathTest, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ALL, 1);
        Assert.assertNotNull(activityObjTest, "Activity object null");
        Assert.assertNotNull(activityObjTest.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTest.getActivity().length > 0), "Activity list object null");

        activity = activityObjTest.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                          "Activity not found. has added or has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


    }

    @Test(priority = 3, dependsOnMethods = {"promoteServiceToTesting"}, description = "Promote Service")
    public void promoteServiceToProduction()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        lifeCycleAdminService.invokeAspect(servicePathTest, aspectName, ACTION_PROMOTE, null);
        servicePathProd = "/_system/governance/branches/production/services/sns/1.0.0-SNAPSHOT/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathProd);

        Resource service = registry.get(servicePathProd);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathProd);
        Assert.assertEquals(service.getPath(), servicePathProd, "Service not in branches/production. " + servicePathProd);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Production",
                            "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(servicePathTest).getPath(), servicePathTest, "Preserve original failed");


        //activity search for test branch
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTest = activitySearch.getActivities(sessionCookie, userName
                , servicePathTest, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTest, "Activity object null");
        Assert.assertNotNull(activityObjTest.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTest.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTest.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on activity. " + activity);


        //activity search for production branch
        ActivityBean activityObjProd = activitySearch.getActivities(sessionCookie, userName
                , servicePathProd, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ALL, 1);
        Assert.assertNotNull(activityObjProd, "Activity object null");
        Assert.assertNotNull(activityObjProd.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjProd.getActivity().length > 0), "Activity list object null");
        activity = activityObjProd.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on activity. " + activity);
        Assert.assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                          "Activity not found. has added not contain in activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on activity. " + activity);

    }

    @Test(priority = 2, dependsOnMethods = {"promoteServiceToProduction"}, description = "Promote Service")
    public void demoteServiceToTesting()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        lifeCycleAdminService.invokeAspect(servicePathProd, aspectName, ACTION_DEMOTE, null);
        servicePathTest = "/_system/governance/branches/testing/services/sns/1.0.0-SNAPSHOT/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(servicePathTest);
        Resource service = registry.get(servicePathTest);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTest);
        Assert.assertEquals(service.getPath(), servicePathTest, "Service not in branches/testing. " + servicePathTest);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Testing",
                            "LifeCycle State Mismatched");


        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Effective Inspection Completed", "Effective Inspection Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:Test Cases Passed", "Test Cases Passed  Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:Smoke Test Passed", "Smoke Test Passed Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathProd, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


        //activity search for test branch
        ActivityBean activityObjTest = activitySearch.getActivities(sessionCookie, userName
                , servicePathTest, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ALL, 1);
        Assert.assertNotNull(activityObjTest, "Activity object null");
        Assert.assertNotNull(activityObjTest.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTest.getActivity().length > 0), "Activity list object null");

        activity = activityObjTest.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                          "Activity not found. has added or has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


    }

    @Test(priority = 4, dependsOnMethods = {"addLifecycle"}, description = "Promote Service to testing with new version")
    public void promoteServiceToTestingWithNewVersion()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   InterruptedException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[1];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathDev, "1.0.0"});

        lifeCycleAdminService.invokeAspectWithParams(servicePathDev, aspectName,
                                                     ACTION_PROMOTE, null, parameters);
        testBranch = "/_system/governance/branches/testing/services/sns/1.0.0/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(testBranch);
        Resource service = registry.get(testBranch);
        Assert.assertNotNull(service, "Service Not found on registry path " + testBranch);
        Assert.assertEquals(service.getPath(), testBranch, "Service not in branches/testing. " + testBranch);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Testing",
                            "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(servicePathDev).getPath(), servicePathDev, "Preserve original failed");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userName
                , servicePathDev, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);

        //activity search for test branch
        ActivityBean activityObjTest = activitySearch.getActivities(sessionCookie, userName
                , testBranch, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ALL, 1);
        Assert.assertNotNull(activityObjTest, "Activity object null");
        Assert.assertNotNull(activityObjTest.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTest.getActivity().length > 0), "Activity list object null");

        activity = activityObjTest.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                          "Activity not found. has added or has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("1.0.0"), "Activity not found. version not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);

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

        lifeCycleAdminService.invokeAspectWithParams(testBranch, aspectName, ACTION_PROMOTE
                , null, parameters);
        String prodBranch = "/_system/governance/branches/production/services/sns/1.0.0/" + serviceName;
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(prodBranch);

        Resource service = registry.get(prodBranch);
        Assert.assertNotNull(service, "Service Not found on registry path " + prodBranch);
        Assert.assertEquals(service.getPath(), prodBranch, "Service not in branches/production. " + prodBranch);

        Assert.assertEquals(Utils.getLifeCycleState(lifeCycle), "Production",
                            "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(testBranch).getPath(), testBranch, "Preserve original failed");

        //activity search for test branch
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTest = activitySearch.getActivities(sessionCookie, userName
                , testBranch, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTest, "Activity object null");
        Assert.assertNotNull(activityObjTest.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTest.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTest.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("1.0.0"), "Activity not found. version not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on activity. " + activity);


        //activity search for production branch
        ActivityBean activityObjProd = activitySearch.getActivities(sessionCookie, userName
                , prodBranch, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivityAdminServiceClient.FILTER_ALL, 1);
        Assert.assertNotNull(activityObjProd, "Activity object null");
        Assert.assertNotNull(activityObjProd.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjProd.getActivity().length > 0), "Activity list object null");
        activity = activityObjProd.getActivity()[0];
        Assert.assertTrue(activity.contains(userName), "Activity not found. User name not found on activity. " + activity);
        Assert.assertTrue(activity.contains("has added the resource") || activity.contains("has updated the resource"),
                          "Activity not found. has added not contain in activity. " + activity);
        Assert.assertTrue(activity.contains("1.0.0"), "Activity not found. version not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on activity. " + activity);


    }


    @AfterClass
    public void destroy() {
        servicePathDev = null;
        servicePathTest = null;
        servicePathProd = null;
    }


}
