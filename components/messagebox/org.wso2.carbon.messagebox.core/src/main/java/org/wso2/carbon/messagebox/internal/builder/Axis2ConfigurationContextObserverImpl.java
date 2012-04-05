/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.messagebox.internal.builder;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.messagebox.MessageBoxConstants;
import org.wso2.carbon.messagebox.internal.ds.MessageBoxServiceValueHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.AbstractAxis2ConfigurationContextObserver;

public class Axis2ConfigurationContextObserverImpl extends
                                                   AbstractAxis2ConfigurationContextObserver {
    private static Log log = LogFactory.getLog(Axis2ConfigurationContextObserverImpl.class);

    public void createdConfigurationContext(ConfigurationContext configurationContext) {
        try {
            UserRegistry userRegistry =
                    MessageBoxServiceValueHolder.getInstance().getRegistryService().getGovernanceSystemRegistry(
                            SuperTenantCarbonContext.getCurrentContext(configurationContext).getTenantId());
            if (!userRegistry.resourceExists(MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH)) {
                userRegistry.put(MessageBoxConstants.MB_MESSAGE_BOX_STORAGE_PATH,
                                 userRegistry.newCollection());
            }
            if (!userRegistry.resourceExists(MessageBoxConstants.MB_QUEUE_STORAGE_PATH)) {
                userRegistry.put(MessageBoxConstants.MB_QUEUE_STORAGE_PATH,
                                 userRegistry.newCollection());
            }
        } catch (RegistryException e) {
            log.error("Error in setting message box storage path in tenant", e);
        }
    }
}
