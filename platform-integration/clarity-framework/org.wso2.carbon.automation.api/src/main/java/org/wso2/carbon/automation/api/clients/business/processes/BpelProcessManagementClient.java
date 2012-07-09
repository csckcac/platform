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
package org.wso2.carbon.automation.api.clients.business.processes;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementException;
import org.wso2.carbon.bpel.stub.mgt.ProcessManagementServiceStub;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedProcessInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedProcessInfoList;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.ProcessStatus;

import javax.xml.namespace.QName;
import java.rmi.RemoteException;
import java.util.LinkedList;

public class BpelProcessManagementClient {
    String ServiceEndPoint = null;
    String SessionCookie = null;
    private static final Log log = LogFactory.getLog(BpelProcessManagementClient.class);

    ProcessManagementServiceStub processManagementServiceStub = null;


    public BpelProcessManagementClient(String serviceEndPoint, String sessionCookie) {
        this.ServiceEndPoint = serviceEndPoint;
        this.SessionCookie = sessionCookie;
    }

    private ProcessManagementServiceStub setProcessManagementStub() throws AxisFault {
        final String serviceMgtServiceUrl = ServiceEndPoint + "ProcessManagementService";
        ProcessManagementServiceStub processManagementServiceStub = null;
        processManagementServiceStub = new ProcessManagementServiceStub(serviceMgtServiceUrl);
        AuthenticateStub.authenticateStub(SessionCookie, processManagementServiceStub);
        return processManagementServiceStub;
    }

    public void setStatus(String processID, String status)
            throws RemoteException, ProcessManagementException {
        processManagementServiceStub = this.setProcessManagementStub();
        if (ProcessStatus.ACTIVE.getValue().equals(status.toUpperCase())) {
            processManagementServiceStub.activateProcess(QName.valueOf(processID));
        } else if (ProcessStatus.RETIRED.getValue().equals(status.toUpperCase())) {
            processManagementServiceStub.retireProcess(QName.valueOf(processID));
        }
    }

    public String getStatus(String processID) throws RemoteException, ProcessManagementException {
        String status = null;
        processManagementServiceStub = this.setProcessManagementStub();
        ProcessInfoType processInfo = processManagementServiceStub.
                getProcessInfo(QName.valueOf(processID));
        status = processInfo.getStatus().getValue().toString();

        return status;
    }

    public String getProcessId(String packageName)
            throws RemoteException, ProcessManagementException {
        processManagementServiceStub = this.setProcessManagementStub();
        String processId = null;
        final String processFilter = "name}}";
        final String processListOrderBy = "-deployed";
        String[] processList = processManagementServiceStub.getAllProcesses("y");
        if (processList.length == 0) {
            throw new AssertionError("Process list cannot be empty");
        }
        boolean processFound = false;
        for (String id : processList) {
            if (id.contains(packageName + "-")) {
                processFound = true;
                processId = id;
            }
        }
        return processId;
    }

    public PaginatedProcessInfoList getProcessInfo(String packageName)
            throws RemoteException, ProcessManagementException {
        processManagementServiceStub = this.setProcessManagementStub();

        PaginatedProcessInfoList filteredProcess = new PaginatedProcessInfoList();
        final String processFilter = "name}}* namespace=*";
        final String processListOrderBy = "-deployed";
        PaginatedProcessInfoList processes =
                processManagementServiceStub.getPaginatedProcessList(processFilter,
                                                                     processListOrderBy, 0);
        for (LimitedProcessInfoType processInfo : processes.getProcessInfo()) {
            if (processInfo.getPid().contains(packageName + "-")) {
                filteredProcess.addProcessInfo(processInfo);
            }
        }
        return filteredProcess;
    }


    public LinkedList<String> getProcessInfoList(String packageName)
            throws RemoteException, ProcessManagementException {
        processManagementServiceStub = this.setProcessManagementStub();

        LinkedList<String> filteredProcess = new LinkedList<String>();

        String[] processList = processManagementServiceStub.getAllProcesses("y");
        for (String id : processList) {
            if (id.contains(packageName + "-")) {
                filteredProcess.add(id);
            }
        }
        return filteredProcess;
    }
}



