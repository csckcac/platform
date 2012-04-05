package org.wso2.automation.common.test.manager.throttling;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;

public class StratosManagerThrottlingTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();
            suiteVariablesList.add(new SuiteVariables("UserCount", UserCountTest.class));
            superSuite("StratosManagerThrottlingTestSuite", suiteVariablesList).run();
        }
    }

    public static void main(String[] args) {
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            new StratosManagerThrottlingTestSuite().suiteRunner();
        }
    }
}

