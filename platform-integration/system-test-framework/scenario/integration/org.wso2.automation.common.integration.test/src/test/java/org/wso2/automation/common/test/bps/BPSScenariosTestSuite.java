package org.wso2.automation.common.test.bps;


import org.testng.annotations.AfterSuite;
import org.wso2.automation.common.test.bps.managescenarios.BpelInstanceManagementClient;
import org.wso2.automation.common.test.bps.managescenarios.BpelProcessManagementClient;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;

public class BPSScenariosTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList=new ArrayList<SuiteVariables>();
        suiteVariablesList.add(new SuiteVariables("ProcessManagement", BpelProcessManagementClient.class));
        EnvironmentBuilder builder = new EnvironmentBuilder();
        EnvironmentVariables environmentVariables;

        if(!builder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos())
        {
        suiteVariablesList.add(new SuiteVariables("InstanceManagement", BpelInstanceManagementClient.class));
        }
        setServerList(ProductConstant.BPS_SERVER_NAME);
        superSuite("BPSScenariosSuite",suiteVariablesList).run();
    }

    public static void main(String[] args) {
       new BPSScenariosTestSuite().suiteRunner();
    }
}

