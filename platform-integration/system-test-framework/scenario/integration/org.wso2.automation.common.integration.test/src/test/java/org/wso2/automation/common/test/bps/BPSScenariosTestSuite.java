package org.wso2.automation.common.test.bps;


import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;
import org.wso2.automation.common.test.bps.managescenarios.BpelInstanceManagementClient;
import org.wso2.automation.common.test.bps.managescenarios.BpelProcessManagementClient;

import java.util.ArrayList;
import java.util.List;

public class BPSScenariosTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList=new ArrayList<SuiteVariables>();
        suiteVariablesList.add(new SuiteVariables("ProcessManagement", BpelProcessManagementClient.class));
        suiteVariablesList.add(new SuiteVariables("InstanceManagement", BpelInstanceManagementClient.class));
        superSuite("BPSScenariosSuite",suiteVariablesList).run();
    }

    public static void main(String[] args) {
       new BPSScenariosTestSuite().suiteRunner();
    }
}

