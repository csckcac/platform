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
package org.wso2.carbon.mediator.transform.xml;

import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMAttribute;
import org.wso2.carbon.mediator.transform.BamMediator;

import javax.xml.namespace.QName;
import java.util.Properties;

public class BamMediatorFactory extends AbstractMediatorFactory {
    public static final QName BAM_Q = new QName(
            SynapseConstants.SYNAPSE_NAMESPACE, "bam");

    public static final QName CONFIG_KEY = new QName("config-key");

    public Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        BamMediator bam = new BamMediator();

        OMAttribute configFileAttr = omElement.getAttribute(CONFIG_KEY);

        if (configFileAttr != null) {
            bam.setConfigKey(configFileAttr.getAttributeValue());
        }

        return bam;
    }

    public QName getTagQName() {
        return BAM_Q;
    }
}
