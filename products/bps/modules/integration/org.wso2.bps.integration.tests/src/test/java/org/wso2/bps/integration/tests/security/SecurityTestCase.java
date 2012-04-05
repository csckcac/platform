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


    @BeforeClass(groups = {"wso2.bps"})
    public void init() {

    }

    @Test(groups = {"wso2.bps"})
    public void securityTest() throws Exception {
        List<String> instanceIds = new ArrayList<String>();
        String[] group = {"admin"};
        String serviceName = "HelloService";
        String privateKeyStore = "wso2carbon.jks";
        String[] trustedKeyStore = {"wso2carbon.jks"};


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

        InstanceManagementServiceStub instanceManagementServiceStub = new InstanceManagementServiceStub(INSTANCE_MANAGEMENT_SERVICE_URL);
        BPELUploaderStub bpelUploaderStub = new BPELUploaderStub(UPLOADER_SERVICE_URL);
        SecurityAdminServiceStub securityAdminServiceStub = new SecurityAdminServiceStub(SECURITY_ADMIN_SERVICE_URL);
        BPELPackageManagementServiceStub bpelPackageManagementServiceStub =
                new BPELPackageManagementServiceStub(PACKAGE_MANAGEMENT_SERVICE_URL);

        DisableSecurityOnService disableSecurityOnService = new DisableSecurityOnService();

        setClientOptions(instanceManagementServiceStub, loggedInSessionCookie);
        setClientOptions(bpelUploaderStub, loggedInSessionCookie);
        setClientOptions(securityAdminServiceStub, loggedInSessionCookie);
        setClientOptions(bpelPackageManagementServiceStub, loggedInSessionCookie);


        //Upto 3 security scenarios are tested due to CARBON-9342
        for (int scenarioNum = 1; scenarioNum <= 3; scenarioNum++) {

            ApplySecurity applySecurity = new ApplySecurity();
            log.info("Scenarios Num: " + scenarioNum);
            applySecurity.setServiceName(serviceName);
            applySecurity.setUserGroupNames(group);
            applySecurity.setPrivateStore(privateKeyStore);
            applySecurity.setTrustedStores(trustedKeyStore);
            applySecurity.setPolicyId("scenario" + scenarioNum);
            securityAdminServiceStub.applySecurity(applySecurity);

            SecurityClientUtils.runSecurityClient(scenarioNum, "HelloService", "http://ode/bpel/unit-test.wsdl/HelloPortType/TestIn", "<un:hello xmlns:un=\"http://ode/bpel/unit-test.wsdl\"><TestPart>Hello</TestPart></un:hello>", "Hello World");
            disableSecurityOnService.setServiceName(serviceName);
            securityAdminServiceStub.disableSecurityOnService(disableSecurityOnService);

            List<String> iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1);
            instanceIds.addAll(iids);
            BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub, "COMPLETED", "tmpVar", ">Hello<", instanceIds);
            BPSMgtUtils.deleteInstances(instanceManagementServiceStub, 1);
            instanceIds.clear();
        }

    }

    private void setClientOptions(Stub serviceStub, String loggedInSessionCookie) {
        ServiceClient serviceClient = serviceStub._getServiceClient();
        Options serviceClientOptions = serviceClient.getOptions();
        serviceClientOptions.setManageSession(true);
        serviceClientOptions.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                                         loggedInSessionCookie);

    }
}
