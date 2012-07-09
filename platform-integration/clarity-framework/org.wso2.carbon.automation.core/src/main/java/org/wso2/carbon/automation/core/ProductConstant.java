/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.automation.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkSettings;

import java.io.File;

public class ProductConstant {
    private static final Log log = LogFactory.getLog(ProductConstant.class);

    public static final String APP_SERVER_NAME = "AS";
    public static final String ESB_SERVER_NAME = "ESB";
    public static final String BPS_SERVER_NAME = "BPS";
    public static final String DSS_SERVER_NAME = "DSS";
    public static final String MB_SERVER_NAME = "MB";
    public static final String CEP_SERVER_NAME = "CEP";
    public static final String MS_SERVER_NAME = "MS";
    public static final String GS_SERVER_NAME = "GS";
    public static final String BRS_SERVER_NAME = "BRS";
    public static final String GREG_SERVER_NAME = "GREG";
    public static final String BAM_SERVER_NAME = "BAM";
    public static final String IS_SERVER_NAME = "IS";
    public static final String AM_SERVER_NAME = "AM";
    public static final String  AXIS2_SERVER_NAME = "AXIS2";
    public static final String LB_SERVER_NAME = "LB";
    public static final String CLUSTER = "CLUSTER";
    public static final String MANAGER_SERVER_NAME = "MANAGER";
    public static final String MULTITENANCY_FREE_PLAN = "Demo";
    public static final String MULTITENANCY_SMALL_PLAN = "SMB";
    public static final String MULTITENANCY_MEDIUM_PLAN = "Professional";
    public static final String MULTITENANCY_LARGE_PLAN = "Enterprise";
    public static final String FIREFOX_BROWSER = "firefox";
    public static final String CHROME_BROWSER = "chrome";
    public static final String IE_BROWSER = "ie";
    public static final String HTML_UNIT_DRIVER = "htmlUnit";
    public static final int ADMIN_USER_ID = 0;


    public static EnvironmentBuilder env;
    public static FrameworkSettings framework;
    public static String SYSTEM_TEST_RESOURCE_LOCATION = getSystemResourceLocation();
    public static String REPORT_LOCATION = getReportLocation();
    public static String REPORT_REPOSITORY = REPORT_LOCATION + "reports" + File.separator;


    public static String getResourceLocations(String productName) {
        return SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "artifacts" +
               File.separator + productName;
    }

    public static String getModuleClientPath() {
        return SYSTEM_TEST_RESOURCE_LOCATION + File.separator +
               "client";
    }

    public static String getSecurityScenarios() {
        return SYSTEM_TEST_RESOURCE_LOCATION + File.separator + "security" + File.separator +
               "policies";
    }

    /**
     * construct the resource file path by checking the OS tests are running.
     * Need to replace string path with correct forward or backward slashes as we set the system property with
     * forward slash.
     *
     * @return resource path
     */
    public static String getSystemResourceLocation() {
        String resourceLocation;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            resourceLocation = System.getProperty("framework.resource.location").replace("/", "\\");
        } else {
            resourceLocation = System.getProperty("framework.resource.location").replace("/", "/");
        }
        return resourceLocation;
    }

    /**
     * construct the report path by check OS. TestNG reports will be written to the reportPath.
     * Need to replace string path with correct forward or backward slashes as we set the system property with
     * forward slash.
     *
     * @return report location
     */
    public static String getReportLocation() {
        String reportLocation;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
//            reportLocation = System.getProperty("framework.resource.location").replace("/", "\\").
//                    substring(0, SYSTEM_TEST_RESOURCE_LOCATION.indexOf("core\\org.wso2.automation.platform"));
//
            reportLocation = (System.getProperty("basedir", ".")) + File.separator + "target";

        } else {
//            reportLocation = System.getProperty("framework.resource.location").replace("/", "/").
//                    substring(0, SYSTEM_TEST_RESOURCE_LOCATION.indexOf("core/org.wso2.automation.platform"));
//
            reportLocation = (System.getProperty("basedir", ".")) + File.separator + "target";
        }
        return reportLocation;
    }


    public static String getCarbonHome(String product) {
        EnvironmentBuilder env = new EnvironmentBuilder();
        String deploymentHome = env.getFrameworkSettings().getEnvironmentVariables().getDeploymentFrameworkPath() +
                                File.separator + "SNAPSHOT" + File.separator;
        File deploymentDir = new File(deploymentHome);
        String distributionPrefix;

        if (APP_SERVER_NAME.equalsIgnoreCase(product)) {
            distributionPrefix = "wso2as-";
        } else if (ESB_SERVER_NAME.equalsIgnoreCase(product)) {
            distributionPrefix = "wso2esb-";
        } else if (BPS_SERVER_NAME.equalsIgnoreCase(product)) {
            distributionPrefix = "wso2bps-";
        } else if (DSS_SERVER_NAME.equalsIgnoreCase(product)) {
            distributionPrefix = "wso2dataservices-";
        } else if (MB_SERVER_NAME.equalsIgnoreCase(product)) {
            distributionPrefix = "wso2mb-";
        } else if (CEP_SERVER_NAME.equalsIgnoreCase(product)) {
            distributionPrefix = "wso2cep-";
        } else if (GS_SERVER_NAME.equalsIgnoreCase(product)) {
            distributionPrefix = "wso2gs-";
        } else if (BRS_SERVER_NAME.equalsIgnoreCase(product)) {
            distributionPrefix = "wso2brs-";
        } else if (GREG_SERVER_NAME.equalsIgnoreCase(product)) {
            distributionPrefix = "wso2greg-";
        } else if (MANAGER_SERVER_NAME.equalsIgnoreCase(product)) {
            distributionPrefix = "wso2manager-";
        } else {
            log.warn("Invalid Product Name. Configure product.list in framework.setting correctly");
            return null;
        }

        String[] folderList = deploymentDir.list();
        for (String folderName : folderList) {
            if (folderName.contains(distributionPrefix)) {
                deploymentHome = deploymentHome + folderName;
                break;
            }
        }
        return deploymentHome;
    }

}