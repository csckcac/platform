/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.stratos.landing.page.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.catalina.Context;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.stratos.landing.page.deployer.internal.DataHolder;

import java.io.File;

/**
 * Special deployer to deploy the Stratos Service specific landing pages.
 * Implemented as a separate deployer since we get the ability to modify the the html
 * without having to shutdown the server. 
 */
public class LandingPageWebappDeployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(LandingPageWebappDeployer.class);
    
    private final String LANDING_PAGE_WEBAPP_ROOT = "STRATOS_ROOT";
    public static final String WEBAPPS = "webapps";
    public static final String WEBAPP_CONTEXT = "home";

    public void init(ConfigurationContext configCtx) {
        // Super tenant repository path
        String repoPath = configCtx.getAxisConfiguration().getRepository().getPath();
        String landingpageWebappDir = repoPath + WEBAPPS + File.separator + LANDING_PAGE_WEBAPP_ROOT;
        File webappFile = new File(landingpageWebappDir);
        if (webappFile.exists()) {
            DeploymentFileData landingpageAppFile = new DeploymentFileData(webappFile);
            try {
                deploy(landingpageAppFile);
            } catch (DeploymentException e) {
                log.error("Error while deploying landing page webapp.", e);
            }
        } else {
            log.warn("Product landing page not found.");
        }
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        String carbonWebContextRoot = "/" + WEBAPP_CONTEXT;
        if (deploymentFileData.getFile() != null) {
            Context context = DataHolder.getCarbonTomcatService().
                    addWebApp(carbonWebContextRoot,deploymentFileData.getFile().getAbsolutePath(), null);
            log.info("Deployed product landing page webapp: " + context);
        }
    }

    public void setDirectory(String repoDir) {
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) throws DeploymentException {
    }

    public void cleanup() throws DeploymentException {
    }
}
