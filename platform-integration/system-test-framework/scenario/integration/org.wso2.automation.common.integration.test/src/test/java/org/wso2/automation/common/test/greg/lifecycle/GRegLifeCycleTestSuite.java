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
package org.wso2.automation.common.test.greg.lifecycle;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;

public class GRegLifeCycleTestSuite extends MasterTestSuite {
    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();

        suiteVariablesList.add(new SuiteVariables("DefaultServiceLifeCycleTest",
                                                  DefaultServiceLifeCycleTest.class));
        suiteVariablesList.add(new SuiteVariables("PreserveOriginalDefaultServiceLifeCycle",
                                                  PreserveOriginalDefaultServiceLifeCycle.class));
        suiteVariablesList.add(new SuiteVariables("DefaultServiceLifeCycleWithDependency",
                                                  DefaultServiceLifeCycleWithDependency.class));


        setServerList(ProductConstant.GREG_SERVER_NAME);
        superSuite("GRegLifeCycleTestSuite", suiteVariablesList).run();

    }

    public static void main(String[] args) {
        new GRegLifeCycleTestSuite().suiteRunner();

    }
}
