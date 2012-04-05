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
package org.wso2.carbon.rulecep.service;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.utils.CarbonUtils;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for deploying services such as rule / cep
 */
public class RuleCEPDeployerService {

    private static Log log = LogFactory.getLog(RuleCEPDeployerService.class);

    /**
     * Deploying a service artifacts such as rule / cep
     *
     * @param configurationContext       axis2 configuration context
     * @param deploymentFileData         information about the service artifact
     * @param serviceDeployerInformation information for deployer
     * @throws DeploymentException for any errors during service deployment
     */
    public void deploy(ConfigurationContext configurationContext,
                       DeploymentFileData deploymentFileData,
                       ServiceDeployerInformation serviceDeployerInformation) throws DeploymentException {

        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();

        final StringWriter errorWriter = new StringWriter();
        String serviceStatus = "";
        try {
            deploymentFileData.setClassLoader(deploymentFileData.getFile().isDirectory(),
                    axisConfig.getServiceClassLoader(),
                    (File) axisConfig.getParameterValue(
                            Constants.Configuration.ARTIFACTS_TEMP_DIR), true);
            AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);
            serviceGroup.setServiceGroupClassLoader(deploymentFileData.getClassLoader());

            serviceGroup.addParameter(CarbonConstants.FORCE_EXISTING_SERVICE_INIT, true);
            ServiceBuilder serviceBuilder = new ServiceBuilder(configurationContext,
                    serviceDeployerInformation);
            List<AxisService> servicesList = serviceBuilder.build(serviceGroup,
                    deploymentFileData);

            URL location = deploymentFileData.getFile().toURI().toURL();
            DeploymentEngine.addServiceGroup(serviceGroup,
                    (ArrayList) servicesList,
                    location,
                    deploymentFileData,
                    axisConfig);
        } catch (DeploymentException de) {
            de.printStackTrace();
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            de.printStackTrace(error_ptintWriter);
            serviceStatus = "Error when deploying a rule service" + errorWriter.toString();
            log.error(serviceStatus);
            throw de;
        } catch (AxisFault axisFault) {
            PrintWriter error_ptintWriter = new PrintWriter(errorWriter);
            axisFault.printStackTrace(error_ptintWriter);
            serviceStatus = "Error:\n" + errorWriter.toString();
            log.error(serviceStatus);
            throw new DeploymentException(axisFault);
        } catch (Exception e) {
            log.error("Can not build the service", e);
            throw new DeploymentException(e);

        } finally {
            if (serviceStatus.startsWith("Error:")) {
                axisConfig.getFaultyServices().put(deploymentFileData.getFile().getAbsolutePath(),
                        serviceStatus);
                try {
                    CarbonUtils.registerFaultyService(deploymentFileData.getFile().getAbsolutePath(),
                            "ruleservices", configurationContext);
                } catch (Exception e) {
                    log.error("Cannot register faulty service with Carbon", e);
                }
            }
        }
    }

    /**
     * Removing already deployed service
     *
     * @param configurationContext Axis2 Configuration Context
     * @param fileName             the path to the service to be removed
     * @throws DeploymentException for any errors during service undeployment
     */
    public void undeploy(ConfigurationContext configurationContext,
                         String fileName) throws DeploymentException {
        AxisConfiguration axisConfig = configurationContext.getAxisConfiguration();
        try {
            File file = new File(fileName);
            fileName = file.getName();

            fileName = DeploymentEngine.getAxisServiceName(fileName);
            AxisServiceGroup serviceGroup = axisConfig
                    .removeServiceGroup(fileName);
            configurationContext.removeServiceGroupContext(serviceGroup);
            if (log.isDebugEnabled()) {
                log.debug(Messages.getMessage("serviceremoved", fileName));
            }
        } catch (AxisFault axisFault) {
            axisConfig.removeFaultyService(fileName);
        }
    }
}
