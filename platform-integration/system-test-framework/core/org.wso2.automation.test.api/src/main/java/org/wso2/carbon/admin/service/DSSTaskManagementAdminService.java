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
package org.wso2.carbon.admin.service;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.admin.service.utils.AuthenticateStub;
import org.wso2.carbon.dataservices.task.ui.stub.DSTaskAdminStub;
import org.wso2.carbon.dataservices.task.ui.stub.xsd.DSTaskInfo;

import java.rmi.RemoteException;

public class DSSTaskManagementAdminService {
    private static final Log log = LogFactory.getLog(DSSTaskManagementAdminService.class);

    private final String serviceName = "DSTaskAdmin";
    private DSTaskAdminStub taskManagementAdminServiceStub;
    private String endPoint;

    public DSSTaskManagementAdminService(String backEndUrl) throws AxisFault {
        this.endPoint = backEndUrl + serviceName;
        taskManagementAdminServiceStub = new DSTaskAdminStub(endPoint);
    }

    public void scheduleTask(String sessionCookie, DSTaskInfo dSTaskInfo) throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, taskManagementAdminServiceStub);
        taskManagementAdminServiceStub.scheduleTask(dSTaskInfo);
        log.info("ScheduleTask added");
    }

    public void rescheduleTask(String sessionCookie, DSTaskInfo dSTaskInfo) throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, taskManagementAdminServiceStub);

        taskManagementAdminServiceStub.rescheduleTask(dSTaskInfo);
        log.info("Task rescheduled");
    }

    public void deleteTask(String sessionCookie, String taskName) throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, taskManagementAdminServiceStub);

        taskManagementAdminServiceStub.deleteTask(taskName);
        log.info("ScheduleTask deleted");
    }

    public boolean isTaskScheduled(String sessionCookie, String taskName) throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, taskManagementAdminServiceStub);

        return taskManagementAdminServiceStub.isTaskScheduled(taskName);
    }

    public String[] getAllTaskNames(String sessionCookie) throws RemoteException {
        AuthenticateStub.authenticateStub(sessionCookie, taskManagementAdminServiceStub);

        return taskManagementAdminServiceStub.getAllTaskNames();
    }
}
