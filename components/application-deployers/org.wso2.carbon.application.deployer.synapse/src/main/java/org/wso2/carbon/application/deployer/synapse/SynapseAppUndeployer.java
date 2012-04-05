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

import org.wso2.carbon.application.deployer.handler.AppUndeploymentHandler;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.List;
import java.io.File;

public class SynapseAppUndeployer implements AppUndeploymentHandler {

    private static final Log log = LogFactory.getLog(SynapseAppUndeployer.class);

    /**
     * Undeploys Synapse artifacts found in this application. Just delete the files from the
     * hot folders. Synapse hot deployer will do the rest..
     *
     * @param carbonApplication - CarbonApplication instance
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void undeployArtifacts(CarbonApplication carbonApplication, AxisConfiguration axisConfig) {

        List<Artifact.Dependency> artifacts = carbonApplication.getAppConfig()
                .getApplicationArtifact().getDependencies();

        String synapseRepo = axisConfig.getRepository().getPath() + File.separator +
                SynapseAppDeployerConstants.SYNAPSE_CONFIGS + File.separator +
                SynapseAppDeployerConstants.DEFAULT_DIR;
        String artifactPath, destPath;
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
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
            } else if(SynapseAppDeployerConstants.MESSAGE_STORE_TYPE.equals(artifact.getType())) {
                destPath = synapseRepo + SynapseAppDeployerConstants.MESSAGE_STORE_FOLDER;
            } else if(SynapseAppDeployerConstants.MESSAGE_PROCESSOR_TYPE.equals(artifact.getType())){
                destPath = synapseRepo + SynapseAppDeployerConstants.MESSAGE_PROCESSOR_FOLDER;
            } else {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("Synapse artifact types must have a single file. But " +
                        files.size() + " files found.");
                continue;
            }
            String fileName = artifact.getFiles().get(0).getName();
            artifactPath = destPath + File.separator + fileName;
            File artifactFile = new File(artifactPath);
            if (artifactFile.exists() && !artifactFile.delete()) {
                log.warn("Couldn't delete App artifact file : " + artifactPath);
            }
        }
    }
}
