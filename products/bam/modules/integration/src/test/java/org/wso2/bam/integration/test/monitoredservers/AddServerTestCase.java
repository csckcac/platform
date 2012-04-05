package org.wso2.bam.integration.test.monitoredservers;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.test.common.utils.CommonUtils;
import org.wso2.carbon.bam.stub.configadmin.BAMConfigAdminServiceBAMException;
import org.wso2.carbon.bam.stub.configadmin.BAMConfigAdminServiceStub;
import org.wso2.carbon.bam.stub.configadmin.types.carbon.ServerDO;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import java.rmi.RemoteException;

/**
 * This test case verifies whether a up running server could be added to the BAM server successfully
 */
public class AddServerTestCase {

    public static final int SERVER_SUCCESSFULLY_ADDED = 0;
    private static final Log log = LogFactory.getLog(AddServerTestCase.class);
    public static final int SERVER_ALREADY_ADDED = 1;
    private BAMConfigAdminServiceStub configAdminServiceStub;

    private LoginLogoutUtil util = new LoginLogoutUtil();

    @BeforeClass(groups = {"wso2.bam"})
    public void login() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        String sessionCookie = util.login();
        initStubs(sessionCookie);
    }

    @Test(groups = {"wso2.bam.test"}, description = "Test for adding monitoring server")
    public void addServer() throws BAMConfigAdminServiceBAMException, RemoteException {
        String serverUrl = "https://127.0.0.1:9443";

        ServerDO serverOne = BAMServerUtils.createServerObject(serverUrl, ServerConstants.SERVER_TYPE_PULL);
        int state = configAdminServiceStub.addServer(serverOne);
        Assert.assertEquals(SERVER_SUCCESSFULLY_ADDED, state);
        log.info("\n Server: " + serverUrl + " successfully added.\n");

        log.info("Adding same server again");
        state = configAdminServiceStub.addServer(serverOne);
        Assert.assertEquals(SERVER_ALREADY_ADDED, state);
        log.info("Successful: Server: " + serverUrl + " already added");
    }

    @AfterClass(groups = {"wso2.bam"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
    }


    private void initStubs(String sessionCookie) throws AxisFault {
        configAdminServiceStub = new BAMConfigAdminServiceStub("https://localhost:9443/services/BAMConfigAdminService");
        CommonUtils.setSessionCookie(configAdminServiceStub, sessionCookie);

    }

}


