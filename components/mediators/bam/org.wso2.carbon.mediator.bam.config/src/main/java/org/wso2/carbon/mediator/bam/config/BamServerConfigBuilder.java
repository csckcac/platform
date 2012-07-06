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
import org.wso2.carbon.mediator.bam.config.stream.Property;
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
        boolean keyStoreOk = this.processKeyStoreElement(bamServerConfigElement);
        boolean streamsOk = this.processStreamsElement(bamServerConfigElement);
        return credentialsOk && connectionOk && keyStoreOk && streamsOk;
    }

    private boolean processCredentialElement(OMElement bamServerConfig){
        OMElement credentialElement = bamServerConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "credential"));
        if(credentialElement != null){
            OMAttribute userNameAttr = credentialElement.getAttribute(new QName("userName"));
            OMAttribute passwordAttr = credentialElement.getAttribute(new QName("password"));
            OMAttribute secureAttr = credentialElement.getAttribute(new QName("secure"));
            if(userNameAttr != null && passwordAttr != null && secureAttr != null &&
               !userNameAttr.getAttributeValue().equals("") && !passwordAttr.getAttributeValue().equals("")
               && !secureAttr.getAttributeValue().equals("")){
                this.bamServerConfig.setUsername(userNameAttr.getAttributeValue());
                this.bamServerConfig.setPassword(passwordAttr.getAttributeValue());
                if("true".equals(secureAttr.getAttributeValue())){
                    this.bamServerConfig.setSecurity(true);
                } else if ("false".equals(secureAttr.getAttributeValue())) {
                    this.bamServerConfig.setSecurity(false);
                } else {
                    return false; // Secure attribute should have a value
                }
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

    private boolean processKeyStoreElement(OMElement bamServerConfig){
        OMElement keyStoreElement = bamServerConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "keyStore"));
        if(keyStoreElement != null){
            OMAttribute locationAttr = keyStoreElement.getAttribute(new QName("location"));
            OMAttribute passwordAttr = keyStoreElement.getAttribute(new QName("password"));
            if(locationAttr != null && passwordAttr != null && !locationAttr.getAttributeValue().equals("") && !passwordAttr.getAttributeValue().equals("")){
                this.bamServerConfig.setKeyStoreLocation(locationAttr.getAttributeValue());
                this.bamServerConfig.setKeyStorePassword(passwordAttr.getAttributeValue());
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

            boolean payloadElementOk = this.processPayloadElement(streamElement, streamConfiguration);

            boolean propertiesElementOk = this.processPropertiesElement(streamElement, streamConfiguration);
        
            return (payloadElementOk & propertiesElementOk);
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
            if (!(entryElement != null && this.processEntryElement(entryElement, streamConfiguration))){
                return false;
            }
        }
        return true; // Empty Entry elements are accepted
    }

    private boolean processEntryElement(OMElement entryElement, StreamConfiguration streamConfiguration){
        OMAttribute nameAttr = entryElement.getAttribute(new QName("name"));
        OMAttribute valueAttr = entryElement.getAttribute(new QName("value"));
        OMAttribute typeAttr = entryElement.getAttribute(new QName("type"));
        if(nameAttr != null && valueAttr != null && typeAttr != null && !nameAttr.getAttributeValue().equals("") && !valueAttr.getAttributeValue().equals("") && !typeAttr.getAttributeValue().equals("")){
            StreamEntry streamEntry = new StreamEntry();
            streamEntry.setName(nameAttr.getAttributeValue());
            streamEntry.setValue(valueAttr.getAttributeValue());
            streamEntry.setType(typeAttr.getAttributeValue());
            streamConfiguration.getEntries().add(streamEntry);
            return true;
        }
        return false; // Empty Entry elements and incomplete Entry parameters are not accepted
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
        OMAttribute isExpressionAttr = propertyElement.getAttribute(new QName("isExpression"));
        if(nameAttr != null && valueAttr != null && isExpressionAttr != null && !nameAttr.getAttributeValue().equals("") &&
           !valueAttr.getAttributeValue().equals("") && !isExpressionAttr.getAttributeValue().equals("")){
            Property property = new Property();
            property.setKey(nameAttr.getAttributeValue());
            property.setValue(valueAttr.getAttributeValue());
            property.setExpression("true".equals(isExpressionAttr.getAttributeValue()));
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
