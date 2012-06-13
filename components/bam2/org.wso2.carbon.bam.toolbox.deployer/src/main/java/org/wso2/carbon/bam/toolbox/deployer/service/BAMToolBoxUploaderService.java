package org.wso2.carbon.bam.toolbox.deployer.service;

import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.bam.toolbox.deployer.internal.config.ToolBoxConfigurationManager;
import org.wso2.carbon.bam.toolbox.deployer.util.ToolBoxStatusDTO;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.AbstractAdmin;

import javax.activation.DataHandler;
import java.io.*;
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
public class BAMToolBoxUploaderService extends AbstractAdmin {
    private static String BAM_DEPLOYMET_FOLDER = "bam-toolbox";

    public boolean uploadBAMToolBox(DataHandler toolbox, String toolboxName) throws BAMToolboxDeploymentException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        String repoPath = ServiceHolder.getConfigurationContextService()
                .getServerConfigContext().getAxisConfiguration().getRepository().getPath();
        File hotDeploymentDir = new File(repoPath + File.separator + BAM_DEPLOYMET_FOLDER);
        if (hotDeploymentDir.exists()) {
            File destFile = new File(hotDeploymentDir + File.separator + toolboxName);
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(destFile);
                toolbox.writeTo(fos);
                fos.flush();
                fos.close();
                return true;
            } catch (FileNotFoundException e) {
                return false;
            } catch (IOException e) {
                return false;
            }
        } else {

            throw new BAMToolboxDeploymentException("No deployment folder found for tenant id:" + tenantId);
        }
    }

    public ToolBoxStatusDTO getDeployedToolBoxes() throws BAMToolboxDeploymentException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();

        ToolBoxStatusDTO toolBoxStatusDTO = new ToolBoxStatusDTO();

        String repoPath = ServiceHolder.getConfigurationContextService()
                .getServerConfigContext().getAxisConfiguration().getRepository().getPath();
        File hotDeploymentDir = new File(repoPath + File.separator + BAM_DEPLOYMET_FOLDER);

        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".bar");
            }
        };

        String[] toolsInDir = hotDeploymentDir.list(filter);

        ToolBoxConfigurationManager configurationManager = ToolBoxConfigurationManager.getInstance();
        ArrayList<String> toolsInConf = configurationManager.getAllToolBoxNames(tenantId);

        toolBoxStatusDTO.setDeployedTools(getDeployedTools(toolsInDir, toolsInConf));
        toolBoxStatusDTO.setToBeDeployedTools(getToBeDeployedTools(toolsInDir, toolsInConf));
        toolBoxStatusDTO.setToBeUndeployedTools(getToBeUnDeployedTools(toolsInDir, toolsInConf));

        return toolBoxStatusDTO;
    }


    private String[] getDeployedTools(String[] toolsInDir, ArrayList<String> toolsInConf) {
        ArrayList<String> deployedTools = new ArrayList<String>();
        if (null != toolsInDir) {
            for (String tool : toolsInDir) {
                if (tool.endsWith(".bar")) {
                    tool = tool.replaceAll(".bar", "");
                }
                if (toolsInConf.contains(tool)) {
                    deployedTools.add(tool);
                }
            }
        }
        return deployedTools.toArray(new String[deployedTools.size()]);
    }

    private String[] getToBeDeployedTools(String[] toolsInDir, ArrayList<String> toolsInConf) {
        ArrayList<String> toBedeployedTools = new ArrayList<String>();
        if (null != toolsInDir) {
            for (String tool : toolsInDir) {
                if (tool.endsWith(".bar")) {
                    tool = tool.replaceAll(".bar", "");
                }
                if (!toolsInConf.contains(tool)) {
                    toBedeployedTools.add(tool);
                }
            }
        }
        return toBedeployedTools.toArray(new String[toBedeployedTools.size()]);
    }

    private String[] getToBeUnDeployedTools(String[] toolsInDir, ArrayList<String> toolsInConf) {

        ArrayList<String> toBeUndeployedTools = new ArrayList<String>();
        if (null != toolsInConf) {
            for (String tool : toolsInConf) {
                String toolName  = tool;
                tool += ".bar";
                if (null != toolsInDir) {
                    boolean exists = false;
                    for (String toolDir : toolsInDir) {
                        if (toolDir.equalsIgnoreCase(tool)) {
                            exists = true;
                            break;
                        }
                    }
                    if (!exists){
                        toBeUndeployedTools.add(toolName);
                    }
                }
                else {
                    toBeUndeployedTools.addAll(toolsInConf);
                    break;
                }
            }
        }
        return toBeUndeployedTools.toArray(new String[toBeUndeployedTools.size()]);
    }

    public boolean undeployToolBox(String toolboxName) throws BAMToolboxDeploymentException {
        int tenantId = CarbonContext.getCurrentContext().getTenantId();
        String repoPath = ServiceHolder.getConfigurationContextService()
                .getServerConfigContext().getAxisConfiguration().getRepository().getPath();
        File hotDeploymentDir = new File(repoPath + File.separator + BAM_DEPLOYMET_FOLDER);
        File toolbox = new File(repoPath + File.separator + BAM_DEPLOYMET_FOLDER +
                File.separator + toolboxName + ".bar");
        if (toolbox.exists()) {
            toolbox.delete();
            return true;
        } else {
            throw new BAMToolboxDeploymentException("No Tool Box exists" +
                    " in the deployment folder" + toolboxName);
        }

    }
}
