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

import java.rmi.RemoteException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.GadgetRepoServiceStub;
import org.wso2.carbon.dashboard.mgt.users.stub.GadgetServerUserManagementServiceStub;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.testng.Assert.assertTrue;

/*
 Test for UserManagement in GS
 */

public class UserManagementTestCase {
    private static final Log log = LogFactory.getLog(UserManagementTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();
    GadgetServerUserManagementServiceStub userManagementServiceStub;
    GadgetRepoServiceStub gadgetRepoServiceStub;

    @BeforeMethod(groups = {"wso2.gs"})
    public void init() throws java.lang.Exception {
        String loggedInSessionCookie = util.login();
        userManagementServiceStub =
                UserManagementTestUtils.getGadgetServerUserManagementServiceStub(loggedInSessionCookie);
    }

    // set user self registration test
    @Test(groups = {"wso2.gs"}, description = "set user self registration test")
    public void testSetUserSelfRegistration() throws RemoteException {
        boolean isUserSelfRegistration = userManagementServiceStub.setUserSelfRegistration(true);
        assertTrue (isUserSelfRegistration, "Failed to set User self registration");
    }

    // set user external gadget addition test
    @Test(groups = {"wso2.gs"}, description = "set user external gadget addition test")
    public void testSetUserExternalGadgetAddition() throws RemoteException {
        boolean isUserExternalGadgetAddition = userManagementServiceStub.setUserExternalGadgetAddition(true);
        assertTrue (isUserExternalGadgetAddition, "Failed to set User External Gadget Addition");

    }

    // self registration test
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testSetUserSelfRegistration"}, description = "self registration test")
    public void testIsSelfRegistration() throws RemoteException {
        boolean isSelfRegistration = userManagementServiceStub.isSelfRegistration(null);
        assertTrue(isSelfRegistration, "User is not self registered");
    }

    // set external gadget addition test
    @Test(groups = {"wso2.gs"}, description = "set external gadget addition test")
    public void testSetExternalGadgetAddition() throws RemoteException {
        boolean isExternalGadgetAddition = userManagementServiceStub.setExternalGadgetAddition(true);
        assertTrue( isExternalGadgetAddition, "Failed to set external gadget addition");
    }

    // external gadget addition test
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testSetExternalGadgetAddition"}, description = "external gadget addition test")
    public void testIsExternalGadgetAddition() throws RemoteException {
        boolean isExternalGadgetAddition = userManagementServiceStub.isExternalGadgetAddition();
        assertTrue (isExternalGadgetAddition, "External gadgets could not be added");
    }


    // check whether the portal permission is set to the user
    @Test(groups = {"wso2.gs"}, description = "check whether the portal permission is set to the user")
    public void testIsPortalPermissionsSet() throws RemoteException {
        boolean isExternalGadgetAddition = userManagementServiceStub.isPortalPermissionsSet();
        assertTrue (isExternalGadgetAddition, "User does not have portal permission");
    }

    // set anonymouse mode state test
    @Test(groups = {"wso2.gs"}, description = "set anonymouse mode state test")
    public void testSetAnonModeState() throws RemoteException {
        boolean isAnonModeState = userManagementServiceStub.setAnonModeState(true);
        assertTrue (isAnonModeState,"Failed to set Anonymouse mode state");
    }

    // get anonymouse mode state test
    @Test(groups = {"wso2.gs"}, dependsOnMethods = {"testSetAnonModeState"},description = " get anonymouse mode state test")
    public void testIsAnonModeActive() throws RemoteException {
        boolean isAnonModeState = userManagementServiceStub.isAnonModeActive(null);
        assertTrue (isAnonModeState, "Anonymouse mode state is not active");
    }

    // check whether the user session is valid
    @Test(groups = {"wso2.gs"},description = "check whether the user session is valid ")
    public void testIsSessionValid() throws RemoteException {
        boolean isSessionValid = userManagementServiceStub.isSessionValid();
        assertTrue (isSessionValid, "User's session is not valid");
    }


}
