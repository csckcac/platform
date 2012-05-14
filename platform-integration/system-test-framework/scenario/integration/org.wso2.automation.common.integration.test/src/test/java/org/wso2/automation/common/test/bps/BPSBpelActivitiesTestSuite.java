package org.wso2.automation.common.test.bps;

import org.testng.annotations.AfterSuite;
import org.wso2.automation.common.test.bps.bpelactivities.BpelActComposeUrl;
import org.wso2.automation.common.test.bps.bpelactivities.BpelActIgnoreMissingFromData;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;

import java.util.ArrayList;
import java.util.List;

public class BPSBpelActivitiesTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList=new ArrayList<SuiteVariables>();
        suiteVariablesList.add(new SuiteVariables("composeUrl", BpelActComposeUrl.class));
        suiteVariablesList.add(new SuiteVariables("CombineUrl", BpelActIgnoreMissingFromData.class));
        setServerList("BPS");
        superSuite("BPSSActivitiesSuite",suiteVariablesList).run();
    }

    public static void main(String[] args) {
        new BPSBpelActivitiesTestSuite().suiteRunner();
    }
}