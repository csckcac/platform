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
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;
import org.wso2.carbon.mediator.service.MediatorException;

import javax.xml.namespace.QName;


public class BamMediator extends AbstractMediator {

    private static final QName ATT_CONFIG_KEY = new QName("config-key");
    private String configKey = null;

    private String serverUrl = "https://localhost:9443";
    private String userName = "admin";
    private String password = "admin";
    private String port = "7611";

    public String getConfigKey(){
        return configKey;
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

    public void setConfigKey(String configKey1){
        this.configKey = configKey1;
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


    public String getTagLocalName() {
        return "bam";
    }

    public OMElement serialize(OMElement parent) {
        OMElement bamEle = fac.createOMElement("bam", synNS);
        saveTracingState(bamEle, this);

        if (configKey != null) {
            bamEle.addAttribute(fac.createOMAttribute(
                    "config-key", nullNS, configKey));
        } else {
            throw new MediatorException("config-key not specified");
        }

        bamEle.addChild(serializeCredential());
        bamEle.addChild(serializeTransport());

        if (parent != null) {
            parent.addChild(bamEle);
        }
        return bamEle;
    }

    public void build(OMElement elem) {
        OMAttribute key = elem.getAttribute(ATT_CONFIG_KEY);

        if (key == null) {
            String msg = "The 'config-key' attribute is required";
            throw new MediatorException(msg);
        }
        this.configKey = key.getAttributeValue();

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

}
