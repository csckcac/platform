/*
 * Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.wso2.bps.integration.tests.security;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.security.utils.SecurityClientUtils;
import org.wso2.bps.integration.tests.util.BPSMgtUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.bpel.stub.upload.BPELUploaderStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.security.mgt.stub.config.ApplySecurity;
import org.wso2.carbon.security.mgt.stub.config.DisableSecurityOnService;
import org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceStub;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.fail;


public class SecurityTestCase {
    private static final Log log = LogFactory.getLog(SecurityTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();

    private String[] group = {"admin"};
    private String serviceName = "HelloService";
    private String privateKeyStore = "wso2carbon.jks";
    private String[] trustedKeyStore = {"wso2carbon.jks"};

    private SecurityAdminServiceStub securityAdminServiceStub;
    private InstanceManagementServiceStub instanceManagementServiceStub;

    private DisableSecurityOnService disableSecurityOnService;

    private List<String> instanceIds = new ArrayList<String>();

    @BeforeClass(groups = {"wso2.bps"}, description = "initializing partner service security test")
    public void init() throws Exception {


        if (System.getProperty("bps.sample.location") == null) {
            log.info("System property: bps.sample.location cannot be null");
            fail("System property: bps.sample.location cannot be null");
        }

        final String UPLOADER_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + "/services/BPELUploader";

        final String SECURITY_ADMIN_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + "/services/SecurityAdminService";

        final String PACKAGE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT +
                "/services/BPELPackageManagementService";

        final String INSTANCE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT +
                "/services/InstanceManagementService";


        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        String loggedInSessionCookie = util.login();

        instanceManagementServiceStub = new InstanceManagementServiceStub(INSTANCE_MANAGEMENT_SERVICE_URL);
        BPELUploaderStub bpelUploaderStub = new BPELUploaderStub(UPLOADER_SERVICE_URL);
        securityAdminServiceStub = new SecurityAdminServiceStub(SECURITY_ADMIN_SERVICE_URL);
        BPELPackageManagementServiceStub bpelPackageManagementServiceStub =
                new BPELPackageManagementServiceStub(PACKAGE_MANAGEMENT_SERVICE_URL);

        disableSecurityOnService = new DisableSecurityOnService();

        setClientOptions(instanceManagementServiceStub, loggedInSessionCookie);
        setClientOptions(bpelUploaderStub, loggedInSessionCookie);
        setClientOptions(securityAdminServiceStub, loggedInSessionCookie);
        setClientOptions(bpelPackageManagementServiceStub, loggedInSessionCookie);

    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 01")
    public void securityScenario01Test() throws Exception {
        int scenarioNum = 1;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 02")
    public void securityScenario02Test() throws Exception {
        int scenarioNum = 2;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 03")
    public void securityScenario03Test() throws Exception {
        int scenarioNum = 3;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 04")
    public void securityScenario04Test() throws Exception {
        int scenarioNum = 4;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 05")
    public void securityScenario05Test() throws Exception {
        int scenarioNum = 5;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 06")
    public void securityScenario06Test() throws Exception {
        int scenarioNum = 6;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 07")
    public void securityScenario07Test() throws Exception {
        int scenarioNum = 7;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 08")
    public void securityScenario08Test() throws Exception {
        int scenarioNum = 8;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 09")
    public void securityScenario09Test() throws Exception {
        int scenarioNum = 9;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 10")
    public void securityScenario10Test() throws Exception {
        int scenarioNum = 10;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 11")
    public void securityScenario11Test() throws Exception {
        int scenarioNum = 11;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 12")
    public void securityScenario12Test() throws Exception {
        int scenarioNum = 12;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 13")
    public void securityScenario13Test() throws Exception {
        int scenarioNum = 13;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 14")
    public void securityScenario14Test() throws Exception {
        int scenarioNum = 14;
        executeBPELSecurityScenario(scenarioNum);
    }

    @Test(groups = {"wso2.bps"}, description = "BPEL security test scenario 15")
    public void securityScenario15Test() throws Exception {
        int scenarioNum = 15;
        executeBPELSecurityScenario(scenarioNum);
    }

    private void executeBPELSecurityScenario(int scenarioNum) throws Exception {
        String processId = "{http://ode/bpel/unit-test}HelloWorld2";
        log.info("Scenarios Num: " + scenarioNum);
        ApplySecurity applySecurity = new ApplySecurity();
        applySecurity.setServiceName(serviceName);
        applySecurity.setUserGroupNames(group);
        applySecurity.setPrivateStore(privateKeyStore);
        applySecurity.setTrustedStores(trustedKeyStore);
        applySecurity.setPolicyId("scenario" + scenarioNum);
        securityAdminServiceStub.applySecurity(applySecurity);

        SecurityClientUtils.runSecurityClient(scenarioNum, "HelloService", "http://ode/bpel/unit-test.wsdl/HelloPortType/TestIn", "<un:hello xmlns:un=\"http://ode/bpel/unit-test.wsdl\"><TestPart>Hello</TestPart></un:hello>", "Hello World");
        disableSecurityOnService.setServiceName(serviceName);
        securityAdminServiceStub.disableSecurityOnService(disableSecurityOnService);

        List<String> iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1, processId);
        instanceIds.addAll(iids);
        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "COMPLETED", "tmpVar", ">Hello<", instanceIds);
        BPSMgtUtils.deleteInstances(instanceManagementServiceStub, 1);
        instanceIds.clear();
    }

    private void setClientOptions(Stub serviceStub, String loggedInSessionCookie) {
        ServiceClient serviceClient = serviceStub._getServiceClient();
        Options serviceClientOptions = serviceClient.getOptions();
        serviceClientOptions.setManageSession(true);
        serviceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);

    }

    public static void main(String[] args) {
        SecurityTestCase stc = new SecurityTestCase();
        try {
            System.setProperty("bps.sample.location", "/home/waruna/WSO2/projects/src/trunk/platform/products/bps/modules/integration/org.wso2.bps.integration.tests/target/samples");
            stc.init();
            stc.securityScenario01Test();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
}
