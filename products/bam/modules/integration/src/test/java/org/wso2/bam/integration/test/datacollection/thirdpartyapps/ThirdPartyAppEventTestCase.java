/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.bam.integration.test.datacollection.thirdpartyapps;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bam.integration.test.common.statistics.StatisticUtils;
import org.wso2.bam.integration.test.common.statistics.StatisticsData;
import org.wso2.bam.integration.test.common.utils.CommonUtils;
import org.wso2.carbon.bam.stub.configadmin.BAMConfigAdminServiceBAMException;
import org.wso2.carbon.bam.stub.configadmin.BAMConfigAdminServiceStub;
import org.wso2.carbon.bam.stub.configadmin.types.carbon.ServerDO;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceBAMException;
import org.wso2.carbon.bam.stub.listadmin.BAMListAdminServiceStub;
import org.wso2.carbon.bam.stub.listadmin.types.carbon.MonitoredServerDTO;
import org.wso2.carbon.bam.stub.statquery.BAMStatQueryDSStub;
import org.wso2.carbon.integration.framework.ClientConnectionUtil;
import org.wso2.carbon.integration.framework.LoginLogoutUtil;

import javax.xml.stream.XMLStreamException;
import java.rmi.RemoteException;

/**
 * This test case verifies whether the Third party application events are received successfully
 */
public class ThirdPartyAppEventTestCase {
    private static final Log log = LogFactory.getLog(ThirdPartyAppEventTestCase.class);

    private static final String TARGET_EPR =
            "https://localhost:9443/services/BAMServerUserDefinedDataSubscriberService/Publish";
    private static final String serverName = "http://127.0.0.1:8280";

    private BAMConfigAdminServiceStub configAdminStub;
    private BAMListAdminServiceStub listAdminStub;
    private BAMStatQueryDSStub statQueryStub;

    private ServerDO addedServer;
    private ServiceClient client;

    private LoginLogoutUtil util = new LoginLogoutUtil();


    @BeforeClass(groups = {"wso2.bam"})
    public void login() throws Exception {
        ClientConnectionUtil.waitForPort(9443);
        String sessionCookie = util.login();
        initStubs(sessionCookie);
        initClient(sessionCookie);
    }

    @Test(groups = {"wso2.bam.test"}, description = "Test for third party application event reception")
    public void recieveThirdPartAppEvent() throws Exception,
            RemoteException, XMLStreamException {
        client.fireAndForget(getPayload());
        log.info("Fired service statistics event to BAM server..");

        boolean serverFound = verifyThirdPartyApplicationEvent();
        Assert.assertTrue(serverFound, "3rd party server added to BAM server list");
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


    private OMElement getPayload() throws XMLStreamException {

        String payload = "<svrusrdata:Event xmlns:svrusrdata=\"http://wso2.org/ns/2009/09/bam/server/user-defined/data\">\n" +
                "\t            <svrusrdata:ServerUserDefinedData>\n" +
                "\t\t            <svrusrdata:ServerName>" + serverName + "</svrusrdata:ServerName>\n" +
                "\t\t            <svrusrdata:Data>\n" +
                "\t\t\t            <svrusrdata:Key>EndpointInMaxProcessingTime-simple</svrusrdata:Key>\n" +
                "\t\t\t            <svrusrdata:Value>15</svrusrdata:Value>\n" +
                "\t\t            </svrusrdata:Data>\n" +
                "\t\t            <svrusrdata:Data>\n" +
                "\t\t\t            <svrusrdata:Key>EndpointInAvgProcessingTime-simple</svrusrdata:Key>\n" +
                "\t\t\t            <svrusrdata:Value>15.0</svrusrdata:Value>\n" +
                "\t\t            </svrusrdata:Data>\n" +
                "\t\t            <svrusrdata:Data>\n" +
                "\t\t\t            <svrusrdata:Key>EndpointInMinProcessingTime-simple</svrusrdata:Key>\n" +
                "\t\t\t            <svrusrdata:Value>15</svrusrdata:Value>\n" +
                "\t\t            </svrusrdata:Data>\n" +
                "\t\t            <svrusrdata:Data>\n" +
                "\t\t\t            <svrusrdata:Key>EndpointInCount-simple</svrusrdata:Key>\n" +
                "\t\t\t            <svrusrdata:Value>1</svrusrdata:Value>\n" +
                "\t\t            </svrusrdata:Data>\n" +
                "\t\t            <svrusrdata:Data>\n" +
                "\t\t\t            <svrusrdata:Key>EndpointInFaultCount-simple</svrusrdata:Key>\n" +
                "\t\t\t            <svrusrdata:Value>0</svrusrdata:Value>\n" +
                "\t\t            </svrusrdata:Data>\n" +
                "\t\t            <svrusrdata:Data>\n" +
                "\t\t\t            <svrusrdata:Key>EndpointInID</svrusrdata:Key>\n" +
                "\t\t\t            <svrusrdata:Value>simple</svrusrdata:Value>\n" +
                "\t\t            </svrusrdata:Data>\n" +
                "\t\t            <svrusrdata:Data>\n" +
                "\t\t\t            <svrusrdata:Key>EndpointInCumulativeCount-simple</svrusrdata:Key>\n" +
                "\t\t\t            <svrusrdata:Value>3</svrusrdata:Value>\n" +
                "\t\t            </svrusrdata:Data>\n" +
                "\t\t            <svrusrdata:Data>\n" +
                "\t\t\t            <svrusrdata:Key>EndpointOutCumulativeCount-simple</svrusrdata:Key>\n" +
                "\t\t\t            <svrusrdata:Value>0</svrusrdata:Value>\n" +
                "\t\t            </svrusrdata:Data>\n" +
                "\t            </svrusrdata:ServerUserDefinedData>\n" +
                "            </svrusrdata:Event>";
        return AXIOMUtil.stringToOM(payload);
    }

    private boolean verifyThirdPartyApplicationEvent() throws BAMListAdminServiceBAMException, RemoteException, BAMConfigAdminServiceBAMException {
        boolean success = false;
        try {
            Thread.sleep(5 * 1000);       // Wait on a known field and timeout
        } catch (InterruptedException ignored) {

        }
        ServerDO[] servers = configAdminStub.getServerList();
        for (ServerDO server : servers) {
            if (server.getServerURL().equals(serverName)) {
                success = true;
                addedServer = server;
            }
        }
        if (success) {
            MonitoredServerDTO[] serverList = listAdminStub.getServerList();
            for (MonitoredServerDTO server : serverList) {
                if (server.getServerURL().equals(serverName)) {
                    return true;
                }
            }
        }
        return false;
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

    private void initClient(String sessionCookie) throws AxisFault {
        client = new ServiceClient();
        Options options = client.getOptions();
        options.setTo(new EndpointReference(TARGET_EPR));
        options.setManageSession(true);
        options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, sessionCookie);
        options.setProperty(Constants.Configuration.ENABLE_MTOM, Constants.VALUE_TRUE);
        options.setTimeOutInMilliSeconds(1000000);
    }
}
