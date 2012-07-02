package org.wso2.carbon.mediator.tests.clone;
/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/


import org.apache.axiom.om.OMElement;
import org.testng.annotations.Test;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;
import org.wso2.esb.integration.ESBIntegrationTestCase;
import org.wso2.esb.integration.axis2.SampleAxis2Server;
import org.wso2.esb.integration.axis2.StockQuoteClient;

import java.rmi.RemoteException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CloneMediatorTestCase extends ESBIntegrationTestCase {
    private StockQuoteClient axis2Client;

    public void init() throws Exception {
        axis2Client = new StockQuoteClient();
        launchBackendAxis2Service(SampleAxis2Server.SIMPLE_STOCK_QUOTE_SERVICE);
    }

    @Test(groups = {"wso2.esb"})
    public void testCloneProperty() throws RemoteException {
        String filePath = "/mediator/clone/clone_with_property.xml";
        loadESBConfigurationFromClasspath(filePath);
        OMElement response = axis2Client.sendSimpleStockQuoteRequest(
                null, getMainSequenceURL(), "MSFT");
        assertTrue(response.toString().contains("MSFT"));
    }

    @Test(groups = {"wso2.esb"})
    public void testContinueParentTrue() throws RemoteException {
        boolean isParentMsgFound = false;
        String filePath = "/mediator/clone/continue_parent_True.xml";
        loadESBConfigurationFromClasspath(filePath);

        LogViewerStub logViewerStub = new LogViewerStub(getAdminServiceURL("LogViewer"));
        authenticate(logViewerStub);

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(
                null, getMainSequenceURL(), "IBM");
        assertTrue(response.toString().contains("IBM"));

        LogMessage[] logs = logViewerStub.getLogs("INFO", "LogMediator");
        assertTrue(logs != null && logs.length > 0 && logs[0] != null);

        for (LogMessage l : logs) {
            if (l.getLogMessage().contains("*****PARENT MESSAGE******")) {
                isParentMsgFound = true;
            }
        }
        assertTrue(isParentMsgFound, "ContinueParent = true property failed");
    }

    @Test(groups = {"wso2.esb"})
    public void testWithoutContinueParent() throws RemoteException {
        boolean isParentMsgFound = false;
        String filePath = "/mediator/clone/continue_parent_False.xml";
        loadESBConfigurationFromClasspath(filePath);

        OMElement response = axis2Client.sendSimpleStockQuoteRequest(
                null, getMainSequenceURL(), "IBM");
        assertTrue(response.toString().contains("IBM"));

        LogViewerStub logViewerStub = new LogViewerStub(getAdminServiceURL("LogViewer"));
        authenticate(logViewerStub);

        LogMessage[] logs = logViewerStub.getLogs("INFO", "LogMediator");
        assertTrue(logs != null && logs.length > 0 && logs[0] != null);

        for (LogMessage l : logs) {
            if (l.getLogMessage().contains("*****ORIGINAL PARENT MESSAGE ******")) {
                isParentMsgFound = true;
            }
        }
        assertFalse(isParentMsgFound, "Parent message printed without continue parent property enabled");
    }


}
