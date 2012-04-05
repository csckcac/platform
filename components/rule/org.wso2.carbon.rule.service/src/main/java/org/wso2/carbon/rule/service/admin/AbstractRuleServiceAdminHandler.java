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
package org.wso2.carbon.rule.service.admin;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rule.core.RuleConstants;
import org.wso2.carbon.rule.service.RuleServiceManagementException;
import org.wso2.carbon.rulecep.commons.descriptions.AXIOMXPathSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.rule.service.RuleServiceExtensionSerializer;
import org.wso2.carbon.rulecep.commons.descriptions.service.ExtensionSerializer;
import org.wso2.carbon.rulecep.commons.utils.OMElementHelper;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.FileManipulator;
import org.wso2.carbon.utils.ServerConstants;

import java.io.*;

/**
 * Base class for implementations of the RuleServiceAdminHandler. This contains a few common methods
 */
public abstract class AbstractRuleServiceAdminHandler implements RuleServiceAdminHandler {

    protected Log log;

    protected final static AXIOMXPathSerializer AXIOM_XPATH_SERIALIZER = new AXIOMXPathSerializer();

    protected final static ExtensionSerializer CONFIGURATION_EXTENSION_SERIALIZER =
            new RuleServiceExtensionSerializer();

    protected static final OMFactory OM_FACTORY = OMAbstractFactory.getOMFactory();
    protected static final OMNamespace NULL_NS = OM_FACTORY.createOMNamespace("", "");

    protected AbstractRuleServiceAdminHandler() {
        log = LogFactory.getLog(getClass());
    }

    /**
     * Returns the working directory for rule services
     *
     * @return the working directory
     */
    protected String getTempDir() {
        String workDir =
                (String) MessageContext.getCurrentMessageContext().getProperty(
                        ServerConstants.WORK_DIR);
        String tempDir = workDir + File.separator + RuleConstants.RULE_SERVICE_TEMP_DIR;
        File file = new File(tempDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return tempDir;
    }

    /**
     * Creates the rule services repository  if there is no repository
     *
     * @param axisConfig Axis2 Environment configuration
     * @return Path to the rule services repository
     */
    protected String createServiceRepository(AxisConfiguration axisConfig) {
        String repository = axisConfig
                .getRepository().getPath();
        if (CarbonUtils.isURL(repository)) {
            throw new RuleServiceManagementException(
                    "Uploading services to URL repo is not supported ", log);
        }

        String servicesDir = repository + File.separator +
                RuleConstants.RULE_SERVICE_REPOSITORY_NAME;
        File serviceDir = new File(servicesDir);
        if (!serviceDir.exists()) {
            serviceDir.mkdirs();
        }
        return servicesDir;
    }

    /**
     * Returns the path to the rule service with the given name
     *
     * @param axisConfig  Axis2 Environment configuration
     * @param serviceName the name of the service
     * @return Path to the rule service
     */
    protected String getServicePath(AxisConfiguration axisConfig, String serviceName) {
        AxisService axisService;
        try {
            axisService = axisConfig.getService(serviceName);
        } catch (AxisFault axisFault) {
            throw new RuleServiceManagementException(
                    "Error accessing rule service with name " + serviceName, log);
        }
        String servicePath = null;
        if (axisService != null) {
            Parameter servicePathParameter =
                    axisService.getParameter(RuleConstants.RULE_SERVICE_PATH);
            if (servicePathParameter != null) {
                String value = (String) servicePathParameter.getValue();
                if (value != null && !"".equals(value.trim())) {
                    servicePath = value.trim();
                }
            }
        }
        return servicePath;
    }

    /**
     * Loads a file as an XML
     *
     * @param serviceName name of the service
     * @param ruleFile    file name
     * @return An XML content of the file
     */
    protected OMElement createXML(String serviceName, File ruleFile) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(ruleFile);
            OMElement omElement = OMElementHelper.getInstance().toOM(inputStream);
            if (omElement == null) {
                throw new RuleServiceManagementException("Cannot create an XML representation for" +
                        " the file in the service : " + serviceName + ". File was :" + ruleFile,
                        log);
            }
            omElement.detach();
            return omElement;

        } catch (FileNotFoundException e) {
            throw new RuleServiceManagementException("Cannot find the file : " + ruleFile + " ," +
                    " belongs to rule service with name : " + serviceName, log);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Clean up the temp resources
     *
     * @param paths contains the service path and temp directory path for the service
     */
    protected void cleanUp(Paths paths) {
        FileManipulator.deleteDir(new File(paths.getWorkingDirPath()));
    }
}
