package org.wso2.automation.common.test.bps;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;
import org.wso2.automation.common.test.bps.mgtstructuredactivities.*;

import java.util.ArrayList;
import java.util.List;

public class BPSStructuredActivitiesTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList=new ArrayList<SuiteVariables>();
        suiteVariablesList.add(new SuiteVariables("FlowClient", BpelStructAct_FlowClient.class));
        suiteVariablesList.add(new SuiteVariables("ForEachClient", BpelStructAct_forEachClient.class));
        suiteVariablesList.add(new SuiteVariables("IfClient", BpelStructAct_IfClient.class));
        suiteVariablesList.add(new SuiteVariables("PickClient", BpelStructAct_PickClient.class));
        suiteVariablesList.add(new SuiteVariables("RepeatUntill", BpelStructAct_RepeatUntillClient.class));
        suiteVariablesList.add(new SuiteVariables("WhileClient", BpelStructAct_WhileClient.class));
        superSuite("BPSStructuredSuite",suiteVariablesList).run();
    }

    public static void main(String[] args) {
        new BPSStructuredActivitiesTestSuite().suiteRunner();
    }

}

