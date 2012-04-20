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
import org.testng.annotations.Test;
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

    @Test(groups = {"wso2.csg"},
            description = "Test deploying a CSG proxy.")
    public void testProxyOperations()
            throws RemoteException, ProxyServiceAdminProxyAdminException {

        String proxyName = "TestProxy";

        // FIXME - the CSG proxy actually persist a WSDL into the registry and use that as the WSDL
        // FIXME - of the proxy, add a test for that too
        try {
            proxyServiceAdminStub.addProxy(
                    createProxyData(proxyName, getTestProxyEPR(
                            proxyName, "testServer", "testDomain")));
        } catch (Exception e) {
            fail("Deploying proxy failed!", e);
        }
        assertNotNull(proxyServiceAdminStub.getProxy(proxyName));

        proxyServiceAdminStub.enableStatistics(proxyName);
        ProxyData newProxy = proxyServiceAdminStub.getProxy(proxyName);
        assertTrue(newProxy.getEnableStatistics());

        proxyServiceAdminStub.disableStatistics(proxyName);
        newProxy = proxyServiceAdminStub.getProxy(proxyName);
        assertFalse(newProxy.getEnableStatistics());

        proxyServiceAdminStub.enableTracing(proxyName);
        newProxy = proxyServiceAdminStub.getProxy(proxyName);
        assertTrue(newProxy.getEnableTracing());

        proxyServiceAdminStub.disableTracing(proxyName);
        newProxy = proxyServiceAdminStub.getProxy(proxyName);
        assertFalse(newProxy.getEnableTracing());

        proxyServiceAdminStub.deleteProxyService(proxyName);

    }
}
