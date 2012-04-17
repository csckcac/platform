/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.platform.test.core.utils.axis2client;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.model.CryptoConfig;
import org.apache.rampart.policy.model.RampartConfig;
import org.apache.ws.security.WSPasswordCallback;
import org.testng.Assert;
import org.wso2.platform.test.core.ProductConstant;
import org.wso2.platform.test.core.utils.environmentutils.EnvironmentBuilder;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

public class SecureAxisServiceClient implements CallbackHandler {
    private static final Log log = LogFactory.getLog(SecureAxisServiceClient.class);

    public OMElement sendReceive(String userName, String password, String endpointReference, String operation, OMElement payload, int securityScenarioNo) throws Exception {
        ServiceClient sc = getServiceClient(userName, password, endpointReference, operation, securityScenarioNo);
        OMElement result;
        log.debug("payload :" + payload);
        log.debug("Security Scenario No :" + securityScenarioNo);
        log.debug("Operation :" + operation);
        log.info("Endpoint reference :" + endpointReference);
        log.debug("username :" + userName);
        log.debug("password :" + password);

        try {
            result = sc.sendReceive(payload);
            log.debug("Response :" + result);
        } catch (AxisFault axisFault) {
            log.error("AxisFault : " + axisFault.getMessage());
            throw new AxisFault("AxisFault : " + axisFault.getMessage(), axisFault);
        }
        Assert.assertNotNull( result);
        return result;


    }

    public void sendRobust(String userName, String password, String endpointReference, String operation, OMElement payload, int securityScenarioNo) throws Exception {
        ServiceClient sc = getServiceClient(userName, password, endpointReference, operation, securityScenarioNo);
        try {
            sc.sendRobust(payload);
            log.info("Request Sent");
        } catch (AxisFault axisFault) {
            log.error("AxisFault : " + axisFault.getMessage());
            throw new AxisFault("AxisFault : " + axisFault.getMessage(), axisFault);
        }
    }

    private Policy loadPolicy(String userName, String securityPolicyPath, String keyPath) throws Exception {

        Policy policy = null;
        StAXOMBuilder builder = null;

        try {
            builder = new StAXOMBuilder(securityPolicyPath);
            policy = PolicyEngine.getPolicy(builder.getDocumentElement());

            RampartConfig rc = new RampartConfig();

            rc.setUser(userName);

            rc.setUserCertAlias("wso2carbon");
            rc.setEncryptionUser("wso2carbon");
            rc.setPwCbClass(SecureAxisServiceClient.class.getName());
            String pass = "wso2carbon";


            CryptoConfig sigCryptoConfig = new CryptoConfig();

            sigCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");


            Properties prop1 = new Properties();
            prop1.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
            prop1.put("org.apache.ws.security.crypto.merlin.file", keyPath);
            prop1.put("org.apache.ws.security.crypto.merlin.keystore.password", pass);

            sigCryptoConfig.setProp(prop1);

            CryptoConfig encrCryptoConfig = new CryptoConfig();
            encrCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");

            Properties prop2 = new Properties();

            prop2.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
            prop2.put("org.apache.ws.security.crypto.merlin.file", keyPath);
            prop2.put("org.apache.ws.security.crypto.merlin.keystore.password", pass);

            encrCryptoConfig.setProp(prop2);

            rc.setSigCryptoConfig(sigCryptoConfig);
            rc.setEncrCryptoConfig(encrCryptoConfig);

            policy.addAssertion(rc);
        } finally {
            if (builder != null) {
                builder.close();
            }
        }
        Assert.assertNotNull(policy, "Policy cannot be null");
        return policy;
    }

    private ServiceClient getServiceClient(String userName, String password, 
                                           String endpointReference, String operation, 
                                           int securityScenarioNo) throws Exception {
        EnvironmentBuilder environmentBuilder = new EnvironmentBuilder();

        String keyPath = 
                environmentBuilder.getFrameworkSettings().getEnvironmentVariables().getKeystorePath();
        String securityPolicyPath = 
                ProductConstant.getSecurityScenarios() + File.separator +
                 "scenario"+ securityScenarioNo + "-policy.xml";

        log.debug("Key_Path :" + keyPath);
        log.debug("securityPolicyPath :" + securityPolicyPath);

        System.setProperty("javax.net.ssl.trustStore", keyPath);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
        log.debug("javax.net.ssl.trustStore :" + System.getProperty("javax.net.ssl.trustStore"));
        log.debug("javax.net.ssl.trustStorePassword :" + System.getProperty("javax.net.ssl.trustStorePassword"));

        ConfigurationContext ctx;
        ServiceClient sc;
        try {
            ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem
                    (ProductConstant.getModuleClientPath(), null);

            sc = new ServiceClient(ctx, null);

            sc.engageModule("rampart");
            sc.engageModule("addressing");

            Options opts = new Options();
            if (securityScenarioNo == 1) {
                Assert.assertTrue(endpointReference.startsWith("https:"),"Endpoint reference should be https");
            } else {
                Assert.assertTrue(endpointReference.startsWith("http:"),"Endpoint reference should be https");
            }

            opts.setTo(new EndpointReference(endpointReference));
            opts.setAction("urn:" + operation);
            //setting user credential
            opts.setUserName(userName);
            opts.setPassword(password);

            try {
                if (securityScenarioNo >= 1)
                    opts.setProperty(RampartMessageData.KEY_RAMPART_POLICY,
                                     loadPolicy(userName, securityPolicyPath, keyPath));

            } catch (Exception e) {
                log.error(e.getMessage());
                throw new Exception(e.getMessage(), e);
            }

            sc.setOptions(opts);
        } catch (AxisFault axisFault) {
            log.error("AxisFault : " + axisFault.getMessage());
            throw new RuntimeException("AxisFault : " + axisFault.getMessage(), axisFault);
        }
        Assert.assertNotNull("ServiceClient object is null" + sc);
        return sc;
    }

    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[0];
        String id = pwcb.getIdentifer();
        int usage = pwcb.getUsage();

        if (usage == WSPasswordCallback.SIGNATURE || usage == WSPasswordCallback.DECRYPT) {
            // Logic to get the private key password for signture or decryption
            if ("client".equals(id)) {
                pwcb.setPassword("apache");
            }
            if ("service".equals(id)) {
                pwcb.setPassword("apache");

            }
            if ("wso2carbon".equals(id)) {
                pwcb.setPassword("wso2carbon");
            }
        }
    }
}
