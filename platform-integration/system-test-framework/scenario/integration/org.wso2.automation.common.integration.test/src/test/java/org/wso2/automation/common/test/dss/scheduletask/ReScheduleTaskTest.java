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
package org.wso2.automation.common.test.dss.scheduletask;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.automation.common.test.dss.utils.DataServiceTest;
import org.wso2.carbon.admin.service.DSSTaskManagementAdminService;
import org.wso2.carbon.dataservices.task.ui.stub.xsd.DSTaskInfo;
import org.wso2.platform.test.core.utils.axis2client.AxisServiceClient;

import java.rmi.RemoteException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class ReScheduleTaskTest extends DataServiceTest {
    private static final Log log = LogFactory.getLog(ReScheduleTaskTest.class);

    DSTaskInfo dsTaskInfo;
    private final String scheduleTaskName = "testScheduleTask";
    private final int taskInterval = 10000;
    private final String employeeId = "1";
    private double empSalary;

    private final OMFactory fac = OMAbstractFactory.getOMFactory();
    private final OMNamespace omNs = fac.createOMNamespace("http://ws.wso2.org/dataservice/samples/rdbms_sample", "ns1");

    @Override
    protected void setServiceName() {
        serviceName = "ScheduleTaskTest";
    }

    @Test(priority = 1, dependsOnMethods = {"serviceDeployment"})
    public void serviceInvocation() throws AxisFault {
        deleteEmployees();
        addEmployee(employeeId);
        getEmployeeById(employeeId);
        IncreaseEmployeeSalary(employeeId);

        empSalary = getEmployeeSalary(getEmployeeById(employeeId));
        log.info("service invocation success");
    }

    @Test(priority = 2, dependsOnMethods = {"serviceInvocation"})
    public void addScheduleTask() throws RemoteException {
        DSSTaskManagementAdminService taskManagementAdminService = new DSSTaskManagementAdminService(dssBackEndUrl);
        dsTaskInfo = new DSTaskInfo();

        String[] taskNames = taskManagementAdminService.getAllTaskNames(sessionCookie);

        if (taskNames != null) {
            for (String task : taskNames) {
                if (scheduleTaskName.equals(task)) {
                    taskManagementAdminService.deleteTask(sessionCookie, scheduleTaskName);
                    log.info(scheduleTaskName + " already in scheduled. schedule task deleted");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        log.error("InterruptedException :", e);
                        Assert.fail("InterruptedException :" + e);
                    }
                    break;
                }
            }
        }

        dsTaskInfo.setName(scheduleTaskName);
        dsTaskInfo.setServiceName(serviceName);
        dsTaskInfo.setOperationName("incrementEmployeeSalary");
        dsTaskInfo.setTaskInterval(taskInterval);
        dsTaskInfo.setTaskCount(-1);

        taskManagementAdminService.scheduleTask(sessionCookie, dsTaskInfo);
        log.info("task scheduled");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {

        }

    }

    @Test(priority = 3, dependsOnMethods = {"addScheduleTask"})
    public void startScheduleTask() throws AxisFault {
        //if task count is 9
        for (int i = 0; i < 4; i++) {
            double currentSalary = getEmployeeSalary(getEmployeeById(employeeId));
            log.info("current salary after task: " + currentSalary);
            Assert.assertEquals(currentSalary, (empSalary = empSalary + 10000), "Task not properly Executed");
            if (i == 3) {
                break;
            }
            try {
                Thread.sleep(taskInterval);
            } catch (InterruptedException e) {
                log.error("InterruptedException :", e);
                Assert.fail("InterruptedException :" + e);
            }
        }
        log.info("ScheduleTask verifying Success");
    }

    @Test(priority = 4, dependsOnMethods = {"startScheduleTask"})
    public void reScheduleTask() throws RemoteException {
        DSSTaskManagementAdminService taskManagementAdminService = new DSSTaskManagementAdminService(dssBackEndUrl);
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.SECOND, startTime.get(Calendar.SECOND) + 40);
        dsTaskInfo.setTaskCount(9);
        dsTaskInfo.setTaskInterval(5000);
        dsTaskInfo.setStartTime(startTime);

        log.info("Schedule Task Start time " + getTime(startTime));
        log.info("Current Time " + getTime(Calendar.getInstance()));
        taskManagementAdminService.rescheduleTask(sessionCookie, dsTaskInfo);
        log.info("Task rescheduled");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignore) {

        }
    }

    @Test(priority = 5, dependsOnMethods = {"reScheduleTask"})
    public void stopScheduleTask() throws AxisFault {
        for (int i = 0; i < 5; i++) {
            double currentSalary = getEmployeeSalary(getEmployeeById(employeeId));
            log.info("current salary after rescheduling task " + currentSalary);
            Assert.assertEquals(currentSalary, empSalary, "Task not properly Stopped after rescheduling task");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("InterruptedException :", e);
                Assert.fail("InterruptedException :" + e);
            }
        }
        log.info("schedule task stopped");
    }

    @Test(priority = 6, dependsOnMethods = {"stopScheduleTask"})
    public void reStartScheduleTask() throws AxisFault {
        while (dsTaskInfo.getStartTime().getTimeInMillis() >= Calendar.getInstance().getTimeInMillis()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("InterruptedException :", e);
                Assert.fail("InterruptedException :" + e);
            }

        }
        log.info("Current Time " + getTime(Calendar.getInstance()));
        log.info("task stared");
        for (int i = 0; i < 10; i++) {
            double currentSalary = getEmployeeSalary(getEmployeeById(employeeId));
            log.info("current salary after task rescheduling: " + currentSalary);
            Assert.assertEquals(currentSalary, (empSalary = empSalary + 10000), "Task not properly rescheduled");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                log.error("InterruptedException :", e);
                Assert.fail("InterruptedException :" + e);
            }
        }
        log.info("reschedule task verified");
        //for testing taskCount
        for (int i = 0; i < 5; i++) {
            double currentSalary = getEmployeeSalary(getEmployeeById(employeeId));
            log.info("current salary after exceeding task count " + currentSalary);
            Assert.assertEquals(currentSalary, empSalary, "Task Repeat Counter not properly Executed");
            try {
                Thread.sleep(taskInterval);
            } catch (InterruptedException e) {
                log.error("InterruptedException :", e);
                Assert.fail("InterruptedException :" + e);
            }
        }
        log.info("Task Count verified");
    }

    @Test(priority = 7, dependsOnMethods = {"reStartScheduleTask"})
    public void deleteTask() throws RemoteException {
        DSSTaskManagementAdminService taskManagementAdminService = new DSSTaskManagementAdminService(dssBackEndUrl);
        taskManagementAdminService.deleteTask(sessionCookie, scheduleTaskName);
        log.info(scheduleTaskName + " deleted");
    }


    private void addEmployee(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("addEmployee", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement lastName = fac.createOMElement("lastName", omNs);
        lastName.setText("BBB");
        payload.addChild(lastName);

        OMElement fName = fac.createOMElement("firstName", omNs);
        fName.setText("AAA");
        payload.addChild(fName);

        OMElement email = fac.createOMElement("email", omNs);
        email.setText("aaa@ccc.com");
        payload.addChild(email);

        OMElement salary = fac.createOMElement("salary", omNs);
        salary.setText("50000");
        payload.addChild(salary);

        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "addEmployee");

    }

    private OMElement getEmployeeById(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("employeesByNumber", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement result = new AxisServiceClient().sendReceive(payload, serviceEndPoint, "employeesByNumber");
        Assert.assertNotNull(result, "Employee record null");
        Assert.assertTrue(result.toString().contains("<first-name>AAA</first-name>"), "Expected Result Mismatched");
        return result;
    }

    private void deleteEmployees() throws AxisFault {
        OMElement payload = fac.createOMElement("deleteEmployees", omNs);

        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "deleteEmployees");

    }

    private void IncreaseEmployeeSalary(String employeeNumber) throws AxisFault {
        OMElement payload = fac.createOMElement("incrementEmployeeSalary", omNs);

        OMElement empNo = fac.createOMElement("employeeNumber", omNs);
        empNo.setText(employeeNumber);
        payload.addChild(empNo);

        OMElement salary = fac.createOMElement("increment", omNs);
        salary.setText("10000");
        payload.addChild(salary);

        new AxisServiceClient().sendRobust(payload, serviceEndPoint, "incrementEmployeeSalary");

        OMElement result = getEmployeeById(employeeNumber);
        Assert.assertTrue(result.toString().contains("<salary>60000.0</salary>"), "Expected Result Mismatched");

    }

    private double getEmployeeSalary(OMElement employeeRecord) {
        OMElement employee = employeeRecord.getFirstElement();
        OMElement salary = (OMElement) employee.getChildrenWithLocalName("salary").next();
        return Double.parseDouble(salary.getText());
    }

    private String getTime(Calendar time) {
        Format formatter = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        return formatter.format(time.getTimeInMillis());

    }
}
