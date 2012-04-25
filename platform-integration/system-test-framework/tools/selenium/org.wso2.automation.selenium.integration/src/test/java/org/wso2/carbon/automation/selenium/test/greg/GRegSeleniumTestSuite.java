package org.wso2.carbon.automation.selenium.test.greg;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;


import java.util.ArrayList;
import java.util.List;


public class GRegSeleniumTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (!env.getFrameworkSettings().getEnvironmentSettings().is_enableSelenium()) {
            List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();
            suiteVariablesList.add(new SuiteVariables("GReg_Login", GregLoginSeleniumTest.class));
            suiteVariablesList.add(new SuiteVariables("Selenium_Root_Level_Test",
                                                      GRegRootSeleniumTest.class));
            suiteVariablesList.add(new SuiteVariables("GReg_Collection_Level_Test",
                                                      GRegCollectionSeleniumTest.class));
            suiteVariablesList.add(new SuiteVariables("GReg_Resource_Level_Test",
                                                      GRegResourceSeleniumTest.class));
            suiteVariablesList.add(new SuiteVariables("GReg_Upload_WSDL_from_URL",
                                                      GRegWSDLUploaderFromURLSeleniumTest.class));
            suiteVariablesList.add(new SuiteVariables("GReg_Create_Service",
                                                      GRegServiceCreatorSeleniumTest.class));
            suiteVariablesList.add(new SuiteVariables("GReg_Schema_Uploader",
                                                      GRegSchemaUploaderSeleniumTest.class));
            suiteVariablesList.add(new SuiteVariables("GReg_Policy_Uploader", GRegPolicyUploaderSeleniumTest.class));
            suiteVariablesList.add(new SuiteVariables("GRegLifeCyclePromoteTest", GRegLifeCyclePromoteTest.class));
            superSuite("GRegSeleniumTestSuite", suiteVariablesList).run();
        }
    }

    public static void main(String[] args) {
        EnvironmentBuilder env = new EnvironmentBuilder();
        if (!env.getFrameworkSettings().getEnvironmentSettings().is_enableSelenium()) {
            new GRegSeleniumTestSuite().suiteRunner();
        }
    }
}


