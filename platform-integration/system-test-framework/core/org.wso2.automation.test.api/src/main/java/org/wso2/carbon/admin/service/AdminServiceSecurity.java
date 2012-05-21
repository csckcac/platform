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
package org.wso2.carbon.admin.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.utils.AuthenticateStub;
import org.wso2.carbon.security.mgt.stub.config.*;

import java.rmi.RemoteException;

public class AdminServiceSecurity {
    private static final Log log = LogFactory.getLog(AdminServiceSecurity.class);

    private final String securityServiceName = "SecurityAdminService";
    private SecurityAdminServiceStub securityAdminServiceStub;
    private String endPoint;

    public AdminServiceSecurity(String backEndUrl) throws AxisFault {
        this.endPoint = backEndUrl + securityServiceName;
        securityAdminServiceStub = new SecurityAdminServiceStub(endPoint);
    }

    public void applySecurity(String sessionCookie, String serviceName, String policyId,
                              String[] userGroups, String[] trustedKeyStoreArray,
                              String privateStore)
            throws SecurityAdminServiceSecurityConfigExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, securityAdminServiceStub);
        ApplySecurity applySecurity;
        applySecurity = new ApplySecurity();
        applySecurity.setServiceName(serviceName);
        applySecurity.setPolicyId("scenario" + policyId);
        applySecurity.setTrustedStores(trustedKeyStoreArray);
        applySecurity.setPrivateStore(privateStore);
        applySecurity.setUserGroupNames(userGroups);

        securityAdminServiceStub.applySecurity(applySecurity);
        log.info("Security Applied");


    }

    public void applyKerberosSecurityPolicy(String sessionCookie, String serviceName,
                                            String policyId, String ServicePrincipalName,
                                            String ServicePrincipalPassword)
            throws SecurityAdminServiceSecurityConfigExceptionException, RemoteException {

        AuthenticateStub.authenticateStub(sessionCookie, securityAdminServiceStub);
        ApplyKerberosSecurityPolicy applySecurity;
        applySecurity = new ApplyKerberosSecurityPolicy();
        applySecurity.setServiceName(serviceName);
        applySecurity.setPolicyId("scenario" + policyId);
        applySecurity.setServicePrincipalName(ServicePrincipalName);
        applySecurity.setServicePrincipalPassword(ServicePrincipalPassword);


        securityAdminServiceStub.applyKerberosSecurityPolicy(applySecurity);
        log.info("Security Applied");


    }

    public void disableSecurity(String sessionCookie, String serviceName)
            throws SecurityAdminServiceSecurityConfigExceptionException, RemoteException {

        DisableSecurityOnService disableRequest = new DisableSecurityOnService();
        disableRequest.setServiceName(serviceName);

        AuthenticateStub.authenticateStub(sessionCookie, securityAdminServiceStub);


        securityAdminServiceStub.disableSecurityOnService(disableRequest);
        log.info("Security Disabled");


    }

}
