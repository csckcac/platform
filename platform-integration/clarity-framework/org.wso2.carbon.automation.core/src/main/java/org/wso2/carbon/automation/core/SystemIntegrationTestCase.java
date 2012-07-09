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

import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.frameworkutils.FrameworkSettings;


public class SystemIntegrationTestCase {

    protected void setUp() throws Exception {
//        super.setUp();
        EnvironmentBuilder env = new EnvironmentBuilder();
        FrameworkSettings framework = env.getFrameworkSettings();

        if (framework.getEnvironmentSettings().isEnableDipFramework()) {
            startServers();
        }
//        new UserPopulator().populateUsers();
    }


    protected void tearDown() throws Exception {
//        super.tearDown();
    }

    public void startServers() throws Exception {
        EnvironmentBuilder env = new EnvironmentBuilder();
        FrameworkSettings framework = env.getFrameworkSettings();
        if (!framework.getEnvironmentSettings().is_runningOnStratos()) {
//            ServerGroupManager.startServers();
        }
    }
}
