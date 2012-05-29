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
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.GadgetRepoServiceStub;
import org.wso2.carbon.dashboard.mgt.gadgetrepo.stub.types.carbon.Gadget;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import java.rmi.RemoteException;

 /*
 Tests for GetGadgetList in GS -  Gadget Repository
 */

public class GetGadgetListTestCase {
    private static final Log log = LogFactory.getLog(GetGadgetListTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();
    GadgetRepoServiceStub gadgetRepoServiceStub;

    @BeforeMethod(groups = {"wso2.gs"})
    public void init() throws java.lang.Exception {
        String loggedInSessionCookie = util.login();
        gadgetRepoServiceStub = GadgetRepoTestUtils.getGadgetRepoServiceStub(loggedInSessionCookie);
    }

     // Get default gadget url set
    @Test(groups = {"wso2.gs"}, description = "Get default gadget url set")
    public void testGetDefaultGadgetUrlSet() throws RemoteException {
        String[] defaultGadgetUrlSet = gadgetRepoServiceStub.getDefaultGadgetUrlSet();
        if (defaultGadgetUrlSet != null) {
            for (String gadget : defaultGadgetUrlSet) {
                log.debug("DefaultGadget: " + gadget + "available");
            }
        }

    }


    // Get all available set of gadgets for the user as an array
    @Test(groups = {"wso2.gs"}, description = " Get all available set of gadgets for the user as an array")
    public void testGetAllGadgetData() throws RemoteException {
        Gadget[] gadgets = gadgetRepoServiceStub.getGadgetData();
        if (gadgets != null) {
            for (Gadget gadget : gadgets) {
                if (gadget != null) {
                    log.debug("Gadget: " + gadget + "available");
                }
            }
        }

    }
}
