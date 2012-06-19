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

package org.wso2.carbon.mediator.bam.config;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.Property;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;
import org.wso2.carbon.mediator.bam.config.stream.StreamEntry;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BamServerConfigBuilder {

    private BamServerConfig bamServerConfig = new BamServerConfig();

    public boolean createBamServerConfig(OMElement bamServerConfigElement){
        boolean credentialsOk = this.processCredentialElement(bamServerConfigElement);
        boolean connectionOk = this.processConnectionElement(bamServerConfigElement);
        boolean streamsOk = this.processStreamsElement(bamServerConfigElement);
        return credentialsOk && connectionOk && streamsOk;
        /*return credentialsOk && connectionOk;*/
    }



    public String getRealBamServerProfilePath(String shortServerProfilePath){
        if(shortServerProfilePath != null){
            String registryType = shortServerProfilePath.split(":")[0];
            String remainingPath = shortServerProfilePath.split(":")[1];
            if(registryType.equals("conf")){
                return "/_system/config" + remainingPath;
            }
            else if(registryType.equals("gov")){
                return "/_system/governance" + remainingPath;
            }
            else {
                return null;
            }
        }
        else{
            return null;
        }
    }

    public String getShortBamServerProfilePath(String realServerProfilePath){
        if(realServerProfilePath != null){
            if(realServerProfilePath.startsWith("/_system/config")){
                return realServerProfilePath.replaceFirst("/_system/config", "conf:");
            }
            else if(realServerProfilePath.startsWith("/_system/governance")){
                return realServerProfilePath.replaceFirst("/_system/governance", "gov:");
            }
            else {
                return null;
            }
        }
        return null;
    }

    /*public String getUserName(OMElement bamServerConfig){
        OMElement credentialElement = bamServerConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "credential"));
        if(credentialElement != null && !credentialElement.equals("")){
            OMAttribute pathAttr = credentialElement.getAttribute(new QName("path"));
            if (pathAttr != null && !pathAttr.equals("")){
                return pathAttr.getAttributeValue();
            }
            else {
                return null;
            }
        }
        else {
            return null;
        }
    }*/

    private boolean processCredentialElement(OMElement bamServerConfig){
        OMElement credentialElement = bamServerConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "credential"));
        if(credentialElement != null){
            OMAttribute userNameAttr = credentialElement.getAttribute(new QName("userName"));
            OMAttribute passwordAttr = credentialElement.getAttribute(new QName("password"));
            if(userNameAttr != null && passwordAttr != null && !userNameAttr.getAttributeValue().equals("") && !passwordAttr.getAttributeValue().equals("")){
                this.bamServerConfig.setUsername(userNameAttr.getAttributeValue());
                this.bamServerConfig.setPassword(passwordAttr.getAttributeValue());
            }
            else {
                return false;
            }
        }
        return true;
    }

    private boolean processConnectionElement(OMElement bamServerConfig){
        OMElement connectionElement = bamServerConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "connection"));
        if(connectionElement != null){
            OMAttribute ipAttr = connectionElement.getAttribute(new QName("ip"));
            OMAttribute portAttr = connectionElement.getAttribute(new QName("port"));
            if(ipAttr != null && portAttr != null && !ipAttr.getAttributeValue().equals("") && !portAttr.getAttributeValue().equals("")){
                this.bamServerConfig.setIp(ipAttr.getAttributeValue());
                this.bamServerConfig.setPort(portAttr.getAttributeValue());
            }
            else {
                return false;
            }
        }
        return true;
    }

    private boolean processStreamsElement(OMElement bamServerConfigElement){
        OMElement streamsElement = bamServerConfigElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "streams"));
        if(streamsElement != null){
            return this.processStreamElements(streamsElement);
        }
        else {
            return false;
        }
    }

    private boolean processStreamElements(OMElement streamsElement){
        OMElement streamElement;
        StreamConfiguration streamConfiguration;
        Iterator itr = streamsElement.getChildrenWithName(new QName("stream"));
        while (itr.hasNext()){
            streamElement = (OMElement)itr.next();
            streamConfiguration = new StreamConfiguration();
            if (streamElement != null && this.processStreamElement(streamElement, streamConfiguration)){
                this.bamServerConfig.getStreamConfigurations().add(streamConfiguration);
            }
            else {
                return false;
            }
        }
        return true;
    }

    private boolean processStreamElement(OMElement streamElement, StreamConfiguration streamConfiguration){
        OMAttribute nameAttr = streamElement.getAttribute(new QName("name"));
        OMAttribute versionAttr = streamElement.getAttribute(new QName("version"));
        OMAttribute nickNameAttr = streamElement.getAttribute(new QName("nickName"));
        OMAttribute descriptionAttr = streamElement.getAttribute(new QName("description"));
        if(nameAttr != null && nickNameAttr != null && descriptionAttr != null && !nameAttr.getAttributeValue().equals("") && !nickNameAttr.getAttributeValue().equals("") && !descriptionAttr.getAttributeValue().equals("")){
            streamConfiguration.setName(nameAttr.getAttributeValue());
            streamConfiguration.setVersion(versionAttr.getAttributeValue());
            streamConfiguration.setNickname(nickNameAttr.getAttributeValue());
            streamConfiguration.setDescription(descriptionAttr.getAttributeValue());

            /*boolean payloadElementOk = this.processPayloadElement(streamElement, streamConfiguration);

            boolean propertiesElementOk = this.processPropertiesElement(streamElement, streamConfiguration);
        
            return (payloadElementOk & propertiesElementOk);*/
            return this.processPropertiesElement(streamElement, streamConfiguration);
        }
        return false; // Incomplete attributes are not accepted
    }

    private boolean processPayloadElement(OMElement streamElement, StreamConfiguration streamConfiguration){
        OMElement payloadElement = streamElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "payload"));
        if(payloadElement != null){
            return this.processEntryElements(payloadElement, streamConfiguration);
        }
        return  false;
    }
    
    private boolean processEntryElements(OMElement payloadElement, StreamConfiguration streamConfiguration){
        OMElement entryElement;
        Iterator itr = payloadElement.getChildrenWithName(new QName("entry"));
        while (itr.hasNext()){
            entryElement = (OMElement)itr.next();
            if (entryElement != null && this.processEntryElement(entryElement, streamConfiguration)){

            }
            else {
                return false;
            }
        }
        return true;
    }

    private boolean processEntryElement(OMElement entryElement, StreamConfiguration streamConfiguration){
        OMAttribute nameAttr = entryElement.getAttribute(new QName("name"));
        OMAttribute typeAttr = entryElement.getAttribute(new QName("type"));
        if(nameAttr != null && typeAttr != null && !nameAttr.getAttributeValue().equals("") && !typeAttr.getAttributeValue().equals("")){
            StreamEntry streamEntry = new StreamEntry();
            streamEntry.setName(nameAttr.getAttributeValue());
            streamEntry.setType(typeAttr.getAttributeValue());
            streamConfiguration.getEntries().add(streamEntry);
            return true;
        }
        return false;
    }
    
    private boolean processPropertiesElement(OMElement streamElement, StreamConfiguration streamConfiguration){
        OMElement propertiesElement = streamElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "properties"));
        if(propertiesElement != null){
            return this.processPropertyElements(propertiesElement, streamConfiguration);
        }
        return true; // Properties are not mandatory
    }
    
    private boolean processPropertyElements(OMElement propertiesElement, StreamConfiguration streamConfiguration){
        OMElement propertyElement;
        Iterator itr = propertiesElement.getChildrenWithName(new QName("property"));
        while (itr.hasNext()){
            propertyElement = (OMElement)itr.next();
            if (!(propertyElement != null && this.processPropertyElement(propertyElement, streamConfiguration))){
                return false;
            }
        }
        return true; // Empty Property elements are accepted
    }

    private boolean processPropertyElement(OMElement propertyElement, StreamConfiguration streamConfiguration){
        OMAttribute nameAttr = propertyElement.getAttribute(new QName("name"));
        OMAttribute valueAttr = propertyElement.getAttribute(new QName("value"));
        if(nameAttr != null && valueAttr != null && !nameAttr.getAttributeValue().equals("") &&
           !valueAttr.getAttributeValue().equals("")){
            Property property = new Property();
            property.setKey(nameAttr.getAttributeValue());
            property.setValue(valueAttr.getAttributeValue());
            streamConfiguration.getProperties().add(property);
            return true;
        }
        return false; // Empty Property elements and incomplete Property parameters are not accepted
    }

    public String getServerProfilePathFromXml(OMElement omElement){
        OMElement serverProfileElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "serverProfile"));

        if(serverProfileElement != null && !serverProfileElement.equals("")){
            OMAttribute serverProfileAttr = serverProfileElement.getAttribute(new QName("path"));
            if(serverProfileAttr != null && !serverProfileAttr.equals("")){
                return serverProfileAttr.getAttributeValue();
            }
            return null;
        }
        return null;
    }

    private List<Property> getStreamProperties(OMElement omElement){
        OMElement propertiesElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "properties"));

        if(propertiesElement != null){
            Iterator itr = propertiesElement.getChildrenWithName(new QName("property"));
            List<Property> propertyList = new ArrayList<Property>();
            Property property;
            while (itr.hasNext()){
                OMElement propertyElement = (OMElement)itr.next();
                property = new Property();
                property.setKey(propertyElement.getAttributeValue(new QName("name")));
                property.setValue(propertyElement.getAttributeValue(new QName("value")));
                propertyList.add(property);
            }
            return propertyList;
        }
        return null;
    }

    public void setBamServerConfig(BamServerConfig bamServerConfig){
        this.bamServerConfig = bamServerConfig;
    }

    public BamServerConfig getBamServerConfig(){
        return this.bamServerConfig;
    }

}
