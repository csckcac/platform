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

import javax.xml.namespace.QName;


public class BamMediator extends AbstractMediator {

    private String serverProfilePath = "";
    private String streamName = "";
    private String streamVersion = "";

    public String getServerProfilePath(){
        return this.serverProfilePath;
    }

    public void setServerProfilePath(String serverProfile1){
        this.serverProfilePath = serverProfile1;
    }

    public String getStreamName(){
        return this.streamName;
    }

    public void setStreamName(String streamName){
        this.streamName = streamName;
    }

    public String getStreamVersion(){
        return this.streamVersion;
    }

    public void setStreamVersion(String streamVersion){
        this.streamVersion = streamVersion;
    }

    public String getTagLocalName() {
        return "bam";
    }

    public OMElement serialize(OMElement parent) {
        OMElement bamElement = fac.createOMElement("bam", synNS);
        saveTracingState(bamElement, this);

        /*if (serverProfilePath != null) {
            bamElement.addAttribute(fac.createOMAttribute(
                    "config-key", nullNS, serverProfilePath));
        } else {
            throw new MediatorException("config-key not specified");
        }*/
        bamElement.addChild(serializeServerProfile());
        bamElement.addChild(serializeStreamConfiguration());

        if (parent != null) {
            parent.addChild(bamElement);
        }
        return bamElement;
    }

    public void build(OMElement omElement) {
        /*OMAttribute key = omElement.getAttribute(ATT_CONFIG_KEY);

        if (key == null) {
            String msg = "The 'config-key' attribute is required";
            throw new MediatorException(msg);
        }
        this.serverProfilePath = key.getAttributeValue();*/

        OMElement profileElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "serverProfile"));
        if (profileElement != null){
            processProfile(profileElement);
        }

        OMElement streamElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "streamConfig"));
        if(streamElement != null){
            processStreamConfiguration(streamElement);
        }

        processAuditStatus(this, omElement);
    }

    private void processProfile(OMElement profile){
        OMAttribute pathAttr = profile.getAttribute(new QName("path"));
        if(pathAttr != null){
            String pathValue = pathAttr.getAttributeValue();
            this.setServerProfilePath(pathValue);
        }
    }

    private void processStreamConfiguration(OMElement streamConfig){
        OMAttribute streamNameAttr = streamConfig.getAttribute(new QName("name"));
        OMAttribute streamVersionAttr = streamConfig.getAttribute(new QName("version"));
        if(streamNameAttr != null && streamVersionAttr != null){
            String nameValue = streamNameAttr.getAttributeValue();
            String versionValue = streamVersionAttr.getAttributeValue();
            this.setStreamName(nameValue);
            this.setStreamVersion(versionValue);
        }
    }

    private OMElement serializeServerProfile(){
        OMElement profileElement = fac.createOMElement("serverProfile", synNS);
        profileElement.addAttribute("path", this.serverProfilePath, nullNS);
        return profileElement;
    }

    private OMElement serializeStreamConfiguration(){
        OMElement streamConfigElement = fac.createOMElement("streamConfig",synNS);
        streamConfigElement.addAttribute("name", this.streamName, nullNS);
        streamConfigElement.addAttribute("version", this.streamVersion, nullNS);
        return streamConfigElement;
    }

}
