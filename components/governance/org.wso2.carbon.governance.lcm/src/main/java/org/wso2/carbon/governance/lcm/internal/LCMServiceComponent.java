/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.governance.lcm.internal;

import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.governance.lcm.util.CommonUtil;
import org.wso2.carbon.governance.lcm.listener.LifecycleLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService;
import org.wso2.carbon.registry.core.utils.UUIDGenerator;

/**
 * @scr.component name="org.wso2.carbon.governance.lcm"
 * immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="login.subscription.service"
 * interface="org.wso2.carbon.core.services.callback.LoginSubscriptionManagerService" cardinality="0..1"
 * policy="dynamic" bind="setLoginSubscriptionManagerService" unbind="unsetLoginSubscriptionManagerService"
 */
public class LCMServiceComponent {

    private static Log log = LogFactory.getLog(LCMServiceComponent.class);

    protected void activate(ComponentContext context) {
        log.debug("******* Governance Life Cycle Management Service bundle is activated ******* ");
    }

    protected void deactivate(ComponentContext context) {
        log.debug("******* Governance Life Cycle Management Service bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(registryService);
        // Generate LCM search query if it doesn't exist.
        try {
            CommonUtil.isLifecycleNameInUse(UUIDGenerator.generateUUID(),
                    registryService.getConfigSystemRegistry(),
                    CommonUtil.getRootSystemRegistry());
        } catch (Exception e) {
            log.error("An error occurred while setting up Governance Life Cycle Management", e);
        }
        /*
        String[] lifecycles = null;
        try {
            CommonUtil.addDefaultLifecyclesIfNotAvailable();
            lifecycles = CommonUtil.getLifecycleList();
        } catch (Exception e) {
            log.warn("An error occured while populating lifecycles");
        }
        if (lifecycles != null && lifecycles.length > 0) {
            for(String lifecycle: lifecycles) {
                try {
                    CommonUtil.generateAspect(CommonUtil.getContextRoot() + lifecycle);
                } catch (Exception e) {
                    continue;
                }
            }
        }
        */
    }

    protected void unsetRegistryService(RegistryService registryService) {
        CommonUtil.setRegistryService(null);
    }

    protected void setLoginSubscriptionManagerService(LoginSubscriptionManagerService loginManager) {
        log.debug("******* LoginSubscriptionManagerServic is set ******* ");
        loginManager.subscribe(new LifecycleLoader());
    }

    protected void unsetLoginSubscriptionManagerService(LoginSubscriptionManagerService loginManager) {
        log.debug("******* LoginSubscriptionManagerServic is unset ******* ");
    }
}
