/*
 * Copyright 2004,2005 The Apache Software Foundation.
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
package org.wso2.carbon.tenant.reg.agent.service;

import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.tenant.reg.agent.service.util.Util;

public class TenantRegAgentService implements TenantMgtListener {

    public void addTenant(int tenantId) throws UserStoreException {
        Util.triggerAddTenant(tenantId);
    }

    public void updateTenant(int tenantId) throws UserStoreException {
        Util.triggerUpdateTenant(tenantId);
    }

    public void renameTenant(int tenantId, String oldDomainName,
                             String newDomainName) throws UserStoreException {
        Util.renameTenant(tenantId, oldDomainName, newDomainName);
    }

    // this method is not required for the service..
    public int getListenerOrder() {
        return 0;
    }
}
