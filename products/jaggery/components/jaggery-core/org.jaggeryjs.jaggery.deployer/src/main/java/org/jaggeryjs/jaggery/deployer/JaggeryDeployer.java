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
package org.jaggeryjs.jaggery.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.jaggeryjs.jaggery.app.mgt.*;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.deployer.WebappDeployer;
import org.wso2.carbon.webapp.mgt.*;
import org.jaggeryjs.jaggery.app.mgt.TomcatJaggeryWebappsDeployer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Axis2 deployer for deploying Web applications
 */
public class JaggeryDeployer extends WebappDeployer {

    private static final Log log = LogFactory.getLog(JaggeryDeployer.class);

    public static final String JAGGERY_WEBAPP_FILTER_PROP = "jaggeryWebapp";

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
                        "/" + tenantDomain + "/" + JaggeryConstants.WEBAPP_PREFIX + "/" :
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
        tomcatWebappDeployer = new TomcatJaggeryWebappsDeployer(webContextPrefix,
                tenantId,
                tenantDomain,
                webappsHolder);

        configCtx.setProperty(CarbonConstants.TOMCAT_GENERIC_WEBAPP_DEPLOYER, tomcatWebappDeployer);
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
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
                // if found, set the filter property to separately identify the Jaggery webapp
                deployedWebapp.setProperty(WebappsConstants.WEBAPP_FILTER, JAGGERY_WEBAPP_FILTER_PROP);
            }
        }
    }

    public void undeploy(String fileName) throws DeploymentException {
        if (!new File(fileName).exists()) {
            super.undeploy(fileName);
        }
    }

}
