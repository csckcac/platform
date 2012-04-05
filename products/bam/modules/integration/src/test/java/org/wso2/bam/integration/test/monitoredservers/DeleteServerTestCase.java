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

package org.wso2.bam.integration.test.monitoredservers;

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
import org.wso2.carbon.bam.stub.configadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.configadmin.types.carbon.ServerDO;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceStub;
import org.wso2.carbon.bam.stub.summaryquery.BAMSummaryQueryDSStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import java.rmi.RemoteException;

/**
 * This test case verifies whether the monitored server by the BAM can be removed.
 */
public class DeleteServerTestCase {

    private static final Log log = LogFactory.getLog(DeleteServerTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();
    BAMConfigAdminServiceStub configAdminServiceStub;
    private BAMListAdminServiceStub listAdminStub;


    @BeforeClass(groups = {"wso2.bam"})
    public void login() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        String sessionCookie = util.login();
        initStubs(sessionCookie);
    }

    @Test(groups = {"wso2.bam.test"},
            description = "Test for deletion of monitoring server")
    public void deleteServer() throws BAMConfigAdminServiceBAMException, RemoteException {
        String serverUrl = "https://127.0.0.1:9443";
        ServerDO addedServer = BAMServerUtils.createServerObject(serverUrl, ServerConstants.SERVER_TYPE_PULL);
        configAdminServiceStub.addServer(addedServer);

        ServerDO[] serverList = configAdminServiceStub.getServerList();
        int serverId = -1;

        for (ServerDO server : serverList) {
            if (server.getServerURL().equals(serverUrl)) {
                serverId = server.getId();
            }
        }

        if (serverId != -1) {
            MonitoredServerDTO monitoredServerDTO = new MonitoredServerDTO();
            monitoredServerDTO.setServerId(serverId);
            configAdminServiceStub.removeServer(monitoredServerDTO);
        }

        boolean deleteFlag = true;

        ServerDO[] serverLists = configAdminServiceStub.getServerList();
        if (serverLists != null) {
            for (ServerDO server : serverLists) {
                if (server.getId() == serverId) {
                    deleteFlag = false;
                    break;
                }
            }
        }

        Assert.assertTrue(deleteFlag, "Failed to delete added server.");
        log.info("Removing already added server was successful.");
    }

    @AfterClass(groups = {"wso2.bam"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
    }


    private void initStubs(String sessionCookie) throws AxisFault {
        configAdminServiceStub = new BAMConfigAdminServiceStub("https://localhost:9443/services/BAMConfigAdminService");
        listAdminStub = new BAMListAdminServiceStub("https://localhost:9443/services/BAMListAdminService");

        CommonUtils.setSessionCookie(listAdminStub, sessionCookie);
        CommonUtils.setSessionCookie(configAdminServiceStub, sessionCookie);
    }

}
