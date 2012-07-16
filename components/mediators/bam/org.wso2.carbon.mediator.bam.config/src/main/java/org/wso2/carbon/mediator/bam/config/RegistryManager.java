/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.bam.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

/**
 * Does Configuration Registry operations required to store/fetch BAM server configurations
 */
public class RegistryManager extends RegistryAbstractAdmin {

    private static final Log log = LogFactory.getLog(RegistryManager.class);
    private Registry registry;
    private Resource resource;

    public RegistryManager(){
        registry = getConfigSystemRegistry();
    }

    public void saveResourceString(String resourceString, String gadgetResourcePath){
        try {
            resource = registry.newResource();
            resource.setContent(resourceString);
            registry.put(gadgetResourcePath, resource);
        } catch (RegistryException e) {
            String errorMsg = "Error while saving resource string from Registry. " + e.getMessage();
            log.error(errorMsg, e);
        }
    }
    
    public boolean resourceAlreadyExists(String bamServerProfileLocation){
        try {
            return registry.resourceExists(bamServerProfileLocation);
        } catch (RegistryException e) {
            String errorMsg = "Error while checking resource string from Registry. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return true;
    }

    public boolean removeResource(String path){
        try {
            registry.delete(path);
            return true;
        } catch (RegistryException e) {
            String errorMsg = "Error while removing the resource from Registry. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return false;
    }

    public String getResourceString(String bamServerProfileLocation){
        try {
            resource = registry.get(bamServerProfileLocation);
            return new String((byte[])resource.getContent());
        } catch (RegistryException e) {
            String errorMsg = "Error while getting the resource from Registry. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return null;
    }
    
    public boolean addCollection(String bamServerProfileCollectionLocation){
        try {
            resource = registry.newCollection();
            registry.put(bamServerProfileCollectionLocation, resource);
            return true;
        } catch (RegistryException e) {
            String errorMsg = "Error while adding the collection to the Registry. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return false;
    }
    
    public String[] getServerProfileNameList(String bamServerProfileCollectionLocation){
        try {
            return ((String[])registry.get(bamServerProfileCollectionLocation).getContent());
        } catch (RegistryException e) {
            String errorMsg = "Error while getting the Server Profile Name List from the Registry. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return new String[0];
    }

}
