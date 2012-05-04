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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceBillingDataAccessService;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

import static org.testng.Assert.*;

public class UsagePlanDowngradeTest {
    private static final Log log = LogFactory.getLog(UsagePlanDowngradeTest.class);
    private UserInfo userInfo;
    private String sessionCookie;
    private AdminServiceBillingDataAccessService billingDataAccessStub;
    private EnvironmentBuilder builder;

    @BeforeClass
    public void initializeProperties() throws RemoteException,
                                              LoginAuthenticationExceptionException {
        int tenantId = 13;
        builder = new EnvironmentBuilder().manager(tenantId);
        ManageEnvironment environment = builder.build();
        billingDataAccessStub = new AdminServiceBillingDataAccessService(environment.getManager().getBackEndUrl());
        userInfo = UserListCsvReader.getUserInfo(tenantId);
        sessionCookie = environment.getManager().getSessionCookie();
    }

    @Test(groups = "wso2.stratos", description = "update usage plan", priority = 1)
    public void testUsagePlanDowngrade() throws Exception {
        //upgrade usage plan as Enterprise
        log.info("Running usage plan downgrade test");
        assertTrue(billingDataAccessStub.updateUsagePlan(sessionCookie, userInfo.getDomain(),
                                                         ProductConstant.MULTITENANCY_LARGE_PLAN),
                   "Usage plan updation fail");

        assertEquals(ProductConstant.MULTITENANCY_LARGE_PLAN, billingDataAccessStub.getUsagePlanName
                (sessionCookie, userInfo.getDomain()), "Usage plan doesn't get updated");

        log.info("Usage plan has been updated to Enterprise");

        //upgrade usage plan as Professional
        assertTrue(billingDataAccessStub.updateUsagePlan(sessionCookie, userInfo.getDomain(),
                                                         ProductConstant.MULTITENANCY_MEDIUM_PLAN),
                   "Usage plan updation fail");
        assertEquals(ProductConstant.MULTITENANCY_MEDIUM_PLAN,
                     billingDataAccessStub.getUsagePlanName(sessionCookie, userInfo.getDomain()),
                     "Usage plan doesn't get updated");
        log.info("Usage plan has been updated to professional");

        //upgrade usage plan as SMB
        assertTrue(billingDataAccessStub.updateUsagePlan(sessionCookie, userInfo.getDomain(),
                                                         ProductConstant.MULTITENANCY_SMALL_PLAN),
                   "Usage plan updation fail");
        assertEquals(ProductConstant.MULTITENANCY_SMALL_PLAN, billingDataAccessStub.getUsagePlanName
                (sessionCookie, userInfo.getDomain()), "Usage plan doesn't get updated");
        log.info("Usage plan has been updated to SMB");
    }

    @Test(groups = "wso2.stratos", description = "Reset usage plan to DEMO", priority = 1)
    public void testRestUsagePlan() throws Exception {
        //Reset usage plan
        assertTrue(billingDataAccessStub.updateUsagePlan(sessionCookie, userInfo.getDomain(),
                                                         ProductConstant.MULTITENANCY_FREE_PLAN),
                   "Usage plan update fail");
        assertEquals(ProductConstant.MULTITENANCY_FREE_PLAN,
                     billingDataAccessStub.getUsagePlanName(sessionCookie, userInfo.getDomain()),
                     "Usage plan doesn't get updated");
        log.info("Usage plan has been reset to Demo");
    }

    protected static String login(String userName, String password, String hostName)
            throws LoginAuthenticationExceptionException, RemoteException {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }
}
