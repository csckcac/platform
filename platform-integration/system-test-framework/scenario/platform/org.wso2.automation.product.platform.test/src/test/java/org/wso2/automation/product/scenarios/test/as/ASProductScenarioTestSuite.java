/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.automation.product.scenarios.test.as;

import org.testng.annotations.AfterSuite;
import org.wso2.automation.product.scenarios.test.esb.HttpGetTest;
import org.wso2.automation.product.scenarios.test.esb.XFormURLEncodedBuilderTest;
import org.wso2.automation.product.scenarios.test.esb.XFormURLEncodedBuilderWithWSDLTest;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;

public class ASProductScenarioTestSuite extends MasterTestSuite{

        @AfterSuite
        public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();
        suiteVariablesList.add(new SuiteVariables("UnpackWarTest", UnpackWarTest.class));
        superSuite("ASProductScenarioTestSuite", suiteVariablesList).run();
    }
}
