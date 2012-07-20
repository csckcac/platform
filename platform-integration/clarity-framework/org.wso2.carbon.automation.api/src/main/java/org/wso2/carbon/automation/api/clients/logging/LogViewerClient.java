/**
 *  Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.automation.api.clients.logging;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.automation.api.utils.SetAxis2ConfigurationContext;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.carbon.LogEvent;

import java.rmi.RemoteException;

/**
 * This class can use to get system logs information
 */

public class LogViewerClient {

    private static final Log log = LogFactory.getLog(LogViewerClient.class);
    private LogViewerStub logViewerStub;
    String serviceName = "LogViewer";
    SetAxis2ConfigurationContext setContext;

    public LogViewerClient(String sessionCookie, String backEndUrl)
            throws AxisFault {
        String endpoint = backEndUrl + serviceName;
        logViewerStub = new LogViewerStub(endpoint);
        AuthenticateStub.authenticateStub(sessionCookie, logViewerStub);
    }


    public LogViewerClient(String backEndURL, String userName, String password)
            throws AxisFault {
        setContext = new SetAxis2ConfigurationContext();
         String endpoint = backEndURL + serviceName;
        logViewerStub = new LogViewerStub(setContext.setConfigurationContext(),endpoint);
        AuthenticateStub.authenticateStub(userName, password, logViewerStub);
    }


    /**
     * Getting system logs
     *
     * @param logType   Log type (INFO,WARN,ERROR,DEBUG)
     * @param searchKey searching keyward
     * @return logMessage array
     * @throws RemoteException Exception
     */
    public LogEvent[] getLogs(String logType, String searchKey) throws RemoteException {
        return logViewerStub.getLogs(logType, searchKey);
    }


    public boolean clearLogs() throws RemoteException {
        return logViewerStub.clearLogs();
    }
}
