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


import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.Property;
import org.wso2.carbon.mediator.bam.config.stream.StreamConfiguration;
import org.wso2.carbon.mediator.bam.config.stream.StreamEntry;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.List;


public class BamServerConfigXml {

    private org.apache.axiom.om.OMFactory fac = OMAbstractFactory.getOMFactory();
    private org.apache.axiom.om.OMNamespace synNS = SynapseConstants.SYNAPSE_OMNAMESPACE;
    private org.apache.axiom.om.OMNamespace nullNS;
    private OMElement serverProfileElement;

    public OMElement buildServerProfile(String ip, String port, String userName, String password,
                                        List<StreamConfiguration> streamConfigurations){
        serverProfileElement = this.serializeServerProfile();
        serverProfileElement.addChild(this.serializeConnection(ip, port));
        serverProfileElement.addChild(this.serializeCredential(userName, password));
        serverProfileElement.addChild(this.serializeStreams(streamConfigurations));
        return serverProfileElement;
    }

    /*public OMElement addStream(StreamConfiguration streamConfiguration){
        OMElement streamsElement = serverProfileElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "streams"));
        if(streamsElement == null){
            serverProfileElement.addChild(this.serializeStreams());
        }
        if(!streamExists(streamConfiguration)){
            streamsElement = serverProfileElement.getFirstChildWithName(
                    new QName(SynapseConstants.SYNAPSE_NAMESPACE, "streams"));
            OMElement streamElement = this.serializeStream(streamConfiguration);
            streamsElement.addChild(streamElement);
            return streamsElement;
        }
        else {
            return fac.createOMElement(new QName(""));
        }
    }*/
    
    public boolean streamExists(StreamConfiguration streamConfiguration){
        String streamName = streamConfiguration.getName();
        Iterator itr = serverProfileElement.getChildrenWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "stream"));
        OMElement tmpStream;
        QName tmpStreamName = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "name");
        while (itr.hasNext()){
            tmpStream = (OMElement)itr.next();
            if(tmpStream.getAttributeValue(tmpStreamName).equals(streamName)){
                return true;
            }
        }
        return false;
    }

    public OMElement getServerProfileElement(){
        return this.serverProfileElement;
    }

    public void setServerProfileElement(OMElement serverProfileElement){
        this.serverProfileElement = serverProfileElement;
    }

    private OMElement serializeConnection(String ip, String port){
        OMElement credentialElement = fac.createOMElement("connection", synNS);
        credentialElement.addAttribute("ip", ip, nullNS);
        credentialElement.addAttribute("port", port, nullNS);
        return credentialElement;
    }

    private OMElement serializeCredential(String userName, String password){
        OMElement credentialElement = fac.createOMElement("credential", synNS);
        credentialElement.addAttribute("userName", userName, nullNS);
        credentialElement.addAttribute("password", password, nullNS);
        return credentialElement;
    }

    private OMElement serializeServerProfile(){
        OMElement profileElement = fac.createOMElement("serverProfile", synNS);
        return profileElement;
    }

    private OMElement serializeStreams(List<StreamConfiguration> streamConfigurations){
        OMElement streamsElement = fac.createOMElement("streams", synNS);
        if(streamConfigurations != null){
            for (StreamConfiguration streamConfiguration : streamConfigurations) {
                streamsElement.addChild(this.serializeStream(streamConfiguration));
            }
        }
        return streamsElement;
    }

    private OMElement serializeStream(StreamConfiguration streamConfiguration){
        OMElement streamElement = fac.createOMElement("stream", synNS);

        streamElement.addAttribute("name", streamConfiguration.getName(), nullNS);
        streamElement.addAttribute("version", streamConfiguration.getVersion(), nullNS);
        streamElement.addAttribute("nickName", streamConfiguration.getNickname(), nullNS);
        streamElement.addAttribute("description", streamConfiguration.getDescription(), nullNS);

        OMElement payloadElement = streamElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "payload"));
        if(payloadElement == null){
            streamElement.addChild(serializePayload());
        }

        payloadElement = streamElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "payload"));
        List<StreamEntry> streamEntries = streamConfiguration.getEntries();
        if(streamEntries != null){
            String tmpEntryName;
            String tmpEntryType;
            for (StreamEntry streamEntry : streamEntries) {
                tmpEntryName = streamEntry.getName();
                tmpEntryType = streamEntry.getType();
                payloadElement.addChild(this.serializeEntry(tmpEntryName, tmpEntryType));
            }
        }

        OMElement propertiesElement = streamElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "properties"));
        if(propertiesElement == null){
            streamElement.addChild(this.serializeProperties());
        }
        propertiesElement = streamElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "properties"));
        List<Property> properties = streamConfiguration.getProperties();
        if(properties != null){
            String tmpEntryName;
            String tmpEntryValue;
            for (Property property : properties) {
                tmpEntryName = property.getKey();
                tmpEntryValue = property.getValue();
                propertiesElement.addChild(this.serializeProperty(tmpEntryName, tmpEntryValue));
            }
        }

        return streamElement;
    }

    private OMElement serializePayload(){
        OMElement payloadElement = fac.createOMElement("payload", synNS);
        return payloadElement;
    }

    private OMElement serializeEntry(String name, String type){
        OMElement entryElement = fac.createOMElement("entry", synNS);
        entryElement.addAttribute("name", name, nullNS);
        entryElement.addAttribute("type", type, nullNS);
        return entryElement;
    }

    private boolean entryExists(OMElement payloadElement, String name){
        Iterator itr = payloadElement.getChildrenWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "entry"));
        OMElement tmpEntry;
        QName tmpEntryName = new QName(SynapseConstants.SYNAPSE_NAMESPACE, "name");
        while (itr.hasNext()){
            tmpEntry = (OMElement)itr.next();
            if(tmpEntry.getAttributeValue(tmpEntryName).equals(name)){
                return true;
            }
        }
        return false;
    }

    private OMElement serializeProperties(){
        OMElement propertiesElement = fac.createOMElement("properties", synNS);
        return propertiesElement;
    }

    private OMElement serializeProperty(String name, String value){
        OMElement propertyElement = fac.createOMElement("property", synNS);
        propertyElement.addAttribute("name", name, nullNS);
        propertyElement.addAttribute("value", value, nullNS);
        return propertyElement;
    }

    /*private OMElement serializePropertyList(List<Property> properties){
        OMElement propertiesElement = fac.createOMElement("properties", synNS);
        if(properties != null){
            OMElement propertyElement;
            for (Property property : properties) {
                propertyElement = fac.createOMElement("property", synNS);
                propertyElement.addAttribute("name", property.getKey(), nullNS);
                propertyElement.addAttribute("value", property.getValue(), nullNS);
                propertiesElement.addChild(propertyElement);
            }
        }
        return propertiesElement;
    }*/
}
