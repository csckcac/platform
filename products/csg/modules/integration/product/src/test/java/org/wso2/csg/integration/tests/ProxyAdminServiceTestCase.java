/*
 * Copyright WSO2, Inc. (http://wso2.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.csg.integration.tests;

import org.apache.axis2.AxisFault;
import org.apache.synapse.SynapseConstants;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminProxyAdminException;
import org.wso2.carbon.proxyadmin.stub.ProxyServiceAdminStub;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;

import java.rmi.RemoteException;

import static org.testng.Assert.*;

/**
 * CSG depends on the proxy admin service heavily to deploy.update/undeploy the proxy
 */
public class ProxyAdminServiceTestCase extends CSGIntegrationTestCase {
    private ProxyServiceAdminStub proxyServiceAdminStub;
    private static final String PROXY_NAME = "CSGTestProxy";

    public ProxyAdminServiceTestCase() {
        super("ProxyServiceAdmin");
    }

    public void init() throws Exception {
        proxyServiceAdminStub = new ProxyServiceAdminStub(getAdminServiceURL());
        authenticate(proxyServiceAdminStub);
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        try {
            proxyServiceAdminStub.cleanup();
        } catch (AxisFault axisFault) {
            log.error("Error while cleaning up the proxy admin stub", axisFault);
        }
    }

    @Test(groups = {"wso2.csg"},
            description = "Test the available transports to check if http https transports are available.")
    public void testGetTransports() throws RemoteException, ProxyServiceAdminProxyAdminException {
        String[] transports = proxyServiceAdminStub.getAvailableTransports();
        assertTrue(transports != null && transports.length > 0 && transports[0] != null);
        boolean httpFound = false;
        boolean httpsFound = false;
        for (String t : transports) {
            if ("https".equals(t)) {
                httpsFound = true;
            } else if ("http".equals(t)) {
                httpFound = true;
            }
        }
        assertTrue(httpFound && httpsFound);
    }

    @Test(description = "Test deploying a CSG proxy.",
            expectedExceptions = {RemoteException.class, ProxyServiceAdminProxyAdminException.class})
    public void testProxyOperations()
            throws RemoteException, ProxyServiceAdminProxyAdminException {
        String epr = "csg://testServer/SimpleStockQuoteService";
        if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            epr = "csg://testDomain/testServer/SimpleStockQuoteService";
        }

        ProxyData proxyData = new ProxyData();
        proxyData.setName(PROXY_NAME);
        proxyData.setInSeqXML("<inSequence xmlns=\"" + SynapseConstants.SYNAPSE_NAMESPACE + "\">" +
                "<property name=\"transportNonBlocking\" scope=\"axis2\" action=\"remove\"/>" +
                "<property name=\"preserveProcessedHeaders\" value=\"true\"/> + " +
                "<property name=\"OUT_ONLY\" scope=\"axis2\" action=\"set\" value=\"true\"/>" +
                "</inSequence>");

        proxyData.setOutSeqXML("<outSequence xmlns=\"http://ws.apache.org/ns/synapse\"><send /></outSequence>");
        proxyData.setEndpointXML("<endpoint xmlns=\"" + SynapseConstants.SYNAPSE_NAMESPACE + "\">" +
                "<address uri=\"" + epr + "\">" +
                "<suspendOnFailure>" +
                "<errorCodes>400207</errorCodes>" +
                "<initialDuration>1000</initialDuration>" +
                "<progressionFactor>2</progressionFactor>" +
                "<maximumDuration>64000</maximumDuration>" +
                "</suspendOnFailure>" +
                "</address>" +
                "</endpoint>");
        proxyData.setFaultSeqXML("<faultSequence xmlns=\"" + SynapseConstants.SYNAPSE_NAMESPACE + "\">" +
                "<log level=\"full\"/>" +
                "<drop/>" +
                "</faultSequence>");
        String wsdl = null;
        // FIXME - the CSG proxy actually persist a WSDL into the registry and use that as the WSDL
        // FIXME - of the proxy, add a test for that too
        try {
            proxyServiceAdminStub.addProxy(proxyData);
        } catch (Exception e) {
            fail("Deploying proxy failed!", e);
        }
        assertNotNull(proxyServiceAdminStub.getProxy(PROXY_NAME));

        proxyServiceAdminStub.enableStatistics(PROXY_NAME);
        ProxyData newProxy = proxyServiceAdminStub.getProxy(PROXY_NAME);
        assertTrue(newProxy.getEnableStatistics());

        proxyServiceAdminStub.disableStatistics(PROXY_NAME);
        newProxy = proxyServiceAdminStub.getProxy(PROXY_NAME);
        assertFalse(newProxy.getEnableStatistics());

        proxyServiceAdminStub.enableTracing(PROXY_NAME);
        newProxy = proxyServiceAdminStub.getProxy(PROXY_NAME);
        assertTrue(newProxy.getEnableTracing());

        proxyServiceAdminStub.disableTracing(PROXY_NAME);
        newProxy = proxyServiceAdminStub.getProxy(PROXY_NAME);
        assertFalse(newProxy.getEnableTracing());

        proxyServiceAdminStub.deleteProxyService(PROXY_NAME);

        assertNull(proxyServiceAdminStub.getProxy(PROXY_NAME));
    }
}
