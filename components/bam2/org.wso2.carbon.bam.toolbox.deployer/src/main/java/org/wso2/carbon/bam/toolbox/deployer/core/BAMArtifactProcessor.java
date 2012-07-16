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
        setJasperResourceNames(toolBoxDTO, barDir);
        return toolBoxDTO;
    }

    private void setScriptsNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {
        String analyticsDir = barDir + File.separator + BAMToolBoxDeployerConstants.SCRIPTS_DIR;
        if (new File(analyticsDir).exists()) {
            ArrayList<String> scriptNames = getFilesInDirectory(analyticsDir);
            int i = 0;
            for (String aFile : scriptNames) {
                if (aFile.equalsIgnoreCase(BAMToolBoxDeployerConstants.ANALYZERS_PROPERTIES_FILE)) {
                    scriptNames.remove(i);
                    break;
                }
                i++;
            }
            if (scriptNames.size() == 0) {
                toolBoxDTO.setScriptsParentDirectory(null);
                log.warn("No scripts available in the specified directory");
            } else {
                toolBoxDTO.setScriptsParentDirectory(analyticsDir);
                toolBoxDTO.setScriptNames(scriptNames);
                setCronForAnalyticScripts(toolBoxDTO, analyticsDir);
            }
        } else {
            log.warn("No Analytics found for toolbox :" + toolBoxDTO.getName());
        }
    }

    private void setCronForAnalyticScripts(ToolBoxDTO toolBoxDTO, String analyticDir) {
        String analyticsPropPath = analyticDir + File.separator + BAMToolBoxDeployerConstants.ANALYZERS_PROPERTIES_FILE;
        File analyticsProps = new File(analyticsPropPath);
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(analyticsProps));
            String scripts = props.getProperty(BAMToolBoxDeployerConstants.ANALYZER_SCRIPTS_VAR_NAME).trim();
            if (null != scripts && !scripts.equals("")) {
                String[] scriptNames = scripts.split(",");
                if (scriptNames == null || scriptNames.length == 0) {
                    throw new BAMToolboxDeploymentException("Invalid tbox artifact. No scripts found in analyzers.properties");
                } else {
                    boolean valid = false;
                    for (String aScriptVarName : scriptNames) {
                        if (!aScriptVarName.trim().equals("")) {
                            valid = true;
                            String scriptFileName = props.getProperty(BAMToolBoxDeployerConstants.ANALYZER_SCRIPT_PREFIX + "."
                                    + aScriptVarName.trim() + "." + BAMToolBoxDeployerConstants.ANALYZER_SCRIPT_FILE_NAME_SUFFIX);
                            if (null == scriptFileName || scriptFileName.equals("")) {
                                scriptFileName = aScriptVarName;
                            }
                            String cron = props.getProperty(BAMToolBoxDeployerConstants.ANALYZER_SCRIPT_PREFIX + "."
                                    + aScriptVarName.trim() + "." + BAMToolBoxDeployerConstants.ANALYZER_SCRIPT_CRON_SUFFIX);
                            if (null != cron && !cron.trim().equals("")) {
                                toolBoxDTO.setCronForScript(scriptFileName, cron);
                            } else {
                                log.warn("No cron are specified for script: " + scriptFileName);
                            }
                        }
                    }
                    if (!valid) {
                        toolBoxDTO.setGagetsParentDirectory(null);
                        log.error("Invalid tbox artifact. No tab names " +
                                "found in dashboard.properties");
                        throw new BAMToolboxDeploymentException("Invalid tbox artifact. No tab names " +
                                "found in dashboard.properties");

                    }
                }
            } else {
                toolBoxDTO.setGagetsParentDirectory(null);
                log.error("Invalid tbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                        "found in dashboard.properties");
                throw new BAMToolboxDeploymentException("Invalid tbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                        "found in dashboard.properties");
            }
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("No " + BAMToolBoxDeployerConstants.ANALYZERS_PROPERTIES_FILE + " file found. and all scripts won't be scheduled");
            }
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
        if (new File(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR
                + File.separator + BAMToolBoxDeployerConstants.GADGETS_DIR).exists()) {

            toolBoxDTO.setGagetsParentDirectory(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR
                    + File.separator + BAMToolBoxDeployerConstants.GADGETS_DIR);
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                        File.separator + BAMToolBoxDeployerConstants.GADGET_META_FILE));
                setTabAndGadgetNames(toolBoxDTO, properties);

            } catch (FileNotFoundException e) {
                log.warn("No " + BAMToolBoxDeployerConstants.GADGET_META_FILE +
                        " found in dir:" + barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR);
                log.error("Skipping installing dashboard artifacts..");
                toolBoxDTO.setGagetsParentDirectory(null);
                toolBoxDTO.setDashboardTabs(new ArrayList<DashBoardTabDTO>());
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
        String jasperDirectory = barDir + File.separator +
                BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                File.separator + BAMToolBoxDeployerConstants.JASPER_DIR;
        if (new File(jasperDirectory).exists()) {
            toolBoxDTO.setJasperParentDirectory(barDir + File.separator +
                    BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                    File.separator + BAMToolBoxDeployerConstants.JASPER_DIR);
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(barDir + File.separator +
                        BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                        File.separator +
                        BAMToolBoxDeployerConstants.JASPER_META_FILE));

                setJasperTabAndJrxmlNames(toolBoxDTO, properties);

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
        } else {
            toolBoxDTO.setJasperParentDirectory(null);
            toolBoxDTO.setJasperTabs(new ArrayList<JasperTabDTO>());
        }
    }

    private void setJasperTabAndJrxmlNames(ToolBoxDTO toolBoxDTO, Properties props)
            throws BAMToolboxDeploymentException {
        String tabs = props.getProperty(BAMToolBoxDeployerConstants.JASPER_TABS_VAR_NAME).trim();
        if (null != tabs && !tabs.equals("")) {
            String[] tabVarNames = tabs.split(",");
            if (tabVarNames == null || tabVarNames.length == 0) {
                throw new BAMToolboxDeploymentException("Invalid tbox artifact. No tabs found in jasper.properties");
            } else {
                boolean valid = false;
                int tabId = 1;
                for (String aTabVarName : tabVarNames) {
                    if (!aTabVarName.trim().equals("")) {
                        valid = true;
                        JasperTabDTO tabDTO = new JasperTabDTO();
                        String tabName = props.getProperty(BAMToolBoxDeployerConstants.JASPER_TAB_PREFIX + "."
                                + aTabVarName.trim() + "." + BAMToolBoxDeployerConstants.TAB_NAME_SUFFIX);
                        if (null == tabName || tabName.equals("")) {
                            tabName = aTabVarName;
                        }
                        tabDTO.setTabName(tabName);
                        toolBoxDTO.addJasperTab(tabDTO);

                        String jrxml = props.getProperty(BAMToolBoxDeployerConstants.JASPER_TAB_PREFIX + "."
                                + aTabVarName.trim() + "." + BAMToolBoxDeployerConstants.JRXML_NAME_SUFFIX);
                        if (null != jrxml && !jrxml.trim().equals("")) {
                            tabDTO.setTabId(tabId);
                            tabDTO.setJrxmlFileName(jrxml.trim());
                        } else {
                            log.warn("No gadgets are specified for tab: " + tabName);
                        }
                        tabId++;
                    }
                }
                if (!valid) {
                    toolBoxDTO.setJasperParentDirectory(null);
                    log.error("Invalid tbox artifact. No tab names " +
                            "found in jasper.properties");
                    throw new BAMToolboxDeploymentException("Invalid tbox artifact. No tab names " +
                            "found in jasper.properties");

                }
            }
        } else {
            toolBoxDTO.setJasperParentDirectory(null);
            log.error("Invalid tbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                    "found in jasper.properties");
            throw new BAMToolboxDeploymentException("Invalid tbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                    "found in jasper.properties");
        }
    }

    private void setTabAndGadgetNames(ToolBoxDTO toolBoxDTO, Properties props)
            throws BAMToolboxDeploymentException {
        String tabs = props.getProperty(BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME).trim();
        if (null != tabs && !tabs.equals("")) {
            String[] tabVarNames = tabs.split(",");
            if (tabVarNames == null || tabVarNames.length == 0) {
                throw new BAMToolboxDeploymentException("Invalid tbox artifact. No tabs found in dashboard.properties");
            } else {
                boolean valid = false;
                for (String aTabVarName : tabVarNames) {
                    if (!aTabVarName.trim().equals("")) {
                        valid = true;
                        DashBoardTabDTO tabDTO = new DashBoardTabDTO();
                        String tabName = props.getProperty(BAMToolBoxDeployerConstants.DASHBOARD_TAB_PREFIX + "."
                                + aTabVarName.trim() + "." + BAMToolBoxDeployerConstants.TAB_NAME_SUFFIX);
                        if (null == tabName || tabName.equals("")) {
                            tabName = aTabVarName;
                        }
                        tabDTO.setTabName(tabName);
                        toolBoxDTO.addDashboradTab(tabDTO);

                        String gadgets = props.getProperty(BAMToolBoxDeployerConstants.DASHBOARD_TAB_PREFIX + "."
                                + aTabVarName.trim() + "." + BAMToolBoxDeployerConstants.GADGET_NAME_SUFFIX);
                        if (null != gadgets && !gadgets.trim().equals("")) {
                            String[] gadgetNames = gadgets.trim().split(",");
                            for (String aGadget : gadgetNames) {
                                if (null != aGadget && !aGadget.trim().equals("")) {
                                    tabDTO.addGadget(aGadget.trim());
                                } else {
                                    log.warn("Empty gadget name found for tab: " + tabName);
                                }
                            }
                        } else {
                            log.warn("No gadgets are specified for tab: " + tabName);
                        }
                    }
                }
                if (!valid) {
                    toolBoxDTO.setGagetsParentDirectory(null);
                    log.error("Invalid tbox artifact. No tab names " +
                            "found in dashboard.properties");
                    throw new BAMToolboxDeploymentException("Invalid tbox artifact. No tab names " +
                            "found in dashboard.properties");

                }
            }
        } else {
            toolBoxDTO.setGagetsParentDirectory(null);
            log.error("Invalid tbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                    "found in dashboard.properties");
            throw new BAMToolboxDeploymentException("Invalid tbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                    "found in dashboard.properties");
        }
    }

    private void setGadgetNamesForTab(DashBoardTabDTO dashBoardTabDTO, String gagetXmlsNames)
            throws BAMToolboxDeploymentException {
        if (gagetXmlsNames != null && !gagetXmlsNames.trim().equals("")) {
            String[] gadgets = gagetXmlsNames.split(",");
            if (gadgets == null || gadgets.length == 0) {
                throw new BAMToolboxDeploymentException("Invalid tbox artifact. No gadget names found for tab :"
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
                    throw new BAMToolboxDeploymentException("Invalid tbox artifact. No gadget names found for tab :"
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

