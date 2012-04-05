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

package org.wso2.automation.common.perf.as;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.platform.test.core.utils.ArtifactCleanerUtil;
import org.wso2.platform.test.core.utils.ArtifactDeployerUtil;

import java.rmi.RemoteException;

import static org.testng.AssertJUnit.fail;


public class WebAppWorker extends Thread {

    private final Log log = LogFactory.getLog(WebAppWorker.class);
    private String session;
    private String backendURL;
    private String filePath;

    public WebAppWorker(String session, String backendURL, String filePath) {
        this.session = session;
        this.backendURL = backendURL;
        this.filePath = filePath;
    }


    public void run() {
        ArtifactDeployerUtil deployerUtil = new ArtifactDeployerUtil();
        try {
            String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1);
            log.info("Deploying webapp file: " + fileName);
            deployerUtil.warFileUploder(session, backendURL, filePath);

        } catch (RemoteException e) {
            fail("Webapp deployment fail " + e.getMessage());
        }
    }

    public void deleteWebApp(String fileName) throws RemoteException {
        log.info("Undeploying webapp :" + fileName);
        ArtifactCleanerUtil artifactCleanerUtil = new ArtifactCleanerUtil();
        artifactCleanerUtil.deleteWebApp(session, fileName, backendURL);
    }
}

