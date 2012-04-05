/* Copyright (c) 2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


package org.wso2.bps.integration.tests.security.utils;

import junit.framework.Assert;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.AXIOMUtil;
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
import org.wso2.bps.integration.tests.util.BPSTestUtils;
import org.wso2.bps.integration.tests.util.FrameworkSettings;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.File;
import java.io.IOException;
import java.util.Properties;


public class SecurityClientUtils implements CallbackHandler {
    private static final Log log = LogFactory.getLog(SecurityClientUtils.class);

    public static OMElement runSecurityClient(int scenarioNo, String serviceName, String soapAction,
                                              String payload, String expectedString)
            throws Exception {


        String trustStore = null;
        String endpointHttpS = null;
        String endpointHttp = null;
        String securityPolicy = null;
        String clientKey = null;
        OMElement result;


        String carbonHome = System.getProperty("user.dir");
        securityPolicy = BPSTestUtils.BPEL_TEST_RESOURCE_LOCATION + "policyFiles/";

        trustStore = carbonHome + File.separator + "repository" + File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
        clientKey = carbonHome + File.separator + "repository" + File.separator + "resources" + File.separator + "security" + File.separator + "wso2carbon.jks";
        endpointHttpS = "https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT + "/services/" + serviceName;
        endpointHttp = "http://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTP_PORT + "/services/" + serviceName;
        System.setProperty("javax.net.ssl.trustStore", trustStore);
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        ConfigurationContext ctx = ConfigurationContextFactory.createConfigurationContextFromFileSystem(carbonHome + File.separator + "repository" + File.separator + "deployment" + File.separator + "client", null);
        ServiceClient sc = new ServiceClient(ctx, null);
        sc.engageModule("addressing");
        sc.engageModule("rampart");

        Options opts = new Options();

        if (scenarioNo == 1) {
            opts.setTo(new EndpointReference(endpointHttpS));
            log.info(endpointHttpS);
        } else {
            opts.setTo(new EndpointReference(endpointHttp));
            log.info(endpointHttp);
        }

        opts.setAction(soapAction);

        if (scenarioNo != 0) {
            try {
                String securityPolicyPath = securityPolicy + File.separator + "scenario" + scenarioNo + "-policy.xml";
                log.info("SecurityPolicyPath " + securityPolicyPath);
                opts.setProperty(RampartMessageData.KEY_RAMPART_POLICY, loadPolicy(securityPolicyPath, clientKey));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sc.setOptions(opts);
        result = sc.sendReceive(AXIOMUtil.stringToOM(payload));
        log.info(result.getFirstElement().getText());
        Assert.assertFalse("Incorrect Test Result: " + result.toString(),
                           !result.toString().contains(expectedString));
        return result;
    }

    private static Policy loadPolicy(String xmlPath, String clientKey) throws Exception {

        StAXOMBuilder builder = new StAXOMBuilder(xmlPath);
        Policy policy = PolicyEngine.getPolicy(builder.getDocumentElement());

        RampartConfig rc = new RampartConfig();

        rc.setUser("admin");
        rc.setUserCertAlias("wso2carbon");
        rc.setEncryptionUser("wso2carbon");
        rc.setPwCbClass(SecurityClientUtils.class.getName());

        CryptoConfig sigCryptoConfig = new CryptoConfig();
        sigCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");

        Properties prop1 = new Properties();
        prop1.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
        prop1.put("org.apache.ws.security.crypto.merlin.file", clientKey);
        prop1.put("org.apache.ws.security.crypto.merlin.keystore.password", "wso2carbon");
        sigCryptoConfig.setProp(prop1);

        CryptoConfig encrCryptoConfig = new CryptoConfig();
        encrCryptoConfig.setProvider("org.apache.ws.security.components.crypto.Merlin");

        Properties prop2 = new Properties();
        prop2.put("org.apache.ws.security.crypto.merlin.keystore.type", "JKS");
        prop2.put("org.apache.ws.security.crypto.merlin.file", clientKey);
        prop2.put("org.apache.ws.security.crypto.merlin.keystore.password", "wso2carbon");
        encrCryptoConfig.setProp(prop2);

        rc.setSigCryptoConfig(sigCryptoConfig);
        rc.setEncrCryptoConfig(encrCryptoConfig);

        policy.addAssertion(rc);
        return policy;
    }


    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {

        WSPasswordCallback pwcb = (WSPasswordCallback) callbacks[0];
        String id = pwcb.getIdentifier();
        int usage = pwcb.getUsage();

        if (usage == WSPasswordCallback.USERNAME_TOKEN) {

            if ("admin".equals(id)) {
                pwcb.setPassword("admin");
            }

        } else if (usage == WSPasswordCallback.SIGNATURE || usage == WSPasswordCallback.DECRYPT) {

            if ("wso2carbon".equals(id)) {
                pwcb.setPassword("wso2carbon");
            }
        }
    }
}
