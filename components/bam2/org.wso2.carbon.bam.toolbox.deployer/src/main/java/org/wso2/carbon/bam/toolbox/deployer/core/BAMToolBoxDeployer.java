package org.wso2.carbon.bam.toolbox.deployer.core;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.AbstractDeployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.BAMToolBoxDeployerConstants;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.bam.toolbox.deployer.deploy.BAMArtifactDeployerManager;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.bam.toolbox.deployer.internal.ServerStartUpInspector;
import org.wso2.carbon.bam.toolbox.deployer.internal.config.ToolBoxConfigurationManager;
import org.wso2.carbon.bam.toolbox.deployer.util.ToolBoxDTO;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.multitenancy.SuperTenantCarbonContext;
import org.wso2.carbon.user.api.Tenant;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;
import java.util.ArrayList;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class BAMToolBoxDeployer extends AbstractDeployer {
    private ConfigurationContext configurationContext;
    private String extension;
    private String directory;

    private static final Log log = LogFactory.getLog(BAMToolBoxDeployer.class);


    private static BAMToolBoxDeployer pausedDeployments = new BAMToolBoxDeployer();
    private ArrayList<DeploymentFileData> pausedDeploymentFileDatas = new ArrayList<DeploymentFileData>();

    @Override
    public void init(ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        createHotDeployementFolderIfNotExists();
        CarbonContext.getCurrentContext().getTenantId();

        if (!ServerStartUpInspector.isServerStarted()) {
            if (getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
                pausedDeployments = this;
                int port = CarbonUtils.getTransportPort(configurationContext, "https");
                ServerStartUpInspector inspector = new ServerStartUpInspector();
                inspector.setPort(port);
                inspector.start();

            }
        } else {
            doInitialUnDeployments();
        }


    }

    public void doInitialUnDeployments() {
        try {
            ArrayList<String> allTools = ToolBoxConfigurationManager.getInstance().getAllToolBoxNames(getTenantId());
            ArrayList<String> availArtifacts = getAllBAMArtifacts();
            for (String aTool : allTools) {
                if (!availArtifacts.contains(aTool)) {
                    removeAllArtifacts(aTool);
                }
            }
        } catch (BAMToolboxDeploymentException e) {
            log.error("Error in initializing the tasks of BAM Tool Box deployement", e);
        }
    }

    @Override
    public void setDirectory(String dir) {
        this.directory = dir;
    }

    @Override
    public void setExtension(String extension) {
        this.extension = extension;
    }

    /**
     * Unzip the bar artifact and calls the necessary admin services to deploy the artifacts
     *
     * @param deploymentFileData .bar artifact
     * @throws DeploymentException
     */
    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {
        if (ServerStartUpInspector.isServerStarted()) {
            String path = deploymentFileData.getAbsolutePath();
            File toolBox = new File(path);
            String destDir = toolBox.getParent();
            ToolBoxConfigurationManager manager = ToolBoxConfigurationManager.getInstance();
            try {
                ArrayList<String> allTools = manager.getAllToolBoxNames(getTenantId());
                if (!allTools.contains(toolBox.getName().replaceAll("." + BAMToolBoxDeployerConstants.BAM_ARTIFACT_EXT, ""))) {
                    log.info("Deploying file:" + path);
                    BAMArtifactProcessor processor = BAMArtifactProcessor.getInstance();
                    String barDir = processor.extractBAMArtifact(path, destDir + "/temp");

                    ToolBoxDTO aTool = processor.getToolBoxDTO(barDir);

                    int tenantId = getTenantId();
                    BAMArtifactDeployerManager.getInstance().deploy(aTool, tenantId, getTenantAdminName(tenantId));

                    manager.addNewToolBoxConfiguration(aTool, getTenantId());

                    String repoPath = this.configurationContext.getAxisConfiguration().getRepository().getPath();
                    removeTempFiles(new File(repoPath + File.separator + this.directory + "/temp"));
                    log.info("Deployed successfully file: " + path);
                }
            } catch (BAMToolboxDeploymentException e) {
                log.error("Error while deploying bam  artifact :" + deploymentFileData.getAbsolutePath(), e);
                throw new BAMToolboxDeploymentException("Error while deploying bam  artifact :" + deploymentFileData.getAbsolutePath(), e);
            }
        } else {
            this.pausedDeploymentFileDatas.add(deploymentFileData);
        }

    }

    private int getTenantId() {
        AxisConfiguration axisConfiguration = this.configurationContext.getAxisConfiguration();
        return SuperTenantCarbonContext.getCurrentContext(axisConfiguration).getTenantId();
    }


    public void undeploy(String fileName) throws DeploymentException {
        log.info("Undeploying file:" + fileName);
        File file = new File(fileName);
        try {
            removeAllArtifacts(file.getName().replaceAll("." + BAMToolBoxDeployerConstants.BAM_ARTIFACT_EXT, ""));
            super.undeploy(fileName);
            log.info("Undeployed successfully file: " + file);
        } catch (BAMToolboxDeploymentException e) {
            log.error("Error while undeploying file " + fileName, e);
            throw new BAMToolboxDeploymentException("Error while undeploying file " + fileName, e);
        }
    }


    private ArrayList<String> getAllBAMArtifacts() {
        String repoPath = this.configurationContext.getAxisConfiguration().getRepository().getPath();
        File dir = new File(repoPath+ File.separator + this.directory);
        log.info(dir.getAbsolutePath());
        ArrayList<String> files = new ArrayList<String>();

        String[] children = dir.list();
        if (children == null) {

        } else {
            for (int i = 0; i < children.length; i++) {
                if (!new File(children[i]).isDirectory()) {
                    if (children[i].endsWith(BAMToolBoxDeployerConstants.BAM_ARTIFACT_EXT)) {
                        files.add(children[i].replaceAll("." + BAMToolBoxDeployerConstants.BAM_ARTIFACT_EXT, ""));
                    }
                }
            }
        }
        return files;
    }


    private void removeAllArtifacts(String aToolName) throws BAMToolboxDeploymentException {
        int tenantId = getTenantId();
        ToolBoxConfigurationManager manager = ToolBoxConfigurationManager.getInstance();
        ToolBoxDTO toolBoxDTO = manager.getToolBox(aToolName, getTenantId());

        BAMArtifactDeployerManager.getInstance().undeploy(toolBoxDTO, getTenantAdminName(tenantId));
        manager.deleteToolBoxConfiguration(aToolName, getTenantId());
    }


    public void cleanup() throws DeploymentException {
        super.cleanup();
        String repoPath = this.configurationContext.getAxisConfiguration().getRepository().getPath();
        removeTempFiles(new File(repoPath + File.separator + this.directory + "/temp"));
    }

    private boolean removeTempFiles(File tempDir) {
        if (tempDir == null)
            return false;
        if (!tempDir.exists())
            return true;
        if (!tempDir.isDirectory())
            return false;

        String[] list = tempDir.list();

        if (list != null) {
            for (int i = 0; i < list.length; i++) {
                File entry = new File(tempDir, list[i]);
                if (entry.isDirectory()) {
                    if (!this.removeTempFiles(entry))
                        return false;
                } else {
                    if (!entry.delete())
                        return false;
                }
            }
        }
        return tempDir.delete();
    }

    private void createHotDeployementFolderIfNotExists() {
        String repoPath = this.configurationContext.getAxisConfiguration().getRepository().getPath();
        File hotDeploymentDir = new File(repoPath + File.separator + this.directory);
        if (!hotDeploymentDir.exists()) {
            hotDeploymentDir.mkdir();
        }
    }


    private String getTenantAdminName(int tenantId) throws BAMToolboxDeploymentException {
        if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
            try {
                return ServiceHolder.getUserRealm().getRealmConfiguration().getAdminUserName();
            } catch (UserStoreException e) {
                log.error(e.getMessage(), e);
                throw new BAMToolboxDeploymentException("Error while obtaining " +
                        "the admin username for tenant: " + tenantId, e);
            }
        } else {
            TenantManager manager = ServiceHolder.getRealmService().getTenantManager();
            try {
                Tenant tenant = manager.getTenant(tenantId);
                return tenant.getRealmConfig().getAdminUserName();
            } catch (org.wso2.carbon.user.api.UserStoreException e) {
                log.error(e.getMessage(), e);
                throw new BAMToolboxDeploymentException("Error while obtaining " +
                        "the admin username for tenant: " + tenantId, e);
            }
        }
    }

    public ArrayList<DeploymentFileData> getPausedDeploymentFileDatas() {
        return pausedDeploymentFileDatas;
    }


    public static BAMToolBoxDeployer getPausedDeployments() {
        return pausedDeployments;
    }


}


