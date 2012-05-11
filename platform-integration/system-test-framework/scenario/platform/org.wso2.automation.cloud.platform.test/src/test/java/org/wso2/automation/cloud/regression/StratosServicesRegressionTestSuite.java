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
package org.wso2.automation.cloud.regression;

import org.testng.annotations.AfterSuite;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

/**
 * intended to run as regression suite for Stratos live
 * mainly target to find out data migration issues.
 * pre configured tenants with data will be used for testing.
 */
public class StratosServicesRegressionTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() throws LoginAuthenticationExceptionException, RemoteException {
        List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder().manager(1);
        ManageEnvironment environment = environmentBuilder.build();

        //runs only on Stratos live environment
        if (environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos() &&
            environment.getManager().getProductVariables().getHostName().equals("stratoslive.wso2.com")) {

            suiteVariablesList.add(new SuiteVariables("StratosAppServiceTest", StratosAppServiceTest.class));
            suiteVariablesList.add(new SuiteVariables("StratosBAMServiceTest", StratosBAMServiceTest.class));
            suiteVariablesList.add(new SuiteVariables("StratosBPSServiceTest", StratosBPSServiceTest.class));
            suiteVariablesList.add(new SuiteVariables("StratosBRSServiceTest", StratosBRSServiceTest.class));

            suiteVariablesList.add(new SuiteVariables("StratosDSSServiceTest", StratosDSSServiceTest.class));
            suiteVariablesList.add(new SuiteVariables("StratosESBServiceTest", StratosESBServiceTest.class));
            suiteVariablesList.add(new SuiteVariables("StratosGREGServiceTest", StratosGREGServiceTest.class));
            suiteVariablesList.add(new SuiteVariables("StratosGSServiceTest", StratosGSServiceTest.class));
            suiteVariablesList.add(new SuiteVariables("StratosISServiceTest", StratosISServiceTest.class));
            suiteVariablesList.add(new SuiteVariables("StratosManagerServiceTest", StratosManagerServiceTest.class));
//            suiteVariablesList.add(new SuiteVariables("StratosMBServiceTest", StratosMBServiceTest.class));
//            suiteVariablesList.add(new SuiteVariables("StratosCEPServiceTest", StratosCEPServiceTest.class));
            suiteVariablesList.add(new SuiteVariables("StratosMSServiceTest", StratosMSServiceTest.class));
        }
        superSuite("StratosServicesRegressionTestSuite", suiteVariablesList).run();
    }

    public static void main(String[] args)
            throws LoginAuthenticationExceptionException, RemoteException {
        new StratosServicesRegressionTestSuite().suiteRunner();
    }
}