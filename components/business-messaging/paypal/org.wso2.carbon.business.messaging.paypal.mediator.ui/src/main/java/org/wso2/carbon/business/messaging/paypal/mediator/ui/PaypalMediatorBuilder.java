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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
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
 */
public class PaypalMediatorBuilder {

    protected static final QName PAYPAL_Q = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "paypal");
    protected static final QName Q_CONFIG = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "configuration");
    private static final QName Q_INPUTS = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "inputs");
    private static final QName Q_INPUT = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "input");
    private static final QName Q_OPERATION = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "operation");
    private static final QName Q_OUTPUT = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "output");
    private static final QName Q_OUTPUTS = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "outputs");

    // credential specific elements
    protected static final QName Q_CREDENTIALS = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "credentials");
    protected static final QName Q_AUTH_TOKEN = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "auth-token");
    protected static final QName Q_HARD_EXP_WARN = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "hard-expiration_warning");
    protected static final QName Q_APP_ID = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "app-id");
    protected static final QName Q_DEV_ID = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "dev-id");
    protected static final QName Q_AUTH_CERT = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "auth-cert");
    protected static final QName Q_USERNAME = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "username");
    protected static final QName Q_PASSWORD = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "password");
    protected static final QName Q_SIGNATURE = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "signature");
    protected static final QName Q_SUBJECT = new QName(
            XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), "subject");

    protected static final QName ATT_AXIS2XML = new QName("axis2xml");
    protected static final QName ATT_ACTION = new QName("action");
    protected static final QName ATT_REPOSITORY = new QName("repository");
    protected static final QName ATT_CURRENCY = new QName("currency");
    // common attribute for input and output
    private static final QName ATT_SOURCE_XPATH = new QName("source-xpath");
    // input attributes
    private static final QName ATT_NAME = new QName("name");
    private static final QName ATT_TYPE = new QName("type");
    private static final QName ATT_SOURCE_VALUE = new QName("source-value");

    // output attributes
    private static final QName ATT_TARGET_XPATH = new QName("target-xpath");
    private static final QName ATT_TARGET_KEY = new QName("target-key");

    // credential specific attributes
    private static final QName ATT_XPATH = new QName("xpath");

    /*
      * (non-Javadoc)
      *
      * @see
      * org.apache.synapse.config.xml.MediatorFactory#createMediator(org.apache
      * .axiom.om.OMElement)
      */

    public void buildMediator(OMElement elem, Mediator mediator) {

        if (!PAYPAL_Q.equals(elem.getQName())) {
            handleException("Unable to create the Paypal mediator. "
                            + "Unexpected element as the Paypal mediator configuration");
        }

        if (!(mediator instanceof PaypalMediator)) {
            handleException("Unsupported mediator passed in for serialization : "
                            + mediator.getClass());
        }

        PaypalMediator paypalMediator = (PaypalMediator) mediator;
        OMElement configElt = elem.getFirstChildWithName(Q_CONFIG);

        if (configElt != null) {

            OMAttribute axis2xmlAttr = configElt.getAttribute(ATT_AXIS2XML);
            OMAttribute repoAttr = configElt.getAttribute(ATT_REPOSITORY);

            if (axis2xmlAttr != null
                && axis2xmlAttr.getAttributeValue() != null) {
                paypalMediator.setAxis2xml(axis2xmlAttr.getAttributeValue());
            }

            if (repoAttr != null && repoAttr.getAttributeValue() != null) {
                paypalMediator
                        .setClientRepository(repoAttr.getAttributeValue());
            }
        }

        paypalMediator.setRequestCredential(createRequestCredential(elem
                .getFirstChildWithName(Q_CREDENTIALS)));
        paypalMediator.setOperation(createOperation(elem
                .getFirstChildWithName(Q_OPERATION)));

    }

    /**
     * This method parses the operation.
     *
     * @param requestCredentialElement
     * @return
     */
    public RequestCredential createRequestCredential(
            OMElement requestCredentialElement) {

        if (null == requestCredentialElement) {
            handleException("PaypalMediator without credentials element has been found, "
                            + "but it is required to have credentials element for PaypalMediator");
        }

//		if (null == requestCredentialElement.getAttribute(ATT_NAMESPACE)
//				|| null == requestCredentialElement
//						.getAttributeValue(ATT_NAMESPACE)) {
//			handleException("PaypalMediator with a credentials element that has no namespace attribute, "
//					+ "but it is required to have the namespace attribute for credentials");
//		}
//		if (null == requestCredentialElement.getAttribute(ATT_NAMESPACE_PREFIX)
//				|| null == requestCredentialElement
//						.getAttributeValue(ATT_NAMESPACE_PREFIX)) {
//			handleException("PaypalMediator with a credentials element that has no ns-prifix attribute, "
//					+ "but it is required to have the ns-prifix attribute for credentials");
//		}

        RequestCredential credential = new RequestCredential();
//		credential.setNamespace(requestCredentialElement
//				.getAttributeValue(ATT_NAMESPACE));
//		credential.setNamespacePrefix(requestCredentialElement
//				.getAttributeValue(ATT_NAMESPACE_PREFIX));

        credential.setUsernameXPath(getXPath(requestCredentialElement,
                                             Q_USERNAME, true));
        credential.setUsernameValue(getValue(requestCredentialElement,
                                             Q_USERNAME, true));

        credential.setPasswordXPath(getXPath(requestCredentialElement,
                                             Q_PASSWORD, true));
        credential.setPasswordValue(getValue(requestCredentialElement,
                                             Q_PASSWORD, true));

        credential.setAuthTokenXPath(getXPath(requestCredentialElement,
                                              Q_AUTH_TOKEN, false));
        credential.setAuthTokenXPath(getXPath(requestCredentialElement,
                                              Q_AUTH_TOKEN, false));

        credential.setHardExpirationValue(getValue(requestCredentialElement,
                                                   Q_HARD_EXP_WARN, false));
        credential.setHardExpirationXPath(getXPath(requestCredentialElement,
                                                   Q_HARD_EXP_WARN, false));

        credential.setAppIdXPath(getXPath(requestCredentialElement, Q_APP_ID,
                                          false));
        credential.setAppIdValue(getValue(requestCredentialElement, Q_APP_ID,
                                          false));

        credential.setDevIdXPath(getXPath(requestCredentialElement, Q_DEV_ID,
                                          false));
        credential.setDevIdValue(getValue(requestCredentialElement, Q_DEV_ID,
                                          false));

        credential.setAuthCertXPath(getXPath(requestCredentialElement,
                                             Q_AUTH_CERT, false));
        credential.setAuthCertValue(getValue(requestCredentialElement,
                                             Q_AUTH_CERT, false));

        credential.setSignatureXPath(getXPath(requestCredentialElement,
                                              Q_SIGNATURE, false));
        credential.setSignatureValue(getValue(requestCredentialElement,
                                              Q_SIGNATURE, false));

        credential.setSubjectXPath(getXPath(requestCredentialElement,
                                            Q_SUBJECT, false));
        credential.setSubjectValue(getValue(requestCredentialElement,
                                            Q_SUBJECT, false));

        return credential;
    }


    private SynapseXPath getXPath(OMElement requestCredentialElement,
                                  QName childElementName, boolean isMandatoryElement) {

        OMElement credentialElem = requestCredentialElement
                .getFirstChildWithName(childElementName);
        SynapseXPath xpath = null;
        if (null == credentialElem && isMandatoryElement) {
            handleException(String.format("Credential element %s is mandatory",
                                          childElementName.getLocalPart()));
        }
        if (null != credentialElem) {

            OMAttribute authTokenElemXPathAttr = credentialElem
                    .getAttribute(ATT_XPATH);
            OMAttribute authTokenElemValueAttr = credentialElem
                    .getAttribute(ATT_SOURCE_VALUE);

            if ((null == authTokenElemXPathAttr || null == authTokenElemXPathAttr
                    .getAttributeValue())
                && (null == authTokenElemValueAttr || null == authTokenElemValueAttr
                    .getAttributeValue())) {
                handleException(String
                        .format(
                        "Credential element %s that has no xpath attribute or source value",
                        childElementName.getLocalPart()));

            }
            if (null != authTokenElemXPathAttr
                && null != authTokenElemXPathAttr.getAttributeValue()) {
                try {
                    xpath = SynapseXPathFactory.getSynapseXPath(credentialElem,
                                                                ATT_XPATH);
                } catch (JaxenException e) {
                    handleException(
                            String
                                    .format(
                                    "Credential element %s: couldn't build the source-xpath from the expression: %s",
                                    childElementName.getLocalPart(),
                                    authTokenElemXPathAttr
                                            .getAttributeValue()));
                }
            }
        }
        return xpath;
    }

    private String getValue(OMElement requestCredentialElement,
                            QName childElementName, boolean isMandatoryElement) {

        OMElement credentialElem = requestCredentialElement
                .getFirstChildWithName(childElementName);
        String sourceValue = null;
        if (null == credentialElem && isMandatoryElement) {
            handleException(String.format("Credential element %s is mandatory",
                                          childElementName.getLocalPart()));
        }
        if (null != credentialElem) {

            OMAttribute authTokenElemXPathAttr = credentialElem
                    .getAttribute(ATT_XPATH);
            OMAttribute authTokenElemValueAttr = credentialElem
                    .getAttribute(ATT_SOURCE_VALUE);

            if ((null == authTokenElemXPathAttr || null == authTokenElemXPathAttr
                    .getAttributeValue())
                && (null == authTokenElemValueAttr || null == authTokenElemValueAttr
                    .getAttributeValue())) {
                handleException(String
                        .format(
                        "Credential element %s that has no xpath attribute or source value",
                        childElementName.getLocalPart()));

            }

            if (null != authTokenElemValueAttr
                && null != authTokenElemValueAttr.getAttributeValue()) {

                sourceValue = authTokenElemValueAttr.getAttributeValue();

            }
        }
        return sourceValue;
    }

    /**
     * This method parses the operation.
     *
     * @param operationElement
     * @return
     */
    protected Operation createOperation(OMElement operationElement) {

        if (null == operationElement) {
            handleException("PaypalMediator without an operation element has been found, "
                            + "but it is required to have an operation element for PaypalMediator");
        }

        OMAttribute nameAttr = operationElement.getAttribute(ATT_NAME);
        OMAttribute actionAttr = operationElement.getAttribute(ATT_ACTION);
        if (nameAttr == null || nameAttr.getAttributeValue() == null) {

            handleException("Operation without the name attribute has been found, "
                            + "but it is required to have the name attribute for a operation");
        }
        String action = null;
        if (null != actionAttr) {
            action = actionAttr.getAttributeValue();
        }
        Operation operation = new Operation();
        operation.setName(nameAttr.getAttributeValue());
        operation.setAction(action);

        if (null != operationElement.getAttribute(ATT_CURRENCY)) {
            operation.setCurrency(operationElement.getAttribute(ATT_CURRENCY)
                    .getAttributeValue());
        }
        OMElement inputsElement = operationElement
                .getFirstChildWithName(Q_INPUTS);
        OMElement outputsElement = operationElement
                .getFirstChildWithName(Q_OUTPUTS);

        if (null != inputsElement) {
            operation.setInputs(createInputs(inputsElement));
        }
        if (null != outputsElement) {
            operation.setOutputs(createOutputs(outputsElement));
        }

        return operation;
    }

    /**
     * This method parses the operation inputs.
     *
     * @param inputsElement the input element.
     */
    @SuppressWarnings("unchecked")
    protected List<Input> createInputs(OMElement inputsElement) {

        List<Input> inputs = new ArrayList<Input>();
        // Handling the inputs
        if (null != inputsElement
            && null != inputsElement.getChildrenWithName(Q_INPUT)) {
            for (Iterator<OMElement> inputItr = inputsElement
                    .getChildrenWithName(Q_INPUT); inputItr.hasNext();) {

                inputs.add(createInput(inputItr.next()));

            }
        }
        return inputs;
    }

    /**
     * This method parses the operation outputs.
     *
     * @param outputsElement the input element.
     */
    @SuppressWarnings("unchecked")
    protected List<Output> createOutputs(OMElement outputsElement) {

        List<Output> outputs = new ArrayList<Output>();
        // Handling the outputs
        if (null != outputsElement
            && null != outputsElement.getChildrenWithName(Q_OUTPUT)) {
            for (Iterator<OMElement> outputItr = outputsElement
                    .getChildrenWithName(Q_OUTPUT); outputItr.hasNext();) {

                outputs.add(createOutput(outputItr.next()));

            }
        }
        return outputs;
    }

    /**
     * This method builds the following from an <code>OMElement</code>.
     * <p/>
     * <pre>
     * &lt;input source-xpath=&quot;expression&quot; type=[xml|string] mode=[single|list]| name=&quot;string&quot; source-value=&quot;value&quot; /&gt;
     * </pre>
     *
     * @param inputElement
     * @param expressionAttr
     * @param typeAttr
     * @param modeAttr
     * @param valueAttr
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Input createInput(OMElement inputElement) {

        Input parameter = new Input();
        OMAttribute parameterNameAttr = inputElement.getAttribute(ATT_NAME);
        OMAttribute srcXPathAttr = inputElement.getAttribute(ATT_SOURCE_XPATH);
        OMAttribute typeAttr = inputElement.getAttribute(ATT_TYPE);
        OMAttribute srcValueAttr = inputElement.getAttribute(ATT_SOURCE_VALUE);

        if (null != typeAttr && null != typeAttr.getAttributeValue()) {
            parameter.setType(typeAttr.getAttributeValue());

            for (Iterator<OMElement> itr = inputElement
                    .getChildrenWithName(Q_INPUT); itr.hasNext();) {
                parameter.getSubInputs().add(createInput(itr.next()));
            }
        } else {
            if (null == parameterNameAttr
                || null == parameterNameAttr.getAttributeValue()) {
                handleException("Input without the name attribute has been found, but it is required to have the name attribute for all inputs");
            }

            parameter.setName(parameterNameAttr.getAttributeValue());

            if ((null == srcXPathAttr || null == srcXPathAttr
                    .getAttributeValue())
                && (null == srcValueAttr || null == srcValueAttr
                    .getAttributeValue())) {
                handleException(String
                        .format(
                        "Input parameter %s: has no source-xpath or source-value attribute, "
                        + "but it is required to have source-xpath or source-value attribute for all inputs",
                        parameter.getName()));
            }
            if (null != srcXPathAttr
                && null != srcXPathAttr.getAttributeValue()) {
                try {
                    parameter.setSourceXPath(SynapseXPathFactory
                            .getSynapseXPath(inputElement, ATT_SOURCE_XPATH));
                } catch (JaxenException e) {
                    handleException(String
                            .format(
                            "Input parameter %s: couldn't build the source-xpath from the expression: %s",
                            parameter.getName(), srcXPathAttr
                                    .getAttributeValue()));
                }

            } else {
                parameter.setSourceValue(srcValueAttr.getAttributeValue());
            }
        }
        return parameter;
    }

    /**
     * This method builds the following from an <code>OMElement</code>.
     * <p/>
     * <pre>
     * &lt;input source-xpath=&quot;expression&quot; type=[xml|string] mode=[single|list]| name=&quot;string&quot; source-value=&quot;value&quot; /&gt;
     * </pre>
     *
     * @param parameterElement
     * @param expressionAttr
     * @param typeAttr
     * @param modeAttr
     * @param valueAttr
     * @return
     */
    protected Output createOutput(OMElement parameterElement) {

        Output parameter = new Output();

        OMAttribute srcXPathAttr = parameterElement
                .getAttribute(ATT_SOURCE_XPATH);
        OMAttribute targetXPathAttr = parameterElement
                .getAttribute(ATT_TARGET_XPATH);
        OMAttribute targetKeyAttr = parameterElement
                .getAttribute(ATT_TARGET_KEY);

        if ((null == targetXPathAttr || null == targetXPathAttr
                .getAttributeValue())
            && (null == targetKeyAttr || null == targetKeyAttr
                .getAttributeValue())) {
            handleException("Output without a target-xpath or target-key attribute, "
                            + "but it is required to have a target-xpath or target-key attribute for all outputs");
        }
        if (null != targetXPathAttr
            && null != targetXPathAttr.getAttributeValue()) {

            if (null == srcXPathAttr
                || null == srcXPathAttr.getAttributeValue()) {
                handleException("Output without source-xpath attribute, "
                                + "but it is required to have  source-xpath attribute when target-xpath is present for all outputs");
            }
            try {
                parameter.setSourceXPath(SynapseXPathFactory.getSynapseXPath(
                        parameterElement, ATT_SOURCE_XPATH));
            } catch (JaxenException e) {
                handleException(String
                        .format(
                        "Couldn't build the source-xpath from the expression: %s for the output",
                        srcXPathAttr.getAttributeValue()));
            }

            try {
                parameter.setTargetXPath(SynapseXPathFactory.getSynapseXPath(
                        parameterElement, ATT_TARGET_XPATH));
            } catch (JaxenException e) {
                handleException(String
                        .format(
                        "Couldn't build the target-xpath from the expression: %s for the output",
                        srcXPathAttr.getAttributeValue()));
            }

        } else {
            parameter.setTargetKey(targetKeyAttr.getAttributeValue());
        }

        return parameter;
    }

    /*
      * (non-Javadoc)
      *
      * @see org.apache.synapse.config.xml.MediatorFactory#getTagQName()
      */

    public QName getTagQName() {
		return PAYPAL_Q;
	}

	protected void handleException(String exceptionMsg) {
		throw new MediatorException(exceptionMsg);
	}
}
