/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.tenant.mgt.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.email.sender.api.EmailSender;
import org.wso2.carbon.stratos.common.beans.TenantInfoBean;
import org.wso2.carbon.stratos.common.util.ClaimsMgtUtil;
import org.wso2.carbon.tenant.mgt.internal.TenantMgtServiceComponent;
import org.wso2.carbon.user.core.tenant.Tenant;
import org.wso2.carbon.user.core.tenant.TenantManager;

import java.util.HashMap;
import java.util.Map;

/**
 * PasswordUtil - Utility class with the password related admin-management operations.
 */
public class PasswordUtil {
    private static final Log log = LogFactory.getLog(PasswordUtil.class);
    private static EmailSender passwordResetMsgSender;

    /**
     * Notifies the tenant admin the reset password by the super admin, via
     * email.
     *
     * @param tenantInfoBean tenant information
     * @throws Exception if retrieving the credentials or sending the mail failed.
     */
    public static void notifyResetPassword(TenantInfoBean tenantInfoBean) throws Exception {
        TenantManager tenantManager = TenantMgtServiceComponent.getTenantManager();

        int tenantId = tenantInfoBean.getTenantId();
        Tenant tenant = (Tenant) tenantManager.getTenant(tenantId);
        String firstName = ClaimsMgtUtil.getFirstName(TenantMgtServiceComponent.getRealmService(),
                                                      tenant, tenant.getId());

        // load the mail configuration
        Map<String, String> userParams = new HashMap<String, String>();
        userParams.put("admin-name", tenantInfoBean.getAdmin());
        userParams.put("first-name", firstName);
        userParams.put("domain-name", tenantInfoBean.getTenantDomain());
        userParams.put("password", tenantInfoBean.getAdminPassword());

        try {
            passwordResetMsgSender.sendEmail(tenantInfoBean.getEmail(), userParams);
        } catch (Exception e) {
            // just catch from here..
            String msg = "Error in sending the notification email.";
            log.error(msg, e);
        }
    }

    public static void setPasswordResetMsgSender(EmailSender passwordResetMsgSender) {
        PasswordUtil.passwordResetMsgSender = passwordResetMsgSender;
    }
}
