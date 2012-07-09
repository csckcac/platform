/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.carbon.bam.toolbox.deployer.util;

import java.util.ArrayList;


public class ToolBoxDTO {
    private String name;
    private ArrayList<String> scriptNames;
    private ArrayList<DashBoardTabDTO> dashboardTabs;
    private ArrayList<JasperTabDTO> jasperTabs;
    private ArrayList<String> evenStreamDefs;
    private String scriptsParentDirectory;
    private String gagetsParentDirectory;
    private String jasperParentDirectory;
    private String jaggeryAppParentDirectory;
    private String streamDefnParentDirectory;
    private String hotDeploymentRootDir;
    private String datasource;
    private String dsConfiguration;

    public ToolBoxDTO(String name) {
        this.name = name;
        scriptNames = new ArrayList<String>();
        dashboardTabs = new ArrayList<DashBoardTabDTO>();
        jasperTabs = new ArrayList<JasperTabDTO>();
        evenStreamDefs = new ArrayList<String>();
        datasource = "";
    }

    public String getName() {
        return name;
    }

    public ArrayList<DashBoardTabDTO> getDashboardTabs() {
        return dashboardTabs;
    }

    public void setDashboardTabs(ArrayList<DashBoardTabDTO> dashboardTabs) {
        this.dashboardTabs = dashboardTabs;
    }

    public ArrayList<JasperTabDTO> getJasperTabs() {
        return jasperTabs;
    }

    public void setJasperTabs(ArrayList<JasperTabDTO> jasperTabs) {
        this.jasperTabs = jasperTabs;
    }

    public ArrayList<String> getScriptNames() {
        return scriptNames;
    }


    public void addScriptName(String scriptName) {
        this.scriptNames.add(scriptName);
    }


    public void setScriptNames(ArrayList<String> scriptNames) {
        this.scriptNames = scriptNames;
    }


    public String getScriptsParentDirectory() {
        return scriptsParentDirectory;
    }

    public void setScriptsParentDirectory(String scriptsParentDirectory) {
        this.scriptsParentDirectory = scriptsParentDirectory;
    }

    public String getGagetsParentDirectory() {
        return gagetsParentDirectory;
    }

    public void setGagetsParentDirectory(String gagetsParentDirectory) {
        this.gagetsParentDirectory = gagetsParentDirectory;
    }

    public void addGadgets(int tabId, String gadgetName) {
        for (DashBoardTabDTO aTab : dashboardTabs) {
            if (aTab.getTabId() == tabId) {
                aTab.addGadget(gadgetName);
            }
        }
    }

    public void addDashboradTab(DashBoardTabDTO dashBoardTabDTO) {
        this.dashboardTabs.add(dashBoardTabDTO);
    }

    public void addJasperTab(JasperTabDTO jasperTabDTO) {
        this.jasperTabs.add(jasperTabDTO);
    }

    public void addGadgetsInTabIndex(int index, String gadgetName) {
        dashboardTabs.get(index).addGadget(gadgetName);
    }

    public void replaceGadgetName(String oldName, String newName) {
        for (DashBoardTabDTO tabDTO : this.dashboardTabs) {
            ArrayList<String> newGadgetNames = new ArrayList<String>();
            for (String aGadget : tabDTO.getGadgets()) {
                if (aGadget.equals(oldName)) {
                    newGadgetNames.add(newName);
                } else {
                    newGadgetNames.add(oldName);
                }
            }
        }
    }

    public String getJaggeryAppParentDirectory() {
        return jaggeryAppParentDirectory;
    }

    public void setJaggeryAppParentDirectory(String jaggeryAppParentDirectory) {
        this.jaggeryAppParentDirectory = jaggeryAppParentDirectory;
    }

    public String getHotDeploymentRootDir() {
        return hotDeploymentRootDir;
    }

    public void setHotDeploymentRootDir(String hotDeploymentRootDir) {
        this.hotDeploymentRootDir = hotDeploymentRootDir;
    }

    public String getJasperParentDirectory() {
        return jasperParentDirectory;
    }

    public void setJasperParentDirectory(String jasperParentDirectory) {
        this.jasperParentDirectory = jasperParentDirectory;
    }

    public void setDataSource(String dataSourceName) {
        this.datasource = dataSourceName;
    }

    public String getDataSource() {
        if(null == datasource) return "";
        return datasource;
    }

    public void setDataSourceConfiguration(String dsConfiguration) {
        this.dsConfiguration = dsConfiguration;
    }

    public String getDataSourceConfiguration() {
        if(null == dsConfiguration) return "";
        return dsConfiguration;
    }

    public ArrayList<String> getEvenStreamDefs() {
        return evenStreamDefs;
    }

    public void setEvenStreamDefs(ArrayList<String> evenStreamDefs) {
        this.evenStreamDefs = evenStreamDefs;
    }

    public String getStreamDefnParentDirectory() {
        return streamDefnParentDirectory;
    }

    public void setStreamDefnParentDirectory(String streamDefnParentDirectory) {
        this.streamDefnParentDirectory = streamDefnParentDirectory;
    }
}
