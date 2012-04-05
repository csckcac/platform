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
import org.apache.synapse.task.TaskDescription;
import org.apache.synapse.task.service.TaskManagementService;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.CarbonException;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class FunctionSchedulingAdminService extends AbstractAdmin implements TaskManagementService {

    private static Log log = LogFactory.getLog(FunctionSchedulingAdminService.class);

    private FunctionSchedulingManager functionSchedulingManager;

    public FunctionSchedulingAdminService() {
        functionSchedulingManager = FunctionSchedulingManager.getInstance();
    }

    public void addTaskDescription(TaskDescription taskDescription) {

        try {
            validateTaskDescription(taskDescription);
            functionSchedulingManager.scheduleTask(taskDescription, getConfigContext());
        } catch (CarbonException e) {
            handleException(e.getMessage());
        }
    }

    public void deleteTaskDescription(String taskName) {
        validateName(taskName);
        functionSchedulingManager.deleteTask(taskName, getConfigContext());
    }

    public void editTaskDescription(TaskDescription taskDescription) {
        validateTaskDescription(taskDescription);
        functionSchedulingManager.deleteTaskDescription(taskDescription.getName(), getConfigContext());
        addTaskDescription(taskDescription);
    }

    public List<TaskDescription> getAllTaskDescriptions() {

        List<TaskDescription> taskDescriptions = new ArrayList<TaskDescription>();

        Iterator<TaskDescription> iterator = functionSchedulingManager.getAllTaskDescriptions(getConfigContext());
        while (iterator.hasNext()) {
            TaskDescription taskDescription = iterator.next();
            if (taskDescription != null) {
                taskDescriptions.add(taskDescription);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("All available Task based Scheduled Functions " + taskDescriptions);
        }
        return taskDescriptions;
    }

    public TaskDescription getTaskDescription(String taskName) {
        validateName(taskName);
        return functionSchedulingManager.getTaskDescription(taskName, getConfigContext());
    }

    public boolean isContains(String taskName) {
        validateName(taskName);
        return functionSchedulingManager.isContains(taskName, getConfigContext());
    }

    public List<String> getPropertyNames(String taskClass) {

        List<String> names = new ArrayList<String>();
        try {
            Class clazz = Class.forName(taskClass.trim());
            Method[] methods = clazz.getMethods();
            Field[] fields = clazz.getDeclaredFields();
            for (Method method : methods) {
                if (method == null) {
                    continue;
                }
                String methodName = method.getName();
                if (methodName == null) {
                    continue;
                }

                if (methodName.startsWith("set") && !"setTraceState".equals(methodName)) {
                    for (Field field : fields) {

                        if (field.getName().equalsIgnoreCase(methodName.substring(3))) {
                            names.add(field.getName());
                            break;
                        }
                    }
                }

            }
        } catch (ClassNotFoundException e) {
            handleException("Class " + taskClass + " not found in the path", e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Task class '" + taskClass + "' contains property Names : " + names);
        }
        return names;
    }

    private static void validateTaskDescription(TaskDescription description) {
        if (description == null) {
            handleException("Task Description can not be found.");
        }
    }

    private static void validateTaskElement(OMElement taskElement) {
        if (taskElement == null) {
            handleException("Task Description OMElement can not be found.");
        }
    }

    private static void validateName(String name) {
        if (name == null || "".equals(name)) {
            handleException("Name is null or empty");
        }
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new IllegalArgumentException(msg);
    }

    private static void handleException(String msg, Throwable throwable) {
        log.error(msg, throwable);
        throw new RuntimeException(msg);
    }

}