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
package org.wso2.bam.integration.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterGroups;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.bam.integration.test.common.events.EventException;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.utils.FileManipulator;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Prepares the WSO2 BAM for test runs, starts the server, and stops the server after
 * test runs
 */
public class BAMTestServerManager extends TestServerManager {
    private static final Log log = LogFactory.getLog(BAMTestServerManager.class);

    @Override
    @BeforeSuite(timeOut = 120000, description = "Starting BAM server")
    public String startServer() throws IOException {
        String carbonHome = super.startServer();
        System.setProperty("carbon.home", carbonHome);
        return carbonHome;
    }

    @Test(groups = {"wso2.bam.restart"}, dependsOnGroups = {"wso2.bam.test"})
    public void restartServer() throws Exception {
        log.info("Stopping BAM Sever..");
        super.stopServer();
        try {
            log.info("Restarting BAM Server");
            Thread.sleep(5 * 1000);
        } catch (InterruptedException ignored) {

        }
        super.startServer();
    }

    @Override
    @AfterSuite(timeOut = 120000, description = "Shutting down server")
    public void stopServer() throws Exception {
        super.stopServer();

    }

    protected void copyArtifacts(String carbonHome) throws IOException {
// There is nothing to copy
    }

    private void copySampleFile(String sourcePath, String destPath) {
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        try {
            FileManipulator.copyFile(sourceFile, destFile);
        } catch (IOException e) {
            log.error("Error while copying the HelloWorld sample into BAMServer", e);
        }
    }

    private String computeSourcePath(String sampleFolder, String fileName) {
        String samplesDir = System.getProperty("samples.dir");
        return samplesDir + File.separator + sampleFolder + File.separator
                + "target" + File.separator + fileName;
        //return "";
    }

    private String computeDestPath(String carbonHome,
                                   String deploymentFolder,
                                   String fileName) {
        // First create the deployment folder in the server if it doesn't already exist
        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                + "deployment" + File.separator + "server" + File.separator +
                deploymentFolder;
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            log.error("Error while creating the deployment folder : " + deploymentPath);
        }
        return deploymentPath + File.separator + fileName;
    }
}
