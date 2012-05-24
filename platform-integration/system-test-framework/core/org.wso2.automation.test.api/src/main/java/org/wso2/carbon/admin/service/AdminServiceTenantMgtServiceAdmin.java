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

package org.wso2.carbon.admin.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.admin.service.utils.AuthenticateStub;
import org.wso2.carbon.tenant.mgt.stub.*;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AdminServiceTenantMgtServiceAdmin {

    private static final Log log = LogFactory.getLog(AdminServiceTenantMgtServiceAdmin.class);

    private TenantMgtAdminServiceStub tenantMgtAdminServiceStub;

    public AdminServiceTenantMgtServiceAdmin(String backEndUrl) {
        String serviceName = "TenantMgtAdminService";
        String endPoint = backEndUrl + serviceName;
        try {
            tenantMgtAdminServiceStub = new TenantMgtAdminServiceStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("Initializing tenantMgtAdminServiceStub failed : " + axisFault.getMessage());
            Assert.fail("Initializing tenantMgtAdminServiceStub failed : " + axisFault.getMessage());
        }
    }

    public void addTenant(String sessionCookie, String domainName, String password, String firstName, String usagePlan)
            throws TenantMgtAdminServiceExceptionException {

        AuthenticateStub.authenticateStub(sessionCookie, tenantMgtAdminServiceStub);

        Date date = new Date();
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);

        TenantInfoBean tenantInfoBean = new TenantInfoBean();
        tenantInfoBean.setActive(true);
        tenantInfoBean.setEmail("wso2automation.test@wso2.com");
        tenantInfoBean.setAdminPassword(password);
        tenantInfoBean.setAdmin(firstName);
        tenantInfoBean.setTenantDomain(domainName);
        tenantInfoBean.setCreatedDate(calendar);
        tenantInfoBean.setFirstname(firstName);
        tenantInfoBean.setLastname(firstName + "wso2automation");
        tenantInfoBean.setSuccessKey("true");
        tenantInfoBean.setUsagePlan(usagePlan);

        TenantInfoBean tenantInfoBeanGet;
        try {
            tenantInfoBeanGet = tenantMgtAdminServiceStub.getTenant(domainName);

            if (!tenantInfoBeanGet.getActive() && tenantInfoBeanGet.getTenantId() != 0) {
                tenantMgtAdminServiceStub.activateTenant(domainName);
                log.info("Tenant domain " + domainName + " Activated successfully");

            } else if (!tenantInfoBeanGet.getActive() && tenantInfoBeanGet.getTenantId() == 0) {
                tenantMgtAdminServiceStub.addTenant(tenantInfoBean);
                tenantMgtAdminServiceStub.activateTenant(domainName);
                log.info("Tenant domain " + domainName + " created and activated successfully");
            } else {
                log.info("Tenant domain " + domainName + " already registered");
            }
        } catch (RemoteException e) {
            log.error("RemoteException thrown while adding user/tenants : " + e);
            Assert.fail("RemoteException thrown while adding user/tenants : " + e);
        } catch (TenantMgtAdminServiceExceptionException e) {
            log.error("GetTenantExceptionException thrown when getting user list : " + e);
            Assert.fail("GetTenantExceptionException thrown when getting user list : " + e);
        }
    }

    public TenantInfoBean getTenant(String sessionCookie, String tenantDomain) {
        AuthenticateStub.authenticateStub(sessionCookie, tenantMgtAdminServiceStub);
        TenantInfoBean getTenantBean = null;
        try {
            getTenantBean = tenantMgtAdminServiceStub.getTenant(tenantDomain);
            Assert.assertNotNull(getTenantBean, "Domain Name not found");
        } catch (RemoteException e) {
            log.error("RemoteException thrown while retrieving user/tenants : " + e);
            Assert.fail("RemoteException thrown while retrieving user/tenants : " + e);
        } catch (TenantMgtAdminServiceExceptionException e) {
            log.error("GetTenantExceptionException thrown when getting user/tenant list : " + e);
            Assert.fail("GetTenantExceptionException thrown when getting user/tenant list : " + e);
        }
        return getTenantBean;
    }

    public void updateTenant(String sessionCookie, TenantInfoBean infoBean) {
        AuthenticateStub.authenticateStub(sessionCookie, tenantMgtAdminServiceStub);
        try {
            tenantMgtAdminServiceStub.updateTenant(infoBean);
        } catch (RemoteException e) {
            log.error("RemoteException thrown while retrieving user/tenants : " + e);
            Assert.fail("RemoteException thrown while retrieving user/tenants : " + e);
        } catch (TenantMgtAdminServiceExceptionException e) {
            log.error("UpdateTenantExceptionException thrown while updating tenant info : " + e);
            Assert.fail("UpdateTenantExceptionException thrown while updating tenant info : " + e);
        }
    }

    public void activateTenant(String sessionCookie, String domainName) {
        AuthenticateStub.authenticateStub(sessionCookie, tenantMgtAdminServiceStub);
        try {
            tenantMgtAdminServiceStub.activateTenant(domainName);
        } catch (RemoteException e) {
            log.error("RemoteException thrown while retrieving user/tenants : " + e);
            Assert.fail("RemoteException thrown while retrieving user/tenants : " + e);
        } catch (TenantMgtAdminServiceExceptionException e) {
            log.error("Tenant domain" + domainName + " activation fail" + e);
            Assert.fail("Tenant domain" + domainName + " activation fail" + e);

        }

    }

}
