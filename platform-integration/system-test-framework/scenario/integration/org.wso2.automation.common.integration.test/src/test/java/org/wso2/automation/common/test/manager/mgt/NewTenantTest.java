/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.automation.common.test.manager.mgt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.AdminServiceAuthentication;
import org.wso2.carbon.admin.service.AdminServiceTenantMgtServiceAdmin;

import java.util.Random;

import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkFactory;
import org.wso2.platform.test.core.utils.frameworkutils.FrameworkProperties;

import static org.testng.Assert.*;

public class NewTenantTest {
    private static final Log log = LogFactory.getLog(NewTenantTest.class);

    @Test(groups = "stratos.manager", description = "add new tenant to cloud as super user")
    public void testAddNewTenantTest() {
        log.info("Running Add new tenant test");
        Random rand = new Random();
        int tenantID = 0;
        EnvironmentBuilder builder = new EnvironmentBuilder().manager(tenantID);
        ManageEnvironment manageEnvironment = builder.build();
        AdminServiceTenantMgtServiceAdmin tenantStub =
                new AdminServiceTenantMgtServiceAdmin(manageEnvironment.getManager().getBackEndUrl());
        String domainName = "WSO2TestAutomation" +
                            System.currentTimeMillis() + rand.nextInt() + ".org";
        String password = "admin123";
        String firstName = "automatedTenant";
        String sessionCookie = manageEnvironment.getManager().getSessionCookie();
        tenantStub.addTenant(sessionCookie, domainName, password, "automatedTenant", "Demo");

        TenantInfoBean getTenantInfoBean;
        getTenantInfoBean = tenantStub.getTenant(sessionCookie, domainName);
        assertEquals(domainName, getTenantInfoBean.getTenantDomain(),
                     "Tenant domain name does not match");
        assertTrue(getTenantInfoBean.getActive(), "Tenant is not active");
        assertEquals("wso2automation.test@wso2.com", getTenantInfoBean.getEmail(),
                     "Tenant email does not match");
        assertEquals(firstName, getTenantInfoBean.getFirstname(),
                     "Tenant first name does not match");
        assertEquals("automatedTenantwso2automation", getTenantInfoBean.getLastname(),
                     "Tenant last name does not match");
        assertEquals("Demo", getTenantInfoBean.getUsagePlan(),
                     "Tenant usage plan does not match");
        assertNotNull(getTenantInfoBean.getTenantId(), "Tenant Id not found");
        log.info("Tenant " + domainName + " was created successfully");

        FrameworkProperties manProperties =
                FrameworkFactory.getFrameworkProperties(ProductConstant.MANAGER_SERVER_NAME);
        login(firstName + "@" + domainName, password, manProperties.getProductVariables().getBackendUrl());
    }

    protected static String login(String userName, String password, String hostName) {
        AdminServiceAuthentication loginClient = new AdminServiceAuthentication(hostName);
        return loginClient.login(userName, password, hostName);
    }
}
