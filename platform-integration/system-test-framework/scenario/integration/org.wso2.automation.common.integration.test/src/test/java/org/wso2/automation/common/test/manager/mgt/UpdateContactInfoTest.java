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
import org.wso2.carbon.account.mgt.stub.services.GetContactExceptionException;
import org.wso2.carbon.account.mgt.stub.services.UpdateContactExceptionException;
import org.wso2.carbon.admin.service.AdminServiceStratosAccountMgt;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.rmi.RemoteException;

import static org.testng.Assert.assertEquals;


public class UpdateContactInfoTest {
    private static final Log log = LogFactory.getLog(UpdateContactInfoTest.class);
    private AdminServiceStratosAccountMgt adminServiceStratosAccountMgt;
    private String sessionCookie;

    @BeforeClass
    public void initializeProperties() throws RemoteException,
                                              LoginAuthenticationExceptionException {
        int tenantId = 13;
        EnvironmentBuilder builder = new EnvironmentBuilder().manager(tenantId);
        ManageEnvironment manageEnvironment = builder.build();
        sessionCookie = manageEnvironment.getManager().getSessionCookie();
        adminServiceStratosAccountMgt =
                new AdminServiceStratosAccountMgt(manageEnvironment.getManager().getBackEndUrl());
    }

    @Test(groups = "stratos.manager", description = "update tenant contact info", priority = 1)
    public void testUpdateContactInfo()
            throws GetContactExceptionException, RemoteException, UpdateContactExceptionException {
        log.info("Runner tenant email update test");
        String updatedEmailAddress = "update.email.test@wso2.com";
        String tenantOldContactInfo = adminServiceStratosAccountMgt.getTenantcontact(sessionCookie);
        //update tenant email address
        adminServiceStratosAccountMgt.updateTenantContact(sessionCookie, updatedEmailAddress);
        assertEquals(adminServiceStratosAccountMgt.getTenantcontact(sessionCookie), updatedEmailAddress,
                     "Contact info doesn't get updated");
        log.info("Tenant email update test passed..");
    }


}
