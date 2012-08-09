package org.wso2.carbon.webapp.mgt;

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
import org.wso2.carbon.webapp.mgt.utils.GhostWebappDeployerUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWebappDeployer extends AbstractDeployer {

    private static final Log log = LogFactory.getLog(AbstractWebappDeployer.class);
    protected String webappsDir;
    protected TomcatGenericWebappsDeployer tomcatWebappDeployer;
    protected final List<WebContextParameter> servletContextParameters = new ArrayList<WebContextParameter>();
    protected ConfigurationContext configContext;
    protected AxisConfiguration axisConfig;
    protected WebApplicationsHolder webappsHolder;

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
                "/" + MultitenantConstants.TENANT_AWARE_URL_PREFIX + "/" + tenantDomain + "/" + this.webappsDir + "/" :
                "";
        // try to get the webapps holder from config ctx. if null, create one..
        webappsHolder = (WebApplicationsHolder) configCtx
                .getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);
        if (webappsHolder == null) {
            webappsHolder = new WebApplicationsHolder(new File(webappsDir));
            configCtx.setProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER, webappsHolder);
        }

        tomcatWebappDeployer = createTomcatGenericWebappDeployer(webContextPrefix, tenantId, tenantDomain);
        configCtx.setProperty(CarbonConstants.SERVLET_CONTEXT_PARAMETER_LIST, servletContextParameters);
        configCtx.setProperty(CarbonConstants.TOMCAT_GENERIC_WEBAPP_DEPLOYER, tomcatWebappDeployer);
    }

    protected abstract TomcatGenericWebappsDeployer createTomcatGenericWebappDeployer(
            String webContextPrefix, int tenantId, String tenantDomain);

    protected abstract String getType();

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        // We now support for exploded webapp deployment, so we have to check if unpackedWar
        // files are getting deployed again, which will cause conflict at tomcat level.
        if (!isSkippedWebapp(deploymentFileData.getFile())) {
            String webappName = deploymentFileData.getFile().getName();
            if (!GhostWebappDeployerUtils.isGhostOn()) {
                deployThisWebApp(deploymentFileData);
            } else {
                // Check the ghost file
                String absoluteFilePath = deploymentFileData.getAbsolutePath();
                File ghostFile = GhostWebappDeployerUtils.getGhostFile(absoluteFilePath, axisConfig);
                if (ghostFile == null || !ghostFile.exists()) {
                    // ghost file is not found. so this is a new webapp and we have to deploy it
                    deployThisWebApp(deploymentFileData);

                    // iterate all deployed webapps and find the deployed webapp and create the ghost file
                    WebApplication webApplication = GhostWebappDeployerUtils.
                            findDeployedWebapp(configContext, webappName);

                    if (webApplication != null) {
                        GhostWebappDeployerUtils.updateLastUsedTime(webApplication);
                        GhostWebappDeployerUtils.serializeWebApp(webApplication, axisConfig, absoluteFilePath);
                    }
                } else {
                    // load the ghost webapp
                    WebApplication ghostWebApplication = GhostWebappDeployerUtils.createGhostWebApp(
                            ghostFile, deploymentFileData.getFile(), tomcatWebappDeployer, axisConfig);
                    ghostWebApplication.setServletContextParameters(servletContextParameters);
                    String ghostWebappFileName = deploymentFileData.getFile().getName();

                    WebApplicationsHolder webappsHolder = (WebApplicationsHolder) configContext.
                            getProperty(CarbonConstants.WEB_APPLICATIONS_HOLDER);

                    log.info("Deploying Ghost webapp : " + ghostWebappFileName);
                    webappsHolder.getStartedWebapps().put(ghostWebappFileName, ghostWebApplication);
                    webappsHolder.getFaultyWebapps().remove(ghostWebappFileName);

                    // TODO:  add webbapp to eventlistners
                }
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
                    (ArrayList<WebContextParameter>) configContext.getProperty(
                            CarbonConstants.SERVLET_CONTEXT_PARAMETER_LIST),
                    listeners);
            super.deploy(deploymentFileData);

            WebApplication webApplication = GhostWebappDeployerUtils.findDeployedWebapp(
                    configContext, deploymentFileData.getFile().getName());

            if (webApplication != null) {
                webApplication.setProperty(WebappsConstants.WEBAPP_FILTER, getType());
            }

        } catch (Exception e) {
            String msg = "Error occurred while deploying webapp : " + deploymentFileData.getFile().getAbsolutePath();
            log.error(msg, e);
            throw new DeploymentException(msg, e);
        }
    }

    public void undeploy(String fileName) throws DeploymentException {
        try {
            File webappToUndeploy = new File(fileName);
            tomcatWebappDeployer.undeploy(webappToUndeploy);
            if (GhostWebappDeployerUtils.isGhostOn() &&
                    !GhostWebappDeployerUtils.skipUndeploy(fileName)) {
                // Remove the corresponding ghost file and dummy context directory
                File ghostFile = GhostWebappDeployerUtils.getGhostFile(fileName, axisConfig);
                File dummyContextDir = GhostWebappDeployerUtils.
                        getDummyContextFile(fileName, axisConfig);
                if (ghostFile != null && ghostFile.exists() && !ghostFile.delete()) {
                    log.error("Error while deleting Ghost webapp file : " +
                            ghostFile.getAbsolutePath());
                }
                if (dummyContextDir != null && dummyContextDir.exists() &&
                        !dummyContextDir.delete()) {
                    log.error("Error while deleting dummy context file : " +
                            dummyContextDir.getAbsolutePath());
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

    private boolean isSkippedWebapp(File webappFile) {
        if (webappFile.isDirectory()) {
            String webapFilePath = webappFile.getPath();
            // check for .svn and .meta files to be skipped. these are created by depsynch
            if (webapFilePath.endsWith(".svn") || webapFilePath.endsWith(".meta")) {
                return true;
            }
            File warFile = new File(webapFilePath + ".war");
            return warFile.exists();
        } else {
            return false;
        }
    }

}
