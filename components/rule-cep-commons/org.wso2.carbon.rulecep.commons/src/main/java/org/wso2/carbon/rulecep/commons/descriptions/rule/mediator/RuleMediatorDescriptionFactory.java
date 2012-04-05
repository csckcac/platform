/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.carbon.rulecep.commons.descriptions.rule.mediator;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rulecep.commons.CommonsConstants;
import org.wso2.carbon.rulecep.commons.LoggedRuntimeException;
import org.wso2.carbon.rulecep.commons.descriptions.QNameFactory;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescriptionFactory;
import org.wso2.carbon.rulecep.commons.descriptions.XPathFactory;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescriptionFactory;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.SessionDescriptionFactory;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionBuilder;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Factory for creating <code>RuleMediatorDescription</code> from an XML
 */
public class RuleMediatorDescriptionFactory {

    private static final Log log = LogFactory.getLog(RuleMediatorDescriptionFactory.class);

    /**
     * Creates a  <code>RuleMediatorDescription</code>  from the given XML
     *
     * @param configurationXML              an XML representation of the  <code>RuleMediatorDescription</code>
     * @param xPathFactory                  to be used to create XPaths
     * @param configurationExtensionBuilder <code>ConfigurationExtensionBuilder</code> instance
     * @return <code>RuleMediatorDescription</code> for the given configuration
     */
    public static RuleMediatorDescription create(OMElement configurationXML,
                                                 XPathFactory xPathFactory,
                                                 ExtensionBuilder configurationExtensionBuilder) {
        QName tagQName = configurationXML.getQName();
        QNameFactory qNameFactory = QNameFactory.getInstance();
        RuleMediatorDescription ruleMediatorDescription = new RuleMediatorDescription();
        QName sessionQName = qNameFactory.createQName(CommonsConstants.ELE_SESSION, tagQName);
        OMElement sessionElement = configurationXML.getFirstChildWithName(sessionQName);

        if (sessionElement != null) {
            SessionDescription description =
                    SessionDescriptionFactory.create(sessionElement, xPathFactory);
            ruleMediatorDescription.setSessionDescription(description);
        }

        QName executionSetQName = qNameFactory.createQName(CommonsConstants.ELE_RULESET,
                tagQName);
        OMElement executionSetElement = configurationXML.getFirstChildWithName(executionSetQName);

        if (executionSetElement != null) {
            RuleSetDescription description =
                    RuleSetDescriptionFactory.create(executionSetElement, xPathFactory,
                            configurationExtensionBuilder);
            ruleMediatorDescription.setRuleSetDescription(description);
        } else {
            throw new LoggedRuntimeException("ruleset configuration element must be given",
                    log);
        }

        QName factsQName = qNameFactory.createQName("facts", tagQName);
        OMElement factsElement = configurationXML.getFirstChildWithName(factsQName);
        if (factsElement != null) {
            QName inputQName = qNameFactory.createQName(CommonsConstants.ELE_FACT,
                    tagQName);
            Iterator inputs = factsElement.getChildrenWithName(inputQName);
            while (inputs.hasNext()) {
                OMElement inputElem = (OMElement) inputs.next();
                ResourceDescription input =
                        ResourceDescriptionFactory.createResourceDescription(inputElem,
                                xPathFactory);
                if (input != null) {
                    ruleMediatorDescription.addFactDescription(input);
                }
            }
        }

        QName resultsQName = qNameFactory.createQName("results", tagQName);
        OMElement resultsElement = configurationXML.getFirstChildWithName(resultsQName);
        if (resultsElement != null) {
            QName outputQName = qNameFactory.createQName(CommonsConstants.ELE_RESULT,
                    tagQName);
            Iterator outputs = resultsElement.getChildrenWithName(outputQName);
            while (outputs.hasNext()) {
                OMElement outputElem = (OMElement) outputs.next();
                ResourceDescription output =
                        ResourceDescriptionFactory.createResourceDescription(outputElem,
                                xPathFactory);
                if (output != null) {
                    ruleMediatorDescription.addResultDescription(output);
                }
            }
        }
        return ruleMediatorDescription;
    }
}
