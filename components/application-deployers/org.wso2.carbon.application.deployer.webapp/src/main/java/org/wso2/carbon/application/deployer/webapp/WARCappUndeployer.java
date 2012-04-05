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
package org.wso2.carbon.application.deployer.webapp;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.application.deployer.handler.AppUndeploymentHandler;
import org.wso2.carbon.application.deployer.CarbonApplication;
import org.wso2.carbon.application.deployer.config.Artifact;
import org.wso2.carbon.application.deployer.config.CappFile;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.webapp.mgt.WebappAdmin;

import java.util.List;

public class WARCappUndeployer implements AppUndeploymentHandler {

    private static final Log log = LogFactory.getLog(WARCappUndeployer.class);

    /**
     * Check the artifact type and if it is a WAR, delete the file from the WAR
     * deployment hot folder
     *
     * @param carbonApp - CarbonApplication instance to check for WAR artifacts
     * @param axisConfig - AxisConfiguration of the current tenant
     */
    public void undeployArtifacts(CarbonApplication carbonApp, AxisConfiguration axisConfig) {

        List<Artifact.Dependency> artifacts = carbonApp.getAppConfig().getApplicationArtifact()
                .getDependencies();

        for (Artifact.Dependency dep : artifacts) {
            Artifact artifact = dep.getArtifact();
            if (artifact == null) {
                continue;
            }
            if (!WARCappDeployer.WAR_TYPE.equals(artifact.getType())) {
                continue;
            }

            List<CappFile> files = artifact.getFiles();
            if (files.size() != 1) {
                log.error("Web Applications must have a single WAR file. But " +
                        files.size() + " files found.");
                continue;
            }
            String fileName = artifact.getFiles().get(0).getName();
            WebappAdmin webappAdmin = new WebappAdmin();
            try {
                webappAdmin.deleteWebapp(fileName);
            } catch (AxisFault axisFault) {
                log.error("Error while deleting webapp artifact " + fileName, axisFault);
            }
        }
    }

}
