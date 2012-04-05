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
package org.wso2.carbon.tenant.reg.agent.service.util;

import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;

import java.util.ArrayList;
import java.util.List;

public class Util {
    private static List<TenantMgtListener> tenantMgtListeners = new ArrayList<TenantMgtListener>();

    public static void addTenantMgtListener(TenantMgtListener tenantMgtListener) {
        tenantMgtListeners.add(tenantMgtListener);
    }

    public static void removeTenantMgtListener(TenantMgtListener tenantMgtListener) {
        tenantMgtListeners.remove(tenantMgtListener);
    }

    public static void triggerAddTenant(int tenantId) throws UserStoreException {
        for (TenantMgtListener tenantMgtListener: tenantMgtListeners) {
            tenantMgtListener.addTenant(tenantId);
        }
    }

    public static void triggerUpdateTenant(int tenantId) throws UserStoreException {
        for (TenantMgtListener tenantMgtListener: tenantMgtListeners) {
            tenantMgtListener.updateTenant(tenantId);
        }
    }

    public static void renameTenant(int tenantId, String oldName, String newName) throws UserStoreException {
        for (TenantMgtListener tenantMgtListener: tenantMgtListeners) {
            tenantMgtListener.renameTenant(tenantId, oldName, newName);
        }
    }        
}
