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


package org.wso2.automation.common.test.greg.remoteregistry;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;


public class GRegRemoteRegistryAPITestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();


        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();

        suiteVariablesList.add(new SuiteVariables("QueryTest", QueryTest.class));
        suiteVariablesList.add(new SuiteVariables("TestTagging", TestTagging.class));

        if (!environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            suiteVariablesList.add(new SuiteVariables("CommentTest", CommentTest.class));
            suiteVariablesList.add(new SuiteVariables("ContinuousOperations", ContinuousOperations.class));
            suiteVariablesList.add(new SuiteVariables("PropertyTest", PropertiesTest.class));
            suiteVariablesList.add(new SuiteVariables("RatingTest", RatingTest.class));
            suiteVariablesList.add(new SuiteVariables("RenameTest", RenameTest.class));
            suiteVariablesList.add(new SuiteVariables("ResourceHandlingTest", ResourceHandling.class));
            suiteVariablesList.add(new SuiteVariables("TestAssociationTest", TestAssociation.class));
            suiteVariablesList.add(new SuiteVariables("TestContentStream", TestContentStream.class));
            suiteVariablesList.add(new SuiteVariables("TestCopy", TestCopy.class));
            suiteVariablesList.add(new SuiteVariables("TestMove", TestMove.class));
            suiteVariablesList.add(new SuiteVariables("TestPaths", TestPaths.class));
            suiteVariablesList.add(new SuiteVariables("TestResources", TestResources.class));
        }

        setServerList(ProductConstant.GREG_SERVER_NAME);
        superSuite("RemoteRegistryAPITestSuite", suiteVariablesList).run();
    }

    public static void main(String[] args) {
        new GRegRemoteRegistryAPITestSuite().suiteRunner();
    }
}
