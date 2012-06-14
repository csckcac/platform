/*
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.mashup.javascript.hostobjects.system;

import java.util.ArrayList;
import java.util.List;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.core.AbstractAdmin;
import org.wso2.carbon.mashup.javascript.hostobjects.system.internal.SystemHostObjectServiceComponent;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.ntask.core.TaskManager;

public class MSTaskAdmin extends AbstractAdmin {

    private static final Log log = LogFactory.getLog(MSTaskAdmin.class);

    public String[] getAllTaskNames() throws AxisFault {
        try {
            TaskManager taskManager = SystemHostObjectServiceComponent.getTaskService().getTaskManager(
                    MSTaskConstants.MS_TASK_TYPE);
            List<TaskInfo> taskInfoList = taskManager.getAllTasks();
            List<String> result = new ArrayList<String>();
            for (TaskInfo taskInfo : taskInfoList) {
                result.add(taskInfo.getName());
            }
            return result.toArray(new String[result.size()]);
        } catch (Exception e) {
            log.error(e);
            throw new AxisFault("Error in getting task names: " + e.getMessage(), e);
        }
    }

    public MSTaskInfoDTO getTaskInfo(String taskName) throws AxisFault {
        try {
            TaskManager tm = SystemHostObjectServiceComponent.getTaskService().getTaskManager(
                    MSTaskConstants.MS_TASK_TYPE);
            return MSTaskUtils.convert(tm.getTask(taskName));
        } catch (Exception e) {
            log.error(e);
            throw new AxisFault("Error getting task info for task: " + taskName, e);
        }
    }

    public void scheduleTask(MSTaskInfoDTO msTaskInfo) throws AxisFault {
        TaskManager tm = null;
        try {
            tm = SystemHostObjectServiceComponent.getTaskService().getTaskManager(
                    MSTaskConstants.MS_TASK_TYPE);
            MSTaskInfo taskInfo = MSTaskUtils.convert(msTaskInfo);
            log.error("test >>>>>>>>>>>>>>>>>>>>>>>> " + taskInfo.getName() + "  " + taskInfo.getProperties().size());
            tm.registerTask(taskInfo);
            tm.scheduleTask(taskInfo.getName());
        } catch (Exception e) {
            log.error(e);
            if (tm != null) {
                try {
                    tm.deleteTask(msTaskInfo.getName());
                } catch (TaskException e1) {
                    log.error(e1);
                }
            }
            throw new AxisFault("Error scheduling task: " + msTaskInfo.getName(), e);
        }
    }

    public boolean rescheduleTask(MSTaskInfoDTO msTaskInfo) throws AxisFault {
        try {
            TaskManager tm = SystemHostObjectServiceComponent.getTaskService().getTaskManager(
                    MSTaskConstants.MS_TASK_TYPE);
            MSTaskInfo taskInfo = MSTaskUtils.convert(msTaskInfo);
            tm.registerTask(taskInfo);
            tm.rescheduleTask(taskInfo.getName());
        } catch (Exception e) {
            log.error(e);
            throw new AxisFault("Error rescheduling task: " + msTaskInfo.getName(), e);
        }
        return true;
    }

    public void deleteTask(String taskName) throws AxisFault {
        try {
            TaskManager tm = SystemHostObjectServiceComponent.getTaskService().getTaskManager(
                    MSTaskConstants.MS_TASK_TYPE);
            tm.deleteTask(taskName);
        } catch (Exception e) {
            log.error(e);
            throw new AxisFault("Error deleting task: " + taskName, e);
        }
    }

    public boolean isTaskScheduled(String taskName) throws AxisFault {
        try {
            TaskManager tm = SystemHostObjectServiceComponent.getTaskService().getTaskManager(
                    MSTaskConstants.MS_TASK_TYPE);
            return tm.isTaskScheduled(taskName);
        } catch (Exception e) {
            log.error(e);
            throw new AxisFault("Error checking task scheduled status: " + taskName, e);
        }
    }

}
