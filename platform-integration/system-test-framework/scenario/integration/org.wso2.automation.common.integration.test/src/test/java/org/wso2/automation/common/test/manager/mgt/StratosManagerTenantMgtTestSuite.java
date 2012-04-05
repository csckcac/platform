package org.wso2.automation.common.test.manager.mgt;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;

public class StratosManagerTenantMgtTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();
            suiteVariablesList.add(new SuiteVariables("VerifyTenantFullNmae", FullNameVerificationTest.class));
            suiteVariablesList.add(new SuiteVariables("AddNewTenant", NewTenantTest.class));
            suiteVariablesList.add(new SuiteVariables("TenantDeactivationTest", TenantDeactivationTest.class));
            suiteVariablesList.add(new SuiteVariables("UpdateContactInfoTest", UpdateContactInfoTest.class));
            suiteVariablesList.add(new SuiteVariables("UsagePlanDowngradeTest", UsagePlanDowngradeTest.class));
            suiteVariablesList.add(new SuiteVariables("UsagePlanUpdateTest", UsagePlanUpdateTest.class));
            suiteVariablesList.add(new SuiteVariables("UpdateTenantInfoTest", UpdateTenantInfoTest.class));
            superSuite("StratosManagerTenantMgtTestSuite", suiteVariablesList).run();
        }
    }

    public static void main(String[] args) {
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (env.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            new StratosManagerTenantMgtTestSuite().suiteRunner();
        }
    }
}
