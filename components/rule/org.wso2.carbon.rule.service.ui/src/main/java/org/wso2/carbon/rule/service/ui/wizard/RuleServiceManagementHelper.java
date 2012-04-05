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
package org.wso2.carbon.rule.service.ui.wizard;

import org.wso2.carbon.rule.core.RuleConstants;
import org.wso2.carbon.rule.service.ui.ns.NameSpacesFactory;
import org.wso2.carbon.rulecep.adapters.impl.OMElementResourceAdapter;
import org.wso2.carbon.rulecep.commons.descriptions.ResourceDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.OperationDescription;
import org.wso2.carbon.rulecep.commons.descriptions.service.ServiceDescription;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;

/**
 * Helper class to use various requirements such as saving each step of the rule service creation wizard
 */
public class RuleServiceManagementHelper {

    private static String EMPTY_STRING = "";

    /**
     * Saves the data gathered in the step 1 of the rule service wizard
     * <ul>
     * <li>Service Name</li>
     * <li>Service Description</li>
     * </ul>
     *
     * @param ruleServiceDescription <code>RuleServiceDescription</code>  containing information
     *                               about the rule service being created or edited
     * @param request                <code>HttpServletRequest</code>
     */
    public static void saveStep1(ServiceDescription ruleServiceDescription,
                                 HttpServletRequest request) {

        String stepID = request.getParameter("stepID");
        if (!"step1".equals(stepID)) {
            return;
        }

        String serviceName = request.getParameter("ruleServiceName");
        if (serviceName != null && !"".equals(serviceName.trim())) {
            String name = ruleServiceDescription.getName();
            if (name == null || "".equals(name)) {
                ruleServiceDescription.setEditable(true);
            }
            ruleServiceDescription.setName(serviceName);
        }

        String ruleServiceTNS = request.getParameter("ruleServiceTNS");
        if (ruleServiceTNS != null && !"".equals(ruleServiceTNS)) {
            ruleServiceDescription.setTargetNamespace(ruleServiceTNS.trim());
        }

        String description = request.getParameter("description");
        if (description != null && !"".equals(description.trim())) {
            ruleServiceDescription.setDescription(description.trim());
        }

        String generateServicesXML = request.getParameter("generateServiceXML");
        if (generateServicesXML != null) {
            ruleServiceDescription.setContainsServicesXML(true);
        } else {
            ruleServiceDescription.setContainsServicesXML(false);
        }
    }

    /**
     * Saves the data gathered in the step 2 of the rule service wizard
     * (i.e rule set )
     *
     * @param ruleSetDescription <code>RuleSetDescription</code> containing information
     *                           about rule set
     * @param request            <code>HttpServletRequest</code>
     */
    public static void saveStep2(RuleSetDescription ruleSetDescription,
                                 HttpServletRequest request) {

        String stepID = request.getParameter("stepID");
        if (!"step2".equals(stepID)) {
            return;
        }

        String ruleScriptType = request.getParameter("ruleSourceType");
        if ("key".equals(ruleScriptType)) {
            String key = request.getParameter("ruleSourceKey");
            if (key != null && !"".equals(key.trim())) {
//                ruleSetDescription.setKey(key);
                ruleSetDescription.setPath(EMPTY_STRING);
                ruleSetDescription.setRuleSource(null); //remove rule source
                String registryType = request.getParameter("registryResourcePath");
                ruleSetDescription.setKey(registryType);
            }
        } else {
            String inlinedSource = request.getParameter("ruleSourceInlined");
            if (inlinedSource != null && !"".equals(inlinedSource.trim())) {
                ruleSetDescription.setRuleSource(inlinedSource.trim());
                ruleSetDescription.setKey(EMPTY_STRING);
                ruleSetDescription.setPath(EMPTY_STRING);
            }
        }
    }

    /**
     * Saves the information gathered in the step 5 of the rule service wizard
     * * <ul>
     * <li>Operation Name</li>
     * <li>Facts</li>
     * <li>Results</li>
     * </ul>
     *
     * @param serviceDescription <code>RuleServiceDescription</code>  containing information
     *                           about the rule service being created or edited
     * @param request            <code>HttpServletRequest</code>
     */
    public static void saveStep5(ServiceDescription serviceDescription,
                                 HttpServletRequest request) {

        String stepID = request.getParameter("stepID");
        if (!"step5".equals(stepID)) {
            return;
        }

        String operationName = request.getParameter("operationName");
        OperationDescription description =
                serviceDescription.getRuleServiceOperationDescription(operationName);
        if (description == null) {
            description = new OperationDescription();
            description.setName(new QName(operationName));//TODO
            serviceDescription.addRuleServiceOperationDescription(description);
        } else {
            description.clearFacts();
            description.clearResults();
        }

        NameSpacesFactory nameSpacesFactory = NameSpacesFactory.getInstance();

        String factCountParameter = request.getParameter("factCount");

        if (factCountParameter != null && !"".equals(factCountParameter)) {
            int factCount = 0;
            try {
                factCount = Integer.parseInt(factCountParameter.trim());

                for (int i = 0; i < factCount; i++) {
                    String name = request.getParameter("factName" + i);
                    String type = request.getParameter("factType" + i);

                    if (type != null && !"".equals(type)) {
                        ResourceDescription resourceDescription = new ResourceDescription();
                        description.addFactDescription(resourceDescription);
                        resourceDescription.addNameSpaces(
                                nameSpacesFactory.createNameSpaces("factValue" + i,
                                        operationName, request.getSession()));
                        resourceDescription.setType(type.trim());
                        if (name != null && !"".equals(name)) {
                            resourceDescription.setName(name.trim());
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String wrapperName = request.getParameter("wrapperName");
        if (wrapperName == null || "".equals(wrapperName)) {
            wrapperName = RuleConstants.DEFAULT_WRAPPER_NAME;
        }

        ResourceDescription wrapperDescription = new ResourceDescription();
        wrapperDescription.setType(OMElementResourceAdapter.TYPE);
        wrapperDescription.setName(wrapperName);
        String resultCountParameter = request.getParameter("resultCount");

        if (resultCountParameter != null && !"".equals(resultCountParameter)) {
            int resultCount = 0;
            try {
                resultCount = Integer.parseInt(resultCountParameter.trim());

                for (int i = 0; i < resultCount; i++) {
                    String name = request.getParameter("resultName" + i);
                    String type = request.getParameter("resultType" + i);

                    if (type != null && !"".equals(type)) {
                        ResourceDescription resourceDescription = new ResourceDescription();
                        wrapperDescription.addChildResource(resourceDescription);
                        resourceDescription.addNameSpaces(
                                nameSpacesFactory.createNameSpaces("resultValue" + i,
                                        operationName, request.getSession()));
                        resourceDescription.setType(type.trim());
                        if (name != null && !"".equals(name)) {
                            resourceDescription.setName(name.trim());
                        }
                    }
                }
            } catch (NumberFormatException ignored) {
            }
        }
        if (wrapperDescription.hasChildren()) {
            description.addResultDescription(wrapperDescription);
        }
    }
}
