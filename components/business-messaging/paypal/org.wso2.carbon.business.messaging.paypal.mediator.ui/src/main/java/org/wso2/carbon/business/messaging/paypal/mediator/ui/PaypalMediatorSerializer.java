/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.business.messaging.paypal.mediator.ui;

import java.util.List;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.Mediator;

/**
 * <p>
 * Factory for {@link PaypalMediator} instances.Builds the
 * <code>PaypalMediator</code> using the following configuration
 * </p>
 * <p/>
 * <pre>
 * &lt;paypal&gt;
 *      &lt;configuration [axis2xml=&quot;string&quot;] [repository=&quot;string&quot;]/&gt;?
 *      &lt;operation name=&quot;string&quot; [action=&quot;string&quot;] version=&quot;version&quot; currency=&quot;currency&quot; &gt;
 *           &lt;inputs&gt;
 *      	    &lt;input source-xpath=&quot;expression&quot; type=[xml|string] mode=[single|list]| name=&quot;string&quot;
 *      	              source-value=&quot;value&quot; namespace=&quot;uri&quot; ns-prefix=&quot;ns-prefix&quot; /&gt; +
 *          &lt;/inputs&gt; ?
 *          &lt;outputs&gt;
 *      	    &lt;output [source-xpath=&quot;expression&quot; target-xpath=&quot;expression&quot;| target-key=&quot;value&quot;] /&gt; +
 *          &lt;/outputs&gt; ?
 *      &lt;/operation&gt;
 * &lt;/paypal&gt;
 * </pre>
 * <p/>
 * pal&gt; </pre>
 */
public class PaypalMediatorSerializer {

    protected OMFactory fac;
    protected OMNamespace synNS, nullNS;

    public PaypalMediatorSerializer(OMFactory factory, OMNamespace synapseNS,
                                    OMNamespace nullNS) {
        fac = factory;
        synNS = synapseNS;
        this.nullNS = nullNS;
    }

    public OMElement serializeMediator(OMElement parent, Mediator mediator) {

        if (!(mediator instanceof PaypalMediator)) {
            handleException("Unsupported mediator passed in for serialization : "
                            + mediator.getClass());
        }

        PaypalMediator paypalMediator = (PaypalMediator) mediator;

        OMElement paypalElem = fac.createOMElement("paypal", synNS);

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

        if (parent != null) {
            parent.addChild(paypalElem);
        }

        return paypalElem;
    }

    protected OMElement serializeRequestCredential(RequestCredential credentials) {

        if (null == credentials) {
            handleException("PaypalMediator without credentials element has been found, "
                            + "when serializing the RouterMediator");
        }

//		if (null == credentials.getNamespace()) {
//			handleException("PaypalMediator with credentials element without a namespace attribute has been found, "
//					+ "when serializing the RouterMediator");
//		}
//		if (null == credentials.getNamespacePrefix()) {
//			handleException("PaypalMediator with credentials element without a ns-prefix attribute has been found, "
//					+ "when serializing the RouterMediator");
//		}

        if (null == credentials.getUsernameXPath() && null == credentials.getUsernameValue()) {
            handleException("credentials element without a username element has been found, "
                            + "when serializing the PaypalMediator");
        }

        if (null == credentials.getPasswordXPath() && null == credentials.getPasswordValue()) {
            handleException("credentials element without a password element has been found, "
                            + "when serializing the PaypalMediator");
        }

        OMElement credentialsElem = fac.createOMElement("credentials", synNS);

        credentialsElem.addChild(serializeCredentialElement(credentials
                .getUsernameXPath(), credentials
                .getUsernameValue(), "username"));
        credentialsElem.addChild(serializeCredentialElement(credentials
                .getPasswordXPath(), credentials
                .getPasswordValue(), "password"));
        OMElement authTokenElem = serializeCredentialElement(credentials
                .getAuthTokenXPath(), credentials
                .getAuthTokenValue(), "auth-token");
        OMElement harExpWarnElem = serializeCredentialElement(credentials
                .getHardExpirationXPath(), credentials
                .getHardExpirationValue(), "hard-expiration_warning");
        OMElement appIdElem = serializeCredentialElement(credentials
                .getAppIdXPath(), credentials
                .getAppIdValue(), "app-id");
        OMElement devIdElem = serializeCredentialElement(credentials
                .getDevIdXPath(), credentials
                .getDevIdValue(), "dev-id");
        OMElement authCertElem = serializeCredentialElement(credentials
                .getAuthCertXPath(), credentials
                .getAuthCertValue(), "auth-cert");
        OMElement sigElem = serializeCredentialElement(credentials
                .getSignatureXPath(), credentials
                .getSignatureValue(), "signature");
        OMElement subElem = serializeCredentialElement(credentials
                .getSubjectXPath(), credentials
                .getSubjectValue(), "subject");

        if (null != authTokenElem) {
            credentialsElem.addChild(authTokenElem);
        }
        if (null != harExpWarnElem) {
            credentialsElem.addChild(harExpWarnElem);
        }
        if (null != appIdElem) {
            credentialsElem.addChild(appIdElem);
        }
        if (null != devIdElem) {
            credentialsElem.addChild(devIdElem);
        }
        if (null != authCertElem) {
            credentialsElem.addChild(authCertElem);
        }
        if (null != sigElem) {
            credentialsElem.addChild(sigElem);
        }
        if (null != subElem) {
            credentialsElem.addChild(subElem);
        }
        return credentialsElem;
    }

    protected OMElement serializeCredentialElement(SynapseXPath xpath, String sourceValue,
                                                   String childElementName) {

        OMElement xpathElement = null;
        if (null != xpath) {
            xpathElement = fac.createOMElement(childElementName, synNS);
            SynapseXPathSerializer.serializeXPath(xpath, xpathElement, "xpath");
        } else if (null != sourceValue) {
            xpathElement = fac.createOMElement(childElementName, synNS);
            xpathElement.addAttribute(fac.createOMAttribute("source-value", nullNS, sourceValue));
        }
        return xpathElement;
    }

    /**
     * @param operation
     * @return
     */
    protected OMElement serializeOperation(Operation operation) {

        if (null == operation) {
            handleException("PaypalMediator without an operation has been found, "
                            + "when serializing the RouterMediator");
        }

        OMElement operationElem = fac.createOMElement("operation", synNS);

        if (null == operation.getName()) {
            handleException("Operation without the name attribute has been found, "
                            + "when serializing the RouterMediator");
        }

        operationElem.addAttribute(fac.createOMAttribute("name", nullNS,
                                                         operation.getName()));

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
            operationElem.addChild(serializeInputs(inputs));
        }
        if (null != outputs && !outputs.isEmpty()) {
            operationElem.addChild(serializeOutputs(outputs));
        }

        return operationElem;
    }

    /**
     * @param inputs
     * @return
     */
    protected OMElement serializeInputs(List<Input> inputs) {

        OMElement inputsElem = fac.createOMElement("inputs", synNS);
        for (Input input : inputs) {

            inputsElem.addChild(serializeInput(input));
        }
        return inputsElem;
    }

    /**
     * @param outputs
     * @return
     */
    protected OMElement serializeOutputs(List<Output> outputs) {

        OMElement outputsElem = fac.createOMElement("outputs", synNS);
        for (Output output : outputs) {

            outputsElem.addChild(serializeOutput(output));
        }
        return outputsElem;
    }

    /**
     * @param input
     * @return
     */
    protected OMElement serializeInput(Input input) {
        OMElement parameterElem = fac.createOMElement("input", synNS);

        if (null != input.getType()) {
            parameterElem.addAttribute(fac.createOMAttribute("type", nullNS,
                                                             input.getType()));
            for (Input innerInput : input.getSubInputs()) {
                parameterElem.addChild(serializeInput(innerInput));
            }
        } else {

            if (null == input.getName()) {
                handleException("Input without the name attribute has been found, PaypalMediator serialization failed");
            }
            parameterElem.addAttribute(fac.createOMAttribute("name", nullNS,
                                                             input.getName()));

            if (null == input.getSourceXPath()
                && null == input.getSourceValue()) {
                handleException(String.format(
                        "Input %s: has no source-xpath or source-value attribute, "
                        + "PaypalMediator serialization failed", input
                                .getName()));
            }

            if (null != input.getSourceXPath()) {
                SynapseXPathSerializer.serializeXPath(input.getSourceXPath(),
                                                      parameterElem, "source-xpath");

            } else if (null != input.getSourceValue()) {

                parameterElem.addAttribute(fac.createOMAttribute(
                        "source-value", nullNS, input.getSourceValue()));
            }
        }
        return parameterElem;
    }

    /**
     * @param parameter
     * @return
     */
    protected OMElement serializeOutput(Output parameter) {
        OMElement parameterElem = fac.createOMElement("output", synNS);

        if (null == parameter.getTargetXPath()
            && null == parameter.getTargetKey()) {
            handleException("Output without target-xpath or target-key attribute, "
                            + "PaypalMediator serialization failed");
        }

        if (null != parameter.getTargetXPath()) {
            SynapseXPathSerializer.serializeXPath(parameter.getSourceXPath(),
                                                  parameterElem, "source-xpath");

            SynapseXPathSerializer.serializeXPath(parameter.getTargetXPath(),
                                                  parameterElem, "target-xpath");

        } else if (null != parameter.getTargetKey()) {

            parameterElem.addAttribute(fac.createOMAttribute("target-key",
                                                             nullNS, parameter.getTargetKey()));
        }

        return parameterElem;
    }

    protected void handleException(String exceptionMsg) {
        throw new MediatorException(exceptionMsg);
    }
}
