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
package org.wso2.carbon.application.deployer.synapse;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.application.deployer.AppDeployerUtils;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.wso2.carbon.application.deployer.handler.AppDeploymentHandler;
import org.wso2.carbon.application.deployer.synapse.internal.SynapseAppDeployerDSComponent;

import java.io.File;
import java.util.List;
import java.util.Map;

public class SynapseAppDeployer implements AppDeploymentHandler {

    private static final Log log = LogFactory.getLog(SynapseAppDeployer.class);

    private Map<String, Boolean> acceptanceList = null;

    /**
     * Deploy the artifacts which can be deployed through this deployer (endpoints, sequences,
     * proxy service etc.).
     *
     * @param carbonApp  - CarbonApplication instance to check for artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void deployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {

        List<Artifact.Dependency> artifacts = carbonApp.getAppConfig().getApplicationArtifact()
                .getDependencies();

        String synapseRepo = axisConfig.getRepository().getPath() + File.separator +
                             SynapseAppDeployerConstants.SYNAPSE_CONFIGS + File.separator +
                             SynapseAppDeployerConstants.DEFAULT_DIR;
        String artifactPath, destPath;
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }

            if (!isAccepted(artifact.getType())) {
                log.warn("Can't deploy artifact : " + artifact.getName() + " of type : " +
                         artifact.getType() + ". Required features are not installed in the system");
                continue;
            }

            if (SynapseAppDeployerConstants.SEQUENCE_TYPE.equals(artifact.getType())) {
                destPath = synapseRepo + SynapseAppDeployerConstants.SEQUENCES_FOLDER;
            } else if (SynapseAppDeployerConstants.ENDPOINT_TYPE.equals(artifact.getType())) {
                destPath = synapseRepo + SynapseAppDeployerConstants.ENDPOINTS_FOLDER;
            } else if (SynapseAppDeployerConstants.PROXY_SERVICE_TYPE.equals(artifact.getType())) {
                destPath = synapseRepo + SynapseAppDeployerConstants.PROXY_SERVICES_FOLDER;
            } else if (SynapseAppDeployerConstants.LOCAL_ENTRY_TYPE.equals(artifact.getType())) {
                destPath = synapseRepo + SynapseAppDeployerConstants.LOCAL_ENTRIES_FOLDER;
            } else if (SynapseAppDeployerConstants.EVENT_SOURCE_TYPE.equals(artifact.getType())) {
                destPath = synapseRepo + SynapseAppDeployerConstants.EVENTS_FOLDER;
            } else if (SynapseAppDeployerConstants.TASK_TYPE.equals(artifact.getType())) {
                destPath = synapseRepo + SynapseAppDeployerConstants.TASKS_FOLDER;
            } else if (SynapseAppDeployerConstants.MESSAGE_STORE_TYPE.endsWith(artifact.getType())) {
                destPath = synapseRepo + SynapseAppDeployerConstants.MESSAGE_STORE_FOLDER;
            } else if (SynapseAppDeployerConstants.MESSAGE_PROCESSOR_TYPE.endsWith(artifact.getType())) {
                destPath = synapseRepo + SynapseAppDeployerConstants.MESSAGE_PROCESSOR_FOLDER;
            } else {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("Synapse artifact types must have a single file to " +
                          "be deployed. But " + files.size() + " files found.");
                continue;
            }
            String fileName = artifact.getFiles().get(0).getName();
            artifactPath = artifact.getExtractedPath() + File.separator + fileName;
            AppDeployerUtils.createDir(destPath);
            AppDeployerUtils.copyFile(artifactPath, destPath + File.separator + fileName);
        }
    }

    /**
     * Check whether a particular artifact type can be accepted for deployment. If the type doesn't
     * exist in the acceptance list, we assume that it doesn't require any special features to be
     * installed in the system. Therefore, that type is accepted.
     * If the type exists in the acceptance list, the acceptance value is returned.
     *
     * @param serviceType - service type to be checked
     * @return true if all features are there or entry is null. else false
     */
    private boolean isAccepted(String serviceType) {
        if (acceptanceList == null) {
            acceptanceList = AppDeployerUtils.buildAcceptanceList(SynapseAppDeployerDSComponent
                                                                          .getRequiredFeatures());
        }
        Boolean acceptance = acceptanceList.get(serviceType);
        return (acceptance == null || acceptance);
    }
}


