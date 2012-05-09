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
package org.wso2.carbon.jaggery.deployer;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.catalina.deploy.SecurityCollection;
import org.apache.catalina.deploy.SecurityConstraint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.jaggery.app.mgt.*;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.webapp.mgt.WebContextParameter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Axis2 deployer for deploying Web applications
 */
public class JaggeryDeployer extends AbstractDeployer {
    private static final Log log = LogFactory.getLog(JaggeryDeployer.class);
    private String webappsDir;
    private TomcatJaggeryWebappsDeployer tomcatJaggeryDeployer;
    private JaggeryApplicationsHolder webappsHolder;
    private final List<WebContextParameter> servletContextParameters =
            new ArrayList<WebContextParameter>();

    public void init(ConfigurationContext configCtx) {

        String repoPath = configCtx.getAxisConfiguration().getRepository().getPath();
        File webappsDirFile = new File(repoPath + File.separator + webappsDir);
        if (!webappsDirFile.exists() && !webappsDirFile.mkdirs()) {
            log.warn("Could not create directory " + webappsDirFile.getAbsolutePath());
        }
        SuperTenantCarbonContext carbonContext = SuperTenantCarbonContext.getCurrentContext(configCtx);
        int tenantId = carbonContext.getTenantId();
        String tenantDomain = carbonContext.getTenantDomain();
        String webContextPrefix = (tenantDomain != null) ?
                "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX +
                        "/" + tenantDomain + "/" + JaggeryConstants.WEBAPP_PREFIX + "/" :
                "";
        if (configCtx.getProperty(JaggeryMgtConstants.JAGGERY_APPLICATIONS_HOLDER) != null) {
            webappsHolder = (JaggeryApplicationsHolder) configCtx.getProperty(JaggeryMgtConstants.JAGGERY_APPLICATIONS_HOLDER);
        } else {
            webappsHolder = new JaggeryApplicationsHolder(new File(webappsDir));
            configCtx.setProperty(JaggeryMgtConstants.JAGGERY_APPLICATIONS_HOLDER, webappsHolder);
        }

        tomcatJaggeryDeployer = new TomcatJaggeryWebappsDeployer(webContextPrefix, tenantId, tenantDomain, webappsHolder);
        WebContextParameter webServiceServerUrlParam =
                new WebContextParameter("webServiceServerURL",
                        CarbonUtils.getServerURL(ServerConfiguration.getInstance(),
                                configCtx)); // TODO: Senaka, Azeez make this a CarbonContext attribuite?
        servletContextParameters.add(webServiceServerUrlParam);
    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        try {
            //avoiding hirachical deployment
            String filePath = deploymentFileData.getAbsolutePath();
            String appWithDeploymentDir = File.separator + webappsDir + File.separator + deploymentFileData.getName();
            if (filePath.contains(appWithDeploymentDir)) {
                doDeploy(deploymentFileData);
            }
        } catch (Exception e) {
            String msg = "Error occurred while deploying webapp " + deploymentFileData.getFile().getAbsolutePath();
            log.error(msg, e);
            throw new DeploymentException(msg, e);
        }
    }

    private void doDeploy(DeploymentFileData deploymentFileData) throws CarbonException, DeploymentException {
        // Object can be of listeners interfaces in javax.servlet.*

        ArrayList<Object> listeners = new ArrayList<Object>(1);
        // listeners.add(new CarbonServletRequestListener());

        ServletParameter jaggeryServletParameter = new ServletParameter();
        ServletParameter jsspParameter = new ServletParameter();

        jaggeryServletParameter.setServletName(JaggeryConstants.JAGGERY_SERVLET_NAME);
        jaggeryServletParameter.setServletClass(JaggeryConstants.JAGGERY_SERVLET_CLASS);

        jsspParameter.setServletName(JaggeryConstants.JSSP_NAME);
        jsspParameter.setServletClass(JaggeryConstants.JSSP_CLASS);
        jsspParameter.setLoadOnStartup(2);
        HashMap<String, String> jsspInitParamMap = new HashMap<String, String>();
        jsspInitParamMap.put("fork", "false");
        jsspParameter.setInitParams(jsspInitParamMap);

        List<ServletParameter> servletParamList =
                new ArrayList<ServletParameter>();
        servletParamList.add(jaggeryServletParameter);
        servletParamList.add(jsspParameter);

        ServletMappingParameter jaggeryServletMappingParameter = new ServletMappingParameter();
        ServletMappingParameter jsspMappingParameter = new ServletMappingParameter();

        jaggeryServletMappingParameter.setServletName(JaggeryConstants.JAGGERY_SERVLET_NAME);
        jaggeryServletMappingParameter.setUrlPattern(JaggeryConstants.JAGGERY_SERVLET_URL_PATTERN);

        SecurityConstraint securityConstraint = new SecurityConstraint();
        securityConstraint.setAuthConstraint(true);

        SecurityCollection securityCollection = new SecurityCollection();
        securityCollection.setName("ConfigDir");
        securityCollection.setDescription("Jaggery Configuration Dir");
        securityCollection.addPattern("/" + JaggeryConstants.JAGGERY_CONF_FILE);

        securityConstraint.addCollection(securityCollection);

        List<ServletMappingParameter> servletMappingParamList =
                new ArrayList<ServletMappingParameter>();
        servletMappingParamList.add(jaggeryServletMappingParameter);
        servletMappingParamList.add(jsspMappingParameter);

        tomcatJaggeryDeployer.deploy(deploymentFileData.getFile(), servletContextParameters, listeners, servletParamList, servletMappingParamList, securityConstraint);
        super.deploy(deploymentFileData);
    }

    public void setDirectory(String repoDir) {
        this.webappsDir = repoDir;
    }

    public void setExtension(String extension) {
    }

    public void undeploy(String fileName) throws DeploymentException {
        File f = new File(fileName);

        if (!f.exists()) {
            try {
                tomcatJaggeryDeployer.undeploy(new File(fileName));
            } catch (CarbonException e) {
                String msg = "Error occurred during undeploying webapp: " + fileName;
                log.error(msg, e);
                throw new DeploymentException(msg, e);
            }
            super.undeploy(fileName);
        }
    }

    @Override
    public void cleanup() throws DeploymentException {
        for (String filePath : deploymentFileDataMap.keySet()) {
            try {
                tomcatJaggeryDeployer.lazyUnload(new File(filePath));
            } catch (CarbonException e) {
                String msg = "Error occurred during cleaning up webapps";
                log.error(msg, e);
                throw new DeploymentException(msg, e);
            }
        }
    }
}
