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

        /*List<StreamConfiguration> scs = new ArrayList<StreamConfiguration>();
        StreamConfiguration sc1 = new StreamConfiguration();
        sc1.setName("org.wso2.carbon.mediator.bam.BamMediator100");
        sc1.setVersion("1.0.1");
        sc1.setNickname("Log1");
        sc1.setDescription("Description1");
        Property p1 = new Property();
        Property p2 = new Property();
        Property p3 = new Property();
        p1.setKey("key11");
        p1.setValue("val11");
        p2.setKey("key12");
        p2.setValue("val12");
        p3.setKey("key13");
        p3.setValue("val13");
        sc1.getProperties().add(p1);
        sc1.getProperties().add(p2);
        sc1.getProperties().add(p3);
        scs.add(sc1);

        StreamConfiguration sc2 = new StreamConfiguration();
        sc2.setName("org.wso2.carbon.mediator.bam.BamMediator200");
        sc2.setVersion("1.0.2");
        sc2.setNickname("Log2");
        sc2.setDescription("Description2");
        Property p21 = new Property();
        Property p22 = new Property();
        Property p23 = new Property();
        p21.setKey("key21");
        p21.setValue("val21");
        p22.setKey("key22");
        p22.setValue("val22");
        p23.setKey("key23");
        p23.setValue("val23");
        sc2.getProperties().add(p21);
        sc2.getProperties().add(p22);
        sc2.getProperties().add(p23);
        scs.add(sc2);

        StreamConfiguration sc3 = new StreamConfiguration();
        sc3.setName("org.wso2.carbon.mediator.bam.BamMediator300");
        sc3.setVersion("1.0.3");
        sc3.setNickname("Log3");
        sc3.setDescription("Description3");
        Property p31 = new Property();
        Property p32 = new Property();
        Property p33 = new Property();
        p31.setKey("key31");
        p31.setValue("val31");
        p32.setKey("key32");
        p32.setValue("val32");
        p33.setKey("key33");
        p33.setValue("val33");
        sc3.getProperties().add(p31);
        sc3.getProperties().add(p32);
        sc3.getProperties().add(p33);
        scs.add(sc3);*/




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

        String [] streams = streamConfigurationListString.split("~");
        if(streams != null){
            for (String stream : streams) {
                if(this.isNotNullOrEmpty(stream)){
                    currentStreamConfiguration = new StreamConfiguration();
                    currentStreamConfiguration.setName(stream.split("\\^")[0]);
                    currentStreamConfiguration.setVersion(stream.split("\\^")[1]);
                    currentStreamConfiguration.setNickname(stream.split("\\^")[2]);
                    currentStreamConfiguration.setDescription(stream.split("\\^")[3]);
                    if(stream.split("\\^").length > 4){ // Only when properties exist
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

    public String encryptPassword(String plainTextPassword){
        return plainTextPassword; // TODO
    }

    public String decryptPassword(String cipherTextPassword){
        return cipherTextPassword; // TODO
    }

    public String getPropertiesString(StreamConfiguration streamConfiguration){
        String returnString = "";
        if(streamConfiguration != null){
            List<Property> properties = streamConfiguration.getProperties();
            for (Property property : properties) {
                returnString = returnString + property.getKey() + ":" + property.getValue() + ";";
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
        if(string != null && !string.equals("")){
            return true;
        }
        return false;
    }
}