/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.bps.integration.tests.humantask;

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.databinding.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;
import org.wso2.bps.integration.tests.util.FrameworkSettings;
import org.wso2.bps.integration.tests.util.HumanTaskTestConstants;
import org.wso2.carbon.humantask.stub.ui.task.client.api.TaskOperationsStub;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskAbstract;
import org.wso2.carbon.utils.CarbonUtils;

/**
 * Test class to check the task operations functionality.
 */
public class TaskOperationsTestCase {

    private static final Log log = LogFactory.getLog(TaskOperationsTestCase.class);

    private TaskOperationsStub taskOperationsStub = null;

    private URI taskId = null;


    @BeforeGroups(groups = {"wso2.bps"}, description = " Copying sample HumanTask packages")
    public void init() throws Exception {
        initTaskOperationServiceStub();
        taskId = new URI("1");
    }

    private void initTaskOperationServiceStub() throws Exception {

        String TASK_OPERATIONS_SERVICE_URL = "https://" + FrameworkSettings.HOST_NAME +
                                             ":" + FrameworkSettings.HTTPS_PORT +
                                             "/services/taskOperations";
        taskOperationsStub = new TaskOperationsStub(TASK_OPERATIONS_SERVICE_URL);

        ServiceClient serviceClient = taskOperationsStub._getServiceClient();
        CarbonUtils.setBasicAccessSecurityHeaders(HumanTaskTestConstants.CLERK1_USER,
                                                  HumanTaskTestConstants.CLERK1_PASSWORD, serviceClient);
        Options serviceClientOptions = serviceClient.getOptions();
        serviceClientOptions.setManageSession(true);

    }

    @Test(groups = {"wso2.bps"}, description = "Claims approval test case")
    public void testLoadTask()
            throws Exception {

        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertNotNull(loadedTask, "The task is not created successfully");
        Assert.assertEquals(loadedTask.getId().toString(), "1", "The task id is wrong");

    }

    @Test(groups = {"wso2.bps"}, description = "Claims approval test case")
    public void taskClaimTask()
            throws Exception {

        taskOperationsStub.claim(taskId);
        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTask.getActualOwner().getTUser(), HumanTaskTestConstants.CLERK1_USER,
                            "The assignee should be clerk1 !");
        Assert.assertEquals(loadedTask.getStatus().toString(), "RESERVED",
                            "The task status should be RESERVED!");

    }

    @Test(groups = {"wso2.bps"}, description = "Claims approval test case")
    public void testStartTask()
            throws Exception {

        taskOperationsStub.start(taskId);
        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTask.getStatus().toString(), "IN_PROGRESS",
                            "The task status should be IN_PROGRESS after starting the task!");
    }

    @Test(groups = {"wso2.bps"}, description = "Claims approval test case")
    public void testSuspendAndResume()
            throws Exception {

        taskOperationsStub.suspend(taskId);
        TTaskAbstract loadedTask = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTask.getStatus().toString(), "SUSPENDED",
                            "The task status should be SUSPENDED after suspending the task!");
        Assert.assertEquals(loadedTask.getPreviousStatus().toString(), "IN_PROGRESS",
                            "The task previous status should be IN_PROGRESS");

        taskOperationsStub.resume(taskId);
        TTaskAbstract loadedTaskAfterResume = taskOperationsStub.loadTask(taskId);
        Assert.assertEquals(loadedTaskAfterResume.getStatus().toString(), "IN_PROGRESS",
                            "The task status should be IN_PROGRESS after resuming the suspended task!");
    }

}
