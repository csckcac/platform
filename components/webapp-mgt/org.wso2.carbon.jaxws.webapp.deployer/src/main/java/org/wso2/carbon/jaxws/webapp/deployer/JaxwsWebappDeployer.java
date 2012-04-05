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
package org.wso2.carbon.jaxws.webapp.deployer;

import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.jaxws.webapp.mgt.JaxwsWebappConstants;
import org.wso2.carbon.webapp.deployer.WebappDeployer;
import org.wso2.carbon.webapp.mgt.*;

/**
 * Axis2 Deployer for deploying JAX-WS/JAX-RS Web applications
 */
public class JaxwsWebappDeployer extends WebappDeployer {

    private static final Log log = LogFactory.getLog(JaxwsWebappDeployer.class);

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        log.info("Deploying JAX-WS/JAX-RS Web Application : " +
                deploymentFileData.getFile().getAbsolutePath());

        // deploy the webapp using the Webapp Deployer
        super.deploy(deploymentFileData);

        // get the webapp holder from the config context
        WebApplicationsHolder webappsHolder = (WebApplicationsHolder) configContext
                .getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);
        if (webappsHolder != null) {
            // get the deployed webapp
            WebApplication deployedWebapp = webappsHolder
                    .getStartedWebapps().get(deploymentFileData.getFile().getName());
            if (deployedWebapp != null) {
                // if found, set the filter property to separately identify the JAX webapp
                deployedWebapp.setProperty(WebappsConstants.WEBAPP_FILTER,
                        JaxwsWebappConstants.JAX_WEBAPP_FILTER_PROP);
            }
        }
    }

}
