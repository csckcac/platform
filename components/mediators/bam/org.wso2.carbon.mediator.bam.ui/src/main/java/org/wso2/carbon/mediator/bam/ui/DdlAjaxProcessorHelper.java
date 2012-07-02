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

package org.wso2.carbon.mediator.bam.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.mediator.bam.config.BamServerConfig;
import org.wso2.carbon.mediator.bam.config.BamServerConfigBuilder;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DdlAjaxProcessorHelper {
    
    private BamServerProfileConfigAdminClient client;
    //private List<StreamConfiguration> streamConfigurations = new ArrayList<StreamConfiguration>();
    //private StreamConfiguration streamConfiguration = new StreamConfiguration();


    public DdlAjaxProcessorHelper(String cookie, String backendServerURL,
                                  ConfigurationContext configContext, Locale locale){
        try {
            client = new BamServerProfileConfigAdminClient(cookie, backendServerURL, configContext, locale);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private BamServerConfig getResource(String bamServerProfileLocation){
        try {
            String resourceString =  client.getResourceString(bamServerProfileLocation);
            OMElement resourceElement = new StAXOMBuilder(new ByteArrayInputStream(resourceString.getBytes())).getDocumentElement();

            BamServerConfigBuilder bamServerConfigBuilder = new BamServerConfigBuilder();
            bamServerConfigBuilder.createBamServerConfig(resourceElement);
            BamServerConfig bamServerConfig = bamServerConfigBuilder.getBamServerConfig();
            return bamServerConfig;
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (XMLStreamException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

    public boolean resourceAlreadyExists(String bamServerProfileLocation){
        try {
            return client.resourceAlreadyExists(bamServerProfileLocation);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return true;
    }

    public boolean isNotNullOrEmpty(String string){
        if(string != null && !string.equals("")){
            return true;
        }
        return false;
    }

    public String getServerProfileNames(String serverProfilePath){
        String serverProfileNamesString = "";
        try {
            String[] serverProfileNames = client.getServerProfilePathList(serverProfilePath);
            for (String serverProfileName : serverProfileNames) {
                serverProfileNamesString = serverProfileNamesString + "<option>" +
                                           serverProfileName.split("/")[serverProfileName.split("/").length-1] +
                                           "</option>";
            }
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return serverProfileNamesString;
    }

    public String getStreamConfigurationNames(String serverProfilePath){
        String streamNames = "";
        /*String realServerProfilePath = this.getRealBamServerProfilePath(serverProfilePath);*/
        BamServerConfig bamServerConfig = this.getResource(serverProfilePath);
        List<StreamConfiguration> streamConfigurations = bamServerConfig.getStreamConfigurations();
        List<String> foundStreamNames = new ArrayList<String>();
        for (StreamConfiguration configuration : streamConfigurations) {
            if(!foundStreamNames.contains(configuration.getName())){ // Add only unique stream names
                streamNames = streamNames + "<option>" + configuration.getName() + "</option>";
            }
            foundStreamNames.add(configuration.getName());
        }
        return streamNames;
    }
    
    public String getVersionListForStreamName(String serverProfilePath, String streamName){
        String streamVersions = "";
        /*String realServerProfilePath = this.getRealBamServerProfilePath(serverProfilePath);*/
        BamServerConfig bamServerConfig = this.getResource(serverProfilePath);
        List<StreamConfiguration> streamConfigurations = bamServerConfig.getStreamConfigurations();
        for (StreamConfiguration configuration : streamConfigurations) {
            if(configuration.getName().equals(streamName)){
                streamVersions = streamVersions + "<option>" + configuration.getVersion() + "</option>";
            }
        }
        return streamVersions;
    }

    /*private String getRealBamServerProfilePath(String shortServerProfilePath){
        if(shortServerProfilePath != null){
            String registryType = shortServerProfilePath.split(":")[0];
            if (isNotNullOrEmpty(registryType) && registryType.equals("conf")){
                return shortServerProfilePath.split(":")[1];
            }
            return null;
        }
        return null;
    }*/
}
