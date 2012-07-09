/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/

package org.wso2.carbon.automation.core.utils.productutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkSettings;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class PackageCreator {
    private static final Log log = LogFactory.getLog(PackageCreator.class);

    public static boolean createPackage() throws Exception {

        boolean success = true;
        EnvironmentBuilder env = new EnvironmentBuilder();
        FrameworkSettings framework = env.getFrameworkSettings();

        Process process;
        Assert.assertNotNull(framework.getEnvironmentVariables().getDeploymentFrameworkPath(), "Deployment Framework path not provided");
        try {
            File wd = new File(framework.getEnvironmentVariables().getDeploymentFrameworkPath());
            process = Runtime.getRuntime().exec("perl deploy.pl", null, wd);
            process.waitFor();
            if (process.exitValue() == 0) {
                log.info("Command Successful");
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String line = null;
                    while ((line = in.readLine()) != null) {
                        log.info(line);
                    }
                } catch (IOException e) {
                    success = false;
                    throw new IOException("Script Execution Failed:" + e);
                }
            } else {
                log.info("Command Failure");
            }
        } catch (Exception e) {
            success = false;
            log.error("Script Execution Failed:" + e);
            throw new Exception("Script Execution Failed:" + e);
        }
        return success;
    }
}