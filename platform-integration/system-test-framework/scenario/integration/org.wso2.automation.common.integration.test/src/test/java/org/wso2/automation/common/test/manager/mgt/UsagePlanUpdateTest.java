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
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.UserInfo;
import org.wso2.platform.test.core.utils.UserListCsvReader;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import static org.testng.Assert.*;

public class UsagePlanUpdateTest {
    private static final Log log = LogFactory.getLog(UsagePlanUpdateTest.class);
    private UserInfo tenantAdminDetails;
    private String sessionCookie;
    private AdminServiceBillingDataAccessService billingDataAccessStub;
    private EnvironmentBuilder builder;

    @BeforeClass
    public void initializeProperties() throws AxisFault {
        int tenantId = 13;
        builder = new EnvironmentBuilder().manager(tenantId);
        ManageEnvironment environment = builder.build();
        billingDataAccessStub = new AdminServiceBillingDataAccessService(environment.getManager().getBackEndUrl());
        tenantAdminDetails = UserListCsvReader.getUserInfo(tenantId);
        sessionCookie = environment.getManager().getSessionCookie();
    }

    @Test(groups = "wso2.stratos", description = "update usage plan", priority = 1)
    public void testUpdateUsagePlan() throws Exception {
        //upgrade usage plan as SMB
        assertTrue(billingDataAccessStub.updateUsagePlan(sessionCookie, tenantAdminDetails.getDomain(),
                                                         ProductConstant.MULTITENANCY_SMALL_PLAN));
        assertEquals(ProductConstant.MULTITENANCY_SMALL_PLAN,
                     billingDataAccessStub.getUsagePlanName(sessionCookie, tenantAdminDetails.getDomain()),
                     "Usage plan doesn't get updated");
        log.info("Usage plan has been updated to SMB");


        //upgrade usage plan as Professional
        assertTrue(billingDataAccessStub.updateUsagePlan(sessionCookie, tenantAdminDetails.getDomain(),
                                                         ProductConstant.MULTITENANCY_MEDIUM_PLAN),
                   "Usage plan updation fail");
        assertEquals(ProductConstant.MULTITENANCY_MEDIUM_PLAN,
                     billingDataAccessStub.getUsagePlanName(sessionCookie, tenantAdminDetails.getDomain()),
                     "Usage plan doesn't get updated");
        log.info("Usage plan has been updated to professional");


        //upgrade usage plan as Enterprise
        assertTrue(billingDataAccessStub.updateUsagePlan(sessionCookie, tenantAdminDetails.getDomain(),
                                                         ProductConstant.MULTITENANCY_LARGE_PLAN),
                   "Usage plan updation fail");
        assertEquals(ProductConstant.MULTITENANCY_LARGE_PLAN,
                     billingDataAccessStub.getUsagePlanName(sessionCookie, tenantAdminDetails.getDomain()),
                     "Usage plan doesn't get updated");
        log.info("Usage plan has been updated to Enterprise");
    }

    @Test(groups = "wso2.stratos", description = "update usage plan", priority = 1)
    public void testResetUsagePlan() throws Exception {
        //Reset usage plan
        assertTrue(billingDataAccessStub.updateUsagePlan(sessionCookie, tenantAdminDetails.getDomain(),
                                                         ProductConstant.MULTITENANCY_FREE_PLAN),
                   "Usage plan updation fail");
        assertEquals(ProductConstant.MULTITENANCY_FREE_PLAN,
                     billingDataAccessStub.getUsagePlanName(sessionCookie, tenantAdminDetails.getDomain()),
                     "Usage plan doesn't get updated");
    }

    protected static String login(String userName, String password, String hostName) {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }
}
