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
import org.wso2.carbon.admin.service.AdminServiceUserMgtService;
import org.wso2.carbon.admin.service.LifeCycleAdminService;
import org.wso2.carbon.admin.service.LifeCycleManagerAdminService;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.CustomLifecyclesChecklistAdminServiceExceptionException;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.beans.xsd.LifecycleBean;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.services.ArrayOfString;
import org.wso2.carbon.governance.custom.lifecycles.checklist.stub.util.xsd.LifecycleActions;
import org.wso2.carbon.governance.lcm.stub.LifeCycleManagementServiceExceptionException;
import org.wso2.carbon.registry.activities.stub.RegistryExceptionException;
import org.wso2.carbon.registry.activities.stub.beans.xsd.ActivityBean;
import org.wso2.carbon.registry.core.Association;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.ws.client.registry.WSRegistryServiceClient;
import org.wso2.carbon.user.mgt.common.UserAdminException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.gregutils.RegistryProvider;

import java.rmi.RemoteException;
import java.util.Calendar;

public class CustomLifeCycleDemoteForWSDLTest {
    private String sessionCookie;

    private WSRegistryServiceClient registry;
    private LifeCycleAdminService lifeCycleAdminService;
    private LifeCycleManagerAdminService lifeCycleManagerAdminService;
    private ActivitySearchAdminService activitySearch;
    private AdminServiceUserMgtService userManger;
    private UserInfo userInfo;
    String serviceName = "echoServiceForDemote.wsdl";
    private final String ASPECT_NAME = "CustomServiceLifeCycle";
    private final String ACTION_PROMOTE = "Promote";
    private final String ACTION_DEMOTE = "Demote";
    private final String ACTION_ITEM_CLICK = "itemClick";
    private final String ASS_TYPE_DEPENDS = "depends";
    private String servicePathTrunk;
    private String servicePathBranchDev;
    private String servicePathBranchQA;
    private String servicePathBranchProd;
    private String[] dependencyList = null;

    @BeforeClass
    public void init() throws Exception {
        final int userId = 3;
        EnvironmentBuilder builder = new EnvironmentBuilder().greg(userId);
        EnvironmentVariables gregServer = builder.build().getGreg();
        userInfo = UserListCsvReader.getUserInfo(userId);
        sessionCookie = gregServer.getSessionCookie();
        lifeCycleAdminService = new LifeCycleAdminService(gregServer.getBackEndUrl());
        activitySearch = new ActivitySearchAdminService(gregServer.getBackEndUrl());
        lifeCycleManagerAdminService = new LifeCycleManagerAdminService(gregServer.getBackEndUrl());
        userManger = new AdminServiceUserMgtService(gregServer.getBackEndUrl());
        registry = new RegistryProvider().getRegistry(userId, ProductConstant.GREG_SERVER_NAME);
        Registry governance = new RegistryProvider().getGovernance(registry, userId);

        deleteRolesIfExist();

        Utils.deleteLifeCycleIfExist(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);
        String wsdlPath = "/_system/governance" + Utils.addWSDL("echoServiceWsdl.wsdl", governance, serviceName);
        Association[] usedBy = registry.getAssociations(wsdlPath, "usedBy");
        Assert.assertNotNull(usedBy, "WSDL usedBy Association type not found");
        for (Association association : usedBy) {
            if (association.getSourcePath().equalsIgnoreCase(wsdlPath)) {
                servicePathTrunk = association.getDestinationPath();
            }
        }
        Thread.sleep(1000);

        Utils.createNewLifeCycle(sessionCookie, ASPECT_NAME, lifeCycleManagerAdminService);

        Assert.assertNotNull(servicePathTrunk, "Service Not Found associate with WSDL");

        Thread.sleep(1000);
        Association[] dependency = registry.getAssociations(servicePathTrunk, ASS_TYPE_DEPENDS);

        Assert.assertNotNull(dependency, "Dependency Not Found.");
        Assert.assertTrue(dependency.length > 0, "Dependency list empty");
        Assert.assertEquals(dependency.length, 7, "some dependency missing or additional dependency found.");


    }

    @Test(priority = 1, description = "Add lifecycle to a service")
    public void addLifecycle()
            throws RegistryException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, InterruptedException {
        registry.associateAspect(servicePathTrunk, ASPECT_NAME);
        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathTrunk);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties()
                , "registry.lifecycle.CustomServiceLifeCycle.state")[0], "Commencement",
                            "LifeCycle State Mismatched");
        dependencyList = lifeCycleAdminService.getAllDependencies(sessionCookie, servicePathTrunk);
        Assert.assertNotNull(dependencyList, "Dependency List Not Found");
        Assert.assertEquals(dependencyList.length, 8, "Dependency Count mismatched");

    }

    @Test(description = "Click Check List Item", dependsOnMethods = {"addLifecycle"})
    public void clickCommencementCheckList()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   UserAdminException {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        addRole("archrole");
        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "false", "false", "false", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true", "true", "true", "true"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 1, "Action not found");
        Assert.assertEquals(actions[0], "Abort", "Abort Action not found");

        addRole("managerrole");
        lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 2, "Action not found");
        Assert.assertEquals(actions[0], "Promote", "Promote Action not found");
        Assert.assertEquals(actions[1], "Abort", "Abort Action not found");

    }

    @Test(description = "Promote Service to Creation", dependsOnMethods = {"clickCommencementCheckList"})
    public void promoteServiceToCreation()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);
        ArrayOfString[] parameters = new ArrayOfString[2];
        parameters[0] = new ArrayOfString();
        parameters[0].setArray(new String[]{servicePathTrunk, "1.0.0"});

        parameters[1] = new ArrayOfString();
        parameters[1].setArray(new String[]{"preserveOriginal", "true"});

        lifeCycleAdminService.invokeAspectWithParams(sessionCookie, servicePathTrunk, ASPECT_NAME,
                                                     ACTION_PROMOTE, null, parameters);

        Thread.sleep(500);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertEquals(service.getPath(), servicePathTrunk, "Service not in branches/testing. " + servicePathTrunk);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.lifecycle.CustomServiceLifeCycle.state")[0]
                , "Creation", "LifeCycle State Mismatched");

        Assert.assertEquals(registry.get(servicePathTrunk).getPath(), servicePathTrunk, "Preserve original failed");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Code Completed", "Code Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:WSDL Created", "WSDL Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:QoS Created", "QoS Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Schema Created", "Schema Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1],
                            "name:Services Created", "Services Created Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1],
                            "name:Completion of Creation", "Completion of Creation  Check List Item Not Found");

        //activity search for trunk
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , servicePathTrunk, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);


    }

    @Test(description = "click check list in creation stage", dependsOnMethods = {"promoteServiceToCreation"})
    public void clickCreationCheckListToEnableDemote()
            throws CustomLifecyclesChecklistAdminServiceExceptionException, RemoteException,
                   UserAdminException {
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        String[] actions;
        LifecycleActions[] availableActions = lifeCycle.getAvailableActions();
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "false", "false", "false", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        Assert.assertEquals(availableActions.length, 1, "Available Action count mismatched");
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNull(actions, "Available Action found");

        //check demote action
        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathTrunk, ASPECT_NAME, ACTION_ITEM_CLICK,
                                           new String[]{"true", "true", "true", "true", "true", "false"});
        lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        availableActions = lifeCycle.getAvailableActions();
        actions = availableActions[0].getActions();
        Assert.assertNotNull(actions, "Available Action Not found");
        Assert.assertEquals(actions.length, 1, "Action not found");
        Assert.assertEquals(actions[0], "Demote", "Demote Action not found");
    }

    @Test(description = "Demote Service to Commencement", dependsOnMethods = {"clickCommencementCheckList"})
    public void demoteServiceToCreation()
            throws InterruptedException, CustomLifecyclesChecklistAdminServiceExceptionException,
                   RemoteException, RegistryException, RegistryExceptionException {
        Thread.sleep(1000);

        lifeCycleAdminService.invokeAspect(sessionCookie, servicePathTrunk, ASPECT_NAME,
                                           ACTION_DEMOTE, null);
        LifecycleBean lifeCycle = lifeCycleAdminService.getLifecycleBean(sessionCookie, servicePathTrunk);
        Resource service = registry.get(servicePathTrunk);
        Assert.assertNotNull(service, "Service Not found on registry path " + servicePathTrunk);
        Assert.assertTrue(service.getPath().contains("trunk"), "Service not in trunk. " + servicePathTrunk);

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties()
                , "registry.lifecycle.CustomServiceLifeCycle.state")[0], "Commencement",
                            "LifeCycle State Mismatched");

        //life cycle check list
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[1],
                            "name:Requirements Gathered", "Requirements Gathered Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.0.item")[3],
                            "value:true", "Requirements Gathered Check List not checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[1],
                            "name:Document Requirements", "Document Requirements Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.1.item")[3],
                            "value:true", "Document Requirements Check List not checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[1],
                            "name:Architecture Diagram Finalized", "Architecture Diagram Finalize Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.2.item")[3],
                            "value:true", "Architecture Diagram Finalize Check List not checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[1],
                            "name:Design UML Diagrams", "Design UML Diagrams Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.3.item")[3],
                            "value:true", "Design UML Diagrams Check List not checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[1]
                , "name:High Level Design Completed", "High Level Design Completed Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.4.item")[3]
                , "value:true", "High Level Design Completed Check List not checked");

        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[1]
                , "name:Completion of Commencement", "Completion of Commencement  Check List Item Not Found");
        Assert.assertEquals(Utils.getLifeCycleProperty(lifeCycle.getLifecycleProperties(), "registry.custom_lifecycle.checklist.option.5.item")[3]
                , "value:true", "Completion of Commencement  Check List not checked");

        //Activity search
        Thread.sleep(1000 * 10);
        ActivityBean activityObjTrunk = activitySearch.getActivities(sessionCookie, userInfo.getUserName()
                , servicePathTrunk, Utils.formatDate(Calendar.getInstance().getTime())
                , "", ActivitySearchAdminService.FILTER_RESOURCE_UPDATE, 1);
        Assert.assertNotNull(activityObjTrunk, "Activity object null in trunk");
        Assert.assertNotNull(activityObjTrunk.getActivity(), "Activity list object null");
        Assert.assertTrue((activityObjTrunk.getActivity().length > 0), "Activity list object null");
        String activity = activityObjTrunk.getActivity()[0];
        Assert.assertTrue(activity.contains(userInfo.getUserName()), "Activity not found. User name not found on last activity. " + activity);
        Assert.assertTrue(activity.contains("has updated the resource"),
                          "Activity not found. has updated not contain in last activity. " + activity);
        Assert.assertTrue(activity.contains("0m ago"), "Activity not found. current time not found on last activity. " + activity);
    }

    @AfterClass
    public void deleteLifeCycle()
            throws RegistryException, LifeCycleManagementServiceExceptionException,
                   RemoteException {
        if (servicePathTrunk != null) {
            registry.delete(servicePathTrunk);
        }

        if (servicePathBranchDev != null) {
            registry.delete(servicePathBranchDev);
        }

        if (servicePathBranchQA != null) {
            registry.delete(servicePathBranchQA);
        }

        if (servicePathBranchProd != null) {
            registry.delete(servicePathBranchProd);
        }
        Assert.assertTrue(lifeCycleManagerAdminService.deleteLifeCycle(sessionCookie, ASPECT_NAME),
                          "Life Cycle Deleted failed");
        registry = null;
        activitySearch = null;
        lifeCycleAdminService = null;
    }

    private void deleteRolesIfExist() {

        if (userManger.roleNameExists("archrole", sessionCookie)) {
            userManger.deleteRole(sessionCookie, "archrole");
        }

        if (userManger.roleNameExists("managerrole", sessionCookie)) {
            userManger.deleteRole(sessionCookie, "managerrole");
        }

        if (userManger.roleNameExists("devrole", sessionCookie)) {
            userManger.deleteRole(sessionCookie, "devrole");
        }

        if (userManger.roleNameExists("qarole", sessionCookie)) {
            userManger.deleteRole(sessionCookie, "qarole");
        }
        if (userManger.roleNameExists("techoprole", sessionCookie)) {
            userManger.deleteRole(sessionCookie, "techoprole");
        }
    }

    private void addRole(String roleName) throws UserAdminException {
        String[] permissions = {"/permission/"};
        userManger.addRole(roleName, new String[]{userInfo.getUserName()}, permissions, sessionCookie);
    }
}
