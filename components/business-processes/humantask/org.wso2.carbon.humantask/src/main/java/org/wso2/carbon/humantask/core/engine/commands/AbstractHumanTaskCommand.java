/*
 * Copyright (c) 2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.humantask.core.engine.commands;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.OrganizationalEntityDAO;
import org.wso2.carbon.humantask.core.dao.TaskDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.dao.TaskType;
import org.wso2.carbon.humantask.core.engine.HumanTaskCommand;
import org.wso2.carbon.humantask.core.engine.HumanTaskEngine;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.engine.util.OperationAuthorizationUtil;
import org.wso2.carbon.humantask.core.internal.HumanTaskServiceComponent;

import java.util.List;

/**
 * Abstract class for HumanTaskCommand. Contains the common operations and properties
 */
public abstract class AbstractHumanTaskCommand implements HumanTaskCommand {

    private static final Log log = LogFactory.getLog(Claim.class);

    /**
     * The human task engine
     */
    protected HumanTaskEngine engine;

    /**
     * The task related to this operation
     */
    protected TaskDAO task;

    /**
     * The caller of the operation.
     */
    protected OrganizationalEntityDAO caller;

    /**
     * @param callerId : The caller user id.
     * @param taskId   : The task id.
     */
    public AbstractHumanTaskCommand(String callerId, Long taskId) {
        engine = HumanTaskServiceComponent.getHumanTaskServer().getTaskEngine();
        validateCaller(callerId);
        this.caller = engine.getDaoConnectionFactory().getConnection().
                createNewOrgEntityObject(callerId, OrganizationalEntityDAO.OrganizationalEntityType.USER);
        this.task = engine.getDaoConnectionFactory().getConnection().getTask(taskId);
    }

    //checks the method caller is an actual user existing in the user store.
    private void validateCaller(String callerId) {
        if (StringUtils.isEmpty(callerId) || !engine.getPeopleQueryEvaluator().isExistingUser(callerId)) {
            String errMsg = String.format("The caller[name:%s] is not a valid user in the user store.",
                                          caller.getName());
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }
    }

    /**
     * Checks whether the this task is a valid task of TASK.
     *
     * @param operation : The operation against which the task is being validated.
     */
    public void checkForValidTask(Class operation) {
        if (task == null) {
            throw new HumanTaskRuntimeException("The task is not loaded properly");
        }

        if (!TaskType.TASK.equals(task.getType())) {
            String errMsg = String.format("The task[%d] is a notification, hence cannot perform [%s].",
                                          task.getId(), operation.getSimpleName());
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }
    }

    /**
     * Checks whether the this task is a valid task of TASK.
     *
     * @param operation : The operation against which the task is being validated.
     */
    public void checkForValidNotification(Class operation) {
        if (task == null) {
            throw new HumanTaskRuntimeException("The task is not loaded properly");
        }

        if (!TaskType.NOTIFICATION.equals(task.getType())) {

            String errMsg = String.format("The task[%d] is a task, hence cannot perform [%s].",
                                          task.getId(), operation.getSimpleName());
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }
    }

    /**
     * Checks the post state of a task after a command execution
     *
     * @param expectedStates : The expected post states.
     * @param operation      : The command class
     */
    protected void checkPostStates(List<TaskStatus> expectedStates, Class operation) {
        if (!expectedStates.contains(task.getStatus())) {
            String errMsg = String.format("Operation [%s] was not successfully performed on task[id: %d]" +
                                          " as it's state is still in[%s]", operation.getSimpleName(),
                                          task.getId(), task.getStatus());
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }
    }

    /**
     * Checks the post state of a task after a command execution
     *
     * @param expectedStatus : The expected post state.
     * @param operation      : The command class
     */
    protected void checkPostState(TaskStatus expectedStatus, Class operation) {
        if (!expectedStatus.equals(task.getStatus())) {
            String errMsg = String.format("Operation [%s] was not successfully performed on task[id: %d]" +
                                          " as it's state is still in[%s]", operation.getSimpleName(),
                                          task.getId(), task.getStatus());
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }
    }

    /**
     * A common method shared across all the commands to check the expected state of the
     * task before the task operation is performed.
     *
     * @param expectedStatus : The expected pre state.
     * @param operation      : The command class.
     */
    protected void checkPreState(TaskStatus expectedStatus, Class operation) {
        if (!expectedStatus.equals(task.getStatus())) {
            String errMsg = String.format("User[%s] cannot [%s] task[id:%d] as the task is in state[%s]. " +
                                          "[%s] operation can be performed on tasks in state[%s]!",
                                          caller.getName(), operation.getSimpleName(), task.getId(),
                                          task.getStatus(), operation.getSimpleName(),
                                          expectedStatus);
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }
    }

    /**
     * A common method shared across all the commands to check the expected state of the
     * task before the task operation is performed.
     *
     * @param expectedStates : The expected pre states.
     * @param operation      : The command class.
     */
    protected void checkPreStates(List<TaskStatus> expectedStates, Class operation) {
        if (!expectedStates.contains(task.getStatus())) {
            String errMsg = String.format("User[%s] cannot [%s] task[id:%d] as the task is in state[%s]. " +
                                          "[%s] operation can be performed on tasks in states[%s]!",
                                          caller.getName(), operation.getSimpleName(), task.getId(),
                                          task.getStatus(), operation.getSimpleName(),
                                          expectedStates);
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }
    }

    protected void authoriseRoles(List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles,
                                  Class operation) {
        if (!OperationAuthorizationUtil.authoriseUser(this.task, caller, allowedRoles,
                                                      engine.getPeopleQueryEvaluator())) {
            throw new HumanTaskRuntimeException(String.format("The user[%s] cannot perform [%s]" +
                                                              " operation as he is not in task roles[%s]",
                                                              caller.getName(), operation.getSimpleName(),
                                                              allowedRoles));
        }
    }

    protected void reloadTask() {
        this.task = engine.getDaoConnectionFactory().getConnection().getTask(task.getId());
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    protected abstract void checkPreConditions();

    /**
     * Perform the authorization checks before executing the task operation.
     */
    protected abstract void authorise();

    /**
     * Perform the state checks before executing the task operation.
     */
    protected abstract void checkState();

    /**
     * Checks the post-conditions after executing the task operation.
     */
    protected abstract void checkPostConditions();

}
