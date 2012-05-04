package org.wso2.automation.product.scenarios.test.esb;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;

public class ESBProductScenarioTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList = new ArrayList<SuiteVariables>();
        suiteVariablesList.add(new SuiteVariables("XFormURLEncodedBuilderTest", XFormURLEncodedBuilderTest.class));
        suiteVariablesList.add(new SuiteVariables("XFormURLEncodedBuilderWithWSDLTest", XFormURLEncodedBuilderWithWSDLTest.class));
        suiteVariablesList.add(new SuiteVariables("MessageProcessorTest", MessageProcessorTest.class));

        setServerList(ProductConstant.ESB_SERVER_NAME + "," + ProductConstant.APP_SERVER_NAME);
        superSuite("ESBproductScenariosTestSuite", suiteVariablesList).run();
    }

    public static void main(String[] args) {
        ESBProductScenarioTestSuite esbTestSuite = new ESBProductScenarioTestSuite();
        esbTestSuite.suiteRunner();
    }
}