/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.tenant.reg.agent.client.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.stratos.common.listeners.TenantActivationListener;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

public class TenantRegListener implements TenantMgtListener, TenantActivationListener {
    private static final Log log = LogFactory.getLog(TenantMgtListener.class);
    private static final int EXECUTION_ORDER = 50;

    public void addTenant(int tenantId) throws UserStoreException {
        log.info("Alerting tenant creation for for tenant " + tenantId);

        try {
            Util.triggerTenantRegistration(tenantId);
        } catch (Exception e) {
            String msg = "Error in triggering the tenant registration information.";
            log.error(msg, e);
            throw new UserStoreException(msg, e);
        }
    }

    public void updateTenantActivation(int tenantId)
            throws UserStoreException {
        log.info("Alerting tenant activation for tenant " + tenantId);

        try {
            Util.triggerTenantActivation(tenantId);
        } catch (Exception e) {
            String msg = "Error in triggering the service activation information for tenant.";
            log.error(msg, e);
            throw new UserStoreException(msg, e);
        }
    }

    public void updateTenant(int tenantId) throws UserStoreException {
        log.info("Alerting tenant update for for tenant " + tenantId);

        try {
            Util.triggerTenantUpdate(tenantId);
        } catch (Exception e) {
            String msg = "Error in triggering the tenant update information.";
            log.error(msg, e);
            throw new UserStoreException(msg, e);
        }
    }

    public void renameTenant(int tenantId, String oldDomainName,
                             String newDomainName) throws UserStoreException {
        log.info("Alerting tenant renaming for for tenant " + tenantId + ", " +
                "old name: " + oldDomainName + ", new name: " + newDomainName);

        try {
            Util.triggerTenantRename(tenantId, oldDomainName, newDomainName);
        } catch (Exception e) {
            String msg = "Error in triggering the tenant update information.";
            log.error(msg, e);
            throw new UserStoreException(msg, e);
        }
    }

    public int getListenerOrder() {
        return EXECUTION_ORDER;
    }
}
