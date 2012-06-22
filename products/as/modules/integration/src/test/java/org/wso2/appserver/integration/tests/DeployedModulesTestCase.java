/*
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.appserver.integration.tests;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;
import org.wso2.carbon.integration.framework.utils.FrameworkSettings;
import org.wso2.carbon.module.mgt.stub.ModuleAdminServiceStub;
import org.wso2.carbon.module.mgt.stub.types.ModuleMetaData;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertTrue;

/**
 * Tests whether all the default modules of AS are correctly deployed
 */
public class DeployedModulesTestCase {

    private static final Log log = LogFactory.getLog(DeployedModulesTestCase.class);

    private LoginLogoutUtil util = new LoginLogoutUtil();
    private ModuleAdminServiceStub moduleAdminServiceStub;

    @BeforeMethod(groups = {"wso2.as"})
    public void login() throws java.lang.Exception {
        log.info("****Inside Login Service in Deployed Modules Test*****");
        String loggedInSessionCookie = util.login();

        String moduleAdminServiceURL  = "https://" + FrameworkSettings.HOST_NAME +
                ":" + FrameworkSettings.HTTPS_PORT + "/services/ModuleAdminService";

        moduleAdminServiceStub =
                new ModuleAdminServiceStub(moduleAdminServiceURL);
        ServiceClient client = moduleAdminServiceStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING,
                loggedInSessionCookie);
    }

    @AfterClass(groups = {"wso2.as"})
    public void logout() throws java.lang.Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
        log.info("****Inside Log Out Service in Deployment Test*****");
    }

    @Test(groups = {"wso2.as"})
    public void init() {
        String samplesDir = System.getProperty("samples.dir");
    }

    @Test(groups = {"wso2.as"},dependsOnMethods = {"init"}, enabled = true)
    public void testDeployedModules() throws RemoteException, XMLStreamException {
        Set<String> mDataSet = new HashSet<String>();

        if (moduleAdminServiceStub.listModules() != null) {

            for (ModuleMetaData mData : moduleAdminServiceStub.listModules()) {
                log.info("Found module - " + mData.getModuleId());
                mDataSet.add(mData.getModulename());
            }

            assertTrue(mDataSet.contains("wso2throttle"));
            assertTrue(mDataSet.contains("wso2xfer"));
            assertTrue(mDataSet.contains("wso2mex"));
            assertTrue(mDataSet.contains("wso2caching"));
            assertTrue(mDataSet.contains("rahas"));
            assertTrue(mDataSet.contains("addressing"));
            assertTrue(mDataSet.contains("sandesha2"));
            assertTrue(mDataSet.contains("rampart"));
        }
    }
}
