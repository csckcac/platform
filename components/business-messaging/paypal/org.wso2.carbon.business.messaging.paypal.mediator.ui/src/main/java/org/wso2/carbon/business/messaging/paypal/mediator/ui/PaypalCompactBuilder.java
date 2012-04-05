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
package org.wso2.carbon.business.messaging.paypal.mediator.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.ui.Mediator;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * <p>
 * Factory for {@link PaypalMediator} instances.Builds the
 * <code>PaypalMediator</code> using the following configuration
 * </p>
 * <paypal>
 * <credentials xmlns:ns2="http://wso2.services.samples" username="{xpath} | value" password="{xpath} | value"
 * signature="{xpath} | value" ... />
 * <opName currency="{xpath} | value" detailLevel="{xpath} | value" errorLanguage="e{xpath} | value" ref=""* .... />
 * </paypal>
 */

public class PaypalCompactBuilder extends PaypalMediatorBuilder {
    private static final int XPATH_TRIM_START_INDEX = 1;
    private static final int SOURCE_TRIM_START_INDEX = 2;

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

        paypalMediator.setRequestCredential(createRequestCredential(
                elem.getFirstChildWithName(Q_CREDENTIALS)));
        Iterator paypalElements = elem.getChildElements();

        while (paypalElements.hasNext()) {
            OMElement element = (OMElement) paypalElements.next();
            if (element != null && !element.getQName().equals(Q_CREDENTIALS)
                && !element.getQName().equals(Q_CONFIG)) {
                paypalMediator.setOperation(createOperation(element));
                break;
            }
        }

    }

    /**
     * This method parses the credentials and builds the operation credentials.
     *
     * @param requestCredentialElement corresponding configuration element for credentials
     * @return RequestCredential object of the operation
     */
    public RequestCredential createRequestCredential(
            OMElement requestCredentialElement) {

        if (null == requestCredentialElement) {
            handleException("PaypalMediator without credentials element has been found, "
                            + "but it is required to have credentials element for PaypalMediator");
        }


        RequestCredential credential = new RequestCredential();

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
        credential.setAuthTokenValue(getValue(requestCredentialElement,
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

    /**
     * Derive Xpath from the given attribute of subject element
     *
     * @param subjectElement  element containing xpath attributes
     * @param childAttribName attribute QName that contains xpath value
     * @param isMandatoryAttr resolving is mandatory for this
     * @return populated synapse Xpath object
     */
    private SynapseXPath getXPath(OMElement subjectElement,
                                  QName childAttribName, boolean isMandatoryAttr) {

        OMAttribute credentialAttr = subjectElement.getAttribute(
                new QName(childAttribName.getLocalPart()));
        SynapseXPath xpath = null;
        if (null == credentialAttr && isMandatoryAttr) {
            handleException(String.format("Attribute %s is mandatory as an input",
                                          childAttribName.getLocalPart()));
        }

        if (null != credentialAttr) {
            /*reset the name to corresponding format*/
            String xpathExpr = credentialAttr.getAttributeValue();
            if (isXpathString(xpathExpr)) {
                String newXpathExpr = extractXpathString(xpathExpr);
                credentialAttr.setAttributeValue(newXpathExpr);
                try {
                    xpath = SynapseXPathFactory.getSynapseXPath(subjectElement,
                                                                new QName(childAttribName.getLocalPart()));
                } catch (JaxenException e) {
                    handleException(String.format("Selected Attribute %s: couldn't build the " +
                                                  "source-xpath from the expression: %s",
                                                  childAttribName.getLocalPart(),
                                                  credentialAttr.getAttributeValue()));
                }
                finally {
                    //set original attribute expression to avoid conflicts
                    credentialAttr.setAttributeValue(xpathExpr);
                }
            }
        }
        return xpath;
    }

    private String extractXpathString(String xpathExpr) {
        return xpathExpr.trim().substring(XPATH_TRIM_START_INDEX, xpathExpr.length() - 1);
    }

    private boolean isXpathString(String xpathExpr) {
        if (xpathExpr.trim().startsWith("{{")) {
            return false;
        } else if (xpathExpr.trim().startsWith("{")) {
            return true;
        }
        return false;
    }

    /**
     * Derive a Source value from the given attribute of the subject element
     *
     * @param subjectElement  element containing xpath attributes
     * @param childAttribName attribute QName that contains xpath value
     * @param isMandatoryAttr resolving is mandatory for this
     * @return String representation of source value
     */
    private String getValue(OMElement subjectElement,
                            QName childAttribName, boolean isMandatoryAttr) {
        OMAttribute credentialAttr = subjectElement.getAttribute(
                new QName(childAttribName.getLocalPart()));
        String sourceValue = null;
        if (credentialAttr == null && isMandatoryAttr) {
            handleException(String.format("Attribute %s is mandatory",
                                          childAttribName.getLocalPart()));
        }
        if (null != credentialAttr) {
            String xpathExpr = credentialAttr.getAttributeValue();
            if (!isXpathString(xpathExpr)) {
                sourceValue = extractSourceValue(xpathExpr);
            }
        }
        return sourceValue;
    }

    private String extractSourceValue(String xpathExpr) {
        if (xpathExpr.trim().startsWith("{{")) {
            return extractXpathString(xpathExpr.trim().substring(SOURCE_TRIM_START_INDEX, xpathExpr.trim().length()));
        }
        return xpathExpr;
    }

    /**
     * This method parses the operation Config Element and builds the Paypal Operation.
     *
     * @param operationElement configuration elemtnt for operation
     * @return Paypal Operation
     */
    protected Operation createOperation(OMElement operationElement) {
        if (null == operationElement) {
            handleException("PaypalMediator without an operation element has been found, "
                            + "but it is required to have an operation element for PaypalMediator");
        }

        String opName = operationElement.getLocalName();
        OMAttribute actionAttr = operationElement.getAttribute(ATT_ACTION);
        if (opName == null || opName.trim().equals("")) {
            handleException("Operation without the name attribute has been found, "
                            + "but it is required to have the name attribute for a operation");
        }
        String action = null;
        if (null != actionAttr) {
            action = actionAttr.getAttributeValue();
        }
        Operation operation = new Operation();
        operation.setName(opName);
        operation.setAction(action);

        if (null != operationElement.getAttribute(ATT_CURRENCY)) {
            operation.setCurrency(operationElement.getAttribute(ATT_CURRENCY)
                    .getAttributeValue());
        }
        OMElement inputsElement = operationElement;

        /*OMElement outputsElement = operationElement
                .getFirstChildWithName(Q_OUTPUTS);
*/
        if (null != inputsElement) {
            operation.setInputs(createInputs(inputsElement));
        }
        /*if (null != outputsElement) {
            operation.setOutputs(createOutputs(outputsElement));
        }*/

        return operation;
    }

    /**
     * This method parses the operation inputs.
     *
     * @param inputsElement the input element.
     */
    @SuppressWarnings("unchecked")
    protected List<Input> createInputs(OMElement inputsElement) {
        List<Input> inputs = extractInputs(inputsElement);

        return inputs;
    }

    /**
     * This method derive Inputs for a given operation.Configuration is parsed recursively to
     * derive inline inputs(ref Element) if available
     *
     * @param opElement top level operation elemnt
     * @return list of Inputs
     */
    private List<Input> extractInputs(OMElement opElement) {
        List<Input> registeredInputs = new ArrayList<Input>();

        Iterator attribs = opElement.getAllAttributes();
        while (attribs.hasNext()) {
            OMAttribute inputAttribute = (OMAttribute) attribs.next();

            if (!inputAttribute.getLocalName().trim().equals("ref") &&
                !inputAttribute.getLocalName().trim().startsWith("xmlns:") &&
                !inputAttribute.getLocalName().equals("xmlns") &&
                !inputAttribute.getQName().equals(ATT_CURRENCY)) {
                Input parameter = new Input();
                String inputName = inputAttribute.getLocalName();
                parameter.setName(inputName);
                parameter.setSourceXPath(getXPath(opElement, new QName(inputName), true));
                parameter.setSourceValue(getValue(opElement, new QName(inputName), true));
                registeredInputs.add(parameter);
            } else if (inputAttribute.getLocalName().trim().equals("ref")) {
                String refElementName = inputAttribute.getAttributeValue();
                Input parameter = new Input();
                parameter.setType(refElementName);
                OMElement refOMElement = opElement.getFirstChildWithName(
                        new QName(XMLConfigConstants.SYNAPSE_OMNAMESPACE.getNamespaceURI(), refElementName));
                List inlineInputs = extractInputs(refOMElement);
                Iterator iteratorInlines = inlineInputs.iterator();
                while (iteratorInlines.hasNext()) {
                    Input inlineInput = (Input) iteratorInlines.next();
                    parameter.getSubInputs().add(inlineInput);
                }
                registeredInputs.add(parameter);
            }
        }

        return registeredInputs;  //To change body of created methods use File | Settings | File Templates.
    }


}
