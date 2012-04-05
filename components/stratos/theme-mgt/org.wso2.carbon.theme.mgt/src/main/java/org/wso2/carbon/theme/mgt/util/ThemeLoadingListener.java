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
package org.wso2.carbon.theme.mgt.util;

import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.core.UserStoreException;

public class ThemeLoadingListener implements TenantMgtListener {
    private static final Log log = LogFactory.getLog(ThemeLoadingListener.class);
    private static final int EXEC_ORDER = 10;
    public void addTenant(int tenantId) throws UserStoreException {
        try {
            ThemeUtil.loadTheme(tenantId);
        } catch (RegistryException e) {
            String domainName = null;
            try {
                domainName = ThemeUtil.getRealmService().getTenantManager().getDomain(tenantId);
            } catch (org.wso2.carbon.user.api.UserStoreException e1) {
                log.error("Cannot get domain for tenant " + tenantId, e1);
            }
            String msg = "Error in loading the theme for the tenant: " + domainName + ".";
            log.error(msg, e);
            throw new UserStoreException(msg, e);
        }
    }
    
    public void updateTenant(int tenantId) throws UserStoreException {
        // doing nothing
    }
    
    public void renameTenant(int tenantId, String oldDomainName,
                             String newDomainName) throws UserStoreException {
        // doing nothing
    }

    public int getListenerOrder() {
        return EXEC_ORDER;
    }
}
