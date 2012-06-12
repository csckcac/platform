package org.wso2.carbon.mediator.bam.config;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;

import org.wso2.carbon.registry.common.services.RegistryAbstractAdmin;
import org.wso2.carbon.registry.core.ActionConstants;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
public class RegistryManager extends RegistryAbstractAdmin {

    private static final Log log = LogFactory.getLog(RegistryManager.class);

    public void saveResourceString(String resourceString, String gadgetResourcePath) {

        Registry registry = getConfigSystemRegistry();
        Resource resource;
        try {
            resource = registry.newResource();
            resource.setContent(resourceString);
            registry.put(gadgetResourcePath, resource);
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
    
    public boolean resourceAlreadyExists(String bamServerProfileLocation){
        Registry registry = getConfigSystemRegistry();
        try {
            return registry.resourceExists(bamServerProfileLocation);
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return true;
    }

    public String getResourceString(String bamServerProfileLocation){
        Registry registry = getConfigSystemRegistry();
        //Registry registry = getGovernanceSystemRegistry();
        Resource resource;
        try {
            resource = registry.get(bamServerProfileLocation);
            return new String((byte[])resource.getContent());
        } catch (RegistryException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

}
