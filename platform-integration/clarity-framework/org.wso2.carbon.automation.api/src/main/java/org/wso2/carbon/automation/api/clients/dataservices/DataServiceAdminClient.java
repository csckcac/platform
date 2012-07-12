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
package org.wso2.carbon.automation.api.clients.dataservices;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.automation.api.clients.utils.AuthenticateStub;
import org.wso2.carbon.dataservices.ui.stub.DataServiceAdminStub;
//import org.wso2.carbon.dataservices.ui.stub.admin.core.xsd.DSTaskInfo;

import java.rmi.RemoteException;


public class DataServiceAdminClient {
    private static final Log log = LogFactory.getLog(DataServiceAdminClient.class);

    private final String serviceName = "DataServiceAdmin";
    private DataServiceAdminStub dataServiceAdminStub;

    public DataServiceAdminClient(String backEndUrl, String sessionCookie) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        dataServiceAdminStub = new DataServiceAdminStub(endPoint);
        AuthenticateStub.authenticateStub(sessionCookie, dataServiceAdminStub);
    }

    public DataServiceAdminClient(String backEndUrl, String userName, String password) throws AxisFault {
        String endPoint = backEndUrl + serviceName;
        dataServiceAdminStub = new DataServiceAdminStub(endPoint);
        AuthenticateStub.authenticateStub(userName, password, dataServiceAdminStub);
    }

    public String[] getCarbonDataSources() throws RemoteException {

        String[] dataSourceList;

        dataSourceList = dataServiceAdminStub.getCarbonDataSourceNames();

        return dataSourceList;
    }

    public void editDataService(String serviceName, String serviceHierachy,
                                String dataServiceContent)
            throws RemoteException {

        dataServiceAdminStub.saveDataService(serviceName, serviceHierachy, dataServiceContent);

    }

    public String getDataServiceContent(String serviceName)
            throws RemoteException {
        String content;
        content = dataServiceAdminStub.getDataServiceContentAsString(serviceName);
        return content;
    }

/*    public void scheduleTask(String sessionCookie, DSTaskInfo dSTaskInfo) {
        new AuthenticateStub().authenticateStub(sessionCookie, dataServiceAdminStub);
        try {
            dataServiceAdminStub.scheduleTask(dSTaskInfo);
            log.info("ScheduleTask added");
        } catch (RemoteException e) {
            log.error("Remote Exception when adding scheduleTask :", e);
            Assert.fail("Remote Exception when adding scheduleTask : " + e);
        }

    }

    public void rescheduleTask(String sessionCookie, DSTaskInfo dSTaskInfo) {
        new AuthenticateStub().authenticateStub(sessionCookie, dataServiceAdminStub);
        try {
            dataServiceAdminStub.rescheduleTask(dSTaskInfo);
            log.info("Task rescheduled");
        } catch (RemoteException e) {
            log.error("Remote Exception when rescheduling Task :", e);
            Assert.fail("Remote Exception when rescheduling Task : " + e);
        }

    }

    public void deleteTask(String sessionCookie, String taskName) {
        new AuthenticateStub().authenticateStub(sessionCookie, dataServiceAdminStub);
        try {
            dataServiceAdminStub.deleteTask(taskName);
            log.info("ScheduleTask deleted");
        } catch (RemoteException e) {
            log.error("Remote Exception when deleting Task :", e);
            Assert.fail("Remote Exception when deleting Task : " + e);
        }

    }

    public boolean isTaskScheduled(String sessionCookie, String taskName) {
        new AuthenticateStub().authenticateStub(sessionCookie, dataServiceAdminStub);
        try {
            return dataServiceAdminStub.isTaskScheduled(taskName);
        } catch (RemoteException e) {
            log.error("Remote Exception when getting task info :", e);
            Assert.fail("Remote Exception when getting task info : " + e);
        }
        return false;
    }

    public String[] getAllTaskNames(String sessionCookie) {
        new AuthenticateStub().authenticateStub(sessionCookie, dataServiceAdminStub);
        try {
            return dataServiceAdminStub.getAllTaskNames();
        } catch (RemoteException e) {
            log.error("Remote Exception when getting AllTaskNames :", e);
            Assert.fail("Remote Exception when getting AllTaskNames : " + e);
        }
        return null;
    }*/
}
