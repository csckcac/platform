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

package org.wso2.platform.test.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.IExecutionListener;
import org.wso2.carbon.base.ServerConfigurationException;
import org.wso2.platform.test.core.utils.emmautils.CodeCoverageUtils;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.serverutils.ServerManager;

import java.util.ArrayList;
import java.util.List;

public class PlatformExecutionManager implements IExecutionListener {
    private static final Log log = LogFactory.getLog(PlatformExecutionManager.class);
    ServerManager serverManager = null;
    List<ServerManager> serverList = new ArrayList<ServerManager>();
    EnvironmentBuilder environmentBuilder;

    public void onExecutionStart() {
        environmentBuilder = new EnvironmentBuilder();
        if (environmentBuilder.getFrameworkSettings().getCoverageSettings().getCoverageEnable()) {
            for (Object carbonHomePath : environmentBuilder.getFrameworkSettings().getCoverageSettings().getCarbonHome().values()) {
                CodeCoverageUtils.instrument(carbonHomePath.toString());
                serverManager = new ServerManager(carbonHomePath.toString());
                try {
                    serverManager.start();
                } catch (ServerConfigurationException e) {
                    log.error(e);
                }
                serverList.add(serverManager);
            }
            CodeCoverageUtils.init();
        }
    }

    public void onExecutionFinish() {
        if (serverList.size() != 0) {
            for (ServerManager server : serverList) {
                try {
                    server.shutdown();
                } catch (Exception e) {
                    log.error(e);
                }
            }
        }
        if (environmentBuilder.getFrameworkSettings().getCoverageSettings().getCoverageEnable()) {
            for (Object carbonHomePath : environmentBuilder.getFrameworkSettings().getCoverageSettings().getCarbonHome().values()) {
                CodeCoverageUtils.generateReports(carbonHomePath.toString());
            }
        }
    }
}
