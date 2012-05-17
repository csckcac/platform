/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.governance.registry.extensions.internal;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileUtil;

import java.io.File;
import java.io.IOException;

/**
 * @scr.component name="org.wso2.governance.registry.extensions.services" immediate="true"
 * @scr.reference name="registry.service"
 * interface="org.wso2.carbon.registry.core.service.RegistryService"
 * cardinality="1..1" policy="dynamic"  bind="setRegistryService" unbind="unsetRegistryService"
 */

public class GovernanceRegistryExtensionsComponent {

    private static final Log log = LogFactory.getLog(GovernanceRegistryExtensionsComponent.class);
    RegistryService registryService = null;
    private static final String RXT_PATH = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH +
            RegistryConstants.PATH_SEPARATOR + "repository" + RegistryConstants.PATH_SEPARATOR + "components" +
            RegistryConstants.PATH_SEPARATOR + "org.wso2.carbon.governance" + RegistryConstants.PATH_SEPARATOR + "types";
    private static final String RXT_MEDIA_TYPE = "application/vnd.wso2.registry-ext-type+xml";

    protected void activate(ComponentContext componentContext) {
       if(log.isDebugEnabled()){
           log.debug("GovernanceRegistryExtensionsComponent activated");
       }
        try {
            addRxtConfigs();
        } catch (GovernanceException e) {
            log.error("Failed to add rxt files to registry",e);
        }
    }

    protected void setRegistryService(RegistryService registryService) {
        if(registryService!=null && log.isDebugEnabled()){
          log.debug("Registry service initialized");
        }
        this.registryService = registryService;
    }

    protected void unsetRegistryService(RegistryService registryService) {
        this.registryService = null;
    }

    public RegistryService getRegistryService() throws RegistryException {
        return registryService;
    }

    private void addRxtConfigs() throws GovernanceException {
        String path = CarbonUtils.getCarbonHome() + File.separator + "repository" + File.separator +
                "resources" + File.separator + "rxts";
        File file = new File(path);
        String[] rxtFilePaths = file.list();

        for (String rxtPath : rxtFilePaths) {
            try {
                if (!rxtPath.contains(".rxt")) {
                    continue;
                }
                String rxt = FileUtil.readFileToString(path + File.separator + rxtPath);
                Registry registry = registryService.getRegistry();
                UserRegistry systemRegistry = registryService.getRegistry(CarbonConstants.REGISTRY_SYSTEM_USERNAME);
                Resource resource = registry.newResource();
                resource.setContent(rxt.getBytes());
                resource.setMediaType(RXT_MEDIA_TYPE);
                String resourcePath = RXT_PATH + RegistryConstants.PATH_SEPARATOR + rxtPath;
                if (!registry.resourceExists(resourcePath)) {
                    systemRegistry.put(resourcePath, resource);
                }
            } catch (IOException e) {
                String msg = "Failed to read rxt files";
                throw new GovernanceException(msg, e);
            } catch (RegistryException e) {
                String msg = "Failed to add rxt to registry ";
                throw new GovernanceException(msg, e);
            }
        }

    }

}
