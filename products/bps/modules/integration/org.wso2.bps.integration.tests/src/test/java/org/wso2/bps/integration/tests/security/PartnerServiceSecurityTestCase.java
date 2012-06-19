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

import org.apache.axiom.om.util.Base64;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.Stub;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.SimpleAxis2ServerManager;
import org.wso2.bps.integration.tests.util.BPSMgtUtils;
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.carbon.bpel.stub.mgt.BPELPackageManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementException;
import org.wso2.carbon.bpel.stub.mgt.InstanceManagementServiceStub;
import org.wso2.carbon.bpel.stub.upload.BPELUploaderStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.security.mgt.stub.config.SecurityAdminServiceStub;
import org.wso2.carbon.security.mgt.stub.keystore.AddKeyStore;
import org.wso2.carbon.security.mgt.stub.keystore.KeyStoreAdminServiceStub;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.*;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

//import org.wso2.bps.test.utils.BPSMgtUtils;
//import org.wso2.bps.test.utils.BPSTestUtils;


public class PartnerServiceSecurityTestCase {
    private static final Log log = LogFactory.getLog(PartnerServiceSecurityTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();
    private HashMap<Integer, String> securityScenarios = new HashMap<Integer, String>();
    private List<String> instanceIds = new ArrayList<String>();
    private InstanceManagementServiceStub instanceManagementServiceStub;
    private final String BPEL_PARTNER_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
            ":" + FrameworkSettings.HTTPS_PORT +
            "/services/SecurePartnerBPELServiceService";

    @BeforeClass(groups = {"wso2.bps"}, description = "initializing partner service security test")
    public void init() throws Exception {


        //start simple axis2 server - default port 9000
        SimpleAxis2ServerManager.startServer();

        // Put security scenario name to the map
        securityScenarios.put(1, ">Basic Security Scenario - UsernameToken<");
        securityScenarios.put(2, ">Basic Security Scenario - Non-repudiation <");
        securityScenarios.put(3, ">Basic Security Scenario - Integrity <");
        securityScenarios.put(4, ">Basic Security Scenario - Confidentiality <");
        securityScenarios.put(5, ">Sign and encrypt - X509 Authentication<");
        securityScenarios.put(6, ">Sign and Encrypt - Anonymous clients <");
        securityScenarios.put(7, ">Encrypt only - Username Token Authentication<");
        securityScenarios.put(8, ">Sign and Encrypt - Username Token Authentication<");
        securityScenarios.put(9, ">SecureConversation - Sign only - Service as STS - " +
                "Bootstrap policy - Sign and Encrypt , X509 Authentication<");
        securityScenarios.put(10, ">SecureConversation - Encrypt only - Service as STS - " +
                "Bootstrap policy - Sign and Encrypt , X509 Authentication<");
        securityScenarios.put(11, ">SecureConversation - Sign and Encrypt - Service as STS - " +
                "Bootstrap policy - Sign and Encrypt , X509 Authentication <");
        securityScenarios.put(12, ">SecureConversation - Sign Only - Service as STS - " +
                "Bootstrap policy - Sign and Encrypt , Anonymous clients <");
        securityScenarios.put(13, ">SecureConversation - Encrypt Only - Service as STS - " +
                "Bootstrap policy - Sign and Encrypt , Anonymous clients <");
        securityScenarios.put(14, ">SecureConversation - Encrypt Only - Service as STS - " +
                "Bootstrap policy - Sign and Encrypt , Username Token Authentication <");
        securityScenarios.put(15, ">SecureConversation - Sign and Encrypt - Service as STS - " +
                "Bootstrap policy - Sign and Encrypt , Username Token Authentication <");


//        String[] group = {"client"};
//        String serviceName = "SecurePartnerService";
//        String privateKeyStore = "service.jks";
//        String[] trustedKeyStore = {"service.jks"};


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

        final String USER_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT +
                "/services/UserAdmin";

        final String KEY_STORE_MANAGEMENT_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT +
                "/services/KeyStoreAdminService";

        ClientConnectionUtil.waitForPort(FrameworkSettings.HTTPS_PORT);
        String loggedInSessionCookie = util.login();


        instanceManagementServiceStub = new InstanceManagementServiceStub(INSTANCE_MANAGEMENT_SERVICE_URL);
        BPELUploaderStub bpelUploaderStub = new BPELUploaderStub(UPLOADER_SERVICE_URL);
        SecurityAdminServiceStub securityAdminServiceStub = new SecurityAdminServiceStub(SECURITY_ADMIN_SERVICE_URL);
        UserAdminStub userAdminStub = new UserAdminStub(USER_MANAGEMENT_SERVICE_URL);
        KeyStoreAdminServiceStub keyStoreAdminServiceStub = new KeyStoreAdminServiceStub(KEY_STORE_MANAGEMENT_SERVICE_URL);
        BPELPackageManagementServiceStub bpelPackageManagementServiceStub =
                new BPELPackageManagementServiceStub(PACKAGE_MANAGEMENT_SERVICE_URL);

        //DisableSecurityOnService disableSecurityOnService = new DisableSecurityOnService();

        setClientOptions(instanceManagementServiceStub, loggedInSessionCookie);
        setClientOptions(bpelUploaderStub, loggedInSessionCookie);
        setClientOptions(securityAdminServiceStub, loggedInSessionCookie);
        setClientOptions(userAdminStub, loggedInSessionCookie);
        setClientOptions(keyStoreAdminServiceStub, loggedInSessionCookie);
        setClientOptions(bpelPackageManagementServiceStub, loggedInSessionCookie);


        File serviceJks = new File(BPSTestUtils.BPEL_TEST_RESOURCE_LOCATION + File.separator + "partnerServices" +
                File.separator + "service.jks");

        byte content[] = getBytesFromFile(serviceJks);
        String data = Base64.encode(content);
        AddKeyStore keyStore = new AddKeyStore();
        keyStore.setFileData(data);
        keyStore.setFilename("service.jks");
        keyStore.setPassword("apache");
        keyStore.setProvider("org.wso2.carbon.security.util.ServerCrypto");
        keyStore.setType("JKS");
        keyStore.setPvtkeyPass("apache");

        //add key store
        keyStoreAdminServiceStub.addKeyStore(keyStore);


        //deployPackage("SecurePartnerBPEL", "SecurePartnerBPELServiceService", bpelUploaderStub);
        //checkProcessDeployment("SecurePartnerBPEL", bpelPackageManagementServiceStub);

        //add user and role for scenario1

        addUserWithRole("client", "apache", "admin", userAdminStub);
        addRoleWithUser("client", "client", userAdminStub);

        ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "deployment" + File.separator + "client", null);
        ServiceClient sc = new ServiceClient(ctx, null);
        sc.engageModule("addressing");
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 01")
    public void partnerServiceSecurityScenario01Test() throws Exception {
        int scenario = 1;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 02")
    public void partnerServiceSecurityScenario02Test() throws Exception {
        int scenario = 2;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 03")
    public void partnerServiceSecurityScenario03Test() throws Exception {
        int scenario = 3;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 04")
    public void partnerServiceSecurityScenario04Test() throws Exception {
        int scenario = 4;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 05")
    public void partnerServiceSecurityScenario05Test() throws Exception {
        int scenario = 5;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 06")
    public void partnerServiceSecurityScenario06Test() throws Exception {
        int scenario = 6;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 07")
    public void partnerServiceSecurityScenario07Test() throws Exception {
        int scenario = 7;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 08")
    public void partnerServiceSecurityScenario08Test() throws Exception {
        int scenario = 8;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 09")
    public void partnerServiceSecurityScenario09Test() throws Exception {
        int scenario = 9;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 10")
    public void partnerServiceSecurityScenario10Test() throws Exception {
        int scenario = 10;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 11")
    public void partnerServiceSecurityScenario11Test() throws Exception {
        int scenario = 11;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 12")
    public void partnerServiceSecurityScenario12Test() throws Exception {
        int scenario = 12;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 13")
    public void partnerServiceSecurityScenario13Test() throws Exception {
        int scenario = 13;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 14")
    public void partnerServiceSecurityScenario14Test() throws Exception {
        int scenario = 14;
        executeSecurityScenario(scenario);
    }

    @Test(groups = {"wso2.bps"}, description = "partner service security test scenario 15")
    public void partnerServiceSecurityScenario15Test() throws Exception {
        int scenario = 15;
        executeSecurityScenario(scenario);
    }

    @AfterClass(groups = {"wso2.bps"}, description = "Cleanup partner service security test")
    public void cleanup() throws Exception {
        //stop simple axis2 server
        SimpleAxis2ServerManager.shutdown();
    }

    private void executeSecurityScenario(int scenario) throws InstanceManagementException,
            RemoteException, InterruptedException, XMLStreamException {
        String processId = "{http://SecurePartnerBPEL.bpel}SecurePartnerBPEL";

        String scenarioResponse = securityScenarios.get(scenario);

        log.info("Security Scenario :" + scenario + ": ");
        log.info("Security Scenario Name :" + scenarioResponse);

        List<String> iids = BPSMgtUtils.listInstances(instanceManagementServiceStub, 0, processId);
        instanceIds.addAll(iids);
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add(scenarioResponse);

        BPSTestUtils.sendRequest(BPEL_PARTNER_SERVICE_URL,
                "SecurePartnerBPELServiceOperation",
                "<p:SecurePartnerBPELServiceRequest xmlns:p=\"http://www.example.org/messages\">\n" +
                        "            <p:param0>" + scenario + "</p:param0>\n" +
                        "         </p:SecurePartnerBPELServiceRequest>",
                1,
                expectedOutput,
                true);

        List<String> iidsAll = BPSMgtUtils.listInstances(instanceManagementServiceStub, 1, processId);
        instanceIds.addAll(iidsAll);

        BPSMgtUtils.getInstanceInfo(instanceManagementServiceStub,
                "COMPLETED", "SecurePartnerService" + scenario + "Output", scenarioResponse,
                instanceIds);
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

    private void addRoleWithUser(String roleName, String userName, UserAdminStub userAdminStub)
            throws Exception {
        userAdminStub.addRole(roleName, new String[]{userName}, null);
        FlaggedName[] roles = userAdminStub.getAllRolesNames();
        for (FlaggedName role : roles) {
            if (!role.equals(roleName)) {
                continue;
            } else {
                assertTrue(role.equals(roleName));
            }
            fail("Role: " + roleName + " was not added properly.");
        }
    }

    private void addUserWithRole(String userName, String credential, String roleName,
                                 UserAdminStub userAdminStub)
            throws Exception {
        userAdminStub.addUser(userName, credential, new String[]{roleName}, null, null);
        FlaggedName[] users = userAdminStub.getUsersOfRole(roleName, userName);
        assertTrue(users.length != 0);

    }

    private byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];

        // Read in the bytes
        int offset = 0;
        int numRead;
        while (offset < bytes.length
                && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
            offset += numRead;
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}
