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
package org.wso2.carbon.issue.tracker.core.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.issue.tracker.core.util.Util;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.ConfigurationContextService;


/**
 * @scr.component name="org.wso2.carbon.issue.tracker.core" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="configuration.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class IssueTrackerServiceComponent {

    private static Log log = LogFactory.getLog(IssueTrackerServiceComponent.class);

    private static RegistryService registryService;

    private ServerConfiguration serverConfig;

    private static ConfigurationContextService configurationContextService;

    private static RealmService realmService;

    protected void activate(ComponentContext ctxt) {

        if (log.isDebugEnabled()) {
            log.info("**************Issue tracker core bundle is activated*************");
        }

    }


    protected void deactivate(ComponentContext ctxt) {
        if (log.isDebugEnabled()) {
            log.info("Issue tracker core bundle is deactivated");
        }
    }


    protected void setRegistryService(RegistryService registryService) {
        IssueTrackerServiceComponent.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        Util.setRegistryService(null);
    }


    public static void registerRetrieverServices(BundleContext bundleContext) throws Exception {
        ConfigurationContextService confCtxSvc = Util.getConfigurationContextService();
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        this.configurationContextService = configurationContextService;
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
    }


    protected void setServerConfiguration(ServerConfiguration serverConfig) {
        this.serverConfig = serverConfig;
    }

    protected void unsetServerConfiguration(ServerConfiguration serverConfig) {
        this.serverConfig = null;
    }

    protected void setRealmService(RealmService realmService) {
        IssueTrackerServiceComponent.realmService = realmService;
    }

    protected void unsetRealmService(RealmService realmService) {
        setRealmService(null);
    }


}
