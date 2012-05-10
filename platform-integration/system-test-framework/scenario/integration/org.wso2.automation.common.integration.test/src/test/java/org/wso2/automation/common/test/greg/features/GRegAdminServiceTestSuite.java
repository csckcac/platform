package org.wso2.automation.common.test.greg.features;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;


public class GRegAdminServiceTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();

        suiteVariablesList.add(new SuiteVariables("Apply_Symbolink_to_Collection_at_rootLevel",
                                                  SymbolicServiceTestClient.class));

        suiteVariablesList.add(new SuiteVariables("GRegLoginPermissionServiceTestClient",
                                                  GRegLoginPermissionServiceTestClient.class));

        suiteVariablesList.add(new SuiteVariables("RolePermissionServiceTestClient",
                                                  RolePermissionServiceTestClient.class));

        if (!environmentBuilder.getFrameworkSettings().getEnvironmentSettings().is_runningOnStratos()) {
            suiteVariablesList.add(new SuiteVariables("GRegMetaDataPermissionServiceTestClient",
                                                      GRegMetaDataPermissionServiceTestClient.class));
        }

        setServerList(ProductConstant.GREG_SERVER_NAME);
        superSuite("G-RegFunctionalityTest", suiteVariablesList).run();
    }

    public static void main(String[] args) {
        new GRegAdminServiceTestSuite().suiteRunner();

    }
}
