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
package org.wso2.bam.integration.test.datacollection.service;

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
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceBAMException;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceStub;
import org.wso2.carbon.bam.stub.statquery.BAMStatQueryDSStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

/**
 * This test case verifies whether the statistics events are successfully received
 */
public class StatisticEventReceiverTestCase {

    private static final Log log = LogFactory.getLog(StatisticEventReceiverTestCase.class);

    private static final int SERVER_SUCCESSFULLY_ADDED = 0;
    private static final int SERVER_ALREADY_EXIST = 1;
    private static final String TARGET_EPR =
            "https://localhost:9443/services/BAMServiceStatisticsSubscriberService";

    private BAMConfigAdminServiceStub configAdminStub;
    private BAMListAdminServiceStub listAdminStub;
    private BAMStatQueryDSStub statQueryStub;

    private ServerDO addedServer;
    private StatisticsData eventData;
    private OMElement eventElement;

    private LoginLogoutUtil util = new LoginLogoutUtil();


    @BeforeClass(groups = {"wso2.bam"})
    public void login() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        String sessionCookie = util.login();
        initStubs(sessionCookie);
    }


    private void init() throws XMLStreamException, BAMConfigAdminServiceBAMException, RemoteException, EventException {
        eventElement = EventUtils.getServiceStatSystemEvent(1);

        if (eventElement == null) {
            log.error("Skipping tests due BY ID to error in parsing the event..");
            Assert.fail("Skipping tests due to error in parsing the event..");
        }
        eventData = StatisticUtils.getStatisticsData(eventElement);

        int stateOfAddedServer;
        addedServer = new ServerDO();
        addedServer.setServerURL("https://127.0.0.1:9443");
        addedServer.setUserName("admin");
        addedServer.setPassword("admin");
        addedServer.setCategory(1);
        addedServer.setServerType("EventingServer");
        addedServer.setSubscriptionEPR("");
        addedServer.setDescription("");
        addedServer.setSubscriptionID(null);
        stateOfAddedServer = configAdminStub.addServer(addedServer);

        if (stateOfAddedServer == SERVER_SUCCESSFULLY_ADDED) {
            log.info("Added " + addedServer.getServerType() + " with URL " +
                    addedServer.getServerURL());
        } else if (stateOfAddedServer == SERVER_ALREADY_EXIST) {
            log.info("Skip adding the server. Server already exists..");
        } else {
            log.error("Unknown return code while adding server..");
        }
    }

    @Test(groups = {"wso2.bam.test"}, description = "Test for statistics event reception")
    public void receiveStatisticsEvent() throws XMLStreamException, BAMConfigAdminServiceBAMException,
            EventException, RemoteException,
            BAMListAdminServiceBAMException {
        this.init();
        ServiceClient serviceClient = new ServiceClient();
        Options options = new Options();
        options.setTo(new EndpointReference(TARGET_EPR));
        options.setAction("Publish");
        serviceClient.setOptions(options);

        serviceClient.fireAndForget(eventElement);
        log.info("Fired service statistics event to BAM server..");

        StatisticUtils.checkDataAgainstDB(eventData, addedServer, listAdminStub, statQueryStub);
        cleanup();
    }

    private void cleanup() throws AxisFault {
        configAdminStub._getServiceClient().cleanupTransport();
        configAdminStub._getServiceClient().cleanup();
        configAdminStub.cleanup();

        listAdminStub._getServiceClient().cleanupTransport();
        listAdminStub._getServiceClient().cleanup();
        listAdminStub.cleanup();

        statQueryStub._getServiceClient().cleanupTransport();
        statQueryStub._getServiceClient().cleanup();

        statQueryStub.cleanup();
    }

    @AfterClass(groups = {"wso2.bam"})
    public void logout() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        util.logout();
    }

    private void initStubs(String sessionCookie) throws AxisFault {

        configAdminStub = new BAMConfigAdminServiceStub(
                "https://localhost:9443/services/BAMConfigAdminService");
        listAdminStub = new BAMListAdminServiceStub(
                "https://localhost:9443/services/BAMListAdminService");
        statQueryStub = new BAMStatQueryDSStub("https://localhost:9443/services/BAMStatQueryDS");

        CommonUtils.setSessionCookie(configAdminStub, sessionCookie);
        CommonUtils.setSessionCookie(listAdminStub, sessionCookie);
        CommonUtils.setSessionCookie(statQueryStub, sessionCookie);

    }

}
