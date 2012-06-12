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
package org.wso2.carbon.mediator.bam.xml;

import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.Mediator;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.Property;
import org.wso2.carbon.mediator.bam.BamMediator;

import java.util.List;


public class BamMediatorSerializer extends AbstractMediatorSerializer {


    public OMElement serializeSpecificMediator(Mediator mediator) {
        assert mediator instanceof BamMediator : "BAM mediator is expected";

        BamMediator bamMediator = (BamMediator) mediator;
        OMElement bam = fac.createOMElement("bam", synNS);

        OMElement serverProfileElement = fac.createOMElement("serverProfile", synNS);
        serverProfileElement.addAttribute(fac.createOMAttribute("path", nullNS, bamMediator.getServerProfile()));
        bam.addChild(serverProfileElement);

        OMElement streamConfigElement = fac.createOMElement("streamConfig", synNS);
        streamConfigElement.addAttribute(fac.createOMAttribute("name", nullNS, bamMediator.getStreamName()));
        streamConfigElement.addAttribute(fac.createOMAttribute("version", nullNS, bamMediator.getStreamVersion()));
        bam.addChild(streamConfigElement);

       /* OMElement propertiesElement = fac.createOMElement("properties", synNS);
        List<Property> properties = bamMediator.getProperties();
        if(properties != null){
            OMElement propertyElement;
            for (Property property : properties) {
                propertyElement = fac.createOMElement("property", synNS);
                propertyElement.addAttribute("name", property.getKey(), nullNS);
                propertyElement.addAttribute("value", property.getValue(), nullNS);
                propertiesElement.addChild(propertyElement);
            }
        }
        bam.addChild(propertiesElement);*/

        return bam;
    }

    public String getMediatorClassName() {
        return BamMediator.class.getName();
    }
}
