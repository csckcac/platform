/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.bam.integration.test.monitoredservers;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.test.common.events.EventException;
import org.wso2.bam.integration.test.common.statistics.StatisticUtils;
import org.wso2.bam.integration.test.common.statistics.StatisticsData;
import org.wso2.bam.integration.test.common.utils.CommonUtils;
import org.wso2.carbon.bam.stub.configadmin.BAMConfigAdminServiceBAMException;
import org.wso2.carbon.bam.stub.configadmin.BAMConfigAdminServiceStub;
import org.wso2.carbon.bam.stub.configadmin.types.carbon.ServerDO;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceStub;
import org.wso2.carbon.bam.stub.statquery.BAMStatQueryDSStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

/**
 * This test case verifies whether the server added to BAM for monitoring,
 * could be changed and updated.
 */
public class EditServerTestCase {

    private static final Log log = LogFactory.getLog(EditServerTestCase.class);
    private LoginLogoutUtil util = new LoginLogoutUtil();

    BAMListAdminServiceStub listAdminStub = null;
    BAMStatQueryDSStub statQueryStub = null;
    BAMConfigAdminServiceStub configAdminServiceStub = null;

    @BeforeClass(groups = {"wso2.bam"})
    public void login() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        String sessionCookie = util.login();
        initStubs(sessionCookie);
    }

    @Test(enabled = true, groups = {"wso2.bam.test"}, description = "Test for updating of monitored server")
    public void editServer() throws BAMConfigAdminServiceBAMException, RemoteException, EventException, XMLStreamException {
        String serverUrl = "https://127.0.0.1:9443";
        ServerDO serverOne = BAMServerUtils.createServerObject(serverUrl, ServerConstants.SERVER_TYPE_PUSH);

        int addState = configAdminServiceStub.addServer(serverOne);
        boolean isServerAvailable = false;
        if (addState == 0 | addState == 1) isServerAvailable = true;
        Assert.assertTrue(isServerAvailable, "Server is not added..");
        log.info("Server: " + serverUrl + " successfully added");

        ServerDO[] serverList = configAdminServiceStub.getServerList();
        int serverId = serverList[serverList.length - 1].getId();

        OMElement eventElement = BAMServerUtils.getServiceStatSystemEvent("ServiceStatEvent.xml");
        publishEventToServer(serverUrl, eventElement);

        StatisticsData statisticsData = StatisticUtils.getStatisticsData(eventElement);

        log.info("Editing server: " + serverUrl);
        serverUrl = "https://127.0.0.1:9444";
        ServerDO updatedServerObj = BAMServerUtils.createServerObject(serverUrl, ServerConstants.SERVER_TYPE_PUSH);
        updatedServerObj.setId(serverId);
        configAdminServiceStub.updateServer(updatedServerObj);

        ServerDO[] servers = configAdminServiceStub.getServerList();

        boolean isTestSuccess = false;
        for (ServerDO server : servers) {
            if (server.getServerURL().equals(serverUrl)) {
                isTestSuccess = true;
            }
        }

        Assert.assertTrue(isTestSuccess, "Editing server information is unsuccessful");
        log.info("Editing already added server was successful");
        statisticsData.setServerName(updatedServerObj.getServerURL());
        log.info("Data retrieval successful after updating the server url.");
    }

    @AfterClass(groups = {"wso2.bam"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
    }

    private void initStubs(String sessionCookie) throws AxisFault {
        configAdminServiceStub = new BAMConfigAdminServiceStub("https://localhost:9443/services/BAMConfigAdminService");
        listAdminStub = new BAMListAdminServiceStub("https://localhost:9443/services/BAMListAdminService");
        statQueryStub = new BAMStatQueryDSStub("https://localhost:9443/services/BAMStatQueryDS");

        CommonUtils.setSessionCookie(configAdminServiceStub, sessionCookie);
        CommonUtils.setSessionCookie(listAdminStub, sessionCookie);
        CommonUtils.setSessionCookie(statQueryStub, sessionCookie);

    }

    private void publishEventToServer(String serverUrl, OMElement eventElement) throws AxisFault {
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference(serverUrl + "/services/BAMServiceStatisticsSubscriberService"));
        options.setAction("Publish");
        serviceClient.setOptions(options);
        serviceClient.fireAndForget(eventElement);
        log.info("Fired service statistics event to BAM server..");
    }
}
