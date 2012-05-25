/*
*Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.appserver.integration.tests;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.UUIDGenerator;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.PolicyInclude;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.sandesha2.client.SandeshaClientConstants;
import org.apache.sandesha2.client.SandeshaListener;
import org.apache.sandesha2.client.SequenceReport;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.appserver.integration.tests.utils.HelloServiceCallbackHandler;
import org.wso2.appserver.integration.tests.utils.PWCallback;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.module.mgt.stub.ModuleAdminServiceModuleMgtExceptionException;
import org.wso2.carbon.module.mgt.stub.types.ModuleMetaData;
import org.wso2.carbon.module.mgt.ui.client.ModuleManagementClient;
import org.wso2.carbon.rm.stub.service.RMAdminServiceStub;
import org.wso2.carbon.rm.stub.service.xsd.RMParameterBean;
import org.wso2.carbon.security.ui.client.SecurityAdminClient;

import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

import static org.testng.Assert.*;

/**
 * Tests 15 security scenarios and Reliable Messaging on Helloworld Axis2 service sample
 *
 * You need to patch the JDK to run some scenarios.
 * That is because there are key size restrictions with the default JDK comes with - which limits it to 128.
 * The solution can be found here -
 * http://blog.rampartfaq.com/2009/08/faq-001-javasecurityinvalidkeyexception.html
 */
public class HelloWorldSampleTestCase {

    public static final String QOS_VALUE_SECURE_RM = "securerm";

    // ==========================================================
    private static final String INVOCATION_TYPE_ASYNC = "async";
    private static final String INVOCATION_TYPE_SYNC = "sync";
    private static final String MODULE_SECURITY = "rampart";
    private static final String MODULE_RM = "sandesha2";

    private static String invocationType = null;
    private static String qosValue = null;

    String HTTPS_SERVICE_URL = null;
    String HTTP_SERVICE_URL = null;
    String RMADMINSERVICE_URL = null;
    EndpointReference HTTP_EPR = null;
    EndpointReference HTTPS_EPR = null;
    String[] operations = {"greet"};

    static final String SECURITY_TOKEN_ERROR_STR =
            "The security token could not be authenticated or authorized. " +
                    "\nPlease make sure this user is authorized to access the CommodityQuote service, or " +
                    "\nthat this user has a role which is authorized to access the CommodityQuote service.";

    private LoginLogoutUtil util = new LoginLogoutUtil();
    String SERVICE_NAME = "HelloService";
    ServiceClient serviceClient;
    String loggedInSessionCookie;

    ConfigurationContext configContext;

    private static final Log log = LogFactory.getLog(HelloWorldSampleTestCase.class);

    @BeforeMethod(groups = {"wso2.as"})
    public void login() throws java.lang.Exception {
        log.info("Inside Login Service in Security Scenarios Test");
        loggedInSessionCookie = util.login();

//        HTTP_SERVICE_URL = "http://localhost:9764/services/" + SERVICE_NAME;
//        HTTPS_SERVICE_URL = "https://localhost:9444/services/" + SERVICE_NAME;
        HTTPS_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + "/services/" + SERVICE_NAME;
        HTTP_SERVICE_URL = "http://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTP_PORT + "/services/" + SERVICE_NAME;

        RMADMINSERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + "/services/" + "RMAdminService.RMAdminServiceHttpsSoap12Endpoint";

        HTTP_EPR = new EndpointReference(HTTP_SERVICE_URL);
        HTTPS_EPR = new EndpointReference(HTTPS_SERVICE_URL);

        ClientConnectionUtil.waitForPort(Integer.parseInt(FrameworkSettings.HTTP_PORT));
        serviceClient = new ServiceClient();
        Options options = serviceClient.getOptions();
        options.setTo(new EndpointReference(HTTP_SERVICE_URL));
        options.setAction("urn:" + operations[0]);
//        options.setManageSession(true);
//        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
//                loggedInSessionCookie);
//        serviceClient.setOptions(options);
    }

    @BeforeMethod(groups = {"wso2.as"})
    public void init() throws AxisFault {
        configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(
                        System.getProperty("carbon.home") +
                                File.separator + "repository" +
                                File.separator + "deployment" +
                                File.separator + "client",
                        System.getProperty("carbon.home") +
                                File.separator + "repository" +
                                File.separator + "conf" +
                                File.separator + "axis2" +
                                File.separator + "axis2_client.xml"
                );
    }

    @Test(groups = {"wso2.as"}, enabled = true)
    public void testGreet() throws AxisFault, XMLStreamException {
        log.info("Inside greet test");
        ClientConnectionUtil.waitForPort(9763);
        serviceClient.getOptions().setTo(HTTP_EPR);

        OMElement result = serviceClient.sendReceive(createPayload());
        log.info("testGreet(): received response " + result);
        assertNotNull(result, "Result cannot be null");
        assertEquals(result.toString().trim(),
                "<ns:greetResponse xmlns:ns=\"http://www.wso2.org/types\">" +
                        "<return>Hello World, isuru !!!</return></ns:greetResponse>");
    }

    private OMElement createPayload() throws XMLStreamException {
        String request = "<p:greet xmlns:p=\"http://www.wso2.org/types\">" +
                "<name>isuru</name></p:greet>";
        return new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
    }

    private void doGreetWithSec() throws AxisFault, XMLStreamException {
        log.info("Inside greet test ");

        String request = "<p:greet xmlns:p=\"http://www.wso2.org/types\">" +
                "<name>kasun</name></p:greet>";
        OMElement payload = new StAXOMBuilder(new ByteArrayInputStream(request.getBytes())).getDocumentElement();
        OMElement result;
        if (INVOCATION_TYPE_ASYNC.equalsIgnoreCase(invocationType)) {
            log.info("Sending asynchronous request..");
            // run the stub in async two channel mode
            HelloServiceCallbackHandler callback = new HelloServiceCallbackHandler();
            serviceClient.sendReceiveNonBlocking(payload, callback);

            int i = 0;
            while (!callback.isComplete) {
                i++;
                if (i == 600) {
                    throw new RuntimeException("The onComplete in HelloServiceCallbackHandler didn't get invoked " +
                            "within 60 seconds");
                } else if (i % 20 == 0) {
                    log.info("Waiting till callback invokes onComplete in HelloServiceCallbackHandler");
                }
                try {

                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException("HelloServiceCallbackHandler for Reliable Messaging interrupted  " + e);
                }
            }

            result = callback.getMessageContext().getEnvelope().getBody().getFirstElement();
        } else {
            result = serviceClient.sendReceive(payload);        //synchronous
        }

        //remove unnecessary namespaces that gets added when sec added
        for (Iterator it = result.getAllDeclaredNamespaces(); it.hasNext(); ) {
            OMNamespace ns = (OMNamespace) it.next();
            if (!ns.getPrefix().equals("ns")) {
                it.remove();
            }
        }
        assertNotNull(result, "Result cannot be null");
        assertEquals("<ns:greetResponse xmlns:ns=\"http://www.wso2.org/types\">" +
                "<return>Hello World, kasun !!!</return></ns:greetResponse>",
                result.toString().trim());
    }

    @Test(groups = {"wso2.as"}, enabled = true)
    public void reliableMessagingTestCase() throws IOException, XMLStreamException,
            InterruptedException, ModuleAdminServiceModuleMgtExceptionException {
        log.info("Reliable Messaging Tests");
        invocationType = INVOCATION_TYPE_ASYNC;
        RMAdminServiceStub rmAdminServiceStub = enableReliableMessaging();
        configureRM();
        doGreetWithSec();

        Thread.sleep(4000);
        rmAdminServiceStub.disableRM(SERVICE_NAME);
        Thread.sleep(2000);
    }

    private RMAdminServiceStub enableReliableMessaging() throws RemoteException {
//        Setting default values for RM. borrowed from
//        components/reliable-messaging/org.wso2.carbon.rm.ui/src/main/resources/web/rm/index.jsp
        RMParameterBean rmParameterBean = new RMParameterBean();
        rmParameterBean.setInactivityTimeoutMeasure("seconds");
        rmParameterBean.setInactivityTimeoutInterval(60);

        rmParameterBean.setSequenceRemovalTimeoutMeasure("seconds");
        rmParameterBean.setSequenceRemovalTimeoutInterval(600);

        rmParameterBean.setAcknowledgementInterval(3000);
        rmParameterBean.setRetransmissionInterval(6000);
        rmParameterBean.setExponentialBackoff(true);
        rmParameterBean.setMaximumRetransmissionCount(10);

        ConfigurationContext configContext = ConfigurationContextFactory.
                createConfigurationContextFromFileSystem(
                        System.getProperty("carbon.home") +
                                File.separator + "repository" +
                                File.separator + "deployment" +
                                File.separator + "client",
                        System.getProperty("carbon.home") +
                                File.separator + "repository" +
                                File.separator + "conf" +
                                File.separator + "axis2" +
                                File.separator + "axis2_client.xml"
                );
        RMAdminServiceStub rmAdminServiceStub = new RMAdminServiceStub(configContext, RMADMINSERVICE_URL);
        ServiceClient client = rmAdminServiceStub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, loggedInSessionCookie);

        rmAdminServiceStub.setParameters(SERVICE_NAME, rmParameterBean);
        rmAdminServiceStub.enableRM(SERVICE_NAME);

        return rmAdminServiceStub;
    }

    /**
     * This tests all the security except 1 & 9 with Reliable Messaging enabled.
     * securityScenariosTestCase will be run before this test case.
     *
     * There's errors in the testcase. So, it is disabled for the moment
     *
     * @throws Exception
     */
    @Test(groups = {"wso2.as"}, dependsOnMethods = "securityScenariosTestCase", alwaysRun = true, enabled = false)
    public void secureRMTestCase() throws Exception {
        log.info("Secure-RM Tests");
        invocationType = INVOCATION_TYPE_ASYNC;
        log.info("Enabling RM for the service");

        RMAdminServiceStub rmAdminServiceStub = null;
        rmAdminServiceStub = enableReliableMessaging();
        configureOverallSecurity();

        for (int scenarioNumber = 1; scenarioNumber <= 15; scenarioNumber++) {
            log.info("Secure-RM: Current security scenario : " + scenarioNumber);
            if (scenarioNumber == 1 || scenarioNumber == 9) {
                log.error("Secure-RM not supported for scenarios 1 & 9 since HTTPS is required " +
                        "on the client side receiver. This is a limitation of the client.");
                continue;
            }
            SecurityAdminClient securityAdminClient = null;
            try {
                securityAdminClient = applySecurity(configContext, scenarioNumber);
                configureScenarioSecurity(scenarioNumber);

                configureRM();
                doGreetWithSec();
            } catch (XMLStreamException e) {
                e.printStackTrace();
            } catch (AxisFault axisFault) {
                axisFault.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ModuleAdminServiceModuleMgtExceptionException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Thread.sleep(3000);
                if (securityAdminClient != null) {
                    securityAdminClient.disableSecurityOnService(SERVICE_NAME);
                }
                Thread.sleep(2000);
            }
        }

        Thread.sleep(3000);
        rmAdminServiceStub.disableRM(SERVICE_NAME);
        Thread.sleep(2000);
    }

    /**
     * Tests whether the needed modules are successfully engaged
     *
     * @param neededModules The needed modules for security or AS
     * @throws AxisFault
     * @throws ModuleAdminServiceModuleMgtExceptionException
     *
     */
    private void checkEngagedModules(List<String> neededModules) throws AxisFault, ModuleAdminServiceModuleMgtExceptionException {
        log.info("Testing the presence of engaged modules");
        List<ModuleMetaData> modules;
//        String SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
//                ":" + FrameworkSettings.HTTPS_PORT + "/services/";

        ModuleManagementClient client = new ModuleManagementClient(configContext, FrameworkSettings.SERVICE_URL,
                loggedInSessionCookie, false);

        modules = Arrays.asList(client.listModulesForService(SERVICE_NAME));

        Set<String> engagedModules = new HashSet<String>(modules.size());
        for (ModuleMetaData module : modules) {
            engagedModules.add(module.getModulename());
        }

        for (String neededModule : neededModules) {
            log.info("Checking the module engaged - " + neededModule);
            assertTrue(
                    engagedModules.contains(neededModule));
        }
    }

    @Test(groups = {"wso2.as"}, dependsOnMethods = "testGreet", alwaysRun = true, enabled = true)
    public void securityScenariosTestCase() throws Exception {

        configureOverallSecurity();
        for (int scenarioNumber = 1; scenarioNumber <= 15; scenarioNumber++) {
            log.info("Current security scenario : " + scenarioNumber);

            SecurityAdminClient securityAdminClient = applySecurity(configContext, scenarioNumber);
            configureScenarioSecurity(scenarioNumber);
            doGreetWithSec();

            Thread.sleep(4000);
            securityAdminClient.disableSecurityOnService(SERVICE_NAME);
            Thread.sleep(2000);
        }
    }

    private void configureOverallSecurity() throws AxisFault, ModuleAdminServiceModuleMgtExceptionException {
        log.info("Configuring Overall Security for Testing Security Scenarios");
        ClientConnectionUtil.waitForPort(9763);

        serviceClient.engageModule(MODULE_SECURITY);
        serviceClient.engageModule(org.apache.axis2.Constants.MODULE_ADDRESSING);
        checkEngagedModules(Arrays.asList(MODULE_SECURITY, Constants.MODULE_ADDRESSING));
        String fileName = "keys/wso2carbon.jks";
        String clientSSLStore = getClass().getClassLoader().getResource(fileName).getFile();

        log.info("Keystore used is = " + clientSSLStore);
        System.getProperties().remove("javax.net.ssl.trustStore");
        System.getProperties().remove("javax.net.ssl.trustStoreType");
        System.getProperties().remove("javax.net.ssl.trustStorePassword");

        System.setProperty("javax.net.ssl.trustStore", clientSSLStore);
        System.setProperty("javax.net.ssl.trustStoreType", "JKS");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        log.info("\n 1. UsernameToken\n" +
                " 2. Non-repudiation\n" +
                " 3. Integrity \t\n" +
                " 4. Confidentiality \n" +
                " 5. Sign and encrypt - X509 Authentication\n" +
                " 6. Sign and Encrypt - Anonymous clients \n" +
                " 7. Encrypt only - Username Token Authentication \n" +
                " 8. Sign and Encrypt - Username Token Authentication\n" +
                " 9. SecureConversation - Sign only - Service as STS - Bootstrap policy - " +
                "Sign and Encrypt , X509 Authentication\n" +
                "10. SecureConversation - Encrypt only - Service as STS - Bootstrap policy - " +
                "Sign and Encrypt , X509 Authentication\n" +
                "11. SecureConversation - Sign and Encrypt - Service as STS - Bootstrap policy - " +
                "Sign and Encrypt , X509 Authentication \n" +
                "12. SecureConversation - Sign Only - Service as STS - Bootstrap policy - " +
                "Sign and Encrypt , Anonymous clients \t\n" +
                "13. SecureConversation - Encrypt Only - Service as STS - Bootstrap policy - " +
                "Sign and Encrypt , Anonymous clients \t\n" +
                "14. SecureConversation - Encrypt Only - Service as STS - Bootstrap policy - " +
                "Sign and Encrypt , Username Token Authentication \t\n" +
                "15. SecureConversation - Sign and Encrypt - Service as STS - Bootstrap policy - " +
                "Sign and Encrypt , Username Token Authentication \n");
    }

    private void configureRM() throws IOException, ModuleAdminServiceModuleMgtExceptionException {
        log.info("Configuring Reliable Messaging on the client");
        serviceClient.engageModule(MODULE_RM);
        serviceClient.engageModule(org.apache.axis2.Constants.MODULE_ADDRESSING);
        checkEngagedModules(Arrays.asList(MODULE_RM, Constants.MODULE_ADDRESSING));

        Options clientOptions = serviceClient.getOptions();

        String sequenceKey = UUIDGenerator.getUUID();  //sequence key for thie sequence.
        clientOptions.setProperty(SandeshaClientConstants.SEQUENCE_KEY, sequenceKey);

        clientOptions.setProperty(SandeshaClientConstants.SANDESHA_LISTENER,
                new SandeshaListenerImpl());

        clientOptions.setTransportInProtocol(Constants.TRANSPORT_HTTP);
        clientOptions.setUseSeparateListener(true);
        clientOptions.setProperty(SandeshaClientConstants.SANDESHA_LISTENER,
                new SandeshaListenerImpl());

        String offeredSequenceId = UUIDGenerator.getUUID();
        clientOptions.setProperty(SandeshaClientConstants.OFFERED_SEQUENCE_ID, offeredSequenceId);
    }

    private SecurityAdminClient applySecurity(ConfigurationContext configContext, int scenarioNumber) throws Exception {
        SecurityAdminClient securityAdminClient = new SecurityAdminClient(loggedInSessionCookie,
                FrameworkSettings.SERVICE_URL, configContext);
        String scenarioId = "scenario" + scenarioNumber;
        String policyPath = null;
//                String[] userGroups = {"everyone", "admin"}; //The user roles
        String[] userGroups = {"everyone"}; //The user roles
        String privateStore = "wso2carbon.jks";
        String[] trustedStores = {privateStore};
        securityAdminClient.applySecurity(SERVICE_NAME, scenarioId, policyPath, trustedStores,
                privateStore, userGroups);

        return securityAdminClient;
    }

    public void configureScenarioSecurity(int scenarioNumber) throws XMLStreamException, AxisFault,
            FileNotFoundException {
        Options options = serviceClient.getOptions();
        if (scenarioNumber == 1) {
            // Use HTTPS EPR
            options.setTo(HTTPS_EPR);
            configureUtSec(serviceClient, scenarioNumber);
        } else if (scenarioNumber == 7 || scenarioNumber == 8 ||
                scenarioNumber == 14 || scenarioNumber == 15) {  // All UT scenarios which involve keys
            // Use HTTP EPR WITH USER + Keystore config
            options.setTo(HTTP_EPR);
            configureUtKeystoreSec(serviceClient, scenarioNumber);
        } else {   // Scenarios only involving keys
            // Use HTTP EPR with Keystore config
            options.setTo(HTTP_EPR);
            configureKeystoreSec(serviceClient, scenarioNumber);
        }
//        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.CHUNKED, Boolean.FALSE);
    }

    private void configureUtSec(ServiceClient serviceClient, int scenario) throws AxisFault, FileNotFoundException,
            XMLStreamException {
//        if (scenario == 1 && isMailEPR) {
//            System.out.println(
//                    "Username token scenario should not work with mail transport. " +
//                            "\n Please load the application again");
//            System.exit(0);
//            return;
//        }
        // username token
        RampartConfig rc = new RampartConfig();
//        String username = "bob";
//        String password = "password";
        String username = "admin";
        String password = "admin";
        rc.setUser(username);
        PWCallback.addUser(username, password);
        rc.setPwCbClass(PWCallback.class.getName());

        Policy policy = loadPolicy(scenario);
        policy.addAssertion(rc);

        if (QOS_VALUE_SECURE_RM.equals(qosValue)) {
//            TODO added per testing securerm scenario
            serviceClient.getServiceContext().getConfigurationContext()
                    .getAxisConfiguration().getPolicyInclude().addPolicyElement(PolicyInclude.AXIS_POLICY, policy);
        }

        serviceClient.getServiceContext()
                .setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
    }

    private void configureUtKeystoreSec(ServiceClient serviceClient, int scenario)
            throws AxisFault, FileNotFoundException,
            XMLStreamException {
//        System.out.println("Client will use client.jks and server should use service.jks.");
        RampartConfig rc = new RampartConfig();
        Policy policy = loadPolicy(scenario);

        String username = "admin";
        String password = "admin";
        rc.setUser(username);
        PWCallback.addUser(username, password);
        rc.setPwCbClass(PWCallback.class.getName());

        rc.setUserCertAlias("wso2carbon");
        rc.setEncryptionUser("wso2carbon");
        rc.setPwCbClass(PWCallback.class.getName());

        CryptoConfig sigCryptoConfig = new CryptoConfig();
        sigCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");

        //todo evaluate the possiblity of using client and server.jks keystores.
        //That involves uploading the service.jks keystore to the server.
        String keystore = getClass().getClassLoader().getResource("keys/wso2carbon.jks").getFile();
//        String keystore = getClass().getClassLoader().getResource("keys/client.jks").getFile();

        Properties prop1 = new Properties();
        prop1.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
        prop1.put("org.apache.ws.security.crypto.merlin.file", keystore);
        prop1.put("org.apache.ws.security.crypto.merlin.keystore.password", "wso2carbon");
//        prop1.put("org.apache.ws.security.crypto.merlin.keystore.password", "testing");
        sigCryptoConfig.setProp(prop1);

        CryptoConfig encrCryptoConfig = new CryptoConfig();
        encrCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");

        Properties prop2 = new Properties();
        prop2.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
        prop2.put("org.apache.ws.security.crypto.merlin.file", keystore);
        prop2.put("org.apache.ws.security.crypto.merlin.keystore.password", "wso2carbon");
//        prop2.put("org.apache.ws.security.crypto.merlin.keystore.password", "testing");
        encrCryptoConfig.setProp(prop2);

        rc.setSigCryptoConfig(sigCryptoConfig);
        rc.setEncrCryptoConfig(encrCryptoConfig);

        policy.addAssertion(rc);
        serviceClient.getServiceContext()
                .setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);
        if (QOS_VALUE_SECURE_RM.equals(qosValue)) {
            serviceClient.getServiceContext().getConfigurationContext()
                    .getAxisConfiguration().getPolicyInclude().addPolicyElement(PolicyInclude.AXIS_POLICY, policy);
        }
    }

    private void configureKeystoreSec(ServiceClient serviceClient, int scenario) throws FileNotFoundException,
            XMLStreamException {
        RampartConfig rc = new RampartConfig();
        Policy policy = loadPolicy(scenario);
        rc.setUser("admin");
        rc.setUserCertAlias("wso2carbon");
        rc.setEncryptionUser("wso2carbon");
        PWCallback.addUser("wso2carbon", "wso2carbon");
        rc.setPwCbClass(PWCallback.class.getName());

        CryptoConfig sigCryptoConfig = new CryptoConfig();
        sigCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");

        String keystore = getClass().getClassLoader().getResource("keys/wso2carbon.jks").getFile();
//        String keystore = getClass().getClassLoader().getResource("keys/client.jks").getFile();

        Properties prop1 = new Properties();
        prop1.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
        prop1.put("org.apache.ws.security.crypto.merlin.file", keystore);
        prop1.put("org.apache.ws.security.crypto.merlin.keystore.password", "wso2carbon");
//        prop1.put("org.apache.ws.security.crypto.merlin.keystore.password", "testing");
        sigCryptoConfig.setProp(prop1);

        CryptoConfig encrCryptoConfig = new CryptoConfig();
        encrCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");

        Properties prop2 = new Properties();
        prop2.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
        prop2.put("org.apache.ws.security.crypto.merlin.file", keystore);
        prop2.put("org.apache.ws.security.crypto.merlin.keystore.password", "wso2carbon");
        encrCryptoConfig.setProp(prop2);

        rc.setSigCryptoConfig(sigCryptoConfig);
        rc.setEncrCryptoConfig(encrCryptoConfig);
        rc.setTimestampTTL("300");

        policy.addAssertion(rc);
        serviceClient.getServiceContext()
                .setProperty(RampartMessageData.KEY_RAMPART_POLICY, policy);

        if (QOS_VALUE_SECURE_RM.equals(qosValue)) {
            serviceClient.getServiceContext().getConfigurationContext()
                    .getAxisConfiguration().getPolicyInclude().addPolicyElement(PolicyInclude.AXIS_POLICY, policy);
        }
    }

    private Policy loadPolicy(int scenario) throws FileNotFoundException,
            XMLStreamException {
        String policyFile = getClass().getClassLoader().getResource("conf/rampart/scenario" + scenario + "-policy.xml").getFile();
        StAXOMBuilder builder = new StAXOMBuilder(policyFile);
        log.info("Policy File - " + policyFile);
        return PolicyEngine.getPolicy(builder.getDocumentElement());
    }

    private static class SandeshaListenerImpl implements SandeshaListener {
        public void onError(AxisFault fault) {
            System.out.println("ERROR:" + fault.getMessage());
        }

        public void onTimeOut(SequenceReport report) {
            System.out.println("ERROR: RM Sequence timed out");
        }
    }


}
