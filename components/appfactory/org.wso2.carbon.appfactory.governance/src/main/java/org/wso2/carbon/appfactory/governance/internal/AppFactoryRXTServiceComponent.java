/*
 * Copyright 2005-2011 WSO2, Inc. (http://wso2.com)
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.wso2.carbon.appfactory.governance.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.api.util.GovernanceConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @scr.component name="appfactory.rxt" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic" bind="setRegistryService" unbind="unsetRegistryService"
 * @scr.reference name="user.realm.service"
 * interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 */
public class AppFactoryRXTServiceComponent {

    private static final Log log = LogFactory.getLog(AppFactoryRXTServiceComponent.class);

    protected void activate(ComponentContext context) {
        log.info("AppFactoryRXTServiceComponent activating...");
        BundleContext bundleContext = context.getBundleContext();
        //AppFactoryConfiguration configuration;

        try {
            addRxtConfigs();
        } catch (Exception e) {
            log.error("Can not add rxts", e);
        }

        try {
            //addAppFactoryLifecycles();
        } catch (Exception e) {
            log.error("Can not add AppFactory Lifecycles..", e);
        }

        if (log.isDebugEnabled()) {
            log.debug("Appfactory common bundle is activated");
        }
    }

    protected void deactivate(ComponentContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Appfactory common bundle is deactivated");
        }
    }

     protected void setRegistryService(RegistryService registryService) {
        if (registryService != null && log.isDebugEnabled()) {
            log.debug("Registry service initialized");
        }
        ServiceReferenceHolder.getInstance().setRegistryService(registryService);
    }

    protected void unsetRegistryService(RegistryService registryService) {
        ServiceReferenceHolder.getInstance().setRegistryService(null);
    }

    protected void setRealmService(RealmService realmService) {
        if (realmService != null && log.isDebugEnabled()) {
            log.debug("Realm service initialized");
        }
        ServiceReferenceHolder.getInstance().setRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        ServiceReferenceHolder.getInstance().setRealmService(null);
    }

    /**
     * Register rxt's defined in rxts folder
     * @throws Exception
     */
    private void addRxtConfigs() throws Exception {
        String rxtDir = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "resources" + File.separator + "rxts";
        File file = new File(rxtDir);
        //create a FilenameFilter
        FilenameFilter filenameFilter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                //if the file extension is .rxt return true, else false
                return name.endsWith(".rxt");
            }
        };
        String[] rxtFilePaths = file.list(filenameFilter);
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        UserRegistry systemRegistry;
        Registry registry;
        try {
            systemRegistry = registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
            registry = registryService.getRegistry();
        } catch (RegistryException e) {
            throw new Exception("Failed to get registry", e);
        }

        for (String rxtPath : rxtFilePaths) {
            String resourcePath = GovernanceConstants.RXT_CONFIGS_PATH +
                    RegistryConstants.PATH_SEPARATOR + rxtPath;
            try {
                if (registry.resourceExists(resourcePath)) {
                    continue;
                }
                String rxt = FileUtil.readFileToString(rxtDir + File.separator + rxtPath);
                Resource resource = registry.newResource();
                resource.setContent(rxt.getBytes());
                resource.setMediaType("application/vnd.wso2.registry-ext-type+xml");

                systemRegistry.put(resourcePath, resource);
            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new Exception(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new Exception(msg, e);
            }
        }
    }


}
