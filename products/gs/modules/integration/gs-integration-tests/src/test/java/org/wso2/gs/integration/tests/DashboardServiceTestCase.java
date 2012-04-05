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


package org.wso2.gs.integration.tests;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.dashboard.stub.DashboardServiceStub;
import org.wso2.carbon.dashboard.stub.types.bean.DashboardContentBean;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;

import java.rmi.RemoteException;

import static org.testng.Assert.assertTrue;

/*
 Tests for DashboardService in GS - Dashboard
 */
public class DashboardServiceTestCase {
    private static final Log log = LogFactory.getLog(DashboardServiceTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();
    DashboardServiceStub dashboardServiceStub;
    String userID = "admin";
    String dashboardName = null;
    String tDomain = null;
    int tabID;
    String tabIdValue = "0";
    String tabTitle = "IntegrationTestTab";
    String gadgetUrl = "/registry/resource/_system/config/repository/gadget-server/gadgets/jira-gadget.xml";
    String gadgetGroup = "G1#";
    String backendServerURL = FrameworkSettings.SERVICE_URL;


    @BeforeMethod(groups = {"wso2.gs"})
    public void init() throws java.lang.Exception {
        String loggedInSessionCookie = util.login();
        dashboardServiceStub =
                DashboardTestUtils.getDashboardServiceStub(loggedInSessionCookie);

    }

    // Check the validity of the session
    @Test(groups = {"wso2.gs"}, description = "Check the validity of the session")
    public void testIsSessionValid() throws RemoteException {
        boolean isValid = dashboardServiceStub.isSessionValid();
        assertTrue(isValid, "Invalid session ");

    }

    // Get the default gadget url set
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testIsSessionValid"}, description = "Get the default gadget url set")
    public void testGetDefaultGadgetUrlSet() throws RemoteException {
        String[] defaultGadgetUrlSet = dashboardServiceStub.getDefaultGadgetUrlSet(userID);
        if (defaultGadgetUrlSet != null) {
            for (String gadget : defaultGadgetUrlSet) {
                log.debug("DefaultGadget: " + gadget + " -available");
            }
        }
        log.info("Successfully executed getDefaultGadgetUrlSet test");

    }

    // Get dashboard content for the given user as a bean
    @Test(groups = {"wso2.gs"}, description = "Get default gadget url set")
    public void testGetDashboardContent() throws RemoteException {
        DashboardContentBean content = dashboardServiceStub.getDashboardContent(userID, dashboardName, tDomain, backendServerURL);
        assertTrue((content != null), "Failed to get the dashboard content for the given user");

    }

    // Get tab layout for the user
    @Test(groups = {"wso2.gs"}, description = "Get tab layout for the user")
    public void testGetTabLayout() throws RemoteException {
        String tabLayout = dashboardServiceStub.getTabLayout(userID, dashboardName);
        boolean check = "0".equals(tabLayout);
        assertTrue(check, "Failed to get tab layout for the user");

    }

    // Retrieves the stored layout
    @Test(groups = {"wso2.gs"}, description = "Retrieves the stored layout")
    public void testGetGadgetLayout() throws RemoteException {
        String gadgetLayout = dashboardServiceStub.getGadgetLayout(userID, tabIdValue, dashboardName);
        assertTrue((gadgetLayout != null), "Failed to retrieve the stored layout");

    }

    // Get dashboard content as json
    @Test(groups = {"wso2.gs"}, description = "Get dashboard content as json")
    public void testGetDashboardContentAsJson() throws RemoteException {
        String jsonContent = dashboardServiceStub.getDashboardContentAsJson(userID, dashboardName, tDomain, backendServerURL);
        assertTrue((jsonContent.length() != 0), "Failed to get the dashboard content as json");


    }

    // Get tab content as json
    @Test(groups = {"wso2.gs"}, description = "Get tab content ad json")
    public void testGetTabContentAsJson() throws RemoteException {
        String jsonTabContent = dashboardServiceStub.getTabContentAsJson(userID, dashboardName, tDomain, backendServerURL, tabIdValue);
        assertTrue((jsonTabContent.length() != 0), "Failed to get the tab content as json");


    }

    // Get tab layout with names
    @Test(groups = {"wso2.gs"}, description = "Get tab layout with names")
    public void testGetTabLayoutWithNames() throws RemoteException {
        String tabNames = dashboardServiceStub.getTabLayoutWithNames(userID, dashboardName);
        assertTrue((tabNames.length() != 0), "Failed to get the tab layout with names");

    }

    // Get gadget urls to layout
    @Test(groups = {"wso2.gs"}, description = "Get gadget urls to layout")
    public void testGetGadgetUrlsToLayout() throws RemoteException {
        String[] gadgetUrlsToLayout = dashboardServiceStub.getGadgetUrlsToLayout(userID, tabTitle, dashboardName, backendServerURL);
        if (gadgetUrlsToLayout != null) {
            for (String gadget : gadgetUrlsToLayout) {
                log.debug("gadgetUrl: " + gadget + " -available to layout");
            }
        }
        log.info("Successfully executed getGadgetUrlsToLayout test");

    }

    // Add a new tab
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testGetTabContentAsJson"}, description = "Add new tab")
    public void testAddNewTab() throws RemoteException {
        tabID = dashboardServiceStub.addNewTab(userID, tabTitle, dashboardName);
        assertTrue((tabID != 0), "Failed to add a new tab");
        log.info("Successfully executed addNewTab test");

    }

    // Populate default three column layout
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testGetDashboardContentAsJson"}, description = "Populate Default Three Column Layout")
    public void testPopulateDefaultThreeColumnLayout() throws RemoteException {
        tabIdValue = "" + tabID;
        String columnLayout = dashboardServiceStub.populateDefaultThreeColumnLayout(userID, tabIdValue);
        assertTrue((columnLayout != null), "Failed to populate default three column layout");


    }

    // Add gadget to user
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testPopulateDefaultThreeColumnLayout"}, description = "Add gadget to user")
    public void testAddGadgetToUser() throws RemoteException {
        boolean addGadgetToUserStatus = dashboardServiceStub.addGadgetToUser(userID, tabIdValue, gadgetUrl, dashboardName, gadgetGroup);
        assertTrue(addGadgetToUserStatus, "Failed to add a gadget to user");


    }

    // Removes a given tab from the system
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testAddNewTab"}, description = "Removes a given tab from the system")
    public void testRemoveTab() throws RemoteException {
        boolean removeTabStatus = dashboardServiceStub.removeTab(userID, tabIdValue, dashboardName);
        assertTrue(removeTabStatus, "Failed to remove a tab");

    }


}
