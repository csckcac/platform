/*
 * Copyright WSO2, Inc. (http://wso2.com)
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
package org.wso2.carbon.url.mapper.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.tomcat.api.CarbonTomcatService;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.TomcatValveContainer;
import org.wso2.carbon.url.mapper.HotUpdateService;
import org.wso2.carbon.url.mapper.UrlMapperValve;
import org.wso2.carbon.url.mapper.internal.util.DataHolder;
import org.wso2.carbon.url.mapper.internal.util.UrlMapperConstants;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;

import java.util.ArrayList;
import java.util.List;

import static org.wso2.carbon.url.mapper.internal.util.HostUtil.addHostToEngine;
import static org.wso2.carbon.url.mapper.internal.util.HostUtil.getAllHostsFromRegistry;

/**
 * This is urlmapper component which retrieve virtual host from
 * registry and add to tomcat engine in its initialization.
 * adds the CarbonTomcatValve to TomcatContainer.
 *
 * @scr.component name="org.wso2.carbon.url.mapper" immediate="true"
 * @scr.reference name="tomcat.service.provider"  
 * interface="org.wso2.carbon.tomcat.api.CarbonTomcatService"
 * cardinality="1..1" policy="dynamic" bind="setCarbonTomcatService"
 * unbind="unsetCarbonTomcatService"
 * @scr.reference name="org.wso2.carbon.registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService"
 * unbind="unsetRegistryService"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"
 * unbind="unsetRealmService"
 */
public class UrlMapperServiceComponent {
    private static Log log = LogFactory.getLog(UrlMapperServiceComponent.class);
    private ServiceRegistration serviceRegistration;

    protected void activate(final ComponentContext componentContext) {
        final BundleContext bundleContext = componentContext.getBundleContext();
        // If Carbon is running as a webapp within some other servlet container, then we should
        // uninstall this component
        if (!CarbonUtils.isRunningInStandaloneMode()) {
            Thread th = new Thread() {
                public void run() {
                    try {
                        bundleContext.getBundle().uninstall();
                    } catch (Throwable e) {
                        log.warn("Error occurred while uninstalling webapp-mgt UI bundle", e);
                    }
                }
            };
            try {
                th.join();
            } catch (InterruptedException ignored) {
            }
            th.start();
        }
        serviceRegistration = bundleContext.
                registerService(HotUpdateService.class.getName(), new HotUpdateManager(), null);
        //register the UrlMapperValve to the TomcatValveContainer to add to the tomcat engine
        List<CarbonTomcatValve> carbonTomcatValves = new ArrayList<CarbonTomcatValve>();
        carbonTomcatValves.add(new UrlMapperValve());
        TomcatValveContainer.addValves(carbonTomcatValves);
        try {
            //adding the all existing virtual hosts to the tomcat engine in the server startup
            addHostToTomcat();
        } catch (Exception e) {
            log.warn("Error occurred while activating the UrlMapperServiceComponent", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        serviceRegistration.unregister();
    }

    protected void setCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        //keeping the carbonTomcatService in UrlMapperAdminService class
        DataHolder.getInstance().setCarbonTomcatService(carbonTomcatService);
    }

    protected void unsetCarbonTomcatService(CarbonTomcatService carbonTomcatService) {
        DataHolder.getInstance().setCarbonTomcatService(null);
    }

    protected void setRegistryService(RegistryService registryService) {
        try {
            DataHolder.getInstance().setRegistry(registryService.getGovernanceSystemRegistry());
        } catch (Exception e) {
            log.error("Cannot  retrieve System Registry", e);
        }
    }

    protected void unsetRegistryService(RegistryService registryService) {
        DataHolder.getInstance().setRegistry(null);
    }

    protected void setRealmService(RealmService realmService) {
        //keeping the realm service in the UrlMapperAdminService class
        DataHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        DataHolder.getInstance().setRealmService(null);
    }

    /**
     * Reads the registry for existing virtual host and adding them to the tomcat
     * engine with deploying the webapps if webapps is already existed.
     *
     * @throws Exception If an error occurs when reading from the registry
     */
    public void addHostToTomcat() throws Exception {
        List<String> hosts = getAllHostsFromRegistry();
        if (hosts != null) {
            for (String hostName : hosts) {
                //getting hostname by removing the path
                hostName = hostName.substring(UrlMapperConstants.HostProperties.HOSTINFO_DIR.length());
                //add webapp to host and adding them to tomcat
                addHostToEngine(hostName);
            }
        }
    }
}
