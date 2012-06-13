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
    private String evenStreamDef;
    private String scriptsParentDirectory;
    private String gagetsParentDirectory;

    public ToolBoxDTO(String name) {
        this.name = name;
        scriptNames = new ArrayList<String>();
        dashboardTabs = new ArrayList<DashBoardTabDTO>();
        evenStreamDef = "";
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

    public ArrayList<String> getScriptNames() {
        return scriptNames;
    }



    public void addScriptName(String scriptName) {
        this.scriptNames.add(scriptName);
    }



    public String getEvenStreamDef() {
        return evenStreamDef;
    }

    public void setEvenStreamDef(String evenStreamDef) {
        this.evenStreamDef = evenStreamDef;
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

    public void addGadgets(int tabId, String gadgetName){
       for(DashBoardTabDTO aTab: dashboardTabs){
           if(aTab.getTabId()== tabId){
              aTab.addGadget(gadgetName);
           }
       }
    }

    public void addDashboradTab(DashBoardTabDTO dashBoardTabDTO){
      this.dashboardTabs.add(dashBoardTabDTO);
    }

    public void addGadgetsInTabIndex(int index, String gadgetName){
         dashboardTabs.get(index).addGadget(gadgetName);
    }

}
