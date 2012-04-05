/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.governance.services.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.framework.ServiceRegistration;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.config.RegistryContext;
import org.wso2.carbon.registry.core.secure.AuthorizeRoleListener;
import org.wso2.carbon.governance.services.util.Util;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.user.mgt.UserMgtConstants;
import org.wso2.carbon.user.core.listener.AuthorizationManagerListener;

import java.util.Stack;

/**
 * @scr.component name="org.wso2.carbon.governance.services"
 * immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService" cardinality="1..1"
 * policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 */
public class GovernanceMgtUIAddServicesServiceComponent {
    private static Log log = LogFactory.getLog(GovernanceMgtUIAddServicesServiceComponent.class);
    private Stack<ServiceRegistration> registrations = new Stack<ServiceRegistration>();

    protected void activate(ComponentContext context) {
        log.debug("******* Governance Add Services bundle is activated ******* ");
        try {
            registrations.push(context.getBundleContext().registerService(
                    AuthorizationManagerListener.class.getName(),
                    new AuthorizeRoleListener(
                            RegistryConstants.
                                    ADD_SERVICE_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID,
                            RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                            Util.getRegistryService().getConfigSystemRegistry().
                                    getRegistryContext().getServicePath()),
                            UserMgtConstants.UI_ADMIN_PERMISSION_ROOT +
                            "manage/resources/govern/metadata/add",
                            UserMgtConstants.EXECUTE_ACTION, null), null));
            registrations.push(context.getBundleContext().registerService(
                    AuthorizationManagerListener.class.getName(),
                    new AuthorizeRoleListener(
                            RegistryConstants.
                                    LIST_SERVICE_AUTHORIZE_ROLE_LISTENER_EXECUTION_ORDER_ID,
                            RegistryUtils.getAbsolutePath(RegistryContext.getBaseInstance(),
                            Util.getRegistryService().getConfigSystemRegistry().
                                    getRegistryContext().getServicePath()),
                            UserMgtConstants.UI_ADMIN_PERMISSION_ROOT +
                            "manage/resources/govern/metadata/list",
                            UserMgtConstants.EXECUTE_ACTION, new String[] {ActionConstants.GET}),
                            null));
            log.debug("******* Governance Add Services bundle is activated ******* ");
        } catch (Exception e) {
            log.debug("******* Failed to activate Governance Add Services bundle bundle ******* ");
        }
    }

    protected void deactivate(ComponentContext context) {
        while(!registrations.empty()) {
            registrations.pop().unregister();
        }
        log.debug("******* Governance Add Services bundle is deactivated ******* ");
    }

    protected void setRegistryService(RegistryService registryService) {
        Util.setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Util.setRegistryService(null);
    }
}
