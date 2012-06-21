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

package org.wso2.carbon.bam.toolbox.deployer.deploy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.BAMToolBoxDeployerConstants;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.bam.toolbox.deployer.client.DashboardClient;
import org.wso2.carbon.bam.toolbox.deployer.client.HiveScriptStoreClient;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMComponentNotFoundException;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.bam.toolbox.deployer.util.DashBoardTabDTO;
import org.wso2.carbon.bam.toolbox.deployer.util.ToolBoxDTO;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BAMArtifactDeployerManager {

    private static BAMArtifactDeployerManager instance;

    private static final Log log = LogFactory.getLog(BAMArtifactDeployerManager.class);

    private static final String gadgetsPath = "/repository/dashboards/gadgets";

    private BAMArtifactDeployerManager() {

    }

    public static BAMArtifactDeployerManager getInstance() throws BAMToolboxDeploymentException {
        if (instance == null) {
            instance = new BAMArtifactDeployerManager();
        }
        return instance;
    }

    private void deployScripts(ToolBoxDTO toolBoxDTO) throws BAMToolboxDeploymentException {
        String scriptParent = toolBoxDTO.getScriptsParentDirectory();
        ArrayList<String> scriptNameWithId = new ArrayList<String>();
        for (String aScript : toolBoxDTO.getScriptNames()) {
            String path = scriptParent + File.separator + aScript;
            File scriptFile = new File(path);

            String scriptName = scriptFile.getName();
            scriptName = scriptName.split("\\.")[0];
            String content = getContent(scriptFile);
            scriptName = scriptName + "_" + getRandomArtifactId();
            scriptNameWithId.add(scriptName);
            try {
                HiveScriptStoreClient scriptStoreClient = HiveScriptStoreClient.getInstance();
                scriptStoreClient.saveHiveScript(scriptName, content, null);
            } catch (BAMComponentNotFoundException e) {
                log.error(e.getMessage() + "Skipping deploying Hive scripts..");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        toolBoxDTO.setScriptNames(scriptNameWithId);

    }

    private static int getRandomArtifactId() {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(1000);
    }

    private void deployGadget(ToolBoxDTO toolBoxDTO, String username) {
        try {
            DashboardClient dashboardClient = DashboardClient.getInstance();
            for (DashBoardTabDTO tabDTO : toolBoxDTO.getDashboardTabs()) {
                int tabID = dashboardClient.addTab(username, tabDTO.getTabName());
                tabDTO.setTabId(tabID);
                for (String aGadget : tabDTO.getGadgets()) {
                    dashboardClient.addNewGadget(username, String.valueOf(tabID),
                            "/registry/resource/_system/config/repository/dashboards/gadgets/" + aGadget);
                }
            }
        } catch (BAMComponentNotFoundException e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.warn("Deploying gadget is not successful.. Skipping deploying script..");
        }

    }


    private void undeployScript(String scriptName) {
        HiveScriptStoreClient scriptStoreClient = null;
        try {
            scriptStoreClient = HiveScriptStoreClient.getInstance();
            scriptStoreClient.deleteScript(scriptName);
        } catch (BAMComponentNotFoundException e) {
            log.warn(e.getMessage() + " Skipping un deploying scripts.");
        }

    }

    private void undeployTab(int tabId, String username) {
        DashboardClient dashboardClient = null;
        try {
            dashboardClient = DashboardClient.getInstance();
            dashboardClient.removeTab(username, tabId);
        } catch (BAMComponentNotFoundException e) {
            log.warn(e.getMessage() + " Skipping undeploying tab :" + tabId);
        }

    }

    public void deploy(ToolBoxDTO toolBoxDTO, int tenantId, String username) throws BAMToolboxDeploymentException {
        if (canDeployScripts()) {
            deployScripts(toolBoxDTO);
        }

        if (canDeployGadgets()) {
            transferGadgetsFilesToRegistry(new File(toolBoxDTO.getGagetsParentDirectory()), tenantId);
            deployGadget(toolBoxDTO, username);
            deployJaggeryApps(toolBoxDTO);
        }

    }


    private void deployJaggeryApps(ToolBoxDTO toolBoxDTO) {
        String jaggeryDeployementDir = toolBoxDTO.getHotDeploymentRootDir() +
                File.separator + BAMToolBoxDeployerConstants.JAGGERY_DEPLOYMENT_DIR;
        ArrayList<String> files = getFilesInDir(toolBoxDTO.getJaggeryAppParentDirectory());
        for (String aJaggeryApp : files) {
            String srcFile = toolBoxDTO.getJaggeryAppParentDirectory() + File.separator + aJaggeryApp;
            InputStream in = null;
            try {
                in = new FileInputStream(srcFile);

                OutputStream out = new FileOutputStream(jaggeryDeployementDir + File.separator + aJaggeryApp);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                log.error(e);
            } catch (IOException e) {
                log.error(e);
            }
        }
    }

    private ArrayList<String> getFilesInDir(String dirPath) {
        File dir = new File(dirPath);
        ArrayList<String> files = new ArrayList<String>();

        String[] children = dir.list();
        if (null != children) {
            for (String aChildren : children) {
                if (!new File(aChildren).isDirectory()) {
                    files.add(aChildren);
                }
            }
        }
        return files;
    }

    private boolean canDeployScripts() {
        try {
            Class serviceClass = Class.forName("org.wso2.carbon.analytics.hive.web.HiveScriptStoreService");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean canDeployGadgets() {
        try {
            Class serviceClass = Class.forName("org.wso2.carbon.dashboard.DashboardDSService");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private void transferGadgetsFilesToRegistry(File rootDirectory, int tenantId) throws BAMToolboxDeploymentException {
        try {
            // Storing the root path for future reference
            String rootPath = rootDirectory.getAbsolutePath();

            Registry registry = ServiceHolder.getRegistry(tenantId);

            // Creating the default gadget collection resource

            try {
                registry.beginTransaction();
                if (!registry.resourceExists(gadgetsPath)) {
                    registry.put(gadgetsPath, registry.newCollection());
                }
                transferDirectoryContentToRegistry(rootDirectory, registry, rootPath, tenantId);
                registry.commitTransaction();
            } catch (Exception e) {
                registry.rollbackTransaction();
                log.error(e.getMessage(), e);
            }


        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }
    }

    private static void transferDirectoryContentToRegistry(File rootDirectory, Registry registry,
                                                           String rootPath, int tenantId)
            throws FileNotFoundException, BAMToolboxDeploymentException {

        try {
            File[] filesAndDirs = rootDirectory.listFiles();
            List<File> filesDirs = Arrays.asList(filesAndDirs);

            for (File file : filesDirs) {

                if (!file.isFile()) {
                    // This is a Directory add a new collection
                    // This path is used to store the file resource under registry
                    String directoryRegistryPath =
                            gadgetsPath + file.getAbsolutePath()
                                    .substring(rootPath.length()).replaceAll("[/\\\\]+", "/");
                    Collection newCollection = registry.newCollection();
                    registry.put(directoryRegistryPath, newCollection);

                    // recurse
                    transferDirectoryContentToRegistry(file, registry, rootPath, tenantId);
                } else {
                    // Add this to registry
                    addToRegistry(rootPath, file, tenantId);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }

    }

    private static void addToRegistry(String rootPath, File file, int tenantId) throws BAMToolboxDeploymentException {
        try {
            Registry registry = ServiceHolder.getRegistry(tenantId);

            // This path is used to store the file resource under registry
            String fileRegistryPath =
                    gadgetsPath + file.getAbsolutePath().substring(rootPath.length())
                            .replaceAll("[/\\\\]+", "/");

            // Adding the file to the Registry

            Resource fileResource = registry.newResource();
            String mediaType = MediaTypesUtils.getMediaType(file.getAbsolutePath());
            if (mediaType.equals("application/xml")) {
                fileResource.setMediaType("application/vnd.wso2-gadget+xml");
            } else {
                fileResource.setMediaType(mediaType);
            }
            fileResource.setContentStream(new FileInputStream(file));
            registry.put(fileRegistryPath, fileResource);

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }
    }

    public void undeploy(ToolBoxDTO toolBoxDTO, String username) throws BAMToolboxDeploymentException {
        if (canDeployScripts()) {
            for (String aScript : toolBoxDTO.getScriptNames()) {
                undeployScript(aScript);
            }
        }

        if (canDeployGadgets()) {
            for (DashBoardTabDTO tabDTO : toolBoxDTO.getDashboardTabs()) {
                int tabId = tabDTO.getTabId();
                undeployTab(tabId, username);
            }
        }
    }


    private String getContent(File file) throws BAMToolboxDeploymentException {
        if (!file.isDirectory()) {
            try {
                FileInputStream fstream = new FileInputStream(file);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String content = "";
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    content += strLine;
                }
                in.close();
                return content;
            } catch (FileNotFoundException e) {
                log.error("File not found " + file.getAbsolutePath(), e);
                throw new BAMToolboxDeploymentException("File not found " + file.getAbsolutePath(), e);
            } catch (IOException e) {
                log.error("Exception while reading the file: " + file.getAbsolutePath(), e);
                throw new BAMToolboxDeploymentException("Exception while reading the file: " + file.getAbsolutePath(), e);
            }
        } else {
            return "";
        }
    }
}
