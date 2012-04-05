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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.rule.core.RuleConstants;
import org.wso2.carbon.rule.service.stub.wizard.RuleServiceAdminStub;
import org.wso2.carbon.rule.service.ui.RuleServiceClientException;
import org.wso2.carbon.rulecep.commons.descriptions.AXIOMXPathFactory;
import org.wso2.carbon.rulecep.commons.descriptions.AXIOMXPathSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.OMNamespaceFactory;
import org.wso2.carbon.rulecep.commons.descriptions.rule.RuleSetDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.service.RuleServiceExtensionBuilder;
import org.wso2.carbon.rulecep.commons.descriptions.rule.service.RuleServiceExtensionDescription;
import org.wso2.carbon.rulecep.commons.descriptions.rule.service.RuleServiceExtensionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.*;
import org.wso2.carbon.ui.CarbonUIUtil;
import org.wso2.carbon.utils.ServerConstants;

import javax.activation.DataHandler;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.rmi.RemoteException;

/**
 * WS Client calling the rule service admin
 */
public class RuleServiceAdminClient {

    private static final Log log = LogFactory.getLog(RuleServiceAdminClient.class);

    private RuleServiceAdminStub ruleServiceAdminStub;
    public static final String RULE_SERVIE = "ruleservice";
    public static final String FACTS = "facts";
    private static final ExtensionBuilder CONFIGURATION_EXTENSION_BUILDER = new RuleServiceExtensionBuilder();
    private static final ExtensionSerializer CONFIGURATION_EXTENSION_SERIALIZER = new RuleServiceExtensionSerializer();
    private static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    private static final OMNamespace NULL_NS = OM_FACTORY.createOMNamespace("", "");

    public RuleServiceAdminClient(ConfigurationContext ctx, String serverURL, String cookie)
            throws AxisFault {
        init(ctx, serverURL, cookie);
    }

    public RuleServiceAdminClient(javax.servlet.ServletContext servletContext,
                                  javax.servlet.http.HttpSession httpSession) throws Exception {
        ConfigurationContext ctx =
                (ConfigurationContext) servletContext.getAttribute(
                        CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) httpSession.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);
        String serverURL = CarbonUIUtil.getServerURL(servletContext, httpSession);
        init(ctx, serverURL, cookie);
    }

    private void init(ConfigurationContext ctx,
                      String serverURL,
                      String cookie) throws AxisFault {
        String serviceUploaderServiceEPR = serverURL + "RuleServiceAdmin";
        ruleServiceAdminStub = new RuleServiceAdminStub(ctx, serviceUploaderServiceEPR);
        ServiceClient client = ruleServiceAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(10000);
    }

    /**
     * Retrieves the <code>RuleServiceDescription</code>
     *
     * @param request <code>HttpServletRequest</code>
     * @return <code>RuleServiceDescription</code>  instance containing information about
     *         rule service being edited or created.
     */
    public ServiceDescription getRuleServiceDescription(
            HttpServletRequest request) {

        String serviceName = request.getParameter("serviceName");
        ServiceDescription serviceDescription =
                (ServiceDescription) request.getSession().getAttribute(RULE_SERVIE);
        if (serviceDescription != null) {
            if (serviceName != null && !"".equals(serviceName)) {
                String name = serviceDescription.getName();
                if (serviceName.equals(name)) {
                    return serviceDescription;
                }
            } else {
                return serviceDescription;
            }
        }
        if (serviceName != null && !"".equals(serviceName)) {
            serviceDescription = getRuleService(serviceName.trim());
        } else {
            serviceDescription = new ServiceDescription();
            serviceDescription.setExtension(RuleConstants.RULE_FILE_EXTENSION);
            serviceDescription.setTargetNamespace(RuleConstants.DEFAULT_TARGET_NAMESPACE);
            serviceDescription.setTargetNSPrefix(RuleConstants.DEFAULT_TARGET_NAMESPACE_PREFIX);
        }
        request.getSession().setAttribute(RULE_SERVIE, serviceDescription);
        return serviceDescription;
    }

    /**
     * Uploads facts
     *
     * @param serviceName name of the service facts belongs
     * @param fileName    fact file name
     * @param dataHandler DataHandler representing facts in binary
     * @param request     <code>HttpServletRequest</code>
     * @return A string array contains names of the uploaded facts
     */
    public String[] uploadFacts(String serviceName,
                                String fileName,
                                DataHandler dataHandler, HttpServletRequest request) {
        try {
            String[] strings =
                    ruleServiceAdminStub.uploadFacts(serviceName, fileName, dataHandler);
            ServiceDescription ruleServiceDescription = getRuleServiceDescription(request);
            if (ruleServiceDescription != null) {
                ruleServiceDescription.setExtension(RuleConstants.RULE_SERVICE_ARCHIVE_EXTENSION);
            }
            return strings;
        } catch (Exception e) {
            throw new RuleServiceClientException("Error uploading facts : " + fileName, log);
        }
    }

    /**
     * Upload a ruleset as a file
     *
     * @param serviceName name of the service rule script belongs
     * @param fileName    rule file name
     * @param dataHandler DataHandler representing rule script in binary
     * @param request     <code>HttpServletRequest</code>
     */
    public void uploadRuleFile(String serviceName,
                               String fileName,
                               DataHandler dataHandler,
                               HttpServletRequest request) {

        try {
            ruleServiceAdminStub.uploadRuleFile(serviceName,
                    fileName, dataHandler);
            ServiceDescription ruleServiceDescription = getRuleServiceDescription(request);
            if (ruleServiceDescription != null) {
                ruleServiceDescription.setExtension(RuleConstants.RULE_SERVICE_ARCHIVE_EXTENSION);
                RuleServiceExtensionDescription extensionDescription =
                        (RuleServiceExtensionDescription) ruleServiceDescription.getServiceExtensionDescription();
                RuleSetDescription ruleSetDescription =
                        extensionDescription.getRuleSetDescription();
                if (ruleSetDescription != null) {
                    ruleSetDescription.setPath(fileName);
                    ruleSetDescription.setKey("");
                    ruleSetDescription.setRuleSource(null); //remove rule source
                }
            }
        } catch (Exception e) {
            throw new RuleServiceClientException("Error uploading rule script : " + fileName, log);
        }

    }

    /**
     * Gets all facts belong to the service with the given name
     *
     * @param ruleServiceDescription information about the service
     * @param session                HttpSession
     * @return A string array of the facts' names
     */
    public String[] getAllFacts(ServiceDescription ruleServiceDescription,
                                javax.servlet.http.HttpSession session) {
        String serviceName = ruleServiceDescription.getName();
        String[] classes = (String[]) session.getAttribute(serviceName.trim());
        if (classes != null && classes.length > 1) { // message class already there: need the length to be > 1
            return classes;
        } else {
            try {
                String[] facts = ruleServiceAdminStub.getAllFacts(
                        ruleServiceDescription.getExtension(), serviceName);
                session.setAttribute(serviceName.trim(), facts);
                return facts;
            } catch (Exception e) {
                throw new RuleServiceClientException("Error getting all facts fro rule service : " +
                        serviceName, log);
            }
        }
    }

    /**
     * Save the rule service based on the information in the given
     * <code>RuleServiceDescription </code>
     *
     * @param ruleServiceDescription <code>RuleServiceDescription </code>
     */
    public void saveService(ServiceDescription ruleServiceDescription) {
        String serviceName = ruleServiceDescription.getName();
        OMElement result =
                ServiceDescriptionSerializer.serializeToRuleServiceConfiguration(
                        ruleServiceDescription,
                        OMNamespaceFactory.getInstance().createOMNamespace(new QName(serviceName)),
                        new AXIOMXPathSerializer(), CONFIGURATION_EXTENSION_SERIALIZER);
        result.addAttribute(
                OM_FACTORY.createOMAttribute(RuleConstants.ATT_GENERATE_SERVICES_XML.getLocalPart(),
                        NULL_NS, String.valueOf(ruleServiceDescription.isContainsServicesXML())));
        if (ruleServiceDescription.isContainsServicesXML()) {
            ruleServiceDescription.setExtension(RuleConstants.RULE_SERVICE_ARCHIVE_EXTENSION);
        }
        if (!ruleServiceDescription.isEditable()) {
            try {
                ruleServiceAdminStub.editRuleService(ruleServiceDescription.getExtension(),
                        serviceName, result);
            } catch (Exception e) {
                throw new RuleServiceClientException("Error editing rule service : " + serviceName,
                        log);
            }
        } else {
            try {
                ruleServiceAdminStub.addRuleService(ruleServiceDescription.getExtension(),
                        serviceName, result);
            } catch (Exception e) {
                throw new RuleServiceClientException("Error adding a new rule service : " +
                        serviceName, log);
            }
        }
    }

    /**
     * retrieves the rule service for the given service name
     *
     * @param name name of the rule service
     * @return <code>RuleServiceDescription </code> representing the information of the rule service
     */
    public ServiceDescription getRuleService(String name) {
        try {
            OMElement omElement = ruleServiceAdminStub.getRuleService(name);
            return ServiceDescriptionFactory.create(omElement.getFirstElement(),
                    new AXIOMXPathFactory(), CONFIGURATION_EXTENSION_BUILDER);
        } catch (RemoteException e) {
            throw new RuleServiceClientException("Error retrieving rule service from name : " +
                    name, log);
        }
    }
}
