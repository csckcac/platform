/*
 *  Copyright (c) WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.registry.reporting.ui.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskManager;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * @scr.component name="org.wso2.carbon.registry.reporting.ui" immediate="true"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 */
public class ReportingUIServiceComponent {

    private static final Log log = LogFactory.getLog(ReportingUIServiceComponent.class);
    private static final String REPORTING_TASK_MANAGER = "registryReportingTasks";
    private static TaskManager taskManager;

    public static TaskManager getTaskManager() {
        return taskManager;
    }

    public void setTaskService(TaskService taskService) {
        try {
            updateTaskManager(taskService.getTaskManager(REPORTING_TASK_MANAGER));
        } catch (TaskException e) {
            log.error("Unable to obtain task manager");
        }
    }

    public void unsetTaskService(TaskService taskService) {
        updateTaskManager(null);
    }

    // Method to update task manager.
    private static void updateTaskManager(TaskManager manager) {
        taskManager = manager;
    }
}
