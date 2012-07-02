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

package org.wso2.carbon.mediator.bam.config.ui;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.mediator.bam.config.BamServerConfig;
import org.wso2.carbon.mediator.bam.config.BamServerConfigBuilder;
import org.wso2.carbon.mediator.bam.config.BamServerConfigXml;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;
import org.wso2.carbon.mediator.bam.config.stream.Property;
import org.wso2.carbon.mediator.bam.config.stream.StreamEntry;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BamServerProfileUtils {

    private BamServerProfileConfigAdminClient client;

    public BamServerProfileUtils(String cookie, String backendServerURL, ConfigurationContext configContext, Locale locale){
        try {
            client = new BamServerProfileConfigAdminClient(cookie, backendServerURL, configContext, locale);
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void addResource(String ip, String port, String userName, String password, String streamConfigurationListString,
                            String bamServerProfileLocation){

        List<StreamConfiguration> streamConfigurations = this.getStreamConfigurationListFromString(streamConfigurationListString);
        BamServerConfigXml mediatorConfigurationXml = new BamServerConfigXml();
        String encryptedPassword = this.encryptPassword(password);
        OMElement storeXml = mediatorConfigurationXml.buildServerProfile(ip, port, userName, encryptedPassword, streamConfigurations);
        String stringStoreXml = storeXml.toString();

        try {
            client.saveResourceString(stringStoreXml, this.getRealBamServerProfilePath(bamServerProfileLocation));
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private List<StreamConfiguration> getStreamConfigurationListFromString(String streamConfigurationListString){
        List<StreamConfiguration> streamConfigurations = new ArrayList<StreamConfiguration>();
        StreamConfiguration currentStreamConfiguration;
        Property currentProperty;
        String propertiesString;
        String[] properties;
        String dump;
        boolean dumpHeader;
        boolean dumpBody;
        StreamEntry headerEntry, bodyEntry;

        String [] streams = streamConfigurationListString.split("~");
        if(streams != null){
            for (String stream : streams) {
                if(this.isNotNullOrEmpty(stream)){
                    currentStreamConfiguration = new StreamConfiguration();
                    currentStreamConfiguration.setName(stream.split("\\^")[0]);
                    currentStreamConfiguration.setVersion(stream.split("\\^")[1]);
                    currentStreamConfiguration.setNickname(stream.split("\\^")[2]);
                    currentStreamConfiguration.setDescription(stream.split("\\^")[3]);
                    if(stream.split("\\^").length > 4 && (stream.split("\\^")[4].contains(":") || stream.split("\\^")[5].contains(":"))){ // Only when properties exist
                        propertiesString = stream.split("\\^")[4];
                        properties = propertiesString.split(";");
                        for (String property : properties) {
                            if(this.isNotNullOrEmpty(property)){
                                currentProperty = new Property();
                                currentProperty.setKey(property.split(":")[0]);
                                currentProperty.setValue(property.split(":")[1]);
                                currentStreamConfiguration.getProperties().add(currentProperty);
                            }
                        }
                    }
                    if(stream.split("\\^")[stream.split("\\^").length-1].contains(";") && !stream.split("\\^")[stream.split("\\^").length-1].contains(":")){
                        dump = stream.split("\\^")[stream.split("\\^").length-1];
                        dumpHeader = dump.split(";")[0].equals("dump");
                        dumpBody = dump.split(";")[1].equals("dump");
                        if(dumpHeader){
                            headerEntry = new StreamEntry();
                            headerEntry.setName("SOAPHeader");
                            headerEntry.setValue("$SOAPHeader");
                            headerEntry.setType("STRING");
                            currentStreamConfiguration.getEntries().add(headerEntry);
                        }
                        if(dumpBody){
                            bodyEntry = new StreamEntry();
                            bodyEntry.setName("SOAPBody");
                            bodyEntry.setValue("$SOAPBody");
                            bodyEntry.setType("STRING");
                            currentStreamConfiguration.getEntries().add(bodyEntry);
                        }
                    }
                    streamConfigurations.add(currentStreamConfiguration);
                }
            }
            return streamConfigurations;
        }
        return new ArrayList<StreamConfiguration>();
    }

    public BamServerConfig getResource(String bamServerProfileLocation){
        try {
            String resourceString =  client.getResourceString(this.getRealBamServerProfilePath(bamServerProfileLocation));
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
            return client.resourceAlreadyExists(this.getRealBamServerProfilePath(bamServerProfileLocation));
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return true;
    }
    
    public void addCollection(String path){
        try {
            client.addCollection(path);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    
    public boolean removeResource(String path){
        try {
            return client.removeResource(path);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return false;
    }

    public String encryptPassword(String plainTextPassword){
        try {
            return client.encryptAndBase64Encode(plainTextPassword);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";
    }

    public String decryptPassword(String cipherTextPassword){
        try {
            return client.base64DecodeAndDecrypt(cipherTextPassword);
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return "";
    }

    public String getStreamConfigurationListString(StreamConfiguration streamConfiguration){
        String returnString = "";
        if(streamConfiguration != null){
            List<Property> properties = streamConfiguration.getProperties();
            for (Property property : properties) {
                returnString = returnString + property.getKey() + ":" + property.getValue() + ";";
            }
            returnString = returnString + "^";
            List<StreamEntry> streamEntries = streamConfiguration.getEntries();
            if(streamEntries.size() == 2){
                returnString = returnString + "dump;dump";
            } else if (streamEntries.size() == 1 && streamEntries.get(0).getValue().equals("$SOAPHeader")){
                returnString = returnString + "dump;notDump";
            } else if (streamEntries.size() == 1 && streamEntries.get(0).getValue().equals("$SOAPBody")){
                returnString = returnString + "notDump;dump";
            } else if (streamEntries.size() == 0){
                returnString = returnString + "notDump;notDump";
            }
            return returnString;
        } else {
            return "";
        }
    }

    private String getRealBamServerProfilePath(String shortServerProfilePath){
        if(shortServerProfilePath != null){
            String registryType = shortServerProfilePath.split(":")[0];
            if (isNotNullOrEmpty(registryType) && registryType.equals("conf")){
                return shortServerProfilePath.split(":")[1];
            }
            return shortServerProfilePath;
        }
        return shortServerProfilePath;
    }

    public boolean isNotNullOrEmpty(String string){
        return string != null && !string.equals("");
    }
}