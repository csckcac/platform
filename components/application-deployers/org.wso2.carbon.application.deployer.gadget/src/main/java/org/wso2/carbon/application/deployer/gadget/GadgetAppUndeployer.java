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
package org.wso2.carbon.application.deployer.gadget;

import org.wso2.carbon.application.deployer.handler.AppUndeploymentHandler;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.engine.AxisConfiguration;

import java.util.List;
import java.io.File;

public class GadgetAppUndeployer implements AppUndeploymentHandler {

    private static final Log log = LogFactory.getLog(GadgetAppUndeployer.class);

    /**
     * Check the artifact type and if it is a Gadget, delete the file from the Gadget
     * deployment hot folder
     *
     * @param carbonApp - CarbonApplication instance to check for Gadget artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {

        List<Artifact.Dependency> artifacts = carbonApp.getAppConfig().getApplicationArtifact()
                .getDependencies();

        String repo = axisConfig.getRepository().getPath();
        String artifactPath, destPath;
        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (GadgetAppDeployer.GADGET_TYPE.equals(artifact.getType())) {
                destPath = repo + File.separator + GadgetAppDeployer.GADGET_DIR;
            } else {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("A Gadget must have a single .gar file. But " +
                        files.size() + " files found.");
                continue;
            }
            String fileName = artifact.getFiles().get(0).getName();
            artifactPath = destPath + File.separator + fileName;
            File artifactFile = new File(artifactPath);
            if (artifactFile.exists() && !artifactFile.delete()) {
                log.warn("Couldn't delete Gadget artifact file : " + artifactPath);
            }
        }
    }

}
