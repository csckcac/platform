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
package org.wso2.bps.integration.tests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.carbon.integration.framework.TestServerManager;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.io.IOException;

/**
 * Prepares the WSO2 BPS for test runs, starts the server, and stops the server after
 * test runs
 */
public class BPSTestServerManager extends TestServerManager {
    private static final Log log = LogFactory.getLog(BPSTestServerManager.class);

    @Override
    @BeforeSuite
    public String startServer() throws IOException {
        String carbonHome = super.startServer();
        log.info("Starting Server the test suite");
        System.setProperty("carbon.home", carbonHome);
        return carbonHome;
    }

    @Override
    @AfterSuite
    public void stopServer() throws Exception {
        log.info("Stopping Server after the test suite");
        super.stopServer();
    }


    @Override
    protected void copyArtifacts(String carbonHome) throws IOException {
        File[] samples = FileManipulator.getMatchingFiles(BPSTestUtils.getBpelTestSampleLocation(),
                null, "zip");
        File bpelRepo = new File(carbonHome + File.separator + "repository" + File.separator +
                                 "samples" + File.separator + "bpel" + File.separator);
        for (File sample : samples) {

            FileManipulator.copyFileToDir(sample, bpelRepo);
            log.info("Copying: " + sample.getAbsolutePath() + " to " + bpelRepo.getAbsolutePath());
        }
    }

}
