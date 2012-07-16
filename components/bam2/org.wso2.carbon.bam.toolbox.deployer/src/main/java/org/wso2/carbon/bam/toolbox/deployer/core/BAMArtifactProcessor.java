package org.wso2.carbon.bam.toolbox.deployer.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.BAMToolBoxDeployerConstants;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.bam.toolbox.deployer.util.DashBoardTabDTO;
import org.wso2.carbon.bam.toolbox.deployer.util.JasperTabDTO;
import org.wso2.carbon.bam.toolbox.deployer.util.ToolBoxDTO;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
public class BAMArtifactProcessor {
    private static BAMArtifactProcessor instance;
    private static final Log log = LogFactory.getLog(BAMArtifactProcessor.class);

    private BAMArtifactProcessor() {
        //to avoid instantiation
    }

    public static BAMArtifactProcessor getInstance() {
        if (null == instance) {
            instance = new BAMArtifactProcessor();
        }
        return instance;
    }

    public String extractBAMArtifact(String bamArtifact, String destFolder)
            throws BAMToolboxDeploymentException {
        return unzipFolder(bamArtifact, destFolder);
    }

    private String unzipFolder(String zipFile, String destFolder)
            throws BAMToolboxDeploymentException {
        try {
            ZipFile bamArtifact = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> zipEnum = bamArtifact.entries();

            while (zipEnum.hasMoreElements()) {
                ZipEntry item = (ZipEntry) zipEnum.nextElement();

                if (item.isDirectory()) {
                    File newdir = new File(destFolder + File.separator + item.getName());
                    newdir.mkdir();
                } else {
                    String newfilePath = destFolder + File.separator + item.getName();
                    File newFile = new File(newfilePath);
                    if (!newFile.getParentFile().exists()) {
                        newFile.getParentFile().mkdirs();
                    }

                    InputStream is = bamArtifact.getInputStream(item);
                    FileOutputStream fos = new FileOutputStream(newfilePath);
                    int ch;
                    while ((ch = is.read()) != -1) {
                        fos.write(ch);
                    }
                    is.close();
                    fos.close();
                }
            }
            bamArtifact.close();
            File file = new File(bamArtifact.getName());
            return destFolder + File.separator + file.getName().replace("." + BAMToolBoxDeployerConstants.BAM_ARTIFACT_EXT, "");
        } catch (Exception e) {
            log.error("Exception while extracting the BAM artifact:" + zipFile, e);
            throw new BAMToolboxDeploymentException("Exception while extracting the BAM artifact:" + zipFile, e);
        }
    }


    public ToolBoxDTO getToolBoxDTO(String barDir) throws BAMToolboxDeploymentException {
        return createDTO(barDir);
    }


    private ToolBoxDTO createDTO(String barDir) throws BAMToolboxDeploymentException {
        File file = new File(barDir);
        ToolBoxDTO toolBoxDTO;
        String name = file.getName();

        toolBoxDTO = new ToolBoxDTO(name);
        setScriptsNames(toolBoxDTO, barDir);
        setGadgetNames(toolBoxDTO, barDir);
        setJaggeryAppNames(toolBoxDTO, barDir);
        setStreamDefnNames(toolBoxDTO, barDir);

        String jasperDirectory = barDir + File.separator +
                BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                File.separator + BAMToolBoxDeployerConstants.JASPER_DIR;

        if (new File(jasperDirectory).exists()) {
            setJasperResourceNames(toolBoxDTO, barDir);
        }
        return toolBoxDTO;
    }

    private void setScriptsNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {

        ArrayList<String> scriptNames = getFilesInDirectory(barDir + File.separator + BAMToolBoxDeployerConstants.SCRIPTS_DIR);
        if (scriptNames.size() == 0) {
            toolBoxDTO.setScriptsParentDirectory(null);
            log.warn("No scripts available in the specified directory");
        } else {
            toolBoxDTO.setScriptsParentDirectory(barDir + File.separator + BAMToolBoxDeployerConstants.SCRIPTS_DIR);
            toolBoxDTO.setScriptNames(scriptNames);
        }
    }

    private void setStreamDefnNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {
        ArrayList<String> streamDefNames = getFilesInDirectory(barDir + File.separator + BAMToolBoxDeployerConstants.STREAM_DEFN_DIR);
        if (streamDefNames.size() == 0) {
            toolBoxDTO.setStreamDefnParentDirectory(null);
            log.warn("No event streams found in the specified directory");
        } else {
            toolBoxDTO.setStreamDefnParentDirectory(barDir + File.separator + BAMToolBoxDeployerConstants.STREAM_DEFN_DIR);
            toolBoxDTO.setEvenStreamDefs(streamDefNames);
        }
    }


    private void setJaggeryAppNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {
        if (null != toolBoxDTO.getGagetsParentDirectory()) {
            File jaggeryDir = new File(barDir);
            if (jaggeryDir.exists()) {
                toolBoxDTO.setJaggeryAppParentDirectory(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR
                        + File.separator + BAMToolBoxDeployerConstants.JAGGERY_DIR);
            } else {
                log.warn("No jaggery artifacts found..");
                toolBoxDTO.setJaggeryAppParentDirectory(null);
            }
        } else {
            log.warn("No gadgets dir found, and skipping jaggery artifacts");
            toolBoxDTO.setJaggeryAppParentDirectory(null);
        }
    }

    private void setGadgetNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {
        if (new File(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR).exists()) {
            toolBoxDTO.setGagetsParentDirectory(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR
                    + File.separator + BAMToolBoxDeployerConstants.GADGETS_DIR);
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                        File.separator + BAMToolBoxDeployerConstants.GADGET_META_FILE));
                setTabNames(toolBoxDTO, properties);
                int tabIndex = 1;
                for (DashBoardTabDTO aTab : toolBoxDTO.getDashboardTabs()) {
                    String value = properties.getProperty(BAMToolBoxDeployerConstants.GADGET_XMLS_PREFIX + tabIndex + "." + BAMToolBoxDeployerConstants.GADGET_XMLS_SUFFIX);
                    setGadgetNamesForTab(aTab, value);
                    tabIndex++;
                }
            } catch (FileNotFoundException e) {
                log.warn("No " + BAMToolBoxDeployerConstants.GADGET_META_FILE +
                        " found in dir:" + barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR, e);
                throw new BAMToolboxDeploymentException("No " + BAMToolBoxDeployerConstants.GADGET_META_FILE +
                        " found in dir:" + barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR, e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new BAMToolboxDeploymentException(e.getMessage(), e);
            }
        } else {
            toolBoxDTO.setGagetsParentDirectory(null);
            toolBoxDTO.setDashboardTabs(new ArrayList<DashBoardTabDTO>());
        }
    }

    private void setJasperResourceNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {
        toolBoxDTO.setJasperParentDirectory(barDir + File.separator +
                BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                File.separator + BAMToolBoxDeployerConstants.JASPER_DIR);
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(barDir + File.separator +
                    BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                    File.separator +
                    BAMToolBoxDeployerConstants.JASPER_META_FILE));
            setJasperTabNames(toolBoxDTO, properties);
            int tabIndex = 1;
            for (JasperTabDTO aTab : toolBoxDTO.getJasperTabs()) {
                String value = properties.getProperty(
                        BAMToolBoxDeployerConstants.GADGET_XMLS_PREFIX + tabIndex + "." +
                                BAMToolBoxDeployerConstants.JRXML_SUFFIX);
                aTab.setTabId(tabIndex);
                setJRXMLForTab(aTab, value);
                tabIndex++;
            }

            toolBoxDTO.setDataSource(properties.getProperty(BAMToolBoxDeployerConstants.DATASOURCE));
            toolBoxDTO.setDataSourceConfiguration(barDir + File.separator +
                    BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                    File.separator +
                    properties.getProperty(BAMToolBoxDeployerConstants.
                            DATASOURCE_CONFIGURATION));
        } catch (FileNotFoundException e) {
            log.error("No " + BAMToolBoxDeployerConstants.JASPER_META_FILE +
                    " found in dir:" + barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR, e);
            throw new BAMToolboxDeploymentException("No " + BAMToolBoxDeployerConstants.JASPER_META_FILE +
                    " found in dir:" + barDir + File.separator
                    + BAMToolBoxDeployerConstants.DASHBOARD_DIR, e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }
    }

    private void setJasperTabNames(ToolBoxDTO toolBoxDTO, Properties props)
            throws BAMToolboxDeploymentException {
        String tabs = props.getProperty(BAMToolBoxDeployerConstants.TAB_NAMES).trim();
        String[] tabNames = tabs.split(",");
        if (tabNames == null || tabNames.length == 0) {
            throw new BAMToolboxDeploymentException("Invalid bar artifact. No tab names found in dashboard.properties");
        } else {
            boolean valid = false;
            for (String aTabName : tabNames) {
                if (!aTabName.trim().equals("")) {
                    valid = true;
                    JasperTabDTO tabDTO = new JasperTabDTO();
                    tabDTO.setTabName(aTabName.trim());
                    toolBoxDTO.addJasperTab(tabDTO);
                }
            }
            if (!valid) {
                throw new BAMToolboxDeploymentException("Invalid bar artifact. No tab names " +
                        "found in dashboard.properties");
            }
        }
    }

    private void setTabNames(ToolBoxDTO toolBoxDTO, Properties props)
            throws BAMToolboxDeploymentException {
        String tabs = props.getProperty(BAMToolBoxDeployerConstants.TAB_NAMES).trim();
        String[] tabNames = tabs.split(",");
        if (tabNames == null || tabNames.length == 0) {
            throw new BAMToolboxDeploymentException("Invalid bar artifact. No tab names found in dashboard.properties");
        } else {
            boolean valid = false;
            for (String aTabName : tabNames) {
                if (!aTabName.trim().equals("")) {
                    valid = true;
                    DashBoardTabDTO tabDTO = new DashBoardTabDTO();
                    tabDTO.setTabName(aTabName.trim());
                    toolBoxDTO.addDashboradTab(tabDTO);
                }
            }
            if (!valid) {
                throw new BAMToolboxDeploymentException("Invalid bar artifact. No tab names " +
                        "found in dashboard.properties");
            }
        }
    }

    private void setGadgetNamesForTab(DashBoardTabDTO dashBoardTabDTO, String gagetXmlsNames)
            throws BAMToolboxDeploymentException {
        if (gagetXmlsNames != null && !gagetXmlsNames.trim().equals("")) {
            String[] gadgets = gagetXmlsNames.split(",");
            if (gadgets == null || gadgets.length == 0) {
                throw new BAMToolboxDeploymentException("Invalid bar artifact. No gadget names found for tab :"
                        + dashBoardTabDTO + " in dashboard.properties");
            } else {
                boolean valid = false;
                for (String aGadget : gadgets) {
                    if (!aGadget.trim().equals("")) {
                        valid = true;
                        dashBoardTabDTO.addGadget(aGadget.trim());
                    }
                }
                if (!valid) {
                    throw new BAMToolboxDeploymentException("Invalid bar artifact. No gadget names found for tab :"
                            + dashBoardTabDTO + " in dashboard.properties");
                }
            }
        } else {
            throw new BAMToolboxDeploymentException("No jrxml files specified for tab :" + dashBoardTabDTO.getTabName());
        }
    }

    private void setJRXMLForTab(JasperTabDTO jasperTabDTO, String jrxmlFileName)
            throws BAMToolboxDeploymentException {
        if (jrxmlFileName != null && !jrxmlFileName.trim().equals("")) {
            String jrxmlFile = jrxmlFileName.trim();
            jasperTabDTO.setJrxmlFileName(jrxmlFile);
        } else {
            throw new BAMToolboxDeploymentException("No jrxml file specified for tab :" +
                    jasperTabDTO.getTabName());
        }
    }


    private ArrayList<String> getFilesInDirectory(String dirPath)
            throws BAMToolboxDeploymentException {
        File dir = new File(dirPath);
        ArrayList<String> files = new ArrayList<String>();

        if (dir.exists()) {
            String[] children = dir.list();
            if (null != children) {
                for (String aChildren : children) {
                    if (!new File(aChildren).isDirectory()) {
                        files.add(aChildren);
                    }
                }
            }
        }
        return files;
    }
}

