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
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.transform.BamMediator;
import org.wso2.carbon.mediator.transform.Input;
import org.wso2.carbon.mediator.transform.Output;

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

        OMElement inputElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "input"));
        if (inputElement != null) {
            //bam.setInput(createInput(inputElement));
        } else {
            //bam.setInput(new Input());
        }

        OMElement outputElement = omElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "output"));
        if (inputElement != null) {
            bam.setOutput(createOutput(outputElement));
        } else {
            bam.setOutput(new Output());
        }

        return bam;
    }

    private Input createInput(OMElement input) {
        Input in = new Input();

        OMAttribute typeAttr = input.getAttribute(new QName("type"));
        if (typeAttr == null) {
            handleException("type attribute is required for the input element");
        }

        assert typeAttr != null;

        String typeValue = typeAttr.getAttributeValue();
        if (typeValue.equals("text")) {
            in.setType(BamMediator.TYPES.TEXT);
        } else if (typeValue.equals("xml")) {
            in.setType(BamMediator.TYPES.XML);
        } else {
            handleException("Unexpected type specified as the input: " + typeValue);
        }

        if (input.getAttribute(new QName("expression")) != null) {
            try {
                in.setExpression(SynapseXPathFactory.getSynapseXPath(input, new QName("expression")));
            } catch (JaxenException e) {
                handleException("Error creating the XPath expression", e);
            }
        }

        return in;
    }

    private Output createOutput(OMElement output) {
        Output in = new Output();

        OMAttribute typeAttr = output.getAttribute(new QName("type"));
        if (typeAttr == null) {
            handleException("type attribute is required for the input element");
        }

        assert typeAttr != null;

        String typeValue = typeAttr.getAttributeValue();
        if (typeValue.equals("text")) {
            in.setType(BamMediator.TYPES.TEXT);
        } else if (typeValue.equals("xml")) {
            in.setType(BamMediator.TYPES.XML);
        } else {
            handleException("Unexpected type specified as the input: " + typeValue);
        }

        if (output.getAttribute(new QName("expression")) != null) {
            try {
                in.setExpression(SynapseXPathFactory.getSynapseXPath(output, new QName("expression")));
            } catch (JaxenException e) {
                handleException("Error creating the XPath expression", e);
            }
        }

        OMAttribute actionAttr = output.getAttribute(new QName("action"));
        if (actionAttr != null && actionAttr.getAttributeValue() != null) {
            in.setAction(actionAttr.getAttributeValue());
        }

        OMAttribute propertyAttr = output.getAttribute(new QName("property"));
        if (propertyAttr != null && propertyAttr.getAttributeValue() != null) {
            in.setProperty(propertyAttr.getAttributeValue());
        }

        return in;
    }

    public QName getTagQName() {
        return BAM_Q;
    }
}
