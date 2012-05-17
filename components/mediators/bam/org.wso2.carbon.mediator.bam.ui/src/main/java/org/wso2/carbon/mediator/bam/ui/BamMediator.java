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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.Property;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class BamMediator extends AbstractMediator {

    //private static final QName ATT_CONFIG_KEY = new QName("config-key");

    private String serverUrl = "https://localhost:9443";
    private String userName = "admin";
    private String password = "admin";
    private String port = "7611";

    private String serverProfile = "";
    List<Property> properties = null;

    public String getServerProfile(){
        return serverProfile;
    }

    public String getServerUrl(){
        return serverUrl;
    }

    public String getUserName(){
        return userName;
    }

    public String getPassword(){
        return password;
    }

    public String getPort(){
        return port;
    }

    public List<Property> getProperties(){
        return properties;
    }

    public void setServerProfile(String serverProfile1){
        this.serverProfile = serverProfile1;
    }

    public void setServerUrl(String serverUrl1){
        this.serverUrl = serverUrl1;
    }

    public void setUserName(String userName1){
        this.userName = userName1;
    }

    public void setPassword(String password1){
        this.password = password1;
    }

    public void setPort(String port1){
        this.port = port1;
    }

    public void setProperties(List<Property> propertyList){
        this.properties = propertyList;
    }


    public String getTagLocalName() {
        return "bam";
    }

    public OMElement serialize(OMElement parent) {
        OMElement bamEle = fac.createOMElement("bam", synNS);
        saveTracingState(bamEle, this);

        /*if (serverProfile != null) {
            bamEle.addAttribute(fac.createOMAttribute(
                    "config-key", nullNS, serverProfile));
        } else {
            throw new MediatorException("config-key not specified");
        }*/

        bamEle.addChild(serializeCredential());
        bamEle.addChild(serializeTransport());
        bamEle.addChild(serializeServerProfile());
        bamEle.addChild(serializeProperties());

        if (parent != null) {
            parent.addChild(bamEle);
        }
        return bamEle;
    }

    public void build(OMElement elem) {
        /*OMAttribute key = elem.getAttribute(ATT_CONFIG_KEY);

        if (key == null) {
            String msg = "The 'config-key' attribute is required";
            throw new MediatorException(msg);
        }
        this.serverProfile = key.getAttributeValue();*/

        OMElement credentialElement = elem.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "credential"));
        if (credentialElement != null) {
            processCredential(credentialElement);
        }

        OMElement transportElement = elem.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "transport"));
        if (transportElement != null) {
            processTransport(transportElement);
        }

        OMElement profileElement = elem.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "serverProfile"));
        if (profileElement != null){
            processProfile(profileElement);
        }

        OMElement propertiesElement = elem.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "properties"));
        if (propertiesElement != null){
            processProperties(propertiesElement);
        }

        // after successfully creating the mediator
        // set its common attributes such as tracing etc

        processAuditStatus(this, elem);
    }

    private void processCredential(OMElement credential){
        OMAttribute serverUrlAttr = credential.getAttribute(new QName("serverUrl"));
        if(serverUrlAttr != null){
            String serverUrlValue = serverUrlAttr.getAttributeValue();
            this.setServerUrl(serverUrlValue);
        }

        OMAttribute userNameAttr = credential.getAttribute(new QName("userName"));
        if(userNameAttr != null){
            String userNameValue = userNameAttr.getAttributeValue();
            this.setUserName(userNameValue);
        }

        OMAttribute passwordAttr = credential.getAttribute(new QName("password"));
        if(passwordAttr != null){
            String passwordValue = passwordAttr.getAttributeValue();
            this.setPassword(passwordValue);
        }
    }

    private void processTransport(OMElement transport){
        OMAttribute portAttr = transport.getAttribute(new QName("port"));
        if(portAttr != null){
            String portValue = portAttr.getAttributeValue();
            this.setPort(portValue);
        }
    }

    private void processProfile(OMElement profile){
        OMAttribute pathAttr = profile.getAttribute(new QName("path"));
        if(pathAttr != null){
            String pathValue = pathAttr.getAttributeValue();
            this.setServerProfile(pathValue);
        }
    }

    private void processProperties(OMElement properties){
        if(properties != null){
            Iterator itr = properties.getChildrenWithName(new QName("property"));
            this.properties = new ArrayList<Property>();
            Property property;
            while (itr.hasNext()){
                OMElement propertyElement = (OMElement)itr.next();
                property = new Property();
                property.setKey(propertyElement.getAttributeValue(new QName("name")));
                property.setValue(propertyElement.getAttributeValue(new QName("value")));
                this.properties.add(property);
            }
        }
    }

    private OMElement serializeCredential(){
        OMElement credentialElement = fac.createOMElement("credential", synNS);
        credentialElement.addAttribute("serverUrl", serverUrl, nullNS);
        credentialElement.addAttribute("userName", userName, nullNS);
        credentialElement.addAttribute("password", password, nullNS);
        return credentialElement;
    }

    private OMElement serializeTransport(){
        OMElement credentialElement = fac.createOMElement("transport", synNS);
        credentialElement.addAttribute("port", port, nullNS);
        return credentialElement;
    }

    private OMElement serializeServerProfile(){
        OMElement profileElement = fac.createOMElement("serverProfile", synNS);
        profileElement.addAttribute("path", serverProfile, nullNS);
        return profileElement;
    }

    private OMElement serializeProperties(){
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
    }

}
