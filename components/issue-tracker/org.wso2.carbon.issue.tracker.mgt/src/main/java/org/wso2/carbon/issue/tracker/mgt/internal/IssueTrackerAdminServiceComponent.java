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
package org.wso2.carbon.issue.tracker.mgt.internal;

import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.issue.tracker.mgt.TenantActivityListener;
import org.wso2.carbon.issue.tracker.mgt.config.ManagerConfigurations;
import org.wso2.carbon.stratos.common.listeners.TenantMgtListener;
import org.wso2.carbon.stratos.common.util.StratosConfiguration;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;


/**
 * @scr.component name="org.wso2.carbon.issue.tracker.mgt.component" immediate="true"
 * @scr.reference name="org.wso2.carbon.configCtx"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContext" unbind="unsetConfigurationContext"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="stratos.config.service"
 * interface="org.wso2.carbon.stratos.common.util.StratosConfiguration" cardinality="1..1"
 * policy="dynamic" bind="setStratosConfigurationService" unbind="unsetStratosConfigurationService"
 */
public class IssueTrackerAdminServiceComponent {

    private static Log log = LogFactory.getLog(IssueTrackerAdminServiceComponent.class);

    private static ConfigurationContextService configCtxService;
    private static RealmService realmService;
    private static StratosConfiguration stratosConfiguration;

    private static ManagerConfigurations managerConfigurations;

    protected void activate(ComponentContext context) {

        if (log.isDebugEnabled()) {
            log.info("**************Issue tracker mgt bundle is activated*************");
        }

        try {
            int tenantId = SuperTenantCarbonContext.getCurrentContext().getTenantId();
            SuperTenantCarbonContext.startTenantFlow();
            SuperTenantCarbonContext.getCurrentContext().setTenantId(tenantId);
            managerConfigurations = new ManagerConfigurations();
            managerConfigurations.setStratosConfiguration(stratosConfiguration);
            BundleContext bundleContext = context.getBundleContext();
            TenantActivityListener tenantActivityListener = new TenantActivityListener();
            bundleContext.registerService(TenantMgtListener.class.getName(),
                    tenantActivityListener, null);
        } catch (Exception e) {
            log.error("Error in setting tenant information", e);
        } finally {
            SuperTenantCarbonContext.endTenantFlow();
        }

    }

    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Issue tracker mgt bundle is deactivated");
        }
    }

    protected void setConfigurationContext(ConfigurationContextService ctxService) {
        IssueTrackerAdminServiceComponent.configCtxService = ctxService;
    }

    protected void unsetConfigurationContext(ConfigurationContextService ctxService) {
        IssueTrackerAdminServiceComponent.configCtxService = null;
    }

    public static ConfigurationContextService getConfigCtxService() {
        return configCtxService;
    }

    protected void setRealmService(RealmService realmService) {
        IssueTrackerAdminServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        setRealmService(null);
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    protected void setStratosConfigurationService(StratosConfiguration stratosConfigService) {
        IssueTrackerAdminServiceComponent.stratosConfiguration=stratosConfigService;
    }

    protected void unsetStratosConfigurationService(StratosConfiguration ccService) {
        IssueTrackerAdminServiceComponent.stratosConfiguration = null;
        managerConfigurations.setStratosConfiguration(null);
    }


    public static ManagerConfigurations getManagerConfigurations() {
        return managerConfigurations;
    }

}
