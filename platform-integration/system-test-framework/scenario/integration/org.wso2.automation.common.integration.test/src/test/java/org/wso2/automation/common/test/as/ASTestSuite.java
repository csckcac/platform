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
package org.wso2.automation.common.test.as;

import org.apache.tools.ant.taskdefs.Unpack;
import org.testng.annotations.AfterSuite;
import org.wso2.automation.common.test.as.service.*;
import org.wso2.automation.common.test.as.webapp.UnpackWarTest;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;
import org.wso2.automation.common.test.as.capp.CarFileMultitenancyTest;
import org.wso2.automation.common.test.as.soaptracer.SoapTracerTest;
import org.wso2.automation.common.test.as.stat.ServiceStatTest;
import org.wso2.automation.common.test.as.stat.SystemStatTest;
import org.wso2.automation.common.test.as.webapp.FaultyWebAppTest;
import org.wso2.automation.common.test.as.webapp.WebAppDeploymentTest;

import java.util.ArrayList;
import java.util.List;

public class ASTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();


        EnvironmentBuilder env = new EnvironmentBuilder();


        suiteVariablesList.add(new SuiteVariables("CarFileUploadTest", CarFileMultitenancyTest.class));
        suiteVariablesList.add(new SuiteVariables("JarServiceDependencyTest", JarServiceDependencyTest.class));
        suiteVariablesList.add(new SuiteVariables("SpringServiceUpload", SpringServiceUploadTest.class));
        suiteVariablesList.add(new SuiteVariables("JarServiceUpload", JarServiceUploadTest.class));
        suiteVariablesList.add(new SuiteVariables("AppServerSystemStat", SystemStatTest.class));
        suiteVariablesList.add(new SuiteVariables("AppServerServiceStat", ServiceStatTest.class));
        suiteVariablesList.add(new SuiteVariables("AarUploadTest", AARServiceUploadTest.class));
        suiteVariablesList.add(new SuiteVariables("ServiceReferExternalSchema", ExternalSchemaReferenceTest.class));
        suiteVariablesList.add(new SuiteVariables("SoapTracerTest", SoapTracerTest.class));
        suiteVariablesList.add(new SuiteVariables("StopandRedeployWebapp", WebAppDeploymentTest.class));
        suiteVariablesList.add(new SuiteVariables("FaultyWebappTest", FaultyWebAppTest.class));
        suiteVariablesList.add(new SuiteVariables("AARServiceWithSpaceinFileNameTest", AARServiceSpaceInFileNameTest.class));

        if (!env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            suiteVariablesList.add(new SuiteVariables("UnpackWarTest", UnpackWarTest.class));
            suiteVariablesList.add(new SuiteVariables("JaxWSServiceUpload", JaxWsServiceUploaderTest.class));
        }

        setServerList("AS");
        superSuite("Appserver-test-suite", suiteVariablesList).run();
    }

    public static void main(String[] args) {
        new ASTestSuite().suiteRunner();
    }
}
