/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.humantask.core.dao.GenericHumanRoleDAO;
import org.wso2.carbon.humantask.core.dao.TaskStatus;
import org.wso2.carbon.humantask.core.engine.runtime.api.HumanTaskRuntimeException;
import org.wso2.carbon.humantask.core.engine.util.OperationAuthorizationUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * The start command
 */
public class Start extends AbstractHumanTaskCommand {

    private static final Log log = LogFactory.getLog(Start.class);

    public Start(String callerId, Long taskId) {
        super(callerId, taskId);
    }

    /**
     * If the task is in the READY state, we need to perform a claim operation before the task is started.
     */
    @Override
    protected void checkPreConditions() {

        checkForValidTask(this.getClass());

        if (TaskStatus.READY.equals(task.getStatus())) {
            List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList
                    <GenericHumanRoleDAO.GenericHumanRoleType>();
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.POTENTIAL_OWNERS);

            if (!OperationAuthorizationUtil.authoriseUser(this.task, caller, allowedRoles,
                                                          engine.getPeopleQueryEvaluator())) {
                throw new HumanTaskRuntimeException(String.format("The user[%s] cannot perform [%s]" +
                                                                  " operation as he is not in task roles[%s]",
                                                                  caller.getName(), Claim.class, allowedRoles));
            }

            task.claim(caller);
            reloadTask();
        }
    }

    /**
     * Perform the authorization checks before executing the task operation.
     */
    @Override
    protected void authorise() {

        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles =
                new ArrayList<GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);

        if (!OperationAuthorizationUtil.authoriseUser(this.task, caller, allowedRoles,
                                                      engine.getPeopleQueryEvaluator())) {
            String errMsg = String.format("The user[%s] cannot perform [%s] operation as he is not in " +
                                          "task roles[%s]", caller.getName(), Start.class, allowedRoles);
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }
    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {
        if (!TaskStatus.RESERVED.equals(task.getStatus())) {
            String errMsg = String.format("User[%s] cannot perform [%s] operation on task[%d] as the task is in state[%s]. " +
                                          "[%s] operation can be performed only on tasks in [%s] state",
                                          caller.getName(), Start.class, task.getId(),
                                          task.getStatus(), Start.class, TaskStatus.RESERVED);
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }
    }

    /**
     * Checks the post-conditions after executing the task operation.
     */
    @Override
    protected void checkPostConditions() {
        if (!TaskStatus.IN_PROGRESS.equals(task.getStatus())) {
            String errMsg = String.format("The task[id:%d] did not start successfully as " +
                                          "it's state is still in [%s]", task.getId(), task.getStatus());
            log.error(errMsg);
            throw new HumanTaskRuntimeException(errMsg);
        }
    }

    @Override
    public void execute() {
        checkPreConditions();
        authorise();
        checkState();
        task.start();
        checkPostConditions();
    }
}
