/*
*  Copyright (c) 2005-2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.jaggery.integration.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.integration.framework.utils.ServerUtils;
import org.wso2.carbon.integration.framework.utils.TestUtil;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

/**
 * Prepares the Jaggery for test runs, starts the server, and stops the server after
 * test runs
 */
public class JaggeryTestServerManager extends TestServerManager {

    private static final Log log = LogFactory.getLog(JaggeryTestServerManager.class);

    @Override
    @BeforeSuite(timeOut = 300000)
    public String startServer() throws IOException {
    	
    	String carbonZip = System.getProperty("carbon.zip");
    	ServerUtils serverUtils = new ServerUtils();
        String carbonHome = serverUtils.setUpCarbonHome(carbonZip);
        String carbonFolderPath = "";
    	if(carbonHome != null) {
    		carbonFolderPath = carbonHome + File.separator + "carbon";
    	}
        TestUtil.copySecurityVerificationService(carbonFolderPath);
        copyArtifacts(carbonFolderPath);
        System.setProperty("JAGGERY_HOME", carbonHome);
        serverUtils.startServerUsingCarbonHome(carbonFolderPath, 0);
        FrameworkSettings.init();
        
        System.setProperty("carbon.home", carbonHome);
        
        // Copying jaggery configuration file
        String fileName = "jaggery.conf";
        String sourcePath = computeSourcePath(fileName);
        String destinationPath = computeDestPath(carbonFolderPath, fileName);
        copySampleFile(sourcePath, destinationPath);
        
        return carbonHome;
    }

    @Override
    @AfterSuite(timeOut = 60000)
    public void stopServer() throws Exception {
        super.stopServer();
    }

    protected void copyArtifacts(String carbonHome) throws IOException {

        //email host object
    	String fileName = "email.jag";
    	String sourcePath = computeSourcePath(fileName);
    	String destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);
        
        //database host object
    	fileName = "database.jag";
    	sourcePath = computeSourcePath(fileName);
    	destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);
        
        //feed host object
    	fileName = "feed.jag";
    	sourcePath = computeSourcePath(fileName);
    	destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);
        
        //file host object
    	fileName = "file.jag";
    	sourcePath = computeSourcePath(fileName);
    	destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);
        
        //sample file to read
    	fileName = "testfile.txt";
    	sourcePath = computeSourcePath(fileName);
    	destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);
        
        //log host object
    	fileName = "log.jag";
    	sourcePath = computeSourcePath(fileName);
    	destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);
        
        //wsrequest host object
    	fileName = "wsrequest.jag";
    	sourcePath = computeSourcePath(fileName);
    	destinationPath = computeDestPath(carbonHome, fileName);
        copySampleFile(sourcePath, destinationPath);
        
        //request object
    	fileName = "request.jag";
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
            log.error("Error while copying the sample into Jaggery server", e);
        }
    }

    private String computeSourcePath(String fileName) {
        String samplesDir = System.getProperty("samples.dir");
        return samplesDir + File.separator + fileName;
    }

    private String computeDestPath(String carbonHome, String fileName) {
        String deploymentPath = carbonHome + File.separator + "repository" + File.separator
                                + "deployment" + File.separator + "server" + File.separator
                                + "jaggeryapps" + File.separator + "testapp";
        File depFile = new File(deploymentPath);
        if (!depFile.exists() && !depFile.mkdir()) {
            log.error("Error while creating the deployment folder : " + deploymentPath);
        }
        return deploymentPath + File.separator + fileName;
    }
    
}
