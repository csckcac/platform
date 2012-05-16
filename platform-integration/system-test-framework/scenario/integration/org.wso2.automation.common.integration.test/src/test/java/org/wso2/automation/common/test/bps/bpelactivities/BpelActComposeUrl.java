package org.wso2.automation.common.test.bps.bpelactivities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.carbon.admin.service.*;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.RequestSender;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.platform.test.core.utils.environmentutils.ManageEnvironment;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BpelActComposeUrl{
    String sessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelActComposeUrl.class);
    String backEndUrl = null;
    String serviceUrl = null;
    AdminServiceBpelUploader bpelUploader;
    AdminServiceBpelPackageManager bpelManager;
    AdminServiceBpelProcessManager bpelProcrss;
    AdminServiceBpelInstanceManager bpelInstance;
    AdminServiceAuthentication adminServiceAuthentication;
    RequestSender requestSender;

    @BeforeTest(alwaysRun = true)
    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException {
        EnvironmentBuilder builder = new EnvironmentBuilder().bps(3);
        ManageEnvironment environment = builder.build();
        backEndUrl = environment.getBps().getBackEndUrl();
        serviceUrl=environment.getBps().getServiceUrl();
        sessionCookie = environment.getBps().getSessionCookie();
        bpelUploader =  new AdminServiceBpelUploader(backEndUrl, ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION);
        bpelManager = new AdminServiceBpelPackageManager(backEndUrl, sessionCookie);
        bpelProcrss = new AdminServiceBpelProcessManager(backEndUrl, sessionCookie);
        bpelInstance = new AdminServiceBpelInstanceManager(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true,groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void deployArtifact()
            throws InterruptedException, RemoteException, PackageManagementException,
                   MalformedURLException {
        bpelUploader.deployBPEL("TestComposeUrl",  sessionCookie);
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "Invike combine URL Bpel")
    public void testComposeUrl() throws Exception, RemoteException {
    int instanceCount = 0;

        String processID = bpelProcrss.getProcessId("TestComposeUrl");
        PaginatedInstanceList instanceList = new PaginatedInstanceList();
        instanceList = bpelInstance.filterPageInstances(processID);
        if (instanceList.getInstance() != null) {
            instanceCount = instanceList.getInstance().length;
        }
        if (!processID.isEmpty()) {
            try {
                this.forEachRequest();
                Thread.sleep(5000);
                if (instanceCount >= bpelInstance.filterPageInstances(processID).getInstance().length) {
                    Assert.fail("Instance is not created for the request");
                }
            } catch (InterruptedException e) {
                log.error("Process management failed" + e);
                Assert.fail("Process management failed" + e);
            }
            bpelInstance.clearInstancesOfProcess(processID);
        }
    }

    @AfterTest(alwaysRun = true,groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
                   LogoutAuthenticationExceptionException {
        //bpelManager.undeployBPEL("TestComposeUrl");
      //  adminServiceAuthentication.logOut();
    }

    public void forEachRequest() throws Exception {
         String payload = " <p:composeUrl xmlns:p=\"http://ode/bpel/unit-test.wsdl\">\n" +
                 "      <!--Exactly 1 occurrence-->\n" +
                 "      <template>www.google.com</template>\n" +
                 "      <!--Exactly 1 occurrence-->\n" +
                 "      <name>google</name>\n" +
                 "      <!--Exactly 1 occurrence-->\n" +
                 "      <value>ee</value>\n" +
                 "      <!--Exactly 1 occurrence-->\n" +
                 "      <pairs>\n" +
                 "         <!--Exactly 1 occurrence-->\n" +
                 "         <user>er</user>\n" +
                 "         <!--Exactly 1 occurrence-->\n" +
                 "         <tag>ff</tag>\n" +
                 "      </pairs>\n" +
                 "   </p:composeUrl>";
        String operation = "composeUrl";
        String serviceName = "/TestComposeUrlService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("www.google");

        requestSender.sendRequest(serviceUrl + serviceName, operation, payload,
                1, expectedOutput, true);
    }
}

