/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mashup.javascript.hostobjects.system;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.task.TaskDescriptionRepository;
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.TaskScheduler;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.task.TaskManager;
import org.wso2.carbon.CarbonException;

import javax.xml.namespace.QName;
import java.util.*;

public class FunctionSchedulingManager {
    private final static Log log = LogFactory.getLog(FunctionSchedulingManager.class);

    private final static FunctionSchedulingManager OUR_INSTANCE = new FunctionSchedulingManager();

    public static FunctionSchedulingManager getInstance() {
        return OUR_INSTANCE;
    }

    private FunctionSchedulingManager() {
    }

    public TaskDescription getTaskDescription(String name, ConfigurationContext configCtx) {

        if (log.isDebugEnabled()) {
            log.debug("Returning a Startup : " + name + " from the configuration");
        }
        TaskDescription taskDescription = getTaskDescriptionRepository(configCtx).getTaskDescription(name);
        if (taskDescription != null) {
            if (log.isDebugEnabled()) {
                log.debug("Returning a TaskDescription : " + taskDescription);

            }
            return taskDescription;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("There is no TaskDescription with name :" + name);
            }
            return null;
        }
    }

    // function for initial scheduling in jsFunction_setInterval
    public void scheduleTask(TaskDescription taskDescription, Map<String, Object> resources, ConfigurationContext configCtx) {

        AxisService axisService = (AxisService) resources.get(
                FunctionSchedulingJob.AXIS_SERVICE);
        try {
            if (axisService.getParameterValue(
                    FunctionSchedulingJob.JS_FUNCTION_MAP) != null) {
                // JobDataMap is added to AxisConfiguration
                HashMap tasksMap = (HashMap)axisService.getParameterValue(FunctionSchedulingJob.JS_FUNCTION_MAP);
                tasksMap.put(taskDescription.getName(), resources);

            } else {
                // no function map in AxisConfiguration, new one is created
                HashMap tasksMap = new HashMap();
                tasksMap.put(taskDescription.getName(), resources);
                Parameter parameter = new Parameter(FunctionSchedulingJob.JS_FUNCTION_MAP, tasksMap);
                axisService.addParameter(parameter);
            }
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
        }

        getTaskDescriptionRepository(configCtx).addTaskDescription(taskDescription);
        getTaskScheduler(configCtx).scheduleTask(taskDescription, resources, FunctionSchedulingJob.class);

        if (log.isDebugEnabled()) {
            log.debug("Added TaskDescription : " + taskDescription.getName() + " to the repository");
        }
    }

    // function for re-scheduling via admin UI, the JobDataMap is in the axisConfiguration
    public void scheduleTask(TaskDescription taskDescription, ConfigurationContext configCtx) throws CarbonException {

        Set<OMElement> properties = taskDescription.getProperties();
        Iterator<OMElement> iterator = properties.iterator();
        String serviceName = null;

        while (iterator.hasNext()) {
            Object property = iterator.next();
            if (property instanceof OMElement) {
                OMElement element = (OMElement) property;

                if (element.getAttributeValue(new QName("name")).equals(FunctionSchedulingJob.AXIS_SERVICE)) {
                    serviceName = element.getAttributeValue(new QName("value"));
                }
            }
        }

        if (serviceName != null) {
            AxisService axisService = null;

            try {
                axisService = configCtx.getAxisConfiguration().getService(serviceName);
                Map jsScheduledFunctionMap = (HashMap) axisService.getParameter(
                       FunctionSchedulingJob.JS_FUNCTION_MAP).getValue();

                if (jsScheduledFunctionMap != null) {

                    Map resources = (HashMap) jsScheduledFunctionMap.get(taskDescription.getName());

                    getTaskDescriptionRepository(configCtx).addTaskDescription(taskDescription);
                    getTaskScheduler(configCtx).scheduleTask(taskDescription, resources, FunctionSchedulingJob.class);

                    if (log.isDebugEnabled()) {
                        log.debug("Added TaskDescription : " + taskDescription.getName() + " to the repository");
                    }

                } else {
                    handleException("Cannot create the Function scheduling task. " +
                            "Tasks should be initialted only through js services.");
                }
            } catch (AxisFault axisFault) {
                log.warn("Invalid axis service name, cannot schedule task");
            }
        } else {
            log.warn("Cannot find an axis service associates with " + taskDescription.getName());
        }
    }

    // delete the task, i.e. TaskDescription and JobDataMap are deleted
    public void deleteTask(String name, ConfigurationContext configCtx) {

        TaskDescription taskDescription = getTaskDescriptionRepository(configCtx).getTaskDescription(name);
        Set<OMElement> properties = taskDescription.getProperties();
        Iterator<OMElement> iterator = properties.iterator();
        String serviceName = null;

        while (iterator.hasNext()) {
            Object property = iterator.next();
            if (property instanceof OMElement) {
                OMElement element = (OMElement) property;

                if (element.getAttributeValue(new QName("name")).equals(FunctionSchedulingJob.AXIS_SERVICE)) {
                    serviceName = element.getAttributeValue(new QName("value"));
                }
            }
        }

        getTaskDescriptionRepository(configCtx).removeTaskDescription(name);
        getTaskScheduler(configCtx).deleteTask(name, taskDescription.getGroup());

        if (serviceName != null) {

            AxisService axisService = null;
            try {
                axisService = configCtx.getAxisConfiguration().getService(serviceName);
                ((HashMap) axisService.getParameter(
                        FunctionSchedulingJob.JS_FUNCTION_MAP).getValue()).remove(name);
            } catch (AxisFault axisFault) {
                log.warn("Ivalid axis service name, cannot delete js_function_map");
            }
            if (log.isDebugEnabled()) {
                log.debug("Deleted Task : " + name + " from the configuration");
            }
        } else {
            log.warn("Cannot delete the Task " + name
                    + ", there is no axis service associates with it");
        }
    }

    // delete only the TaskDescrition, this is used when re-scheduling via admin UI
    public void deleteTaskDescription(String name, ConfigurationContext configCtx) {
        TaskDescription taskDescription = getTaskDescriptionRepository(configCtx).getTaskDescription(name);

        if (taskDescription != null) {
            getTaskDescriptionRepository(configCtx).removeTaskDescription(name);
            getTaskScheduler(configCtx).deleteTask(name, taskDescription.getGroup());
        } else {
            log.warn("Cannot delete the Task " + name
                    + ", it doesn't exists in the Repository");
        }

        if (log.isDebugEnabled()) {
            log.debug("Deleted TaskDescription : " + name + " from the configuration");
        }
    }

    // delete all the task associated with the given sevice, i.e. TaskDescription and JobDataMap are deleted
    public void deleteTasks(String serviceName, ConfigurationContext configCtx) {

        AxisService axisService = null;
        Map jsScheduledFunctionMap = null;
        try {
            axisService = configCtx.getAxisConfiguration().getService(serviceName);
            jsScheduledFunctionMap = (HashMap) axisService.getParameterValue(
                    FunctionSchedulingJob.JS_FUNCTION_MAP);
            if (jsScheduledFunctionMap != null) {

                for (Object o : jsScheduledFunctionMap.values()) {
                    Map jdm = (HashMap) o;
                    String taskName = (String) jdm.get(FunctionSchedulingJob.TASK_NAME);

                    deleteTaskDescription(taskName, configCtx);
                }

                axisService.removeParameter(new Parameter(
                        FunctionSchedulingJob.JS_FUNCTION_MAP, jsScheduledFunctionMap));

                if (log.isDebugEnabled()) {
                    log.debug("Deleted Tasks associated with the axis service : "
                            + serviceName + " from the configuration");
                }
            }
        } catch (AxisFault axisFault) {
            log.warn("Invalid axis service name, cannot delete tasks");
        }

    }

    public boolean isTaskActive(String taskName, ConfigurationContext configCtx) {
        return getTaskDescriptionRepository(configCtx).getTaskDescription(taskName) != null;
    }

    public Iterator<TaskDescription> getAllTaskDescriptions(ConfigurationContext configCtx) {

        if (log.isDebugEnabled()) {
            log.debug("Returning a All TaskDescription from the configuration");
        }
        return getTaskDescriptionRepository(configCtx).getAllTaskDescriptions();
    }

    public boolean isContains(String name, ConfigurationContext configCtx) {
        return !getTaskDescriptionRepository(configCtx).isUnique(name);
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new IllegalArgumentException(msg);
    }

    private synchronized TaskManager getTaskManager(ConfigurationContext configCtx) {
        return (TaskManager) configCtx.getProperty(
                TaskManager.CARBON_TASK_MANAGER);
    }

    private synchronized TaskDescriptionRepository getTaskDescriptionRepository(ConfigurationContext configCtx) {
        return (TaskDescriptionRepository) configCtx.getProperty(
                TaskManager.CARBON_TASK_REPOSITORY);
    }

    private synchronized TaskScheduler getTaskScheduler(ConfigurationContext configCtx) {
        return (TaskScheduler) configCtx.getProperty(
                TaskManager.CARBON_TASK_SCHEDULER);
    }
}