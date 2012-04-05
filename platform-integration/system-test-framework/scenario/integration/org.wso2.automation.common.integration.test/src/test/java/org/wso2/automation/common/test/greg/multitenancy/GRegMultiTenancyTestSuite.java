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
package org.wso2.automation.common.test.greg.multitenancy;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;


import java.util.ArrayList;
import java.util.List;


public class GRegMultiTenancyTestSuite extends MasterTestSuite {


    @AfterSuite
    public void suiteRunner() {
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();
            suiteVariablesList.add(new SuiteVariables("Multi-tenancy GAR File Uploader",
                                                      GarfileUploadServiceTestClient.class));
            suiteVariablesList.add(new SuiteVariables("Multi-tenancy Service Life Cycle Invoke",
                                                      LifeCycleComparisonServiceTestClient.class));
            suiteVariablesList.add(new SuiteVariables("Multi-tenancy Policy Uploader",
                                                      PolicyUploadServiceTestClient.class));
            suiteVariablesList.add(new SuiteVariables("Multi-tenancy Resource Handler",
                                                      ResourceHandlingServiceTestClient.class));
            suiteVariablesList.add(new SuiteVariables("Multi-tenancy Schema Uploader",
                                                      SchemaUploadServiceTestClient.class));
            suiteVariablesList.add(new SuiteVariables("Multi-tenancy WSDL Uploader",
                                                      WsdlImportServiceTestClient.class));
            superSuite("Multitenancy-test-suite", suiteVariablesList).run();
        }
    }

    public static void main(String[] args) {
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            new GRegMultiTenancyTestSuite().suiteRunner();
        }
    }
}
