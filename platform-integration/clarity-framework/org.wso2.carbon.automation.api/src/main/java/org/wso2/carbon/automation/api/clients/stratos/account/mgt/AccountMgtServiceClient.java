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

package org.wso2.carbon.automation.api.clients.stratos.account.mgt;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.account.mgt.stub.beans.xsd.AccountInfoBean;
import org.wso2.carbon.account.mgt.stub.services.*;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;

import java.rmi.RemoteException;


public class AccountMgtServiceClient {

    private static final Log log = LogFactory.getLog(AccountMgtServiceClient.class);

    private AccountMgtServiceStub accountMgtServiceStub;

    public AccountMgtServiceClient(String backEndUrl) throws AxisFault {
        String serviceName = "AccountMgtService";
        String endPoint = backEndUrl + serviceName;
        try {
            accountMgtServiceStub = new AccountMgtServiceStub(endPoint);
        } catch (AxisFault axisFault) {
            log.error("Initializing AccountMgtServiceStub failed : " + axisFault.getMessage());
            throw new AxisFault("Initializing AccountMgtServiceStub failed : " + axisFault.getMessage());
        }
    }

    public void deactivateTenant(String sessionCookie)
            throws RemoteException, DeactivateExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, accountMgtServiceStub);
        try {
            accountMgtServiceStub.deactivate();
        } catch (RemoteException e) {
            log.error("Tenant deactivation fail " + e);
            throw new RemoteException("Tenant deactivation fail " + e);
        } catch (DeactivateExceptionException e) {
            log.error("Tenant deactivation fail " + e);
            throw new DeactivateExceptionException("Tenant deactivation fail " + e);
        }
    }

    public void updateTenantContact(String sessionCookie, String email)
            throws RemoteException, UpdateContactExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, accountMgtServiceStub);
        try {
            accountMgtServiceStub.updateContact(email);
        } catch (RemoteException e) {
            log.error("Tenant contact info update fail " + e);
            throw new RemoteException("Tenant contact info update fail " + e);
        } catch (UpdateContactExceptionException e) {
            log.error("Tenant contact info update fail " + e);
            throw new UpdateContactExceptionException("Tenant contact info update fail " + e);
        }
    }

    public void updateTenantFullName(String sessionCookie, AccountInfoBean accountInfoBean)
            throws RemoteException, UpdateFullnameExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, accountMgtServiceStub);
        try {
            accountMgtServiceStub.updateFullname(accountInfoBean);
        } catch (RemoteException e) {
            log.error("Tenant full name update fail " + e);
            throw new RemoteException("Tenant full name update fail " + e);
        } catch (UpdateFullnameExceptionException e) {
            log.error("Tenant full name update fail " + e);
            throw new UpdateFullnameExceptionException("Tenant full name update fail " + e);
        }
    }

    public String getTenantcontact(String sessionCookie)
            throws RemoteException, GetContactExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, accountMgtServiceStub);
        String contactInfo;
        try {
            contactInfo = accountMgtServiceStub.getContact();
        } catch (RemoteException e) {
            log.error("Cannot retrieve tenant contact info " + e);
            throw new RemoteException("Cannot retrieve tenant contact info " + e);

        } catch (GetContactExceptionException e) {
            log.error("Cannot retrieve tenant contact info " + e);
            throw new GetContactExceptionException("Cannot retrieve tenant contact info " + e);
        }
        return contactInfo;
    }

    public AccountInfoBean getTenantFullName(String sessionCookie)
            throws RemoteException, GetFullnameExceptionException {
        AuthenticateStub.authenticateStub(sessionCookie, accountMgtServiceStub);
        AccountInfoBean accountInfoBean;
        try {
            accountInfoBean = accountMgtServiceStub.getFullname();
        } catch (RemoteException e) {
            log.error("Cannot retrieve tenant full name " + e);
            throw new RemoteException("Cannot retrieveenant full name " + e);
        } catch (GetFullnameExceptionException e) {
            log.error("Cannot retrieve tenant full name " + e);
            throw new GetFullnameExceptionException("Cannot retrieve tenant full name " + e);
        }
        return accountInfoBean;
    }
}
