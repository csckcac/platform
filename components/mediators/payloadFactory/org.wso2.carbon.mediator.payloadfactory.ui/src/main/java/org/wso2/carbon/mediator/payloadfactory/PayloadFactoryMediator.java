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
package org.wso2.carbon.mediator.payloadfactory;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class PayloadFactoryMediator extends AbstractMediator {

    private static Log log = LogFactory.getLog(PayloadFactoryMediator.class);

    private static final String PAYLOAD_FACTORY = "payloadFactory";
    private static final String FORMAT = "format";
    private static final String ARGS = "args";
    private static final String ARG = "arg";
    private static final String VALUE = "value";
    private static final String EXPRESSION = "expression";

    private static final QName FORMAT_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "format");
    private static final QName ARGS_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "args");

    private String format;
    private List<Argument> argumentList = new ArrayList<Argument>();

    public OMElement serialize(OMElement parent) {

        OMElement payloadFactoryElem = fac.createOMElement(PAYLOAD_FACTORY, synNS);
        saveTracingState(payloadFactoryElem, this);

        if (format != null) {

            try {
                OMElement formatElem = fac.createOMElement(FORMAT, synNS);
                formatElem.addChild(AXIOMUtil.stringToOM(format));
                payloadFactoryElem.addChild(formatElem);
            } catch (XMLStreamException e) {
                handleException("Error while serializing payloadFactory mediator", e);
            }

        } else {
            handleException("Invalid payloadFactory mediator, format is required");
        }

        if (argumentList != null && argumentList.size() > 0) {

            OMElement argumentsElem = fac.createOMElement(ARGS, synNS);

            for (Argument arg : argumentList) {

                OMElement argElem = fac.createOMElement(ARG, synNS);

                if (arg.getValue() != null) {
                    argElem.addAttribute(fac.createOMAttribute(VALUE, nullNS, arg.getValue()));
                } else if (arg.getExpression() != null) {
                    SynapseXPathSerializer.serializeXPath(arg.getExpression(), argElem, EXPRESSION);
                }
                argumentsElem.addChild(argElem);

            }
            payloadFactoryElem.addChild(argumentsElem);
        }

        if (parent != null) {
            parent.addChild(payloadFactoryElem);
        }

        return payloadFactoryElem;
    }

    public void build(OMElement elem) {

        OMElement formatElem = elem.getFirstChildWithName(FORMAT_Q);

        if (formatElem != null) {
            this.format = formatElem.getFirstElement().toString();
        } else {
            handleException("format element of payloadFactoryMediator is required");
        }

        OMElement argumentsElem = elem.getFirstChildWithName(ARGS_Q);

        if (argumentsElem != null) {

            Iterator itr = argumentsElem.getChildElements();

            while (itr.hasNext()) {
                OMElement argElem = (OMElement) itr.next();
                Argument arg = new Argument();
                String attrValue;

                if ((attrValue = argElem.getAttributeValue(ATT_VALUE)) != null) {
                    arg.setValue(attrValue);
                } else if ((attrValue = argElem.getAttributeValue(ATT_EXPRN)) != null) {
                    if (attrValue.trim().length() == 0) {
                        handleException("Attribute value for expression cannot be empty");
                    } else {
                        try {
                            arg.setExpression(SynapseXPathFactory.getSynapseXPath(argElem, ATT_EXPRN));
                        } catch (JaxenException e) {
                            handleException("Invalid XPath expression for attribute expression : " +
                                    attrValue, e);
                        }
                    }

                } else {
                    handleException("Unsupported arg type or expression attribute required");
                }

                argumentList.add(arg);
            }
        }

        // set its common attributes such as tracing etc
        processAuditStatus(this, elem);
    }

    public String getFormat() {
        return format;
    }

    public void addArgument(Argument arg) {
        argumentList.add(arg);
    }

    public List<Argument> getArgumentList() {
        return argumentList;
    }

    public String getTagLocalName() {
        return PAYLOAD_FACTORY;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new MediatorException(msg);
    }

    private void handleException(String msg, Exception ex) {
        log.error(msg, ex);
        throw new MediatorException(msg + " Caused by " + ex.getMessage());
    }

    public static class Argument {

        private String value;
        private SynapseXPath expression;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public SynapseXPath getExpression() {
            return expression;
        }

        public void setExpression(SynapseXPath expression) {
            this.expression = expression;
        }
    }
}
