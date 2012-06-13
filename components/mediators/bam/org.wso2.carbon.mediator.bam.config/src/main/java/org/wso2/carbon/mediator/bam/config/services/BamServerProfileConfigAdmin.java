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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.mediator.bam.config.BamServerConfig;
import org.wso2.carbon.mediator.bam.config.BamServerConfigBuilder;
import org.wso2.carbon.mediator.bam.config.RegistryManager;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;

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
    




/*    public boolean insertBamServerConfig(BamServerConfigBuilder bamServerConfigBuilder, String bamServerConfigLocation){
        if(registryManager.resourceAlreadyExists(bamServerConfigLocation)){
            return false;
        }
        else{

        }
    }*/

    public BamServerConfig getBamServerConfig(String bamServerConfigLocation){
        String resourceString = registryManager.getResourceString(bamServerConfigLocation);
        BamServerConfigBuilder bamServerConfigBuilder = new BamServerConfigBuilder();
        try {
            OMElement resourceElement = new StAXOMBuilder(new ByteArrayInputStream(resourceString.getBytes())).getDocumentElement();
            bamServerConfigBuilder.createBamServerConfig(resourceElement);
            return bamServerConfigBuilder.getBamServerConfig();
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public boolean bamServerConfigExists(String bamServerConfigLocation){
        return registryManager.resourceAlreadyExists(bamServerConfigLocation);
    }

}
