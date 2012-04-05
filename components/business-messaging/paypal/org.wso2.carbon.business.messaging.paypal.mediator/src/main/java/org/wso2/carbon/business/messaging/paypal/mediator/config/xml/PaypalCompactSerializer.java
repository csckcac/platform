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
package org.wso2.carbon.business.messaging.paypal.mediator.config.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.Mediator;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.business.messaging.paypal.mediator.*;

import javax.xml.namespace.QName;
import java.util.List;
/**
 *
 * <p>
 * Factory for {@link PaypalMediator} instances.Serialize  the
 * <code>PaypalMediator</code> in accordance with the following configuration
 * </p>
 * <paypal>
      <credentials xmlns:ns2="http://wso2.services.samples" username="{xpath} | value" password="{xpath} | value"
                 signature="{xpath} | value" ... />
      <opName currency="{xpath} | value" detailLevel="{xpath} | value" errorLanguage="e{xpath} | value" ref=""* .... />
    </paypal>
 */

public class PaypalCompactSerializer extends PaypalMediatorSerializer {

    private OMElement credentialElement;
    private OMElement opElement;
    private OMElement configElement;


    public OMElement serializeSpecificMediator(Mediator mediator) {

        if (!(mediator instanceof PaypalMediator)) {
            handleException("Unsupported mediator passed in for serialization : "
                            + mediator.getClass());
        }

        PaypalMediator paypalMediator = (PaypalMediator) mediator;

        OMElement paypalElem = fac.createOMElement("paypal", synNS);
        this.configElement = paypalElem;
        if (paypalMediator.getClientRepository() != null
            || paypalMediator.getAxis2xml() != null) {
            OMElement config = fac.createOMElement("configuration", synNS);
            if (paypalMediator.getClientRepository() != null) {
                config.addAttribute(fac.createOMAttribute("repository", nullNS,
                                                          paypalMediator.getClientRepository()));
            }
            if (paypalMediator.getAxis2xml() != null) {
                config.addAttribute(fac.createOMAttribute("axis2xml", nullNS,
                                                          paypalMediator.getAxis2xml()));
            }
            paypalElem.addChild(config);
        }
        paypalElem.addChild(serializeRequestCredential(paypalMediator
                .getRequestCredential()));
        paypalElem.addChild(serializeOperation(paypalMediator.getOperation()));


        return paypalElem;
    }

    /**
     * serialize Paypal Credentials
     * @param credentials represents Paypal credentials
     * @return serialized elements
     */
    public OMElement serializeRequestCredential(RequestCredential credentials) {

        if (null == credentials) {
            handleException("PaypalMediator without credentials element has been found, "
                            + "when serializing the RouterMediator");
        }

        if (null == credentials.getUsernameXPath() && null == credentials.getUsernameValue()) {
            handleException("credentials element without a username element has been found, "
                            + "when serializing the PaypalMediator");
        }

        if (null == credentials.getPasswordXPath() && null == credentials.getPasswordValue()) {
            handleException("credentials element without a password element has been found, "
                            + "when serializing the PaypalMediator");
        }

        OMElement credentialsElem = fac.createOMElement("credentials", synNS);

        this.credentialElement = credentialsElem;

        serializeCredentialElement(credentials.getUsernameXPath(), credentials.getUsernameValue(),
                                   "username");
        serializeCredentialElement(credentials.getPasswordXPath(), credentials.getPasswordValue(),
                                   "password");
        serializeCredentialElement(credentials.getAuthTokenXPath(), credentials.getAuthTokenValue(),
                                   "auth-token");
        serializeCredentialElement(credentials.getHardExpirationXPath(), credentials.getHardExpirationValue(),
                                   "hard-expiration_warning");
        serializeCredentialElement(credentials.getAppIdXPath(), credentials.getAppIdValue(),
                                   "app-id");
        serializeCredentialElement(credentials.getDevIdXPath(), credentials.getDevIdValue(),
                                   "dev-id");
        serializeCredentialElement(credentials.getAuthCertXPath(), credentials.getAuthCertValue(),
                                   "auth-cert");
        serializeCredentialElement(credentials.getSignatureXPath(), credentials.getSignatureValue(),
                                   "signature");
        serializeCredentialElement(credentials.getSubjectXPath(), credentials.getSubjectValue(),
                                   "subject");
        return credentialsElem;
    }

    /**
     * serialize a given credential object
     * @param xpath xpath expression if exists for the credential property
     * @param sourceValue a static source string if exists for the credential property
     * @param attributeName property Name being serialized into
     * @return serialized property
     */
    protected OMElement serializeCredentialElement(SynapseXPath xpath, String sourceValue,
                                                   String attributeName) {
        OMElement credentialEl = getCredentialElement();
        serializeProperties(xpath, sourceValue, attributeName, credentialEl);
        return credentialEl;
    }

    /**
     * Serialize a given property
     * @param xpath xpath expression if exists for the property
     * @param sourceValue a static source string if exists for the property
     * @param attributeName property Name being serialized into
     * @param subjectEl toplevel element that this property will be serialized into  
     */
    private void serializeProperties(SynapseXPath xpath, String sourceValue, String attributeName, OMElement subjectEl) {
        if (null != xpath) {
            SynapseXPathSerializer.serializeXPath(xpath, subjectEl, attributeName);
            QName attrQName = new QName(attributeName);
            String xpathVal = subjectEl.getAttributeValue(attrQName);
            OMAttribute xpathAttr = subjectEl.getAttribute(attrQName);
            xpathAttr.setAttributeValue("{" + xpathVal + "}");
        } else if (null != sourceValue) {
            subjectEl.addAttribute(fac.createOMAttribute(attributeName, nullNS, sourceValue));
        }
    }

    /**
     * Serializes a given operataion
     * @param operation Paypal operation being serialized
     * @return serialized Element
     */
    public OMElement serializeOperation(Operation operation) {

        if (null == operation) {
            handleException("PaypalMediator without an operation has been found, "
                            + "when serializing the RouterMediator");
        }


        if (null == operation.getName()) {
            handleException("Operation without the name attribute has been found, "
                            + "when serializing the RouterMediator");
        }
        OMElement operationElem = fac.createOMElement(operation.getName(), synNS);
        this.opElement = operationElem;

        if (null != operation.getAction()) {
            operationElem.addAttribute(fac.createOMAttribute("action", nullNS,
                                                             operation.getAction()));

        }

        if (null != operation.getCurrency()) {

            operationElem.addAttribute(fac.createOMAttribute("currency",
                                                             nullNS, operation.getCurrency()));
        }

        List<Input> inputs = operation.getInputs();
        List<Output> outputs = operation.getOutputs();

        if (null != inputs && !inputs.isEmpty()) {
            serializeInputs(inputs);
        }
        if (null != outputs && !outputs.isEmpty()) {
            //operationElem.addChild(serializeOutputs(outputs));
        }

        return operationElem;
    }

    /**
     * This method Serialize each input of the operation in to the compact format
     * @param inputs list of input values for a operation
     * @return serialized element
     */
    public OMElement serializeInputs(List<Input> inputs) {
        for (Input input : inputs) {
            serializeInput(input);
        }
        return getOpElement();
    }

    /**
     * Serialize an individual Paypal Input parameter
     * @param input
     * @return serialized Input
     */
    protected OMElement serializeInput(Input input) {
        serializeInput(input, null);
        return getOpElement();
    }

    /**
     * @param input
     * @return
     */
    protected OMElement serializeInput(Input input, OMElement referedElement) {
        OMElement operationElement;
        if (referedElement == null) {
            operationElement = getOpElement();
        } else {
            operationElement = referedElement;
            getOpElement().addChild(referedElement);
        }
        if (null != input.getType()) {
/*
            parameterElem.addAttribute(fac.createOMAttribute("type", nullNS,
                                                             input.getType()));
*/
            OMElement parameterElem = fac.createOMElement(input.getType(), synNS);
            operationElement.addAttribute(fac.createOMAttribute("ref", nullNS,
                                                                input.getType()));
            for (Input innerInput : input.getSubInputs()) {
                serializeInput(innerInput, parameterElem);
            }
        } else {

            if (null == input.getName()) {
                handleException("Input without the name attribute has been found, PaypalMediator serialization failed");
            }
            if (null == input.getSourceXPath()
                && null == input.getSourceValue()) {
                handleException(String.format(
                        "Input %s: has no source-xpath or source-value attribute, "
                        + "PaypalMediator serialization failed", input
                                .getName()));
            }
            serializeProperties(input.getSourceXPath(), input.getSourceValue(), input.getName(), operationElement);
        }
        return operationElement;
    }


    public OMElement getCredentialElement() {
        return credentialElement;
    }

    public OMElement getOpElement() {
        return opElement;
    }

    public OMElement getConfigElement() {
        return configElement;
    }

}
