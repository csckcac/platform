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
 * The release operation logic.
 */
public class Release extends AbstractHumanTaskCommand {

    private static final Log log = LogFactory.getLog(Release.class);

    public Release(String callerId, Long taskId) {
        super(callerId, taskId);
    }

    /**
     * Checks the Pre-conditions before executing the task operation.
     */
    @Override
    protected void checkPreConditions() {
        checkForValidTask(Release.class);
        //if the task is in progress status we need to stop it first before releasing it!
        if (TaskStatus.IN_PROGRESS.equals(task.getStatus())) {

            List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList
                    <GenericHumanRoleDAO.GenericHumanRoleType>();
            allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);

            if (!OperationAuthorizationUtil.authoriseUser(this.task, caller, allowedRoles,
                                                          engine.getPeopleQueryEvaluator())) {
                throw new HumanTaskRuntimeException(String.format("The user[%s] cannot perform [%s]" +
                                                                  " operation as he is not in task roles[%s]",
                                                                  caller.getName(), Release.class, allowedRoles));
            }

            task.stop();
            reloadTask();
        }
    }

    /**
     * Perform the authorization checks before executing the task operation.
     */
    @Override
    protected void authorise() {
        List<GenericHumanRoleDAO.GenericHumanRoleType> allowedRoles = new ArrayList
                <GenericHumanRoleDAO.GenericHumanRoleType>();
        allowedRoles.add(GenericHumanRoleDAO.GenericHumanRoleType.ACTUAL_OWNER);

        if (!OperationAuthorizationUtil.authoriseUser(this.task, caller, allowedRoles,
                                                      engine.getPeopleQueryEvaluator())) {
            throw new HumanTaskRuntimeException(String.format("The user[%s] cannot perform [%s]" +
                                                              " operation as he is not in task roles[%s]",
                                                              caller.getName(), Release.class, allowedRoles));
        }
    }

    /**
     * Perform the state checks before executing the task operation.
     */
    @Override
    protected void checkState() {
        checkPreState(TaskStatus.RESERVED, Release.class);
    }

    /**
     * Checks the post-conditions after executing the task operation.
     */
    @Override
    protected void checkPostConditions() {
        checkPostState(TaskStatus.READY, Release.class);
    }

    @Override
    public void execute() {
        checkPreConditions();
        authorise();
        checkState();
        task.release();
        checkPostConditions();
    }
}
