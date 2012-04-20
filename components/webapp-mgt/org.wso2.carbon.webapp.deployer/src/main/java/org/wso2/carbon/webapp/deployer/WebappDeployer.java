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
package org.wso2.carbon.webapp.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.mgt.TomcatGenericWebappsDeployer;
import org.wso2.carbon.webapp.mgt.WebApplication;
import org.wso2.carbon.webapp.mgt.WebApplicationsHolder;
import org.wso2.carbon.webapp.mgt.WebContextParameter;
import org.wso2.carbon.webapp.mgt.WebappsConstants;
import org.wso2.carbon.webapp.mgt.utils.GhostWebappDeployerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Axis2 deployer for deploying Web applications
 */
public class WebappDeployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(WebappDeployer.class);
    private String webappsDir;
    private TomcatGenericWebappsDeployer tomcatWebappDeployer;
    private final List<WebContextParameter> servletContextParameters =
            new ArrayList<WebContextParameter>();
    protected ConfigurationContext configContext;
    protected AxisConfiguration axisConfig;
    private WebApplicationsHolder webappsHolder;

    public void init(ConfigurationContext configCtx) {
        this.configContext = configCtx;
        this.axisConfig = configCtx.getAxisConfiguration();
        String repoPath = configCtx.getAxisConfiguration().getRepository().getPath();
        File webappsDirFile = new File(repoPath + File.separator + webappsDir);
        if (!webappsDirFile.exists() && !webappsDirFile.mkdirs()) {
            log.warn("Could not create directory " + webappsDirFile.getAbsolutePath());
        }
        SuperTenantCarbonContext carbonContext = SuperTenantCarbonContext.
                getCurrentContext(configCtx);
        int tenantId = carbonContext.getTenantId();
        String tenantDomain = carbonContext.getTenantDomain();
        String webContextPrefix = (tenantDomain != null) ?
                                  "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX +
                                  "/" + tenantDomain + "/" + WebappsConstants.WEBAPP_PREFIX + "/" :
                                  "";
        // try to get the webapps holder from config ctx. if null, create one..
        webappsHolder = (WebApplicationsHolder) configCtx
                .getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);
        if (webappsHolder == null) {
            webappsHolder = new WebApplicationsHolder(new File(webappsDir));
            configCtx.setProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER, webappsHolder);
        }
        configCtx.setProperty(CarbonConstants.SERVLET_CONTEXT_PARAMETER_LIST,
                              servletContextParameters);
        tomcatWebappDeployer = new TomcatGenericWebappsDeployer(webContextPrefix,
                                                                tenantId,
                                                                tenantDomain,
                                                                webappsHolder);

        configCtx.setProperty(CarbonConstants.TOMCAT_GENERIC_WEBAPP_DEPLOYER, tomcatWebappDeployer);

    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        if (!GhostWebappDeployerUtils.isGhostOn()) {
            deployThisWebApp(deploymentFileData);

        } else {
            String absoluteFilePath = deploymentFileData.getAbsolutePath();

            // Check the ghost file
            File ghostFile = GhostWebappDeployerUtils.getGhostFile(absoluteFilePath, axisConfig);
            if (ghostFile == null || !ghostFile.exists()) {
                // ghost file is not found. so this is a new webapp and we have to deploy it
                deployThisWebApp(deploymentFileData);

                // iterate all deployed webapps and find the deployed webapp and create the ghost file
                WebApplication webApplication = GhostWebappDeployerUtils.
                        findDeployedWebapp(configContext, absoluteFilePath);

                if (webApplication != null) {
                    GhostWebappDeployerUtils.updateLastUsedTime(webApplication);
                    GhostWebappDeployerUtils.serializeWebApp(webApplication, axisConfig,
                                                             absoluteFilePath);
                }
            } else {
                // load the ghost webapp
                WebApplication ghostWebApplication = GhostWebappDeployerUtils.
                        createGhostWebApp(ghostFile, deploymentFileData.getFile());
                ghostWebApplication.setServletContextParameters(servletContextParameters);
                String ghostWebappFileName = deploymentFileData.getFile().getName();

                WebApplicationsHolder webappsHolder = (WebApplicationsHolder) configContext.
                        getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);

                log.info("Deploying Ghost WebApp : " + ghostWebappFileName);
                webappsHolder.getStartedWebapps().put(ghostWebappFileName, ghostWebApplication);
                webappsHolder.getFaultyWebapps().remove(ghostWebappFileName);

                // TODO:  add webbapp to eventlistners
            }
        }
    }

    private void deployThisWebApp(DeploymentFileData deploymentFileData)
            throws DeploymentException {
        try {
            // Object can be of listeners interfaces in javax.servlet.*
            ArrayList<Object> listeners = new ArrayList<Object>(1);
//            listeners.add(new CarbonServletRequestListener());
            tomcatWebappDeployer.deploy(deploymentFileData.getFile(),
                                        servletContextParameters,
                                        listeners);
            super.deploy(deploymentFileData);
        } catch (Exception e) {
            String msg = "Error occurred while deploying webapp " + deploymentFileData.getFile().
                    getAbsolutePath();
            log.error(msg, e);
            throw new DeploymentException(msg, e);
        }

    }


    public void setDirectory(String repoDir) {
        this.webappsDir = repoDir;
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) throws DeploymentException {
        try {
            tomcatWebappDeployer.undeploy(new File(fileName));
            if (GhostWebappDeployerUtils.isGhostOn()) {
                // Remove the corresponding ghost file
                File ghostFile = GhostWebappDeployerUtils.getGhostFile(fileName, axisConfig);
                if (ghostFile != null && ghostFile.exists() && !ghostFile.delete()) {
                    log.error("Error while deleting ghost webapp file : " +
                              ghostFile.getAbsolutePath());
                }
            }
        } catch (CarbonException e) {
            String msg = "Error occurred during undeploying webapp: " + fileName;
            log.error(msg, e);
            throw new DeploymentException(msg, e);
        }
        super.undeploy(fileName);
    }

    @Override
    public void cleanup() throws DeploymentException {
        for (String filePath : deploymentFileDataMap.keySet()) {
            try {
                tomcatWebappDeployer.lazyUnload(new File(filePath));
            } catch (CarbonException e) {
                String msg = "Error occurred during cleaning up webapps";
                log.error(msg, e);
                throw new DeploymentException(msg, e);
            }
        }

        if (GhostWebappDeployerUtils.isGhostOn() && webappsHolder != null) {
            for (WebApplication webApplication : webappsHolder.getStartedWebapps().values()) {
                try {
                    tomcatWebappDeployer.lazyUnload(webApplication.getWebappFile());
                } catch (CarbonException e) {
                    String msg = "Error occurred during cleaning up webapps";
                    log.error(msg, e);
                    throw new DeploymentException(msg, e);
                }
            }
        }
    }
}
