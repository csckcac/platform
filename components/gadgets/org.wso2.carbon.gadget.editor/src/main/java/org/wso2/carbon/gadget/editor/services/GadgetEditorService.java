/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.gadget.editor.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.registry.core.ResourcePath;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

public class GadgetEditorService extends AbstractAdmin {
    private static final Log log = LogFactory.getLog(GadgetEditorService.class);
    public boolean saveGadget(String location, String content)throws RegistryException{
        Registry registry = getConfigSystemRegistry();
        if (RegistryUtils.isRegistryReadOnly(registry.getRegistryContext())) {
            return false;
        }
        try {
            Resource gadget;
            try {
                gadget = registry.get(location);
            } catch (RegistryException e) {
                gadget = registry.newResource();
            }
            gadget.setContent(content.getBytes());
            registry.put(location, gadget);            
        } catch (Exception e) {
            if (e instanceof RegistryException) {
                throw (RegistryException)e;
            }
            throw new RegistryException("Unable to save editor", e);
        }
        return true;
    }
    
    public String openGadget(String location)throws RegistryException{
        Registry registry = getConfigSystemRegistry();
        if(!registry.resourceExists(new ResourcePath(location).getPath())){
            return null;
        }
        Resource resource = registry.get(location);
        return new String((byte[])resource.getContent());
    }

    public String[] getImmediateChildResources(String path) {
        /* ending '/' for collections */
        /* without '/' for resources */
        return null;
    }

    public boolean isResourceExist(String path) {
        return false;
    }

    /*public String getGadgetConfiguration() throws RegistryException{
        try{
            Registry registry = getConfigSystemRegistry();
            return Util.getServiceConfig(registry);
        }catch (Exception e){
            return null;
        }
    }

    public boolean saveGadgetConfiguration(String update) throws RegistryException{
        if (RegistryUtils.isRegistryReadOnly()) {
            return false;
        }
        try{
            Registry registry = getConfigSystemRegistry();
            Resource resource = registry.get(RegistryConstants.GOVERNANCE_SERVICES_CONFIG_PATH + "service");
            resource.setContent(update);
            registry.put(RegistryConstants.GOVERNANCE_SERVICES_CONFIG_PATH + "service", resource);
            return true;
        }catch(Exception RegistryException){
            return false;
        }
    }
    
    public String getServicePath() throws RegistryException{
        try{
            Registry registry = getGovernanceRegistry();
            return registry.getRegistryContext().getServicePath();
        }catch(Exception RegistryException){
            return null;
        }
    } */
}

