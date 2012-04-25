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

package org.wso2.carbon.admin.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.utils.AuthenticateStub;
import org.wso2.carbon.logging.view.stub.LogViewerStub;
import org.wso2.carbon.logging.view.stub.types.carbon.LogMessage;

import java.rmi.RemoteException;

/**
 * This class can use to get system logs information
 */

public class AdminServiceLogViewer {

    private static final Log log = LogFactory.getLog(AdminServiceLogViewer.class);
    private LogViewerStub logViewerStub;

    public AdminServiceLogViewer(String sessionCookie, String backEndUrl)
            throws AxisFault {
        String serviceName = "LogViewer";
        String endPoint = backEndUrl + serviceName;
        log.debug("admin service url = " + endPoint);
        logViewerStub = new LogViewerStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, logViewerStub);
    }

    /**
     * Getting system logs
     *
     * @param logType   Log type (INFO,WARN,ERROR,DEBUG)
     * @param searchKey searching keyward
     * @return logMessage array
     * @throws RemoteException Exception
     */
    public LogMessage[] getLogs(String logType, String searchKey) throws RemoteException {
        return logViewerStub.getLogs(logType, searchKey);
    }
}
