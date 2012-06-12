/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.ms.integration.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

/**
 * Prepares the WSO2 MS for test runs, starts the server, and stops the server after
 * test runs
 */
public class MSTestServerxManager extends TestServerManager {

    private static final Log log = LogFactory.getLog(MSTestServerxManager.class);

    @Override
    @BeforeSuite(timeOut = 300000)
    public String startServer() throws IOException {
        String carbonHome = super.startServer();
        System.setProperty("carbon.home", carbonHome);
        return carbonHome;
    }

    @Override
    @AfterSuite(timeOut = 60000)
    public void stopServer() throws Exception {
        super.stopServer();
    }

    protected void copyArtifacts(String carbonHome) throws IOException {
    	 log.info("*******************COPY sample  : " + carbonHome);
         
        // Mashup sample for Email Host Object
        String fileName = "emailTest.js";
        String sourcePath = computeSourcePath(fileName);
        String destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);
        log.info("*******************COPY sample  : sourcePath " + sourcePath);
        log.info("*******************COPY sample  : destinationPath " + destinationPath);
        // Mashup sample for File Host Object
        fileName = "fileTest.js";
        sourcePath = computeSourcePath(fileName);
        destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);

        /* Mashup sample for Registry Host Object
        fileName = "registryTest.js";
        sourcePath = computeSourcePath(fileName);
        destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);*/

        // Mashup sample for Session Host Object
        fileName = "sessionTest.js";
        sourcePath = computeSourcePath(fileName);
        destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);

        // Mashup sample for Request Host Object
        fileName = "requestTest.js";
        sourcePath = computeSourcePath(fileName);
        destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);

        // Mashup sample for Scrapper Host Object
        fileName = "scrapperTest.js";
        sourcePath = computeSourcePath(fileName);
        destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);

        // Mashup sample for System Host Object
        fileName = "systemTest.js";
        sourcePath = computeSourcePath(fileName);
        destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);
        fileName = "concatscript.js";
        sourcePath = computeSourcePath(fileName);
        destinationPath = computeDestPath(carbonHome,
                                          "systemTest.resources" + File.separator + fileName);
        copySampleFile(sourcePath, destinationPath);

        // Mashup sample for HttpClient Host Object
        fileName = "httpClientTest.js";
        sourcePath = computeSourcePath(fileName);
        destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);

    }

    private void copySampleFile(String sourcePath, String destPath) {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        try {
            FileManipulator.copyFile(sourceFile, destFile);
        } catch (IOException e) {
            log.error("Error while copying the sample into MashupServer", e);
        }
    }

    private String computeSourcePath(String fileName) {
        String samplesDir = System.getProperty("samples.dir");
        return samplesDir + File.separator + fileName;
    }

    private String computeDestPath(String carbonHome, String fileName) {
        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                                + "deployment" + File.separator + "server" + File.separator
                                + "jsservices" + File.separator + "admin";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            log.error("Error while creating the deployment folder : " + deploymentPath);
        }
        return deploymentPath + File.separator + fileName;
    }
}
