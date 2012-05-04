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

package org.wso2.automation.common.test.manager.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.account.mgt.stub.services.DeactivateExceptionException;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceStratosAccountMgt;
import org.wso2.carbon.admin.service.AdminServiceTenantMgtServiceAdmin;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;


public class TenantDeactivationTest {

    private static final Log log = LogFactory.getLog(TenantDeactivationTest.class);
    EnvironmentBuilder builder;
    private UserInfo userInfo;


    @Test(groups = "wso2.stratos", description = "deactivate existing tenants", priority = 1)
    public void deactivateExistingTenants() throws DeactivateExceptionException,
                                                   LoginAuthenticationExceptionException,
                                                   RemoteException {
        int tenantId = 13;
        builder = new EnvironmentBuilder().manager(tenantId);
        userInfo = UserListCsvReader.getUserInfo(tenantId);
        ManageEnvironment manageEnvironment = builder.build();

        AdminServiceStratosAccountMgt adminServiceStratosAccountMgt =
                new AdminServiceStratosAccountMgt(manageEnvironment.getManager().getBackEndUrl());
        adminServiceStratosAccountMgt.deactivateTenant(manageEnvironment.getManager().getSessionCookie());
        log.info("Try login after tenant deactivation");
        try {
            assertFalse(unsuccessfulLogin(userInfo.getUserName(), userInfo.getDomain(),
                                          manageEnvironment.getManager().getBackEndUrl()),
                        "Users can login even though they are deactivated");
        } catch (RemoteException e) {  /* handling unsuccessful login attempts */
            log.info("Deactivated tenant cannot login");
        }
    }

    @Test(groups = "wso2.stratos", description = "Reactivate tenants by super user", priority = 2)
    public void testReactivateTenant()
            throws LoginAuthenticationExceptionException, RemoteException {
        int superTenantId = 0;
        EnvironmentBuilder builder = new EnvironmentBuilder().manager(superTenantId);
        ManageEnvironment environment = builder.build();
        AdminServiceTenantMgtServiceAdmin tenantStub =
                new AdminServiceTenantMgtServiceAdmin(environment.getManager().getBackEndUrl());
        tenantStub.activateTenant(environment.getManager().getSessionCookie(), userInfo.getDomain());
        login(userInfo.getUserName(), userInfo.getPassword(), environment.getManager().getBackEndUrl());
        log.info("Tenant activated after test execution");
    }

    private static String login(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }

    private boolean unsuccessfulLogin(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.unsuccessfulLogin(userName, password, hostName);
    }
}
