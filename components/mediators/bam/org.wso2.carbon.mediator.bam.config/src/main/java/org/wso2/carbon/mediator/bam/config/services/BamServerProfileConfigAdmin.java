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
package org.wso2.carbon.mediator.bam.config.services;

import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.mediator.bam.config.RegistryManager;

/**
 * Admin service class to access Registry
 */
public class BamServerProfileConfigAdmin extends AbstractAdmin {

    //private RegistryAccess bamServerProfileUtils;
    private RegistryManager registryManager;

    public BamServerProfileConfigAdmin() {
        //bamServerProfileUtils = new RegistryAccess();
        registryManager = new RegistryManager();
    }

    public void saveResourceString(String resourceString, String bamServerProfileLocation) {
        //bamServerProfileUtils.saveResourceString(resourceString, bamServerProfileLocation);
        registryManager.saveResourceString(resourceString, bamServerProfileLocation);
    }

    public String getResourceString(String bamServerProfileLocation){
        //return bamServerProfileUtils.getResourceString(bamServerProfileLocation);
        return registryManager.getResourceString(bamServerProfileLocation);
    }

    public boolean resourceAlreadyExists(String bamServerProfileLocation){
        //return bamServerProfileUtils.resourceAlreadyExists(bamServerProfileLocation);
        return registryManager.resourceAlreadyExists(bamServerProfileLocation);
    }

}
