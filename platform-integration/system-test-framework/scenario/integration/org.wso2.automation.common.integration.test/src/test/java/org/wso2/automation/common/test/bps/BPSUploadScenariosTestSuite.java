package org.wso2.automation.common.test.bps;

import org.testng.annotations.AfterSuite;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.ProductConfig;
import org.wso2.platform.test.core.utils.suiteutills.MasterTestSuite;
import org.wso2.platform.test.core.utils.suiteutills.SuiteVariables;
import org.wso2.automation.common.test.bps.uploadscenarios.*;

import java.util.ArrayList;
import java.util.List;

public class BPSUploadScenariosTestSuite extends MasterTestSuite {

    @AfterSuite
    public void suiteRunner() {
        List<SuiteVariables> suiteVariablesList=new ArrayList<SuiteVariables>();
        suiteVariablesList.add(new SuiteVariables("Bpel_Deployment", BpelDeployClient.class));
        suiteVariablesList.add(new SuiteVariables("BpelInmemoryDeployment", BpelInMemoryDeploymentClient.class));
        suiteVariablesList.add(new SuiteVariables("BpelRedeployClient", BpelRedeployClient.class));
        suiteVariablesList.add(new SuiteVariables("BpelRetireDeployment", BpelRetireDeploymentClient.class));
        suiteVariablesList.add(new SuiteVariables("BpelVersioningDeployment", BpelVersioningClient.class));

        setServerList(ProductConstant.BPS_SERVER_NAME);
        superSuite("BPSUploadSuite",suiteVariablesList).run();
    }

    public static void main(String[] args) {
        new BPSUploadScenariosTestSuite().suiteRunner();
    }
}

