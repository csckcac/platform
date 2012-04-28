/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.impl.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationServiceImpl;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Properties;

/**
 * @scr.component name="org.wso2.apimgt.impl.services" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */
public class APIManagerComponent {

    private static final Log log = LogFactory.getLog(APIManagerComponent.class);

    private ServiceRegistration registration;

    protected void activate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("API manager component activated");
        }
        
        try {
            addRxtConfigs();
            APIManagerConfiguration configuration = new APIManagerConfiguration();
            String filePath = CarbonUtils.getCarbonHome() + File.separator + "repository" +
                    File.separator + "conf" + File.separator + "amConfig.xml";
            configuration.load(filePath);
            APIManagerConfigurationServiceImpl configurationService =
                    new APIManagerConfigurationServiceImpl(configuration);
            ServiceReferenceHolder.getInstance().setAPIManagerConfigurationService(configurationService);
            registration = componentContext.getBundleContext().registerService(
                    APIManagerConfigurationService.class.getName(),
                    configurationService, new Properties());
        } catch (APIManagementException e) {
            log.fatal("Error while initializing the API manager component", e);
        }
    }

    protected void deactivate(ComponentContext componentContext) {
        if (log.isDebugEnabled()) {
            log.debug("Deactivating API manager component");
        }
        registration.unregister();
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

    public static RegistryService getRegistryService() throws RegistryException {
        RegistryService registryService = ServiceReferenceHolder.getInstance().getRegistryService();
        if (registryService == null) {
            log.error("Failed to get RegistryService");
            throw new RegistryException("Registry Service instance null");
        }
        return registryService;
    }

    private static void addRxtConfigs() throws APIManagementException {
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
            throw new APIManagementException("Failed to get registry", e);
        }

        for (String rxtPath : rxtFilePaths) {
            String resourcePath = APIConstants.RXT_PATH + RegistryConstants.PATH_SEPARATOR + rxtPath;
            try {
                if (registry.resourceExists(resourcePath)) {
                    continue;
                }
                String rxt = FileUtil.readFileToString(dir + File.separator + rxtPath);
                Resource resource = registry.newResource();
                resource.setContent(rxt.getBytes());
                resource.setMediaType(APIConstants.RXT_MEDIA_TYPE);
                systemRegistry.put(resourcePath, resource);
            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new APIManagementException(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new APIManagementException(msg, e);
            }
        }
    }
}
