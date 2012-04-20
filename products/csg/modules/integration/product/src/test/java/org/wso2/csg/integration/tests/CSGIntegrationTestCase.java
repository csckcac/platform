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

import org.apache.axis2.client.Stub;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.proxyadmin.stub.types.carbon.ProxyData;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * An abstract class for all WSO2 CSG integration tests. This class contains
 * some useful utils when writing test cases for WSO2 CSG. It's encourage to
 * extend a new test case from this class
 */
public abstract class CSGIntegrationTestCase {

    protected Log log = LogFactory.getLog(CSGIntegrationTestCase.class);

    private String adminService;

    protected CSGIntegrationTestCase(String adminService) {
        this.adminService = adminService;
    }

    @BeforeMethod(groups = "wso2.csg")
    public final void doInit() throws Exception {
        init();
    }

    @AfterMethod(groups = "wso2.csg")
    public final void doCleanup() {
        cleanup();
    }

    /**
     * This method gets called before each test method. Override this to do any
     * test resource initialization so that everything is properly set up before
     * the test method is invoked. Do not use TestNG annotations on the override
     * method.
     *
     * @throws Exception
     */
    protected void init() throws Exception {
    }

    /**
     * This method gets called after each test method. If override in a sub class
     * make sure to call super.cleanup() to make sure this get called. Also do not
     * use any TestNg annotations on the override method.
     */
    protected void cleanup() {
        // FIXME to do any cleanup tasks
    }

    protected String getProxyServiceURL(String service, boolean isServletTransport) {
        String port = FrameworkSettings.NHTTP_PORT;

        if (isServletTransport) {
            port = FrameworkSettings.HTTP_PORT;
        }

        String url = "http://" + FrameworkSettings.HOST_NAME + ":" + port + "/services/";
        if (FrameworkSettings.STRATOS.equalsIgnoreCase("false")) {
            return url + service;
        } else {
            // FIXME - fix this to return the "correct(?)" url for stratos
            return url + "/t/" + FrameworkSettings.TENANT_NAME + "/" + service;
        }
    }

    /**
     * If this test instance was initialized with a name of an admin service, this
     * method returns the URL of the specified admin service. Otherwise it returns
     * null.
     *
     * @return An admin service URL or null
     */
    protected String getAdminServiceURL() {
        if (adminService != null) {
            return getAdminServiceURL(adminService);
        }
        return null;
    }

    /**
     * Returns the URL of the specified admin service. This method does not validate
     * whether the specified admin service name is real.
     *
     * @param service Name of an admin service
     * @return URL of the specified service
     */
    protected String getAdminServiceURL(String service) {
        return "https://" + FrameworkSettings.HOST_NAME + ":" + FrameworkSettings.HTTPS_PORT +
                "/services/" + service;
    }

    /**
     * Authenticate the given web service stub against the ESB user manager. This
     * will make it possible to use the stub for invoking ESB admin services.
     *
     * @param stub Axis2 service stub which needs to be authenticated
     */
    protected void authenticate(Stub stub) {
        CarbonUtils.setBasicAccessSecurityHeaders(FrameworkSettings.USER_NAME,
                FrameworkSettings.PASSWORD, stub._getServiceClient());
    }

    /**
     * Create a proxy data object for the proxy
     *
     * @param proxyName proxy service name
     * @param epr       EPR of the proxy
     * @return the created proxy data object
     */
    protected ProxyData createProxyData(String proxyName, String epr) {
        ProxyData proxyData = new ProxyData();
        proxyData.setName(proxyName);
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
        return proxyData;
    }

    protected String getTestProxyEPR(String serviceName, String serverName, String domainName) {
        String epr = "csg://" + serverName + "/" + serviceName;
        if (FrameworkSettings.STRATOS.equalsIgnoreCase("true")) {
            epr = "csg://" + domainName + "/" + serverName + "/"+ serviceName;
        }
        return epr;
    }
}
