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
package org.wso2.carbon.rule.service;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.rule.core.RuleConstants;
import org.wso2.carbon.rulecep.commons.descriptions.rule.service.RuleServiceExtensionBuilder;
import org.wso2.carbon.rulecep.service.RuleCEPDeployerService;
import org.wso2.carbon.rulecep.service.ServiceDeployerInformation;

/**
 * Deploy rule service archives as axis2 service
 */
@SuppressWarnings("unused")
public class RuleServiceDeployer extends AbstractDeployer {

    private static Log log = LogFactory.getLog(RuleServiceDeployer.class);
    private ConfigurationContext configurationContext;
    private AxisConfiguration axisConfig;
    private RuleCEPDeployerService ruleCEPDeployerService;
    private String repositoryDirectory = null;
    private String extension = null;
    private final ServiceDeployerInformation deployerInformation =
            new ServiceDeployerInformation();

    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        this.ruleCEPDeployerService =
                RuleServiceManger.getInstance().getRuleCEPDeployerService();
        this.deployerInformation.setArchiveExtension(RuleConstants.RULE_SERVICE_ARCHIVE_EXTENSION);
        this.deployerInformation.setFileExtension(RuleConstants.RULE_FILE_EXTENSION);
        this.deployerInformation.setServiceProvider(new RuleServiceEngineFactory());
        this.deployerInformation.setServiceType(RuleConstants.RULE_SERVICE_TYPE);
        this.deployerInformation.setServicePathKey(RuleConstants.RULE_SERVICE_PATH);
        this.deployerInformation.setExtensionBuilder(new RuleServiceExtensionBuilder());
        this.deployerInformation.setMessageReceiverFactory(new RuleServiceMessageReceiverFactory());
        this.deployerInformation.setServiceArchiveGeneratableKey(RuleConstants.RULE_SERVICE_ARCHIVE_GENERATABLE);
    }

    /**
     * Process the rule  service archive file and create a rule service and deploy it
     *
     * @param deploymentFileData information about the rule service archive file
     * @throws DeploymentException for any errors
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        ruleCEPDeployerService.deploy(configurationContext, deploymentFileData, deployerInformation);
        super.deploy(deploymentFileData);
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Removing already deployed service
     *
     * @param fileName the path to the service to be removed
     * @throws DeploymentException
     */
    public void undeploy(String fileName) throws DeploymentException {
        //TODO clean rule session , destroy rule engine
        ruleCEPDeployerService.undeploy(configurationContext, fileName);
        super.undeploy(fileName);
    }

    public void setDirectory(String directory) {
        this.repositoryDirectory = directory;
    }
}
