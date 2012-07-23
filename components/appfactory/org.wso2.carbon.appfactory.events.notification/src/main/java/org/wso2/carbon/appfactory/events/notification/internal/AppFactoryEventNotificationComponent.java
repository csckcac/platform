/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.appfactory.events.notification.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementService;

/**
 * @scr.component name="org.wso2.carbon.appfactory.core.internal.AppFactoryCoreServiceComponent"
 *                immediate="true"
 *               @scr.reference name="appfactory.mgt"
 *               interface="org.wso2.carbon.appfactory.application.mgt.service.ApplicationManagementService"
 *               cardinality="1..1" policy="dynamic"
 *               bind="setApplicationManagementService"
 *               unbind="unsetApplicationManagementService"
 *
 */

public class AppFactoryEventNotificationComponent {

    private static final Log log = LogFactory
            .getLog(AppFactoryEventNotificationComponent.class);
    private static ApplicationManagementService applicationManagementService;

    protected void activate(ComponentContext context) {
        // BundleContext bundleContext = context.getBundleContext();
        try {
            if (log.isDebugEnabled()) {
                log.debug("Appfactory core bundle is activated");
            }
        } catch (Throwable e) {
            log.error("Error in creating appfactory configuration", e);
        }

    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Appfactory common bundle is deactivated");
        }
    }

    protected void setApplicationManagementService(ApplicationManagementService service){
        applicationManagementService = service;
    }

    protected void unsetApplicationManagementService(ApplicationManagementService service){
        applicationManagementService = null;
    }
    
    public static ApplicationManagementService getApplicationManagementService(){
        return applicationManagementService;
    }
}
