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
package org.wso2.automation.common.test.greg.governance;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;


public class GRegGovernanceTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();

        suiteVariablesList.add(new SuiteVariables("EndpointTest", EndpointServiceTestClient.class));
        suiteVariablesList.add(new SuiteVariables("GarFileImportTest", GarFileImportServiceTestClient.class));
        suiteVariablesList.add(new SuiteVariables("LifeCycleTest", LifeCycleServiceTestClient.class));
        suiteVariablesList.add(new SuiteVariables("PolicyImportTest", PolicyImportServiceTestClient.class));
        suiteVariablesList.add(new SuiteVariables("SchemaImportServiceClient", SchemaImportServiceTestClient.class));
        suiteVariablesList.add(new SuiteVariables("ServiceImportTest", ServiceImportServiceTestClient.class));
        suiteVariablesList.add(new SuiteVariables("WSDLImportTest", WSDLImportServiceTestClient.class));
        suiteVariablesList.add(new SuiteVariables("WSDLContentUpdateTest", WsdlUpadateContentServiceTestClient.class));
        suiteVariablesList.add(new SuiteVariables("WSDLWithSpecialCharTest", WSDLWithSpecialCharTest.class));
        suiteVariablesList.add(new SuiteVariables("EndpointTestCaseClient", EndpointTestCaseClient.class));
        suiteVariablesList.add(new SuiteVariables("PolicyTestCaseClient", PolicyTestCaseClient.class));
        suiteVariablesList.add(new SuiteVariables("ServiceTestCaseClient", ServiceTestCaseClient.class));
        suiteVariablesList.add(new SuiteVariables("WSDLTestCaseClient", WSDLTestCaseClient.class));

        setServerList(ProductConstant.GREG_SERVER_NAME);
        superSuite("GovernanceTestSuite", suiteVariablesList).run();
    }

    public static void main(String[] args) {
        new GRegGovernanceTestSuite().suiteRunner();
    }


}
