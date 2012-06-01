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

import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.wso2.carbon.bam.mediationstats.data.publisher.stub.conf.Property;
import org.wso2.carbon.mediator.bam.BamMediator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class BamMediatorFactory extends AbstractMediatorFactory {
    public static final QName BAM_Q = new QName(
            SynapseConstants.SYNAPSE_NAMESPACE, "bam");

    //public static final QName CONFIG_KEY = new QName("config-key");

    public Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        BamMediator bam = new BamMediator();

        OMElement serverProfileElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "serverProfile"));

        if(serverProfileElement != null){
            OMAttribute serverProfileAttr = serverProfileElement.getAttribute(new QName("path"));
            if(serverProfileAttr != null){
                bam.setServerProfile(serverProfileAttr.getAttributeValue());
            }
        }

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
            bam.setProperties(propertyList);
        }

        return bam;
    }

    public QName getTagQName() {
        return BAM_Q;
    }
}
