/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.proxyadmin.tooling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.proxyadmin.common.service.IProxyServiceAdmin;
import org.wso2.carbon.proxyadmin.tooling.service.ProxyServiceAdmin;
import org.wso2.carbon.proxyadmin.tooling.util.ConfigHolder;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;

/**
 * @scr.component name="org.wso2.carbon.proxyadmin.tooling" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"
 * bind="setRegistryService" unbind="unsetRegistryService"
 */
public class ProxyAdminServiceComponent {

    private static final Log log = LogFactory.getLog(ProxyAdminServiceComponent.class);


    protected void activate(ComponentContext context) {
        try {
            BundleContext bc = context.getBundleContext();
            bc.registerService(IProxyServiceAdmin.class.getName(), new ProxyServiceAdmin(), null);
            if (log.isDebugEnabled()) {
                log.debug("Endpoint Editor tooling bundle is activated ");
            }
        } catch (Throwable e) {
            log.error("Failed to activate Endpoint Editor tooling bundle ", e);
        }
    }

    protected void deactivate(ComponentContext context) {   
    }


    protected void setRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService bound to the ESB initialization process");
        }
        try {
            ConfigHolder.getInstance().setRegistry(regService.getSystemRegistry());
        } catch (RegistryException e) {
            log.error("Couldn't retrieve the registry from the registry service");
        }
    }

    protected void unsetRegistryService(RegistryService regService) {
        if (log.isDebugEnabled()) {
            log.debug("RegistryService unbound from the ESB environment");
        }
        ConfigHolder.getInstance().setRegistry(null);
    }
}
