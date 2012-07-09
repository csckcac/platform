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

package org.wso2.carbon.automation.api.clients.stratos.tenant.mgt;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.tenant.mgt.stub.*;
import org.wso2.carbon.tenant.mgt.stub.beans.xsd.TenantInfoBean;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class TenantMgtAdminServiceClient {

    private static final Log log = LogFactory.getLog(TenantMgtAdminServiceClient.class);

    private TenantMgtAdminServiceStub tenantMgtAdminServiceStub;

    public TenantMgtAdminServiceClient(String backEndUrl) throws AxisFault {
        String serviceName = "TenantMgtAdminService";
        String endPoint = backEndUrl + serviceName;
        try {
            tenantMgtAdminServiceStub = new TenantMgtAdminServiceStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("Initializing tenantMgtAdminServiceStub failed : " + axisFault.getMessage());
            throw new AxisFault("Initializing tenantMgtAdminServiceStub failed : " + axisFault.getMessage());
        }
    }

    public void addTenant(String sessionCookie, String domainName, String password,
                          String firstName, String usagePlan)
            throws TenantMgtAdminServiceExceptionException, RemoteException {

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
            throw new RemoteException("RemoteException thrown while adding user/tenants : " + e);
        }
    }

    public TenantInfoBean getTenant(String sessionCookie, String tenantDomain)
            throws TenantMgtAdminServiceExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, tenantMgtAdminServiceStub);
        TenantInfoBean getTenantBean = null;
        try {
            getTenantBean = tenantMgtAdminServiceStub.getTenant(tenantDomain);
            assert getTenantBean == null : "Domain Name not found";
        } catch (RemoteException e) {
            log.error("RemoteException thrown while retrieving user/tenants : " + e);
            throw new RemoteException("RemoteException thrown while retrieving user/tenants : " + e);
        }
        return getTenantBean;
    }

    public void updateTenant(String sessionCookie, TenantInfoBean infoBean)
            throws TenantMgtAdminServiceExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, tenantMgtAdminServiceStub);
        try {
            tenantMgtAdminServiceStub.updateTenant(infoBean);
        } catch (RemoteException e) {
            log.error("RemoteException thrown while retrieving user/tenants : " + e);
            throw new RemoteException("RemoteException thrown while retrieving user/tenants : " + e);
        }
    }

    public void activateTenant(String sessionCookie, String domainName)
            throws TenantMgtAdminServiceExceptionException, RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, tenantMgtAdminServiceStub);
        try {
            tenantMgtAdminServiceStub.activateTenant(domainName);
        } catch (RemoteException e) {
            log.error("RemoteException thrown while retrieving user/tenants : " + e);
            throw new RemoteException("RemoteException thrown while retrieving user/tenants : " + e);
        }
    }
}
